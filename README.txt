EMPLOYEE PAYROLL MANAGEMENT SYSTEM (Java Swing + CSV)
====================================================

REQUIREMENTS
- JDK 8 or newer (JDK 17 recommended)
- No external libraries required

FILES
- Employee.java          : Abstract parent class; encapsulation and salary contract
- FullTimeEmployee.java  : Full-time employee implementation
- PartTimeEmployee.java  : Part-time employee implementation
- CSVFileHandler.java    : Loads/saves employee records to employees.csv
- PayrollGUI.java        : Swing GUI, table, forms, search, CRUD operations

HOW TO RUN (Windows)
1. Put all five .java files in the same folder.
2. Open Command Prompt in that folder.
3. Compile:
   javac Employee.java FullTimeEmployee.java PartTimeEmployee.java CSVFileHandler.java PayrollGUI.java
4. Run:
   java PayrollGUI

The file employees.csv is created automatically in the current working directory.

PAYROLL FORMULAS
Full-Time Gross = Monthly Salary + Allowance
Part-Time Gross = Hourly Rate * Hours Worked + Allowance
Net Salary = Gross Salary - Deductions

CSV COLUMNS
id,name,type,basePay,hoursWorked,allowance,deductions

NOTES
- For Full-Time, enter the monthly salary in "Monthly Salary / Hourly Rate".
- For Part-Time, enter hourly rate and monthly hours.
- IDs must be unique.
- CSV data is plain text. Do not use real sensitive employee/payroll data in a demo project.
