import java.sql.*;
import java.util.Scanner;

public class ATM_Simulator {
    private static final Scanner SC = new Scanner(System.in);

    // Update these values for your local MySQL setup.
    private static final String DB_URL = "jdbc:mysql://localhost:3306/atm_simulator";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "YOUR_MYSQL_PASSWORD";

    public static void main(String[] args) {
        try (Connection connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD)) {
            System.out.println("Connected to ATM Simulator database.");
            ensureTablesExist(connection);
            runAtm(connection);
        } catch (SQLException e) {
            System.out.println("Could not connect to the database.");
            System.out.println("Check that MySQL is running, the database exists,");
            System.out.println("and the JDBC driver and connection settings are correct.");
            System.out.println("Details: " + e.getMessage());
        } finally {
            SC.close();
        }
    }

    private static void ensureTablesExist(Connection connection) throws SQLException {
        String accountsSql = """
            CREATE TABLE IF NOT EXISTS accounts (
                account_number VARCHAR(20) PRIMARY KEY,
                account_holder VARCHAR(100) NOT NULL,
                pin_hash CHAR(64) NOT NULL,
                balance DECIMAL(12,2) NOT NULL DEFAULT 0.00,
                CHECK (balance >= 0)
            )
            """;

        String transactionsSql = """
            CREATE TABLE IF NOT EXISTS transactions (
                transaction_id BIGINT PRIMARY KEY AUTO_INCREMENT,
                account_number VARCHAR(20) NOT NULL,
                transaction_type VARCHAR(20) NOT NULL,
                amount DECIMAL(12,2) NOT NULL,
                transaction_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                FOREIGN KEY (account_number) REFERENCES accounts(account_number)
            )
            """;

        try (Statement statement = connection.createStatement()) {
            statement.execute(accountsSql);
            statement.execute(transactionsSql);
        }
    }

    private static void runAtm(Connection connection) throws SQLException {
        while (true) {
            System.out.println("\n========== ATM SIMULATOR ==========");
            System.out.println("1. Create account");
            System.out.println("2. Log in");
            System.out.println("3. Exit");
            int choice = readInt("Choose an option: ");

            switch (choice) {
                case 1 -> createAccount(connection);
                case 2 -> {
                    String accountNumber = login(connection);
                    if (accountNumber != null) {
                        accountMenu(connection, accountNumber);
                    }
                }
                case 3 -> {
                    System.out.println("Thank you for using ATM Simulator.");
                    return;
                }
                default -> System.out.println("Invalid choice. Try again.");
            }
        }
    }

    private static void createAccount(Connection connection) throws SQLException {
        System.out.println("\n--- Create Account ---");
        String accountNumber = readNonEmpty("Choose an account number: ");
        String name = readNonEmpty("Enter account holder name: ");
        String pin = readPin("Create a 4-digit PIN: ");
        double openingBalance = readAmount("Enter opening balance (0 or more): ", true);

        String sql = "INSERT INTO accounts (account_number, account_holder, pin_hash, balance) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, accountNumber);
            ps.setString(2, name);
            ps.setString(3, hashPin(pin));
            ps.setBigDecimal(4, java.math.BigDecimal.valueOf(openingBalance).setScale(2, java.math.RoundingMode.HALF_UP));
            ps.executeUpdate();
            System.out.println("Account created successfully. Keep your account number and PIN private.");
        } catch (SQLIntegrityConstraintViolationException e) {
            System.out.println("That account number already exists. Choose another one.");
        }
    }

    private static String login(Connection connection) throws SQLException {
        System.out.println("\n--- Log In ---");
        String accountNumber = readNonEmpty("Account number: ");
        String pin = readPin("4-digit PIN: ");

        String sql = "SELECT pin_hash FROM accounts WHERE account_number = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, accountNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getString("pin_hash").equals(hashPin(pin))) {
                    System.out.println("Login successful.");
                    return accountNumber;
                }
            }
        }

        System.out.println("Invalid account number or PIN.");
        return null;
    }

    private static void accountMenu(Connection connection, String accountNumber) throws SQLException {
        while (true) {
            System.out.println("\n========== ACCOUNT MENU ==========");
            System.out.println("1. Check balance");
            System.out.println("2. Deposit");
            System.out.println("3. Withdraw");
            System.out.println("4. Transaction history");
            System.out.println("5. Log out");
            int choice = readInt("Choose an option: ");

            switch (choice) {
                case 1 -> showBalance(connection, accountNumber);
                case 2 -> deposit(connection, accountNumber);
                case 3 -> withdraw(connection, accountNumber);
                case 4 -> showTransactions(connection, accountNumber);
                case 5 -> {
                    System.out.println("Logged out.");
                    return;
                }
                default -> System.out.println("Invalid choice. Try again.");
            }
        }
    }

    private static void showBalance(Connection connection, String accountNumber) throws SQLException {
        String sql = "SELECT balance FROM accounts WHERE account_number = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, accountNumber);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    System.out.printf("Available balance: %.2f%n", rs.getBigDecimal("balance"));
                }
            }
        }
    }

    private static void deposit(Connection connection, String accountNumber) throws SQLException {
        double amount = readAmount("Deposit amount: ", false);
        connection.setAutoCommit(false);
        try {
            updateBalance(connection, accountNumber, amount, true);
            addTransaction(connection, accountNumber, "DEPOSIT", amount);
            connection.commit();
            System.out.println("Deposit successful.");
            showBalance(connection, accountNumber);
        } catch (SQLException | RuntimeException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    private static void withdraw(Connection connection, String accountNumber) throws SQLException {
        double amount = readAmount("Withdrawal amount: ", false);
        connection.setAutoCommit(false);
        try {
            String sql = "UPDATE accounts SET balance = balance - ? WHERE account_number = ? AND balance >= ?";
            try (PreparedStatement ps = connection.prepareStatement(sql)) {
                java.math.BigDecimal value = java.math.BigDecimal.valueOf(amount).setScale(2, java.math.RoundingMode.HALF_UP);
                ps.setBigDecimal(1, value);
                ps.setString(2, accountNumber);
                ps.setBigDecimal(3, value);
                int updated = ps.executeUpdate();
                if (updated == 0) {
                    connection.rollback();
                    System.out.println("Withdrawal declined: insufficient balance or account not found.");
                    return;
                }
            }

            addTransaction(connection, accountNumber, "WITHDRAWAL", amount);
            connection.commit();
            System.out.println("Withdrawal successful.");
            showBalance(connection, accountNumber);
        } catch (SQLException | RuntimeException e) {
            connection.rollback();
            throw e;
        } finally {
            connection.setAutoCommit(true);
        }
    }

    private static void updateBalance(Connection connection, String accountNumber, double amount, boolean add) throws SQLException {
        String operator = add ? "+" : "-";
        String sql = "UPDATE accounts SET balance = balance " + operator + " ? WHERE account_number = ?";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setBigDecimal(1, java.math.BigDecimal.valueOf(amount).setScale(2, java.math.RoundingMode.HALF_UP));
            ps.setString(2, accountNumber);
            if (ps.executeUpdate() != 1) {
                throw new SQLException("Account was not found.");
            }
        }
    }

    private static void addTransaction(Connection connection, String accountNumber, String type, double amount) throws SQLException {
        String sql = "INSERT INTO transactions (account_number, transaction_type, amount) VALUES (?, ?, ?)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, accountNumber);
            ps.setString(2, type);
            ps.setBigDecimal(3, java.math.BigDecimal.valueOf(amount).setScale(2, java.math.RoundingMode.HALF_UP));
            ps.executeUpdate();
        }
    }

    private static void showTransactions(Connection connection, String accountNumber) throws SQLException {
        String sql = """
            SELECT transaction_type, amount, transaction_time
            FROM transactions
            WHERE account_number = ?
            ORDER BY transaction_time DESC, transaction_id DESC
            """;

        System.out.println("\n--- Transaction History ---");
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, accountNumber);
            try (ResultSet rs = ps.executeQuery()) {
                boolean found = false;
                while (rs.next()) {
                    found = true;
                    System.out.printf("%s | %.2f | %s%n",
                            rs.getString("transaction_type"),
                            rs.getBigDecimal("amount"),
                            rs.getTimestamp("transaction_time"));
                }
                if (!found) {
                    System.out.println("No transactions yet.");
                }
            }
        }
    }

    private static String readPin(String prompt) {
        while (true) {
            String pin = readNonEmpty(prompt);
            if (pin.matches("\\d{4}")) {
                return pin;
            }
            System.out.println("PIN must contain exactly 4 digits.");
        }
    }

    private static double readAmount(String prompt, boolean allowZero) {
        while (true) {
            System.out.print(prompt);
            try {
                double amount = Double.parseDouble(SC.nextLine().trim());
                if (Double.isFinite(amount) && (allowZero ? amount >= 0 : amount > 0)) {
                    return amount;
                }
            } catch (NumberFormatException ignored) {
                // Show the common validation message below.
            }
            System.out.println(allowZero
                    ? "Enter a valid amount that is zero or greater."
                    : "Enter a valid amount greater than zero.");
        }
    }

    private static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return Integer.parseInt(SC.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("Please enter a whole number.");
            }
        }
    }

    private static String readNonEmpty(String prompt) {
        while (true) {
            System.out.print(prompt);
            String value = SC.nextLine().trim();
            if (!value.isEmpty()) {
                return value;
            }
            System.out.println("This field cannot be empty.");
        }
    }

    // Demo-only hashing. For production systems, use a password/PIN hashing
    // algorithm designed for credentials, with a unique salt and rate limiting.
    private static String hashPin(String pin) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(pin.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder result = new StringBuilder();
            for (byte b : bytes) {
                result.append(String.format("%02x", b));
            }
            return result.toString();
        } catch (java.security.NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is unavailable.", e);
        }
    }
}
