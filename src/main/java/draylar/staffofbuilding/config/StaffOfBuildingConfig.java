package draylar.staffofbuilding.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static draylar.staffofbuilding.StaffOfBuilding.MOD_ID;

public class StaffOfBuildingConfig {

    public boolean woodenEnabled = true;
    public int woodenSize = 3;
    public boolean stoneEnabled = true;
    public int stoneSize = 6;
    public boolean ironEnabled = true;
    public int ironSize = 9;
    public boolean goldenEnabled = true;
    public int goldenSize = 12;
    public boolean diamondEnabled = true;
    public int diamondSize = 16;
    public boolean netheriteEnabled = true;
    public int netheriteSize = 32;
    public boolean infiniteEnabled = true;
    public int infiniteSize = 64;

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH;

    static {
        CONFIG_PATH = FabricLoader.getInstance().getConfigDir().resolve(MOD_ID + ".json");
    }

    public static StaffOfBuildingConfig load() {
        try {
            if (!Files.exists(CONFIG_PATH)) {
                StaffOfBuildingConfig config = new StaffOfBuildingConfig();

                FileWriter writer = new FileWriter(CONFIG_PATH.toFile());
                GSON.toJson(config, writer);
                writer.close();

                return config;
            } else {
                FileReader reader = new FileReader(CONFIG_PATH.toFile());
                StaffOfBuildingConfig config = GSON.fromJson(reader, StaffOfBuildingConfig.class);
                reader.close();

                return config;
            }
        } catch (IOException e) {
            throw new RuntimeException("[" + MOD_ID + "] Failed to load config", e);
        }
    }
}
