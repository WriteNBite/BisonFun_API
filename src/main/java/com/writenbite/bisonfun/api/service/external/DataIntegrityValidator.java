package com.writenbite.bisonfun.api.service.external;

import org.springframework.lang.Nullable;

public interface DataIntegrityValidator<T, V> {
    boolean dataIntegrityCheck(@Nullable T t, @Nullable V v);
}
