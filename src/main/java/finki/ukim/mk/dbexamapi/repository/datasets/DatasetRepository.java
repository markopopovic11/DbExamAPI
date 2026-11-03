package finki.ukim.mk.dbexamapi.repository.datasets;

import finki.ukim.mk.dbexamapi.domain.models.datasets.Dataset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DatasetRepository extends JpaRepository<Dataset, String> {
}
