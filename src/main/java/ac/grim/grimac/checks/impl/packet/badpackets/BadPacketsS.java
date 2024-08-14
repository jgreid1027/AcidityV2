package ac.grim.grimac.checks.impl.packet.badpackets;

import ac.grim.grimac.checks.Check;
import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.type.PacketCheck;
import ac.grim.grimac.mitigation.TrustFactorCheckType;
import ac.grim.grimac.player.GrimPlayer;

@CheckData(name = "BadPackets (S)", configName = "BadPacketsS", checkTypeTrustFactor = TrustFactorCheckType.PACKET, changeTrustFactor = 3)
public class BadPacketsS extends Check implements PacketCheck {
    public BadPacketsS(GrimPlayer player) {
        super(player);
    }

}
