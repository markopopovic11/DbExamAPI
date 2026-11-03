package finki.ukim.mk.dbexamapi.domain.exceptions.datasets;

import finki.ukim.mk.dbexamapi.domain.exceptions.AlreadyExistsException;

public class DatasetVersionVisibleAlreadyExistsException extends AlreadyExistsException {

    public DatasetVersionVisibleAlreadyExistsException(String datasetId) {
        super(String.format("Dataset with id %s already has a visible version.", datasetId));
    }
}
