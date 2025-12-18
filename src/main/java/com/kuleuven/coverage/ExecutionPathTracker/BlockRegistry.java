package com.kuleuven.coverage.ExecutionPathTracker;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.InputStreamReader;
import java.lang.reflect.Type;
import java.util.Map;

public final class BlockRegistry {
    private static final String CFG_BLOCK_MAPPING_FILE_PATH = "./out/cfg_block_mapping.json";
    private static Map<Integer, BlockInfo> BLOCKS;

    static {
        load();
    }

    private static void load() {
        try {
            Gson gson = new Gson();
            Type type = new TypeToken<Map<Integer, BlockInfo>>() {}.getType();

            try (InputStreamReader reader =
                         new InputStreamReader(
                                 java.nio.file.Files.newInputStream(
                                         java.nio.file.Path.of(CFG_BLOCK_MAPPING_FILE_PATH)))) {

                BLOCKS = gson.fromJson(reader, type);
            }
        } catch (Exception e) {
            System.err.printf("Failed to load JSON from path %s", CFG_BLOCK_MAPPING_FILE_PATH);
            throw new RuntimeException(e);
        }
    }

    public static Map<Integer, BlockInfo> getBlocks() {
        return BLOCKS;
    }

    private BlockRegistry() {}
}
