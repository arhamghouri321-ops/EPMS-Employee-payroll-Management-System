import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class PayrollGUI extends JFrame {
    private final CSVFileHandler fileHandler = new CSVFileHandler("employees.csv");
    private final List<Employee> employees = new ArrayList<>();

    private final JTextField idField = new JTextField(12);
    private final JTextField nameField = new JTextField(12);
    private final JComboBox<String> typeBox = new JComboBox<>(new String[]{"Full-Time", "Part-Time"});
    private final JTextField basePayField = new JTextField(12);
    private final JTextField hoursField = new JTextField(12);
    private final JTextField allowanceField = new JTextField(12);
    private final JTextField deductionsField = new JTextField(12);
    private final JTextField searchField = new JTextField(12);

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new String[]{"ID", "Name", "Type", "Base Pay / Rate", "Hours", "Allowance", "Deductions", "Gross", "Net"}, 0) {
        @Override public boolean isCellEditable(int row, int column) { return false; }
    };
    private final JTable table = new JTable(tableModel);
    private final JLabel statusLabel = new JLabel("Ready.");

    public PayrollGUI() {
        super("Employee Payroll Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1120, 650);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        add(buildForm(), BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);
        add(buildBottomPanel(), BorderLayout.SOUTH);

        typeBox.addActionListener(e -> updateHoursFieldState());
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && table.getSelectedRow() >= 0) {
                int row = table.convertRowIndexToModel(table.getSelectedRow());
                Employee selected = findById(String.valueOf(tableModel.getValueAt(row, 0)));
                if (selected != null) fillForm(selected);
            }
        });

        loadFromFile();
        updateHoursFieldState();
    }

    private JPanel buildForm() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Employee Details"));
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 6, 5, 6);
        c.anchor = GridBagConstraints.WEST;

        addField(panel, c, 0, 0, "Employee ID:", idField);
        addField(panel, c, 2, 0, "Employee Name:", nameField);
        addField(panel, c, 0, 1, "Employee Type:", typeBox);
        addField(panel, c, 2, 1, "Monthly Salary / Hourly Rate:", basePayField);
        addField(panel, c, 0, 2, "Hours (Part-Time only):", hoursField);
        addField(panel, c, 2, 2, "Allowance:", allowanceField);
        addField(panel, c, 0, 3, "Deductions:", deductionsField);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addButton = new JButton("Add Employee");
        JButton updateButton = new JButton("Update Selected");
        JButton deleteButton = new JButton("Delete Selected");
        JButton clearButton = new JButton("Clear Form");
        addButton.addActionListener(e -> addEmployee());
        updateButton.addActionListener(e -> updateEmployee());
        deleteButton.addActionListener(e -> deleteEmployee());
        clearButton.addActionListener(e -> clearForm());
        buttons.add(addButton);
        buttons.add(updateButton);
        buttons.add(deleteButton);
        buttons.add(clearButton);

        c.gridx = 0; c.gridy = 4; c.gridwidth = 4; c.fill = GridBagConstraints.HORIZONTAL;
        panel.add(buttons, c);
        return panel;
    }

    private void addField(JPanel panel, GridBagConstraints c, int x, int y, String label, Component field) {
        c.gridwidth = 1; c.fill = GridBagConstraints.NONE;
        c.gridx = x; c.gridy = y;
        panel.add(new JLabel(label), c);
        c.gridx = x + 1; c.fill = GridBagConstraints.HORIZONTAL;
        panel.add(field, c);
    }

    private JPanel buildBottomPanel() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Search by ID or Name:"));
        searchPanel.add(searchField);
        JButton searchButton = new JButton("Search");
        JButton showAllButton = new JButton("Show All");
        searchButton.addActionListener(e -> searchEmployees());
        showAllButton.addActionListener(e -> refreshTable(employees));
        searchPanel.add(searchButton);
        searchPanel.add(showAllButton);
        panel.add(searchPanel, BorderLayout.NORTH);
        panel.add(statusLabel, BorderLayout.SOUTH);
        panel.setBorder(BorderFactory.createEmptyBorder(4, 8, 8, 8));
        return panel;
    }

    private Employee readEmployeeFromForm() {
        String id = idField.getText().trim();
        String name = nameField.getText().trim();
        String type = String.valueOf(typeBox.getSelectedItem());
        double basePay = parseNonNegative(basePayField.getText(), "Monthly salary / hourly rate");
        double allowance = parseNonNegative(allowanceField.getText(), "Allowance");
        double deductions = parseNonNegative(deductionsField.getText(), "Deductions");

        if (deductions > basePay + allowance && "Full-Time".equals(type)) {
            int answer = JOptionPane.showConfirmDialog(this,
                    "Deductions are greater than salary plus allowance. Continue?",
                    "Confirm deductions", JOptionPane.YES_NO_OPTION);
            if (answer != JOptionPane.YES_OPTION) return null;
        }

        if ("Part-Time".equals(type)) {
            double hours = parseNonNegative(hoursField.getText(), "Hours worked");
            if (hours > 744) throw new IllegalArgumentException("Hours cannot exceed 744 per month.");
            return new PartTimeEmployee(id, name, basePay, hours, allowance, deductions);
        }
        return new FullTimeEmployee(id, name, basePay, allowance, deductions);
    }

    private double parseNonNegative(String text, String fieldName) {
        try {
            double value = Double.parseDouble(text.trim());
            if (!Double.isFinite(value) || value < 0)
                throw new NumberFormatException();
            return value;
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException(fieldName + " must be a valid number zero or greater.");
        }
    }

    private void addEmployee() {
        try {
            Employee employee = readEmployeeFromForm();
            if (employee == null) return;
            if (findById(employee.getId()) != null) {
                showError("An employee with this ID already exists.");
                return;
            }
            employees.add(employee);
            persistAndRefresh();
            clearForm();
            statusLabel.setText("Employee added successfully.");
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        }
    }

    private void updateEmployee() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            showError("Please select an employee row in the table to update.");
            return;
        }
        try {
            int modelRow = table.convertRowIndexToModel(selectedRow);
            String originalId = String.valueOf(tableModel.getValueAt(modelRow, 0));
            Employee updated = readEmployeeFromForm();
            if (updated == null) return;

            Employee duplicate = findById(updated.getId());
            if (duplicate != null && !originalId.equalsIgnoreCase(updated.getId())) {
                showError("Another employee already uses this ID.");
                return;
            }

            int index = indexOfId(originalId);
            if (index >= 0) {
                employees.set(index, updated);
                persistAndRefresh();
                clearForm();
                statusLabel.setText("Employee updated successfully.");
            }
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        }
    }

    private void deleteEmployee() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow < 0) {
            showError("Please select an employee row in the table to delete.");
            return;
        }
        int answer = JOptionPane.showConfirmDialog(this, "Delete the selected employee?",
                "Confirm delete", JOptionPane.YES_NO_OPTION);
        if (answer != JOptionPane.YES_OPTION) return;

        int modelRow = table.convertRowIndexToModel(selectedRow);
        String id = String.valueOf(tableModel.getValueAt(modelRow, 0));
        employees.removeIf(e -> e.getId().equalsIgnoreCase(id));
        persistAndRefresh();
        clearForm();
        statusLabel.setText("Employee deleted.");
    }

    private void searchEmployees() {
        String query = searchField.getText().trim().toLowerCase();
        if (query.isEmpty()) {
            refreshTable(employees);
            return;
        }
        List<Employee> matches = new ArrayList<>();
        for (Employee e : employees) {
            if (e.getId().toLowerCase().contains(query) || e.getName().toLowerCase().contains(query))
                matches.add(e);
        }
        refreshTable(matches);
        statusLabel.setText(matches.size() + " matching employee(s) found.");
    }

    private void fillForm(Employee e) {
        idField.setText(e.getId());
        nameField.setText(e.getName());
        typeBox.setSelectedItem(e.getEmployeeType());
        basePayField.setText(String.valueOf(e.getBasePay()));
        hoursField.setText(e instanceof PartTimeEmployee ? String.valueOf(e.getHoursWorked()) : "");
        allowanceField.setText(String.valueOf(e.getAllowance()));
        deductionsField.setText(String.valueOf(e.getDeductions()));
        updateHoursFieldState();
    }

    private void clearForm() {
        idField.setText("");
        nameField.setText("");
        typeBox.setSelectedIndex(0);
        basePayField.setText("");
        hoursField.setText("");
        allowanceField.setText("");
        deductionsField.setText("");
        searchField.setText("");
        table.clearSelection();
        updateHoursFieldState();
    }

    private void updateHoursFieldState() {
        boolean partTime = "Part-Time".equals(String.valueOf(typeBox.getSelectedItem()));
        hoursField.setEnabled(partTime);
        if (!partTime) hoursField.setText("");
    }

    private Employee findById(String id) {
        for (Employee e : employees)
            if (e.getId().equalsIgnoreCase(id.trim())) return e;
        return null;
    }

    private int indexOfId(String id) {
        for (int i = 0; i < employees.size(); i++)
            if (employees.get(i).getId().equalsIgnoreCase(id.trim())) return i;
        return -1;
    }

    private void refreshTable(List<Employee> source) {
        tableModel.setRowCount(0);
        for (Employee e : source) {
            tableModel.addRow(new Object[]{
                    e.getId(), e.getName(), e.getEmployeeType(),
                    money(e.getBasePay()), e.getEmployeeType().equals("Part-Time") ? e.getHoursWorked() : "-",
                    money(e.getAllowance()), money(e.getDeductions()),
                    money(e.calculateGrossSalary()), money(e.calculateNetSalary())
            });
        }
    }

    private String money(double value) {
        return String.format(java.util.Locale.US, "%.2f", value);
    }

    private void persistAndRefresh() {
        try {
            fileHandler.saveEmployees(employees);
            refreshTable(employees);
        } catch (IOException ex) {
            showError("Could not save CSV file:\n" + ex.getMessage());
        }
    }

    private void loadFromFile() {
        try {
            employees.clear();
            employees.addAll(fileHandler.loadEmployees());
            refreshTable(employees);
            statusLabel.setText("Loaded " + employees.size() + " employee(s). CSV: " + fileHandler.getFilePath());
        } catch (IOException ex) {
            showError("Could not read CSV file:\n" + ex.getMessage());
        }
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Input / Operation Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new PayrollGUI().setVisible(true));
    }
}
