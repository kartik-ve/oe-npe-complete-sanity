import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.HashMap;

final class ExtractDateAndOrderID {
    private ExtractDateAndOrderID() {
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Usage: java ExtractDateAndOrderID <input_log_file>");
            return;
        }

        String fileName = args[0].substring(0, args[0].lastIndexOf("."));

        try (BufferedReader logFile = new BufferedReader(new FileReader(args[0]), 32 * 1024);
                BufferedWriter outputDateFile = new BufferedWriter(new FileWriter(fileName + ".date"));
                BufferedWriter outputIDFile = new BufferedWriter(new FileWriter(fileName + ".id"));) {
            processFile(logFile, outputDateFile, outputIDFile);
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
    }

    private static void processFile(BufferedReader inputFile, BufferedWriter outputDateFile,
            BufferedWriter outputIDFile) throws IOException {
        String line;

        Pattern datePattern = Pattern.compile("\\b\\d{2}/\\d{2}/\\d{2}\\s+\\d{2}:\\d{2}:\\d{2}\\b");
        DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("MM/dd/yy HH:mm:ss");
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm");

        HashMap<String, Integer> orderIds = new HashMap<>();
        String formattedDateTime = null;

        while ((line = inputFile.readLine()) != null) {
            if (formattedDateTime == null) {
                Matcher matcher = datePattern.matcher(line);
                if (matcher.find()) {
                    String rawDateTime = matcher.group();
                    LocalDateTime dateTime = LocalDateTime.parse(rawDateTime, inputFormatter);
                    formattedDateTime = dateTime.format(outputFormatter);
                }
            }

            String substring = "Order ID:";
            if (line.contains(substring)) {
                String[] parts = line.split(substring);
                if (parts.length > 1) {
                    String orderId = parts[1].trim().split("[\\s,]+")[0];
                    orderIds.put(orderId, orderIds.getOrDefault(orderId, 0) + 1);
                }
            }
        }

        if (formattedDateTime != null) {
            outputDateFile.write(formattedDateTime);
        } else {
            outputDateFile.write("DATE_NOT_FOUND");
        }
        outputDateFile.newLine();

        String finalOrderId = "EMPTY";
        int maxCount = 0;

        for (String id : orderIds.keySet()) {
            int count = orderIds.get(id);
            if (count > maxCount) {
                maxCount = count;
                finalOrderId = id;
            }
        }

        outputIDFile.write(finalOrderId);
        outputIDFile.newLine();
    }
}