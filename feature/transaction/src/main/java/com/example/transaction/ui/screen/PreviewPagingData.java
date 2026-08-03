package com.example.transaction.ui.screens;

import androidx.paging.PagingData;

import java.util.List;

/**
 * Builds a {@link PagingData} for Compose {@code @Preview} rendering.
 *
 * <p>This is intentionally written in Java. Calling {@code PagingData.Companion.from(list)} from
 * Kotlin compiles to the synthetic {@code from$default(...)} overload, whose exact signature changes
 * between paging-common releases (extra defaulted parameters were added in 3.4+). The Compose preview
 * renderer can load an older paging-common than the one the module is compiled against, so that
 * synthetic is absent at render time and the preview crashes with {@code NoSuchMethodError}. A Java
 * caller binds directly to the stable {@code from(List)} method that exists across paging 3.x, which
 * avoids the version-specific synthetic entirely.
 */
final class PreviewPagingData {

    private PreviewPagingData() {
    }

    static <T> PagingData<T> from(List<? extends T> data) {
        return PagingData.Companion.from(data);
    }
}
