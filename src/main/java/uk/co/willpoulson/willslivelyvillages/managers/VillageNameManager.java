package uk.co.willpoulson.willslivelyvillages.managers;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.world.chunk.Chunk;
import uk.co.willpoulson.willslivelyvillages.data.VillageNameData;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VillageNameManager {
    private static final Map<RegistryKey<Biome>, List<String>> namePoolByBiome = new HashMap<>();

    public static void loadVillageNames() {
        // Load village names from JSON file
        JsonObject jsonObject = loadVillageNamesJsonFile();

        // Iterate through biomes in the JSON object
        for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
            String biomeId = entry.getKey();
            JsonArray jsonArray = entry.getValue().getAsJsonArray();  // Get the array of names

            // Convert JsonArray to List<String>
            List<String> villageNames = new ArrayList<>();
            for (JsonElement element : jsonArray) {
                villageNames.add(element.getAsString());
            }

            // Add the village names to the map using the biome's registry key
            Identifier biomeIdentifier = Identifier.tryParse(biomeId);
            RegistryKey<Biome> biomeKey = RegistryKey.of(RegistryKeys.BIOME, biomeIdentifier);
            namePoolByBiome.put(biomeKey, villageNames);
        }
    }

    public static String assignVillageName(ServerWorld world, Chunk villageStartChunk) {
        RegistryKey<Biome> biomeKey = getChunkBiome(villageStartChunk, world);
        String villageName = getVillageNameFromBiomeKey(biomeKey, world);

        VillageNameData villageNameData = VillageNameData.getServerState(world);
        villageNameData.addVillageName(villageStartChunk.getPos(), villageName);
        return villageName;
    }

    public static String getVillageName(ServerWorld world, ChunkPos villageStartChunkPos) {
        VillageNameData villageNameData = VillageNameData.getServerState(world);
        return villageNameData.getVillageName(villageStartChunkPos);
    }

    private static JsonObject loadVillageNamesJsonFile() {
        String villageNamesJsonPath = "/assets/wills-lively-villages/village_names.json";
        InputStream inputStream = VillagerNameManager.class.getResourceAsStream(villageNamesJsonPath);

        if (inputStream == null) {
            throw new RuntimeException("Could not find village names JSON file at " + villageNamesJsonPath);
        }

        Reader reader = new InputStreamReader(inputStream);
        return JsonParser.parseReader(reader).getAsJsonObject();
    }

    private static RegistryKey<Biome> getChunkBiome(Chunk chunk, ServerWorld world) {
        BlockPos chunkCenter = chunk.getPos().getCenterAtY(chunk.getTopY());
        return world.getBiome(chunkCenter).getKey().orElse(BiomeKeys.PLAINS);
    }

    private static String getVillageNameFromBiomeKey(RegistryKey<Biome> biomeKey, ServerWorld world) {
        List<String> villageNames = namePoolByBiome.get(biomeKey);

        if (villageNames == null) {
            return getVillageNameFromBiomeKey(BiomeKeys.PLAINS, world);
        }

        return villageNames.get(world.getRandom().nextInt(villageNames.size()));
    }
}
