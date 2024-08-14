package ac.grim.grimac.checks.impl.movement;

import ac.grim.grimac.GrimAPI;
import ac.grim.grimac.checks.Check;
import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.checks.type.PostPredictionCheck;
import ac.grim.grimac.mitigation.PlayerTrustFactor;
import ac.grim.grimac.mitigation.TrustFactorCheckType;
import ac.grim.grimac.mitigation.types.MovementMitigationType;
import ac.grim.grimac.player.GrimPlayer;
import ac.grim.grimac.utils.anticheat.update.PredictionComplete;
import ac.grim.grimac.utils.data.VectorData;
import ac.grim.grimac.utils.data.VelocityData;
import com.github.retrooper.packetevents.event.PacketSendEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.world.states.WrappedBlockState;
import com.github.retrooper.packetevents.protocol.world.states.defaulttags.BlockTags;
import com.github.retrooper.packetevents.protocol.world.states.type.StateType;
import com.github.retrooper.packetevents.protocol.world.states.type.StateTypes;
import com.github.retrooper.packetevents.protocol.world.states.type.StateValue;
import com.github.retrooper.packetevents.util.Vector3f;
import com.github.retrooper.packetevents.util.Vector3i;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerExplosion;
import lombok.Getter;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Nullable;

import java.util.Deque;
import java.util.LinkedList;

@CheckData(name = "Movement Mitigation Helper", configName = "MovementMitigationHelper")
public class MovementMitigationHelper extends Check implements PostPredictionCheck {

    public MovementMitigationHelper(GrimPlayer player) {
        super(player);
    }

    @Override
    public void onPredictionComplete(final PredictionComplete predictionComplete) {
       if (player.timesToMitigateMovement > 0) {
           if (player.onGround && !player.movementMitigationIsOnGround) {
               player.movementMitigationIsOnGround = true;
               player.movementMitigationSetXZPosition = false;
               player.timesToMitigateMovement--;

               // The actual trust factor setting already happens in MitigationHandler, don't worry about doing it here :D
               player.movementCurrentWeight--;

               if (player.movementTrustFactor == PlayerTrustFactor.GREEN) {
                   for (Player bukkitPlayer : GrimAPI.INSTANCE.getAlertManager().getEnabledVerbose()) {
                       String alertString = "&4[Acidity]: &f" + player.getName() + " was mitigated for Movement Exploitation [" + "&2GREEN" + "&f] (Mitigations Remaining = " + player.timesToMitigateMovement + ")";

                       bukkitPlayer.sendMessage(alertString);
                   }
               }

               if (player.movementTrustFactor == PlayerTrustFactor.YELLOW) {
                   for (Player bukkitPlayer : GrimAPI.INSTANCE.getAlertManager().getEnabledAlerts()) {
                       String alertString = "&4[Acidity]: &f" + player.getName() + " was mitigated for Movement Exploitation [" + "&gYELLOW" + "&f] (Mitigations Remaining = " + player.timesToMitigateMovement + ")";

                       bukkitPlayer.sendMessage(alertString);
                   }
               }

               if (player.movementTrustFactor == PlayerTrustFactor.ORANGE) {
                   for (Player bukkitPlayer : GrimAPI.INSTANCE.getAlertManager().getEnabledAlerts()) {
                       String alertString = "&4[Acidity]: &f" + player.getName() + " was mitigated for Movement Exploitation [" + "&6ORANGE" + "&f] (Mitigations Remaining = " + player.timesToMitigateMovement + ")";

                       bukkitPlayer.sendMessage(alertString);
                   }
               }

               if (player.movementTrustFactor == PlayerTrustFactor.RED) {
                   for (Player bukkitPlayer : GrimAPI.INSTANCE.getAlertManager().getEnabledAlerts()) {
                       String alertString = "&4[Acidity]: &f" + player.getName() + " was mitigated for Movement Exploitation [" + "&4RED" + "&f] (Mitigations Remaining = " + player.timesToMitigateMovement + ")";

                       bukkitPlayer.sendMessage(alertString);
                   }
               }
           }

           if (!player.onGround) {
               player.movementMitigationIsOnGround = false;
           }

           if (!player.onGround && player.movementMitigationType == MovementMitigationType.TP_ACIDITYONE) {
               player.getSetbackTeleportUtil().executeViolationSetback();
           }

           if (!player.onGround && player.movementMitigationType == MovementMitigationType.TP_ACIDITYTWO) {
               player.getSetbackTeleportUtil().executeViolationSetback2();
           }
       }
    }


}
