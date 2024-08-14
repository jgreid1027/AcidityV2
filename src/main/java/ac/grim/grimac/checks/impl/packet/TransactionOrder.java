package ac.grim.grimac.checks.impl.packet;

import ac.grim.grimac.checks.Check;
import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.type.PacketCheck;
import ac.grim.grimac.mitigation.TrustFactorCheckType;
import ac.grim.grimac.player.GrimPlayer;

import java.util.ArrayList;

@CheckData(name = "Protocol (B)", configName = "TransactionOrder", checkTypeTrustFactor = TrustFactorCheckType.PACKET, changeTrustFactor = 1)
public class TransactionOrder extends Check implements PacketCheck {

    public TransactionOrder(GrimPlayer player) {
        super(player);
    }

}