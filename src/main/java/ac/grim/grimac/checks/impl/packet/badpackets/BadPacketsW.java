package ac.grim.grimac.checks.impl.packet.badpackets;

import ac.grim.grimac.checks.Check;
import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.type.PacketCheck;
import ac.grim.grimac.mitigation.TrustFactorCheckType;
import ac.grim.grimac.player.GrimPlayer;

@CheckData(name = "BadPackets (W)", configName = "BadPacketsW", checkTypeTrustFactor = TrustFactorCheckType.PACKET, changeTrustFactor = 3)
public class BadPacketsW extends Check implements PacketCheck {
    public BadPacketsW(GrimPlayer player) {
        super(player);
    }
}
