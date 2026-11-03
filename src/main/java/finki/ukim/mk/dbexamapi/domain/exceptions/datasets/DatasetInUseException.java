package finki.ukim.mk.dbexamapi.domain.exceptions.datasets;

import finki.ukim.mk.dbexamapi.domain.exceptions.InUseException;

public class DatasetInUseException extends InUseException {

    public DatasetInUseException(String id) {
        super(String.format("Dataset with id %s cannot be deleted while tasks run on it.", id));
    }
}
