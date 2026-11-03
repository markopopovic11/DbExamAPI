package finki.ukim.mk.dbexamapi.domain.dtos.datasets;

import finki.ukim.mk.dbexamapi.domain.models.content.Document;
import finki.ukim.mk.dbexamapi.domain.models.datasets.Dataset;

public record DatasetVersionDto(
        Dataset dataset,
        String name,
        boolean visible,
        Document document,
        int sortOrder
) {
}
