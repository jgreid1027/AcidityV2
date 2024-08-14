package ac.grim.grimac.checks.impl.movement;

import ac.grim.grimac.checks.CheckData;
import ac.grim.grimac.mitigation.TrustFactorCheckType;
import ac.grim.grimac.player.GrimPlayer;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;

@CheckData(name = "Timer (A2)", configName = "TimerVehicle", setback = 10, checkTypeTrustFactor = TrustFactorCheckType.MOVEMENT, changeTrustFactor = 5)
public class VehicleTimer extends TimerCheck {
    boolean isDummy = false;

    public VehicleTimer(GrimPlayer player) {
        super(player);
    }

    @Override
    public boolean shouldCountPacketForTimer(PacketTypeCommon packetType) {
        // Ignore teleports
        if (player.packetStateData.lastPacketWasTeleport) return false;

        if (packetType == PacketType.Play.Client.VEHICLE_MOVE) {
            isDummy = false;
            return true; // Client controlling vehicle
        }

        if (packetType == PacketType.Play.Client.STEER_VEHICLE) {
            if (isDummy) { // Server is controlling vehicle
                return true;
            }
            isDummy = true; // Client is controlling vehicle
        }

        return false;
    }
}
