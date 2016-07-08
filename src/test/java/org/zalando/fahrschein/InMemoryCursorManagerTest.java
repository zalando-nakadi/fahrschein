package org.zalando.fahrschein;

import org.zalando.fahrschein.CursorManager;
import org.zalando.fahrschein.InMemoryCursorManager;
import org.zalando.fahrschein.jdbc.AbstractCursorManagerTest;

public class InMemoryCursorManagerTest extends AbstractCursorManagerTest {
    private CursorManager cursorManager = new InMemoryCursorManager();

    @Override
    protected CursorManager cursorManager() {
        return cursorManager;
    }
}
