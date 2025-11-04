package de.delinkedde.redstoneItemClear.model;

/**
 * Typen von erkannten Performance-Problemen
 */
public enum ProblemType {
    TOO_MANY_ENTITIES("Zu viele Entities"),
    TOO_MANY_MOBS("Zu viele Mobs"),
    TOO_MANY_ITEMS("Zu viele Items auf dem Boden"),
    EXCESSIVE_REDSTONE("Übermäßige Redstone-Aktivität");

    private final String displayName;

    ProblemType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
