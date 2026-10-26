
ATM Simulator with Database
A Java console-based ATM Simulator that uses MySQL and JDBC to store account details and transaction history between runs.
Educational project only: This is not a real banking application. Do not use real account details or PINs. The sample PIN hashing is for demonstration and is not suitable for production banking.
Features
Create an account with an account number, account holder name, PIN, and opening balance
Log in using account number and PIN
Check account balance
Deposit money
Withdraw money with an insufficient-balance check
View transaction history with timestamps
Store account and transaction data in MySQL
Use prepared statements for database operations
Use database transactions for balance changes and transaction records
Technologies
Java (JDK 17 or later recommended)
MySQL 8+
JDBC
MySQL Connector/J
Project Files
ATM-Simulator-with-DB/
├── ATM_Simulator.java
└── README.md
Database Setup
Install and start MySQL.
Create the database using MySQL Workbench or another MySQL client:
CREATE DATABASE atm_simulator;
The application creates the accounts and transactions tables when it starts, provided the configured MySQL user has permission to create tables.
Configure the Connection
Open ATM_Simulator.java and update these constants for your local MySQL setup:
private static final String DB_URL = "jdbc:mysql://localhost:3306/atm_simulator";
private static final String DB_USER = "root";
private static final String DB_PASSWORD = "YOUR_MYSQL_PASSWORD";
Do not commit a real database password to a public GitHub repository. For a real deployment, load credentials from environment variables or a secrets manager.
Add the MySQL JDBC Driver
Download MySQL Connector/J from the official MySQL website:
https://dev.mysql.com/downloads/connector/j/

Add the downloaded JAR to your IDE project's classpath/dependencies. If using IntelliJ IDEA, open File → Project Structure → Modules → Dependencies, add the JAR, and apply the changes. The exact menu may vary by IDE version.

Run the Application
Ensure MySQL is running and the atm_simulator database exists.
Configure the JDBC driver and database connection values.
Open ATM_Simulator.java in your Java IDE.
Run the ATM_Simulator class.
Create an account, then log in to use ATM features.
Database Tables
accounts
Stores account number, account holder, PIN hash, and current balance.
transactions
Stores transaction ID, account number, transaction type, amount, and timestamp. Each transaction is linked to an account through a foreign key.
Important Limitations
This is a learning/demo project, not a production financial system.
It does not implement bank-grade authentication, account lockout, audit controls, or regulatory requirements.
SHA-256 alone is not appropriate for securely storing PINs in a real application. Production systems should use a credential-specific password hashing scheme with a unique salt, plus rate limiting and secure secret management.
Monetary calculations use Java double for input before converting to BigDecimal; a production implementation should parse and handle money as BigDecimal throughout.
The account number is a user-provided identifier; the demo does not verify account ownership or connect to a real bank.
Future Enhancements
Use environment variables for database credentials
Use a salted, purpose-built password/PIN hashing algorithm
Add login attempt limits and account lockout
Add money transfers between accounts
Add automated tests and a graphical user interface
Use BigDecimal end-to-end for monetary values
License
For educational and learning purposes.
⋮⋮⋮⋮⋮⋮⋮⋮
