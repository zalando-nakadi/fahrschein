package org.zalando.fahrschein.inmemory;

import org.zalando.fahrschein.CursorManager;
import org.zalando.fahrschein.test.AbstractCursorManagerTest;

public class InMemoryCursorManagerTest extends AbstractCursorManagerTest {
    private CursorManager cursorManager = new InMemoryCursorManager();

    @Override
    protected CursorManager cursorManager() {
        return cursorManager;
    }
}
