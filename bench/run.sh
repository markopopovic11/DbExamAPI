#!/bin/bash
# PROTOTYPE (wayfinder #24) — provisioning throughput benchmark. THROWAWAY.
# Usage: ./run.sh <phase>   phases: setup template baseline clones storage load schema ramp teardown
set -euo pipefail

BENCH="$(cd "$(dirname "$0")" && pwd)"
RES="$BENCH/results"
C=dbexam-bench-prototype-wipeme
PSQL="docker exec -i $C psql -U postgres -X -q -v ON_ERROR_STOP=1"
mkdir -p "$RES"

now_ms() { perl -MTime::HiRes=time -e 'printf "%.0f\n", time()*1000'; }

# awk: pair "MARK <id>" echoes with the Time: lines that follow; one CSV row per mark,
# columns = id then each statement's ms.
parse_marks() {
  awk '/^MARK /{if(id!="")print row; id=$2; row=id}
       /^Time:/{row=row","$2}
       END{if(id!="")print row}'
}

case "${1:-}" in

setup)
  docker rm -f $C 2>/dev/null || true
  docker run -d --name $C --cpus=4 --memory=8g --shm-size=1g \
    -v "$BENCH":/bench \
    -e POSTGRES_PASSWORD=bench postgres:17 \
    -c max_connections=800 -c shared_buffers=1536MB -c max_wal_size=4GB \
    -c effective_cache_size=4GB
  until docker exec $C pg_isready -U postgres -q; do sleep 1; done
  sleep 2
  $PSQL -c "SELECT version()" -c "SHOW max_connections" -c "SHOW shared_buffers"
  docker exec $C ls /bench/ddl.sql   # verify the mount works
  ;;

template)
  t0=$(now_ms)
  $PSQL -d postgres -c "CREATE DATABASE exam_template"
  $PSQL -d exam_template -f /bench/ddl.sql -f /bench/seed.sql -f /bench/triggers.sql
  t1=$(now_ms)
  echo "template_build_ms,$((t1-t0))" | tee "$RES/template.csv"
  $PSQL -d exam_template -c "\timing on" \
        -c "SELECT pg_size_pretty(pg_database_size('exam_template')), pg_database_size('exam_template')" \
        | tee -a "$RES/template.csv"
  $PSQL -d postgres -c "ALTER DATABASE exam_template IS_TEMPLATE true" \
        -c "ALTER DATABASE exam_template ALLOW_CONNECTIONS false"
  # empty template for the catalog-baseline measurement
  $PSQL -d postgres -c "CREATE DATABASE empty_tpl TEMPLATE template0" \
        -c "ALTER DATABASE empty_tpl IS_TEMPLATE true" \
        -c "ALTER DATABASE empty_tpl ALLOW_CONNECTIONS false"
  ;;

baseline)
  { echo '\timing on'
    for i in 1 2 3 4 5; do
      echo "\\echo MARK $i"
      echo "CREATE DATABASE empty_clone_$i TEMPLATE empty_tpl STRATEGY WAL_LOG;"
    done
  } > "$BENCH/gen_baseline.sql"
  $PSQL -d postgres -f /bench/gen_baseline.sql 2>&1 | parse_marks > "$RES/baseline_empty_clone.csv"
  $PSQL -d postgres -t -c "SELECT 'empty_clone_size_bytes', pg_database_size('empty_clone_1')" >> "$RES/baseline_empty_clone.csv"
  cat "$RES/baseline_empty_clone.csv"
  ;;

clones)
  # 300 provisioning triples (role + WAL_LOG clone + revoke), timed per statement.
  { echo '\timing on'
    for i in $(seq -w 1 300); do
      echo "\\echo MARK $i"
      echo "CREATE ROLE s_$i LOGIN PASSWORD 'pw_$i';"
      echo "CREATE DATABASE db_s_$i TEMPLATE exam_template OWNER s_$i STRATEGY WAL_LOG;"
      echo "REVOKE CONNECT ON DATABASE db_s_$i FROM PUBLIC;"
      echo "GRANT CONNECT ON DATABASE db_s_$i TO s_$i;"
    done
  } > "$BENCH/gen_clones.sql"
  echo "id,role_ms,create_ms,revoke_ms,grant_ms" > "$RES/clone_series.csv"
  $PSQL -d postgres -f /bench/gen_clones.sql 2>&1 | parse_marks >> "$RES/clone_series.csv"
  wc -l "$RES/clone_series.csv"

  # FILE_COPY samples at cluster size ~300 (start-of-cluster samples ran as fc_pre)
  { echo '\timing on'
    for i in 1 2 3 4 5; do
      echo "\\echo MARK $i"
      echo "CREATE DATABASE fc_post_$i TEMPLATE exam_template STRATEGY FILE_COPY;"
    done
  } > "$BENCH/gen_fc.sql"
  $PSQL -d postgres -f /bench/gen_fc.sql 2>&1 | parse_marks > "$RES/file_copy_at_300.csv"
  cat "$RES/file_copy_at_300.csv"
  ;;

fc_pre)
  # FILE_COPY samples at cluster size ~0 (run right after template phase)
  { echo '\timing on'
    for i in 1 2 3 4 5; do
      echo "\\echo MARK $i"
      echo "CREATE DATABASE fc_pre_$i TEMPLATE exam_template STRATEGY FILE_COPY;"
      echo "DROP DATABASE fc_pre_$i;"
    done
  } > "$BENCH/gen_fcpre.sql"
  $PSQL -d postgres -f /bench/gen_fcpre.sql 2>&1 | parse_marks > "$RES/file_copy_at_0.csv"
  cat "$RES/file_copy_at_0.csv"
  ;;

storage)
  $PSQL -d postgres -t -A -F, -c "
    SELECT 'db_count', count(*) FROM pg_database;
    SELECT 'template_bytes', pg_database_size('exam_template');
    SELECT 'avg_clone_bytes', avg(pg_database_size(datname))::bigint
      FROM pg_database WHERE datname LIKE 'db\\_s\\_%';
    SELECT 'sum_clone_bytes', sum(pg_database_size(datname))::bigint
      FROM pg_database WHERE datname LIKE 'db\\_s\\_%';
    SELECT 'shared_catalog_pg_database_bytes', pg_total_relation_size('pg_database');
    SELECT 'shared_catalog_pg_authid_bytes', pg_total_relation_size('pg_authid');
  " > "$RES/storage.csv"
  docker exec $C du -s --block-size=1M /var/lib/postgresql/data | \
    awk '{print "pgdata_total_MiB," $1}' >> "$RES/storage.csv"
  docker exec $C du -s --block-size=1M /var/lib/postgresql/data/pg_wal | \
    awk '{print "pg_wal_MiB," $1}' >> "$RES/storage.csv"
  cat "$RES/storage.csv"
  ;;

load)
  # 300 live student connections (6 pgbench instances x 50 clients on 6 clone DBs),
  # then measure refill + drop while they run.
  for n in 1 2 3 4 5 6; do
    docker exec $C pgbench -U postgres -n -c 50 -j 2 -T 420 -P 60 \
      -f /bench/student_load.sql "db_s_00$n" > "$RES/pgbench_load_$n.log" 2>&1 &
  done
  sleep 20   # let all 300 connections establish
  $PSQL -d postgres -t -c "SELECT 'active_backends', count(*) FROM pg_stat_activity" | tee "$RES/load_env.txt"

  # serial refill: 25 provisioning triples under load
  { echo '\timing on'
    for i in $(seq -w 401 425); do
      echo "\\echo MARK $i"
      echo "CREATE ROLE s_$i LOGIN PASSWORD 'pw_$i';"
      echo "CREATE DATABASE db_s_$i TEMPLATE exam_template OWNER s_$i STRATEGY WAL_LOG;"
      echo "REVOKE CONNECT ON DATABASE db_s_$i FROM PUBLIC;"
      echo "GRANT CONNECT ON DATABASE db_s_$i TO s_$i;"
    done
  } > "$BENCH/gen_refill_serial.sql"
  t0=$(now_ms)
  echo "id,role_ms,create_ms,revoke_ms,grant_ms" > "$RES/refill_serial_underload.csv"
  $PSQL -d postgres -f /bench/gen_refill_serial.sql 2>&1 | parse_marks >> "$RES/refill_serial_underload.csv"
  t1=$(now_ms)
  echo "serial_25_wall_ms,$((t1-t0))" | tee -a "$RES/load_env.txt"

  # parallel refill: 4 workers x 10 clones under load
  for w in 1 2 3 4; do
    { echo '\timing on'
      for i in $(seq -w $((430 + w*10)) $((439 + w*10))); do
        echo "\\echo MARK $i"
        echo "CREATE ROLE s_$i LOGIN PASSWORD 'pw_$i';"
        echo "CREATE DATABASE db_s_$i TEMPLATE exam_template OWNER s_$i STRATEGY WAL_LOG;"
        echo "REVOKE CONNECT ON DATABASE db_s_$i FROM PUBLIC;"
        echo "GRANT CONNECT ON DATABASE db_s_$i TO s_$i;"
      done
    } > "$BENCH/gen_refill_par_$w.sql"
  done
  t0=$(now_ms)
  WPIDS=""
  for w in 1 2 3 4; do
    $PSQL -d postgres -f /bench/gen_refill_par_$w.sql 2>&1 | parse_marks > "$RES/refill_par_w$w.csv" &
    WPIDS="$WPIDS $!"
  done
  wait $WPIDS   # NOT bare `wait` — that would also block on the pgbench jobs
  t1=$(now_ms)
  echo "parallel_40_wall_ms,$((t1-t0))" | tee -a "$RES/load_env.txt"

  # drop 20 clones under load (pool-retire path)
  { echo '\timing on'
    for i in $(seq -w 101 120); do
      echo "\\echo MARK $i"
      echo "DROP DATABASE db_s_$i WITH (FORCE);"
      echo "DROP ROLE s_$i;"
    done
  } > "$BENCH/gen_drop.sql"
  echo "id,drop_ms,droprole_ms" > "$RES/drop_underload.csv"
  $PSQL -d postgres -f /bench/gen_drop.sql 2>&1 | parse_marks >> "$RES/drop_underload.csv"

  # autovacuum workers snapshot while cluster has ~350 DBs
  for s in 1 2 3; do
    $PSQL -d postgres -t -c "SELECT 'autovacuum_workers', count(*) FROM pg_stat_activity WHERE backend_type = 'autovacuum worker'" >> "$RES/load_env.txt"
    sleep 10
  done
  wait   # let pgbench instances finish their 420 s
  grep -hE "latency average|tps =|initial connection" "$RES"/pgbench_load_*.log | tee -a "$RES/load_env.txt"
  ;;

schema)
  # cleanup: drop clone DBs (timed, no load) except db_s_0001..0006, to free disk
  { echo '\timing on'
    for i in $(seq -w 7 100) $(seq -w 121 300) $(seq -w 401 425) $(seq -w 440 479); do
      echo "\\echo MARK $i"
      echo "DROP DATABASE IF EXISTS db_s_$i WITH (FORCE);"
    done
    for i in 1 2 3 4 5; do echo "DROP DATABASE IF EXISTS fc_post_$i;"; done
  } > "$BENCH/gen_cleanup.sql"
  echo "id,drop_ms" > "$RES/drop_noload.csv"
  $PSQL -d postgres -f /bench/gen_cleanup.sql 2>&1 | parse_marks >> "$RES/drop_noload.csv"

  # schema-per-student model: one DB, seed schema built once, then 300 student schemas
  $PSQL -d postgres -c "CREATE DATABASE schema_model"
  $PSQL -d schema_model -c "CREATE SCHEMA seed" \
        -c "SET search_path = seed" \
        -f /bench/ddl.sql -f /bench/seed.sql
  { echo '\timing on'
    for i in $(seq -w 1 300); do
      echo "\\echo MARK $i"
      echo "CREATE ROLE ss_$i LOGIN PASSWORD 'pw_$i';"
      echo "CREATE SCHEMA sch_$i AUTHORIZATION ss_$i;"
      echo "SET search_path = sch_$i;"
      echo "\\i /bench/ddl.sql"
      echo "\\i /bench/copydata.sql"
      echo "\\i /bench/triggers.sql"
    done
  } > "$BENCH/gen_schemas.sql"
  $PSQL -d schema_model -f /bench/gen_schemas.sql 2>&1 | parse_marks > "$RES/schema_series_raw.csv"
  # total ms per student = sum of all statement times for its mark
  awk -F, '{s=0; for(f=2;f<=NF;f++)s+=$f; print $1 "," s}' "$RES/schema_series_raw.csv" > "$RES/schema_series.csv"
  wc -l "$RES/schema_series.csv"

  ;;

schema_reset)
  # schema reset: drop cascade + rebuild, x10
  { echo '\timing on'
    for i in $(seq -f '%03g' 1 10); do
      echo "\\echo MARK $i"
      echo "DROP SCHEMA sch_$i CASCADE;"
      echo "CREATE SCHEMA sch_$i AUTHORIZATION ss_$i;"
      echo "SET search_path = sch_$i;"
      echo "\\i /bench/ddl.sql"
      echo "\\i /bench/copydata.sql"
      echo "\\i /bench/triggers.sql"
    done
  } > "$BENCH/gen_schema_reset.sql"
  $PSQL -d schema_model -f /bench/gen_schema_reset.sql 2>&1 | parse_marks > "$RES/schema_reset_raw.csv"
  awk -F, '{s=0; for(f=2;f<=NF;f++)s+=$f; print $1 "," s}' "$RES/schema_reset_raw.csv" > "$RES/schema_reset.csv"

  # catalog weight of 300 schemas in one DB vs one clone DB's catalogs
  $PSQL -d schema_model -t -A -F, -c "
    SELECT 'schema_model_db_bytes', pg_database_size('schema_model');
    SELECT 'schema_model_pg_class_rows', count(*) FROM pg_class;
    SELECT 'schema_model_catalog_bytes', sum(pg_total_relation_size(c.oid))::bigint
      FROM pg_class c JOIN pg_namespace n ON n.oid = c.relnamespace
      WHERE n.nspname = 'pg_catalog' AND c.relkind = 'r';
  " > "$RES/schema_catalog.csv"
  $PSQL -d db_s_001 -t -A -F, -c "
    SELECT 'clone_db_pg_class_rows', count(*) FROM pg_class;
    SELECT 'clone_db_catalog_bytes', sum(pg_total_relation_size(c.oid))::bigint
      FROM pg_class c JOIN pg_namespace n ON n.oid = c.relnamespace
      WHERE n.nspname = 'pg_catalog' AND c.relkind = 'r';
  " >> "$RES/schema_catalog.csv"
  cat "$RES/schema_catalog.csv"
  ;;

drop_load)
  # supplemental: create 20 throwaway clones, then drop them WITH (FORCE)
  # while 300 mostly-idle connections are established.
  for n in 1 2 3 4 5 6; do
    docker exec $C pgbench -U postgres -n -c 50 -j 2 -T 150 \
      -f /bench/idle_load.sql "db_s_00$n" > "$RES/pgbench_droploal_$n.log" 2>&1 &
  done
  sleep 20
  $PSQL -d postgres -t -c "SELECT 'active_backends', count(*) FROM pg_stat_activity" | tee "$RES/drop_load_env.txt"
  { echo '\timing on'
    for i in $(seq -w 1 20); do
      echo "CREATE DATABASE tmp_drop_$i TEMPLATE exam_template STRATEGY WAL_LOG;"
    done
    for i in $(seq -w 1 20); do
      echo "\\echo MARK $i"
      echo "DROP DATABASE tmp_drop_$i WITH (FORCE);"
    done
  } > "$BENCH/gen_drop_load.sql"
  echo "id,drop_ms" > "$RES/drop_underload2.csv"
  $PSQL -d postgres -f /bench/gen_drop_load.sql 2>&1 | parse_marks >> "$RES/drop_underload2.csv"
  cat "$RES/drop_underload2.csv"
  wait
  ;;

ramp)
  # connection scaling: mostly-idle sessions at 100/300/500/700, memory + latency,
  # plus 3 provisioning triples measured during the 500-conn stage.
  echo "conns,init_conn_ms,tps,lat_avg_ms,container_mem" > "$RES/ramp.csv"
  for n in 100 300 500 700; do
    docker exec $C pgbench -U postgres -n -c $n -j 4 -T 45 \
      -f /bench/idle_load.sql db_s_001 > "$RES/pgbench_ramp_$n.log" 2>&1 &
    RAMP_PID=$!
    sleep 25
    MEM=$(docker stats --no-stream --format '{{.MemUsage}}' $C | awk '{print $1}')
    if [ "$n" = "500" ]; then
      { echo '\timing on'
        for i in 1 2 3; do
          echo "\\echo MARK $i"
          echo "CREATE DATABASE ramp_clone_$i TEMPLATE exam_template STRATEGY WAL_LOG;"
          echo "DROP DATABASE ramp_clone_$i;"
        done
      } > "$BENCH/gen_ramp_clone.sql"
      $PSQL -d postgres -f /bench/gen_ramp_clone.sql 2>&1 | parse_marks > "$RES/clone_at_500conns.csv"
    fi
    wait $RAMP_PID
    ICT=$(grep 'initial connection time' "$RES/pgbench_ramp_$n.log" | grep -o '[0-9.]*' | head -1)
    TPS=$(grep 'tps =' "$RES/pgbench_ramp_$n.log" | grep -o 'tps = [0-9.]*' | grep -o '[0-9.]*')
    LAT=$(grep 'latency average' "$RES/pgbench_ramp_$n.log" | grep -o '[0-9.]*' | head -1)
    echo "$n,$ICT,$TPS,$LAT,$MEM" >> "$RES/ramp.csv"
  done
  cat "$RES/ramp.csv"
  ;;

teardown)
  docker rm -f $C
  ;;

*) echo "unknown phase: ${1:-<none>}"; exit 1 ;;
esac
