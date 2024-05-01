package org.sean.kim.jag;

import androidx.annotation.NonNull;

public interface JagJob<T> {
    @NonNull T work() throws Exception;
}
