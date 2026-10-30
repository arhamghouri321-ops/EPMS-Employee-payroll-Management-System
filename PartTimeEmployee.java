public class PartTimeEmployee extends Employee {
    private double hoursWorked;

    public PartTimeEmployee(String id, String name, double hourlyRate,
                            double hoursWorked, double allowance, double deductions) {
        super(id, name, hourlyRate, allowance, deductions);
        setHoursWorked(hoursWorked);
    }

    public void setHoursWorked(double hoursWorked) {
        if (!Double.isFinite(hoursWorked) || hoursWorked < 0 || hoursWorked > 744)
            throw new IllegalArgumentException("Hours must be between 0 and 744 per month.");
        this.hoursWorked = hoursWorked;
    }

    @Override
    public double calculateGrossSalary() {
        return getBasePay() * hoursWorked + getAllowance();
    }

    @Override
    public String getEmployeeType() {
        return "Part-Time";
    }

    @Override
    public double getHoursWorked() {
        return hoursWorked;
    }
}
