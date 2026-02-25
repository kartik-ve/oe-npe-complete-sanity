package com.amdocs.sanity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

final class ExcelReport {

    private ExcelReport() {}

    private static String determineEnvStatus(File envDir) throws Exception {

        try (Stream<Path> paths = Files.walk(envDir.toPath())) {

            List<File> xmlFiles = paths
                    .filter(Files::isRegularFile)
                    .filter(p -> p.toString().endsWith(".xml"))
                    .filter(p -> p.getFileName().toString().startsWith("TEST-"))
                    .map(Path::toFile)
                    .collect(Collectors.toList());

            for (File xml : xmlFiles) {

                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document doc = builder.parse(xml);

                NodeList failures = doc.getElementsByTagName("failure");

                if (failures.getLength() > 0) {
                    return "FAILED";
                }
            }
        }

        return "PASSED";
    }

    private static String readSingleLineFile(Path file) throws IOException {
        if (!Files.exists(file)) return "N/A";
        return Files.readAllLines(file).stream().findFirst().orElse("N/A");
    }

    private static void createExcel(List<String[]> rows, String outputFilePath) throws IOException {

        try (Workbook workbook = new XSSFWorkbook()) {

            Sheet sheet = workbook.createSheet("Sanity Summary");

            int rowNum = 0;

            // Header
            Row header = sheet.createRow(rowNum++);
            header.createCell(0).setCellValue("ENV");
            header.createCell(1).setCellValue("STATUS");
            header.createCell(2).setCellValue("ORDER ID");
            header.createCell(3).setCellValue("RUN DATE");

            // Data rows
            for (String[] rowData : rows) {
                Row row = sheet.createRow(rowNum++);
                for (int i = 0; i < rowData.length; i++) {
                    row.createCell(i).setCellValue(rowData[i]);
                }
            }

            try (FileOutputStream fos = new FileOutputStream(outputFilePath)) {
                workbook.write(fos);
            }
        }
    }

    static void generate(String junitPath,
                               String dataDir,
                               String outputPath,
                               Consumer<String> logger) throws Exception {

        if (logger == null) {
            logger = s -> {};
        }

        List<String> environments = java.util.Arrays.asList("SIT1", "QA1", "UAT1", "HF1");
        List<String[]> excelRows = new ArrayList<>();

        for (String env : environments) {

            logger.accept("Processing environment: " + env);

            File envDir = new File(junitPath, env);

            if (!envDir.exists() || !envDir.isDirectory()) {
                logger.accept("Skipping missing environment: " + env);
                continue;
            }

            String status = determineEnvStatus(envDir);

            String orderId = readSingleLineFile(Paths.get(dataDir, env + ".id"));
            String runDate = readSingleLineFile(Paths.get(dataDir, env + ".date"));

            excelRows.add(new String[]{env, status, orderId, runDate});
        }

        String excelPath = outputPath + File.separator + "SanitySummary.xlsx";
        createExcel(excelRows, excelPath);

        logger.accept("Excel generated at: " + excelPath);
    }
}