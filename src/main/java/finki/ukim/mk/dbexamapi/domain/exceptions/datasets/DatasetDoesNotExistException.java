package finki.ukim.mk.dbexamapi.domain.exceptions.datasets;

import finki.ukim.mk.dbexamapi.domain.exceptions.NotFoundException;

public class DatasetDoesNotExistException extends NotFoundException {

    public DatasetDoesNotExistException(String id) {
        super(String.format("Dataset with id %s does not exist.", id));
    }
}
