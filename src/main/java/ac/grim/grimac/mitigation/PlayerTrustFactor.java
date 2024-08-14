package ac.grim.grimac.mitigation;

public enum PlayerTrustFactor {
    GREEN, // Player is most likely legit, hasn't flagged at all, or has flagged little with low severity
    YELLOW, // Player more likely to be legitimate than orange, has flagged a bit with mainly low severity
    ORANGE, // Player might be cheating, has flagged some with low to medium severity
    RED, // Player is likely cheating, has flagged a lot or flagged some with medium to high severity
}
