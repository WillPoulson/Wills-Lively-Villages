package uk.co.willpoulson.willslivelyvillages.managers;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonElement;
import net.minecraft.util.JsonHelper;

import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import uk.co.willpoulson.willslivelyvillages.classes.VillagerNamePool;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VillagerNameManager {
    private static final Map<RegistryKey<Biome>, VillagerNamePool> namePoolByBiome = new HashMap<>();

    public static void loadVillagerNames() {
        JsonObject jsonObject = loadVillagerNamesJsonFile();

        // Iterate through biomes in the JSON object
        for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
            String biomeId = entry.getKey();
            JsonObject nameObject = entry.getValue().getAsJsonObject();

            // Parse first and last names
            List<String> firstNames = extractStringListFromJsonArray(JsonHelper.getArray(nameObject, "first_names"));
            List<String> lastNames = extractStringListFromJsonArray(JsonHelper.getArray(nameObject, "last_names"));

            // Create a VillagerNamePool for each biome
            VillagerNamePool namePool = new VillagerNamePool(firstNames, lastNames);

            // Add the name pool to the map using the biome's registry key
            Identifier biomeIdentifier = Identifier.tryParse(biomeId);
            RegistryKey<Biome> biomeKey = RegistryKey.of(RegistryKeys.BIOME, biomeIdentifier);
            namePoolByBiome.put(biomeKey, namePool);
        }
    }

    public static void assignVillagerName(VillagerEntity villager, ServerWorld world) {
        RegistryKey<Biome> biomeKey = getVillagerBiomeKey(villager, world);
        String villagerName = getVillagerNameFromBiomeKey(biomeKey, world);

        villager.setCustomName(Text.literal(villagerName));
        villager.setCustomNameVisible(true);
    }

    private static JsonObject loadVillagerNamesJsonFile() {
        String villageNamesJsonPath = "/assets/wills-lively-villages/villager_names.json";
        InputStream inputStream = VillagerNameManager.class.getResourceAsStream(villageNamesJsonPath);

        if (inputStream == null) {
            throw new RuntimeException("Could not find villager names JSON file at " + villageNamesJsonPath);
        }

        Reader reader = new InputStreamReader(inputStream);
        return JsonParser.parseReader(reader).getAsJsonObject();
    }

    private static List<String> extractStringListFromJsonArray(JsonArray jsonArray) {
        List<String> list = new ArrayList<>();
        for (JsonElement element : jsonArray) {
            list.add(element.getAsString());
        }
        return list;
    }

    private static RegistryKey<Biome> getVillagerBiomeKey(VillagerEntity villager, ServerWorld world) {
        BlockPos pos = villager.getBlockPos();
        return world.getBiome(pos).getKey().orElse(BiomeKeys.PLAINS);
    }

    private static String getVillagerNameFromBiomeKey(RegistryKey<Biome> biomeKey, ServerWorld world) {
        VillagerNamePool namePool = namePoolByBiome.get(biomeKey);

        if (namePool == null) {
            namePool = namePoolByBiome.get(BiomeKeys.PLAINS);
        }

        int firstNamesCount = namePool.getFirstNames().size();
        int lastNamesCount = namePool.getLastNames().size();
        int randomFirstNameIndex = world.random.nextInt(firstNamesCount);
        int randomLastNameIndex = world.random.nextInt(lastNamesCount);

        String firstName = namePool.getFirstNames().get(randomFirstNameIndex);
        String lastName = namePool.getLastNames().get(randomLastNameIndex);

        return firstName + " " + lastName;
    }
}
