package uk.co.willpoulson.willslivelyvillages.events;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.world.ServerWorld;
import uk.co.willpoulson.willslivelyvillages.classes.VillageProximityTracker;

public class PlayerEventsListener {
    public static void listenForPlayerEnterVillage() {
        ServerTickEvents.END_WORLD_TICK.register(world -> {
            if (!(world instanceof ServerWorld serverWorld)) {
                return;
            }

            VillageProximityTracker.update(serverWorld);
        });
    }
}
