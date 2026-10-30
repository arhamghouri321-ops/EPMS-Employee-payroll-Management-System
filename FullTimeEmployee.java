public class FullTimeEmployee extends Employee {
    public FullTimeEmployee(String id, String name, double monthlySalary,
                            double allowance, double deductions) {
        super(id, name, monthlySalary, allowance, deductions);
    }

    @Override
    public double calculateGrossSalary() {
        return getBasePay() + getAllowance();
    }

    @Override
    public String getEmployeeType() {
        return "Full-Time";
    }

    @Override
    public double getHoursWorked() {
        return 0;
    }
}
