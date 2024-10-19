package uk.co.willpoulson.willslivelyvillages.classes;

import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.network.packet.s2c.play.SubtitleS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.poi.PointOfInterest;
import net.minecraft.world.poi.PointOfInterestStorage;
import net.minecraft.world.poi.PointOfInterestTypes;
import uk.co.willpoulson.willslivelyvillages.managers.VillageNameManager;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class VillageProximityTracker {
    private static final int CHECK_COOLDOWN_TICKS = 100;
    private static int globalTickCounter = 0;
    private static final Map<ServerPlayerEntity, Boolean> playerInVillage = new HashMap<>();

    public static void update(ServerWorld world) {
        globalTickCounter++;

        if (globalTickCounter < CHECK_COOLDOWN_TICKS) {
            return;
        }

        for (ServerPlayerEntity player : world.getPlayers()) {
            checkPlayerInVillage(world, player);
        }

        globalTickCounter = 0;
    }

    private static void checkPlayerInVillage(ServerWorld world, ServerPlayerEntity player) {
        BlockPos playerPos = player.getBlockPos();

        PointOfInterestStorage poiStorage = world.getPointOfInterestStorage();
        Optional<PointOfInterest> potentialMeetingPoint = poiStorage.getInCircle(
            poiType -> poiType.matchesKey(PointOfInterestTypes.MEETING),
            playerPos,
            100,
            PointOfInterestStorage.OccupationStatus.ANY
        ).findFirst();

        if (potentialMeetingPoint.isEmpty()) {
            playerInVillage.remove(player);
            return;
        }

        PointOfInterest meetingPoint = potentialMeetingPoint.get();
        Chunk meetingPointChunk = world.getChunk(meetingPoint.getPos());

        String villageName = VillageNameManager.getVillageName(world, meetingPointChunk.getPos());

        if (playerInVillage.containsKey(player)) {
            return;
        }

        if (villageName == null) {
            villageName = VillageNameManager.assignVillageName(world, meetingPointChunk);
        }

        displayVillageEntryMessage(player, villageName);
        playVillageEntrySound(world, player);
        playerInVillage.put(player, true);
    }

    public static void displayVillageEntryMessage(ServerPlayerEntity player, String villageName) {
        Text titleText = Text.literal(villageName).formatted(Formatting.GOLD);

        player.networkHandler.sendPacket(new TitleS2CPacket(titleText));
    }

    public static void playVillageEntrySound(ServerWorld world, ServerPlayerEntity player) {
        world.playSound(
            null,
            player.getBlockPos(),
            SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP,
            SoundCategory.PLAYERS,
            0.5f,
            1f
        );
    }
}
