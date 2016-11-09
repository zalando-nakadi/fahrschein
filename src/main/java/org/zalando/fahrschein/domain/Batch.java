package org.zalando.fahrschein.domain;

import java.util.List;

public final class Batch<T> {
    private final Cursor cursor;
    private final List<T> events;

    public Batch(Cursor cursor, List<T> events) {
        this.cursor = cursor;
        this.events = events;
    }

    public Cursor getCursor() {
        return cursor;
    }

    public List<T> getEvents() {
        return events;
    }
}
