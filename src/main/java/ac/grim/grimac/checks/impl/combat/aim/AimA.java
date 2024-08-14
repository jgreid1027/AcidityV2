package ac.grim.grimac.checks.impl.combat.aim;

import ac.grim.grimac.checks.Check;
import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.type.RotationCheck;
import ac.grim.grimac.mitigation.TrustFactorCheckType;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.utils.anticheat.update.RotationUpdate;

@CheckData(name = "Aim (A)", configName = "AimDuplicateSpeed", checkTypeTrustFactor = TrustFactorCheckType.COMBAT, changeTrustFactor = 5)
public class AimA extends Check implements RotationCheck {
    public AimA(GrimPlayer playerData) {
        super(playerData);
    }

    boolean exempt = false;

    @Override
    public void process(final RotationUpdate rotationUpdate) {
        // TODO: Fix check

        if (player.packetStateData.lastPacketWasTeleport || player.packetStateData.lastPacketWasOnePointSeventeenDuplicate || player.compensatedEntities.getSelf().getRiding() != null) {
            exempt = true;
            return;
        }

        if (exempt) { // Exempt for a tick on teleport
            exempt = false;
            return;
        }

        player.previousDeltaX = player.currentDeltaX;
        player.previousDeltaY = player.currentDeltaY;

        player.currentDeltaX = rotationUpdate.getDeltaXRotABS();
        player.currentDeltaY = rotationUpdate.getDeltaYRotABS();

        if (player.currentDeltaX < 0f) return;
        if (player.previousDeltaX < 0f) return;

        if (player.currentDeltaY < 0f) return;
        if (player.previousDeltaY < 0f) return;



            /*
        if (player.currentDeltaX == player.previousDeltaX) {
            flagAndAlert();
        }
        if (player.currentDeltaY == player.previousDeltaY) {
            flagAndAlert();
        }
             */
    }
}
