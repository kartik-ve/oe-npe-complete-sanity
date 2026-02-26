package com.amdocs.sanity;

import java.io.FileInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class SanityRunner {
    public static void main(String[] args) {
        Map<String, String> params = parseArgs(args);

        Properties config = new Properties();
        try (FileInputStream fis = new FileInputStream(params.get("config"))) {
            config.load(fis);
        } catch (Exception e) {
            System.err.println("Failed to load config: " + e.getMessage());
            System.exit(1);
        }

        Path buildDir = Paths.get(params.get("buildDir"));

        try {
            ExcelReport.generate(
                buildDir,
                config,
                s -> System.out.println(s)
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static Map<String, String> parseArgs(String[] args) {
        Map<String, String> map = new HashMap<>();
        for (int i = 0; i < args.length; i += 2) {
            map.put(args[i].replace("--", ""), args[i + 1]);
        }

        return map;
    }
}
