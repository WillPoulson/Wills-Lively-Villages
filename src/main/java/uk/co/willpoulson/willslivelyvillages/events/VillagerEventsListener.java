package uk.co.willpoulson.willslivelyvillages.events;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.server.world.ServerWorld;
import uk.co.willpoulson.willslivelyvillages.managers.VillagerNameManager;

public class VillagerEventsListener {
    public static void listenForVillagerSpawns() {
        ServerEntityEvents.ENTITY_LOAD.register((entity, world) -> {
            if (
                !(world instanceof ServerWorld serverWorld) ||
                !(entity instanceof VillagerEntity villagerEntity)
            ) {
                return;
            }

            VillagerNameManager.assignVillagerName(villagerEntity, serverWorld);
        });
    }
}
