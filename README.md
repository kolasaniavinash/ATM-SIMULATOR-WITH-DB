# ATM Simulator with Database

A console-based **ATM Simulator developed using Java, MySQL, and JDBC** that allows users to perform basic ATM operations while storing account information and transaction history in a database.

## Project Overview

The ATM Simulator is a menu-driven Java application designed to demonstrate database connectivity and basic banking operations. Users can create an account, log in using their account number and PIN, check their balance, deposit money, withdraw money, and view transaction history.

Unlike a basic banking application that stores data only in memory, this project uses MySQL to maintain account records and transactions between application sessions.

## Features

- **Account Creation:** Create an account with an account number, account holder name, PIN, and opening balance.
- **User Login:** Log in using an account number and four-digit PIN.
- **Balance Inquiry:** Check the current account balance.
- **Cash Deposit:** Deposit money into an account.
- **Cash Withdrawal:** Withdraw money while checking the available balance.
- **Transaction History:** View previous deposits and withdrawals with timestamps.
- **Database Storage:** Store account information and transaction records in MySQL.
- **Input Validation:** Validate menu selections, PIN format, and monetary amounts.
- **Prepared Statements:** Use parameterized SQL queries for database operations.
- **Transaction Management:** Commit successful transactions and roll back unsuccessful database operations.

## Technologies Used

- **Programming Language:** Java
- **Database:** MySQL
- **Database Connectivity:** JDBC
- **JDBC Driver:** MySQL Connector/J
- **Input Handling:** Scanner
- **Concepts:** Object-Oriented Programming (OOP), encapsulation, exception handling, SQL queries, and database transactions.

## Project Structure

```text
ATM-Simulator-with-DB/
│
├── ATM_Simulator.java
└── README.md
```

## Database Design

The application uses two database tables.

### 1. Accounts Table

Stores account information.

| Column | Description |
|---|---|
| account_number | Unique account identifier |
| account_holder | Account holder's name |
| pin_hash | Hash of the account PIN |
| balance | Current account balance |

### 2. Transactions Table

Stores deposit and withdrawal records.

| Column | Description |
|---|---|
| transaction_id | Unique transaction identifier |
| account_number | Associated account number |
| transaction_type | Deposit or withdrawal |
| amount | Transaction amount |
| transaction_time | Date and time of transaction |

A foreign key connects the transactions table to the accounts table.

## Database Setup

### Step 1: Install MySQL

Install MySQL Server and ensure that it is running.

### Step 2: Create the Database

Open MySQL Workbench or another MySQL client and execute:

```sql
CREATE DATABASE atm_simulator;
```

The application creates the `accounts` and `transactions` tables automatically when it starts, provided the configured MySQL user has the required permissions.

## Configure Database Connectivity

Open `ATM_Simulator.java` and update the database connection settings:

```java
private static final String DB_URL =
    "jdbc:mysql://localhost:3306/atm_simulator";

private static final String DB_USER = "root";

private static final String DB_PASSWORD =
    "YOUR_MYSQL_PASSWORD";
```

Replace `YOUR_MYSQL_PASSWORD` with your local MySQL password.

**Security note:** Never upload a real database password to a public GitHub repository. For a safer implementation, load credentials from environment variables or a secrets manager.

## Install the JDBC Driver

Download MySQL Connector/J from the official website:

https://dev.mysql.com/downloads/connector/j/

Add the downloaded JAR file to your Java project's classpath or IDE dependencies.

## How to Run

1. Install the Java Development Kit (JDK 17 or later recommended).
2. Install and start MySQL Server.
3. Create the `atm_simulator` database.
4. Configure the database connection settings in `ATM_Simulator.java`.
5. Add MySQL Connector/J to the project's dependencies.
6. Open `ATM_Simulator.java` in IntelliJ IDEA or another Java IDE.
7. Run the `ATM_Simulator` class.
8. Create an account and log in to access the ATM menu.

## Application Menu

```text
========== ATM SIMULATOR ==========
1. Create account
2. Log in
3. Exit
```

After logging in, users can choose from:

```text
========== ACCOUNT MENU ==========
1. Check balance
2. Deposit
3. Withdraw
4. Transaction history
5. Log out
```

## Sample Workflow

1. Create an account with an account number and PIN.
2. Log in using the account credentials.
3. Check the account balance.
4. Deposit or withdraw money.
5. View the transaction history.
6. Log out or exit the application.

Account details and transaction records remain in the database after the program closes.

## Learning Outcomes

This project demonstrates practical knowledge of:

- Java classes, objects, and encapsulation
- JDBC database connectivity
- SQL table creation and data manipulation
- Prepared statements and result sets
- Exception handling
- Account balance validation
- Database transactions and rollback
- Persistent data storage

## Limitations

This is an educational project, not a production banking application. It does not connect to a real bank or process actual financial transactions.

The demo uses basic SHA-256 hashing for PIN storage, which is **not suitable for production authentication**. A real system should use a purpose-built password/PIN hashing method with a unique salt, rate limiting, secure credential storage, and additional security controls. The application also uses Java `double` for monetary input before converting values to `BigDecimal`; a production implementation should handle monetary values using `BigDecimal` throughout.

## Future Enhancements

- Add multiple account types.
- Implement account lockout and login attempt limits.
- Add fund transfers between accounts.
- Use secure PIN hashing and environment-based credentials.
- Use `BigDecimal` throughout for monetary calculations.
- Add automated unit and integration tests.
- Develop a graphical user interface.
- Add administrator functionality and account management.

## License

This project is intended for educational and learning purposes.
