package de.delinkedde.redstoneItemClear.model;

/**
 * Stufen der automatischen Maßnahmen basierend auf TPS
 */
public enum ActionLevel {
    NORMAL("Normal", 20.0),
    WARNING("Warnung", 18.0),
    MODERATE("Mäßig", 15.0),
    SEVERE("Schwer", 12.0),
    EMERGENCY("Notfall", 10.0);

    private final String displayName;
    private final double defaultThreshold;

    ActionLevel(String displayName, double defaultThreshold) {
        this.displayName = displayName;
        this.defaultThreshold = defaultThreshold;
    }

    public String getDisplayName() {
        return displayName;
    }

    public double getDefaultThreshold() {
        return defaultThreshold;
    }

    public static ActionLevel fromTPS(double tps, double warningThreshold, double criticalThreshold,
                                       double severeThreshold, double emergencyThreshold) {
        if (tps <= emergencyThreshold) return EMERGENCY;
        if (tps <= severeThreshold) return SEVERE;
        if (tps <= criticalThreshold) return MODERATE;
        if (tps <= warningThreshold) return WARNING;
        return NORMAL;
    }
}
