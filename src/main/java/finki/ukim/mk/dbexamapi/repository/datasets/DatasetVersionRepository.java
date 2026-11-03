package finki.ukim.mk.dbexamapi.repository.datasets;

import finki.ukim.mk.dbexamapi.domain.models.datasets.DatasetVersion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DatasetVersionRepository extends JpaRepository<DatasetVersion, String> {

    boolean existsByDataset_IdAndName(String datasetId, String name);

    boolean existsByDataset_IdAndNameAndIdNot(String datasetId, String name, String id);

    boolean existsByDataset_IdAndVisibleTrue(String datasetId);

    boolean existsByDataset_IdAndVisibleTrueAndIdNot(String datasetId, String id);

    List<DatasetVersion> findAllByDataset_Id(String datasetId);
}
