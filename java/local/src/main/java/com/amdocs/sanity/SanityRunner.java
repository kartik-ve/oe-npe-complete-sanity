package com.amdocs.sanity;

public class SanityRunner {
    public static void main(String[] args) {
        String junitPath = args[0];
        String dataDir = args[1];
        String outputPath = args[2];
        try {
            ExcelReport.generate(
                junitPath,
                dataDir,
                outputPath,
                s -> System.out.println(s)
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
