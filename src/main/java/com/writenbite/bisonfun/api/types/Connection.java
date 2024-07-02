package com.writenbite.bisonfun.api.types;

import java.util.List;

public interface Connection<T> {
    List<T> nodes();
    PageInfo pageInfo();
}
