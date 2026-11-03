package finki.ukim.mk.dbexamapi.domain.exceptions.datasets;

import finki.ukim.mk.dbexamapi.domain.exceptions.NotFoundException;

public class DatasetVersionDoesNotExistException extends NotFoundException {

    public DatasetVersionDoesNotExistException(String id) {
        super(String.format("Dataset version with id %s does not exist.", id));
    }
}
