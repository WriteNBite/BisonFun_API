package com.writenbite.bisonfun.api.types;

public record PageInfo(
        Integer total,
        Integer perPage,
        Integer currentPage,
        Integer lastPage,
        Boolean hasNextPage
) {
}
