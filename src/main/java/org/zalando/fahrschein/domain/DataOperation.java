package org.zalando.fahrschein.domain;

/**
 * The type of operation executed on the entity.
 * <ul>
 * <li><strong>C</strong>: Creation</li>
 * <li><strong>U</strong>: Update</li>
 * <li><strong>D</strong>: Deletion</li>
 * <li><strong>S</strong>: Snapshot</li>
 * </ul>
 */
enum DataOperation {

    CREATE("C"),
    UPDATE("U"),
    DELETE("D"),
    SNAPSHOT("S");

    private final String operation;

    DataOperation(final String operation) {
        this.operation = operation;
    }

    public String getOperation() {
        return operation;
    }
}
