package ac.grim.grimac.checks.impl.packet.badpackets;

import ac.grim.grimac.checks.Check;
import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.type.PacketCheck;
import ac.grim.grimac.mitigation.TrustFactorCheckType;
import ac.grim.grimac.player.GrimPlayer;

@CheckData(name = "BadPackets (N)", configName = "BadPacketsN", checkTypeTrustFactor = TrustFactorCheckType.PACKET, changeTrustFactor = 3)
public class BadPacketsN extends Check implements PacketCheck {
    public BadPacketsN(final GrimPlayer player) {
        super(player);
    }
}
