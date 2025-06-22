package uk.co.willpoulson.willslivelyvillages.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.PersistentStateType;
import net.minecraft.datafixer.DataFixTypes;

import java.util.HashMap;
import java.util.Map;

public class VillageNameData extends PersistentState {
    public static final String KEY = "village_names";
    private final Map<ChunkPos, String> villageNames = new HashMap<>();

    private static final Codec<Map.Entry<ChunkPos, String>> ENTRY_CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.INT.fieldOf("x").forGetter(e -> e.getKey().x),
                    Codec.INT.fieldOf("z").forGetter(e -> e.getKey().z),
                    Codec.STRING.fieldOf("name").forGetter(Map.Entry::getValue)
            ).apply(instance, (x, z, name) -> Map.entry(new ChunkPos(x, z), name))
    );

    public static final Codec<VillageNameData> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.list(ENTRY_CODEC)
                            .fieldOf(KEY)
                            .forGetter(data -> data.villageNames.entrySet().stream().toList())
            ).apply(instance, list -> {
                VillageNameData data = new VillageNameData();
                for (var entry : list) {
                    data.villageNames.put(entry.getKey(), entry.getValue());
                }
                return data;
            })
    );

    public static final PersistentStateType<VillageNameData> TYPE =
            new PersistentStateType<>(
                    KEY,
                    ctx -> new VillageNameData(),
                    ctx -> CODEC,
                    DataFixTypes.LEVEL
            );

    public void addVillageName(ChunkPos pos, String name) {
        villageNames.put(pos, name);
        markDirty();
    }

    public String getVillageName(ChunkPos pos) {
        return villageNames.get(pos);
    }

    public static VillageNameData getServerState(ServerWorld world) {
        PersistentStateManager manager = world.getPersistentStateManager();
        return manager.getOrCreate(TYPE);
    }
}
