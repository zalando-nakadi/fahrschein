package org.zalando.fahrschein.domain;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.hamcrest.core.IsNot.not;

public class CursorTest {

    @Test
    public void testEquality() {
        final Cursor cursor1 = new Cursor("partition1", "101");
        final Cursor cursor2 = new Cursor("partition1", "101");
        assertThat(cursor1, equalTo(cursor2));
    }

    @Test
    public void testInequality() {
        final Cursor cursor1 = new Cursor("partition1", "101");
        final Cursor cursor2 = new Cursor("partition1", "102");
        assertThat(cursor1, not(equalTo(cursor2)));
    }
}
