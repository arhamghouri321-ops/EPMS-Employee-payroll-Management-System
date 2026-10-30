public abstract class Employee {
    private String id;
    private String name;
    private double basePay;
    private double allowance;
    private double deductions;

    public Employee(String id, String name, double basePay,
                    double allowance, double deductions) {
        setId(id);
        setName(name);
        setBasePay(basePay);
        setAllowance(allowance);
        setDeductions(deductions);
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public double getBasePay() { return basePay; }
    public double getAllowance() { return allowance; }
    public double getDeductions() { return deductions; }

    public void setId(String id) {
        if (id == null || id.trim().isEmpty())
            throw new IllegalArgumentException("Employee ID is required.");
        this.id = id.trim();
    }

    public void setName(String name) {
        if (name == null || name.trim().isEmpty())
            throw new IllegalArgumentException("Employee name is required.");
        this.name = name.trim();
    }

    public void setBasePay(double basePay) {
        if (!Double.isFinite(basePay) || basePay < 0)
            throw new IllegalArgumentException("Base pay/rate must be a valid non-negative number.");
        this.basePay = basePay;
    }

    public void setAllowance(double allowance) {
        if (!Double.isFinite(allowance) || allowance < 0)
            throw new IllegalArgumentException("Allowance must be a valid non-negative number.");
        this.allowance = allowance;
    }

    public void setDeductions(double deductions) {
        if (!Double.isFinite(deductions) || deductions < 0)
            throw new IllegalArgumentException("Deductions must be a valid non-negative number.");
        this.deductions = deductions;
    }

    public abstract double calculateGrossSalary();

    public double calculateNetSalary() {
        return calculateGrossSalary() - deductions;
    }

    public abstract String getEmployeeType();
    public abstract double getHoursWorked();
}
