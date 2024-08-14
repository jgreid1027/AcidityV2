package ac.grim.grimac.mitigation;

import ac.grim.grimac.checks.Check;
import ac.grim.grimac.mitigation.types.CombatMitigationType;
import ac.grim.grimac.mitigation.types.MovementMitigationType;
import ac.grim.grimac.player.GrimPlayer;

public class MitigationHandler {

    public void handleFlag(GrimPlayer player, Check check) {
        // TODO: Add stuff for violation severity

        if (check.getTrustFactorType() == TrustFactorCheckType.COMBAT) {
            player.combatCurrentWeight += check.getTrustFactorChange();

            if (player.combatCurrentWeight >= player.greenTrustFactorMinCombat && player.combatCurrentWeight <= player.greenTrustFactorMaxCombat) {
                player.combatTrustFactor = PlayerTrustFactor.GREEN;
            }
            if (player.combatCurrentWeight >= player.yellowTrustFactorMinCombat && player.combatCurrentWeight <= player.yellowTrustFactorMaxCombat) {
                player.combatTrustFactor = PlayerTrustFactor.YELLOW;
            }
            if (player.combatCurrentWeight >= player.orangeTrustFactorMinCombat && player.combatCurrentWeight <= player.orangeTrustFactorMaxCombat) {
                player.combatTrustFactor = PlayerTrustFactor.ORANGE;
            }

            // integer limit on top!!11
            if (player.combatCurrentWeight >= player.redTrustFactorMinCombat && player.combatCurrentWeight < player.redTrustFactorMaxCombat) {
                player.combatTrustFactor = PlayerTrustFactor.RED;
            }

            // mitigation stuff
            if (player.combatTrustFactor == PlayerTrustFactor.GREEN) {
                player.timesToMitigateCombat++; // Doesn't really do anything until you get to YELLOW or lower trust factor
            }

            if (player.combatTrustFactor == PlayerTrustFactor.YELLOW) {
                player.combatMitigationType = CombatMitigationType.REDUCE;
                player.timesToMitigateCombat++;
            }

            if (player.combatTrustFactor == PlayerTrustFactor.ORANGE) {
                player.combatMitigationType = CombatMitigationType.SLOWREDUCE;
                player.timesToMitigateCombat += 3;
            }

            if (player.combatTrustFactor == PlayerTrustFactor.RED) {
                player.combatMitigationType = CombatMitigationType.CANCEL;
                player.timesToMitigateCombat += 5;
            }
        }
        if (check.getTrustFactorType() == TrustFactorCheckType.MOVEMENT) {
            player.movementCurrentWeight += check.getTrustFactorChange();

            if (player.movementCurrentWeight >= player.greenTrustFactorMinMovement && player.movementCurrentWeight <= player.greenTrustFactorMaxMovement) {
                player.combatTrustFactor = PlayerTrustFactor.GREEN;
            }
            if (player.movementCurrentWeight >= player.yellowTrustFactorMinMovement && player.movementCurrentWeight <= player.yellowTrustFactorMaxMovement) {
                player.combatTrustFactor = PlayerTrustFactor.YELLOW;
            }
            if (player.movementCurrentWeight >= player.orangeTrustFactorMinMovement && player.movementCurrentWeight <= player.orangeTrustFactorMaxMovement) {
                player.combatTrustFactor = PlayerTrustFactor.ORANGE;
            }

            // integer limit on top!!11
            if (player.movementCurrentWeight >= player.redTrustFactorMinMovement && player.movementCurrentWeight < player.redTrustFactorMaxMovement) {
                player.combatTrustFactor = PlayerTrustFactor.RED;
            }

            // mitigation stuff
            if (player.movementTrustFactor == PlayerTrustFactor.YELLOW) {
                player.movementMitigationType = MovementMitigationType.TP_GRIM;
            }

            if (player.movementTrustFactor == PlayerTrustFactor.ORANGE) {
                player.movementMitigationType = MovementMitigationType.TP_ACIDITYONE;
            }

            if (player.movementTrustFactor == PlayerTrustFactor.RED) {
                player.movementMitigationType = MovementMitigationType.TP_ACIDITYTWO;
            }

        }
    }

}
