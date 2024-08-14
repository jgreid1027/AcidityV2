package ac.grim.grimac.checks.impl.combat.aim;

import ac.grim.grimac.checks.Check;
import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.type.RotationCheck;
import ac.grim.grimac.mitigation.TrustFactorCheckType;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.utils.anticheat.update.RotationUpdate;

@CheckData(name = "Aim (D)", configName = "AimSpeedLimit", checkTypeTrustFactor = TrustFactorCheckType.COMBAT, changeTrustFactor = 3)
public class AimD extends Check implements RotationCheck {
    public AimD(GrimPlayer playerData) {
        super(playerData);
    }

    boolean exempt = false;

    @Override
    public void process(final RotationUpdate rotationUpdate) {
        if (player.packetStateData.lastPacketWasTeleport || player.packetStateData.lastPacketWasOnePointSeventeenDuplicate || player.compensatedEntities.getSelf().getRiding() != null) {
            exempt = true;
            return;
        }

        if (exempt) { // Exempt for a tick on teleport
            exempt = false;
            return;
        }

        if (player.snappedAimFlag && player.snapHitAimFlag) {
            flagAndAlert();
            player.snapHitAimFlag = false;
            player.snappedAimFlag = false;
            return;
        }

        if (player.snappedAimFlag && !player.snapHitAimFlag) {
            player.snappedAimFlag = false;
            return;
        }

        if (rotationUpdate.getDeltaXRotABS() >= 50.0f) {
            player.snappedAimFlag = true;
        }
    }
}
