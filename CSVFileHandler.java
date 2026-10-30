import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

public class CSVFileHandler {
    private final Path filePath;

    public CSVFileHandler(String fileName) {
        this.filePath = Paths.get(fileName).toAbsolutePath();
    }

    public List<Employee> loadEmployees() throws IOException {
        List<Employee> employees = new ArrayList<>();
        if (!Files.exists(filePath)) {
            saveEmployees(employees); // Create CSV with header on first run.
            return employees;
        }

        try (BufferedReader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
            String line;
            boolean firstLine = true;
            int lineNumber = 0;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.trim().isEmpty()) continue;
                if (firstLine && line.toLowerCase(Locale.ROOT).startsWith("id,")) {
                    firstLine = false;
                    continue;
                }
                firstLine = false;
                try {
                    List<String> fields = parseCsvLine(line);
                    if (fields.size() != 7) {
                        throw new IllegalArgumentException("Expected 7 CSV columns.");
                    }
                    String id = fields.get(0);
                    String name = fields.get(1);
                    String type = fields.get(2);
                    double basePay = Double.parseDouble(fields.get(3));
                    double hours = Double.parseDouble(fields.get(4));
                    double allowance = Double.parseDouble(fields.get(5));
                    double deductions = Double.parseDouble(fields.get(6));

                    Employee employee;
                    if ("Full-Time".equalsIgnoreCase(type)) {
                        employee = new FullTimeEmployee(id, name, basePay, allowance, deductions);
                    } else if ("Part-Time".equalsIgnoreCase(type)) {
                        employee = new PartTimeEmployee(id, name, basePay, hours, allowance, deductions);
                    } else {
                        throw new IllegalArgumentException("Unknown employee type: " + type);
                    }
                    employees.add(employee);
                } catch (RuntimeException ex) {
                    System.err.println("Skipping invalid CSV line " + lineNumber + ": " + ex.getMessage());
                }
            }
        }
        return employees;
    }

    public void saveEmployees(List<Employee> employees) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(filePath, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE)) {
            writer.write("id,name,type,basePay,hoursWorked,allowance,deductions");
            writer.newLine();
            for (Employee e : employees) {
                writer.write(csv(e.getId()) + "," + csv(e.getName()) + "," + csv(e.getEmployeeType()) + ","
                        + e.getBasePay() + "," + e.getHoursWorked() + ","
                        + e.getAllowance() + "," + e.getDeductions());
                writer.newLine();
            }
        }
    }

    public String getFilePath() {
        return filePath.toString();
    }

    private static String csv(String value) {
        String safe = value.replace("\"", "\"\"");
        return "\"" + safe + "\"";
    }

    // Supports quoted CSV values, including commas and escaped double quotes.
    private static List<String> parseCsvLine(String line) {
        List<String> values = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (c == ',' && !inQuotes) {
                values.add(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        if (inQuotes) throw new IllegalArgumentException("Unclosed quotation mark.");
        values.add(current.toString());
        return values;
    }
}
