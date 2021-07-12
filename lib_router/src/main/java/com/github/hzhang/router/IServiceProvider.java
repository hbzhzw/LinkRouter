package com.github.hzhang.router;

import androidx.annotation.NonNull;

public interface IServiceProvider<T> {
    @NonNull
    T build();
}
