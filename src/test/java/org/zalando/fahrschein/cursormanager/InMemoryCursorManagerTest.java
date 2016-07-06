package org.zalando.fahrschein.cursormanager;

public class InMemoryCursorManagerTest extends AbstractCursorManagerTest {
    private CursorManager cursorManager = new InMemoryCursorManager();

    @Override
    protected CursorManager cursorManager() {
        return cursorManager;
    }
}
