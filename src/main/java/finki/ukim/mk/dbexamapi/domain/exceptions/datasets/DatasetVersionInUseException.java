package finki.ukim.mk.dbexamapi.domain.exceptions.datasets;

import finki.ukim.mk.dbexamapi.domain.exceptions.InUseException;

public class DatasetVersionInUseException extends InUseException {

    public DatasetVersionInUseException(String id) {
        super(String.format("Dataset version with id %s cannot be deleted while tasks are tested on it.", id));
    }
}
