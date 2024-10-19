package uk.co.willpoulson.willslivelyvillages.data;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtElement;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;

import java.util.HashMap;
import java.util.Map;

public class VillageNameData extends PersistentState {
    private static final String KEY = "village_names";
    private final Map<ChunkPos, String> villageNames = new HashMap<>();

    private static final Type<VillageNameData> type = new Type<>(
            VillageNameData::new,
            VillageNameData::fromNbt,
            null
    );

    // Add a village name and mark the data as dirty (requiring save)
    public void addVillageName(ChunkPos villageStartChunkPos, String villageName) {
        villageNames.put(villageStartChunkPos, villageName);
        markDirty();
    }

    // Retrieve a village name
    public String getVillageName(ChunkPos villageStartChunkPos) {
        return villageNames.getOrDefault(villageStartChunkPos, null);
    }

    // Implement the correct writeNbt method (no WrapperLookup needed)
    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        NbtList villageList = new NbtList();

        // Store each village's center position and its name
        for (Map.Entry<ChunkPos, String> entry : villageNames.entrySet()) {
            NbtCompound villageNbt = new NbtCompound();
            ChunkPos pos = entry.getKey();
            String name = entry.getValue();

            villageNbt.putInt("x", pos.x);
            villageNbt.putInt("z", pos.z);
            villageNbt.putString("name", name);

            villageList.add(villageNbt);
        }

        nbt.put(KEY, villageList);
        return nbt;
    }

    // Load the village names from NBT
    public static VillageNameData fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
        VillageNameData data = new VillageNameData();
        NbtList villageList = nbt.getList(KEY, NbtElement.COMPOUND_TYPE);

        for (int i = 0; i < villageList.size(); i++) {
            NbtCompound villageNbt = villageList.getCompound(i);
            int x = villageNbt.getInt("x");
            int z = villageNbt.getInt("z");
            String name = villageNbt.getString("name");

            ChunkPos pos = new ChunkPos(x, z);
            data.villageNames.put(pos, name);
        }

        return data;
    }

    public static VillageNameData getServerState(ServerWorld world) {
        PersistentStateManager persistentStateManager = world.getPersistentStateManager();
        return persistentStateManager.getOrCreate(type, KEY);
    }
}
