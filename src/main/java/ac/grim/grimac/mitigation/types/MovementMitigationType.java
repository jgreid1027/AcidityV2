package ac.grim.grimac.mitigation.types;

public enum MovementMitigationType {
    NONE, // I don't know If I will end up using this to be honest

    TP_FROM, // Similar to NoCheatPlus, just teleport back to previous position
    TP_GRIM, // Grim default setbacks
    TP_ACIDITYONE, // Does Grim setbacks until back on ground
    TP_ACIDITYTWO, // Don't allow changes on the X and Y levels, probably should only be used with RED trust factor
}
