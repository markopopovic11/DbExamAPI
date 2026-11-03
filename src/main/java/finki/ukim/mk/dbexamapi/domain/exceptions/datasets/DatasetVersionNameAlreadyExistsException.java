package finki.ukim.mk.dbexamapi.domain.exceptions.datasets;

import finki.ukim.mk.dbexamapi.domain.exceptions.AlreadyExistsException;

public class DatasetVersionNameAlreadyExistsException extends AlreadyExistsException {

    public DatasetVersionNameAlreadyExistsException(String datasetId, String name) {
        super(String.format("Dataset version named %s in dataset with id %s already exists.", name, datasetId));
    }
}
