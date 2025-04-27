import java.sql.*;
import java.time.LocalDate;
import java.util.Scanner;

public class DemoJdbc {

    // ============ Configuration ============
    static final String URL = "jdbc:postgresql://localhost:5432/YOUR_DATABASE_NAME"; // TODO: replace when running
    static final String USERNAME = "YOUR_USERNAME"; // TODO: replace when running
    static final String PASSWORD = "YOUR_PASSWORD"; // TODO: replace when running
    // =========================================

    public static void main(String[] args) {
        Connection con = null;
        Scanner scanner = new Scanner(System.in);

        try {
            Class.forName("org.postgresql.Driver");
            con = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            System.out.println("\n✅ Connected to the database.\n");

            boolean running = true;

            while (running) {
                showMenu();
                int choice = getIntInput(scanner, "Enter your choice: ");

                switch (choice) {
                    case 1 -> createTable(con);
                    case 2 -> insertOrder(con, scanner);
                    case 3 -> updateOrder(con, scanner);
                    case 4 -> readOrders(con);
                    case 5 -> deleteOrder(con, scanner);
                    case 6 -> searchOrderByCustomerId(con, scanner);
                    case 7 -> {
                        running = false;
                        System.out.println("Exiting program...");
                    }
                    default -> System.out.println("❌ Invalid choice. Please enter a number between 1 and 7.");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            closeConnection(con);
            scanner.close();
        }
    }

    // ================= Menu and Helpers =================
    static void showMenu() {
        System.out.println("""
                ================== MENU ==================
                [1] Create Orders Table
                [2] Insert New Order
                [3] Update Order Status
                [4] Display All Orders
                [5] Delete an Order
                [6] Search Orders by Customer ID
                [7] Exit
                =============================================
                """);
    }

    static int getIntInput(Scanner scanner, String message) {
        int num = -1;
        while (true) {
            try {
                System.out.print(message);
                num = Integer.parseInt(scanner.nextLine());
                break;
            } catch (NumberFormatException e) {
                System.out.println("❌ Invalid input. Please enter a valid number.");
            }
        }
        return num;
    }

    static void closeConnection(Connection con) {
        if (con != null) {
            try {
                con.close();
                System.out.println("\n✅ Database connection closed.");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    // ================= CRUD Functions =================
    static void createTable(Connection con) throws SQLException {
        String sql = """
                CREATE TABLE IF NOT EXISTS Orders (
                    OrderID INT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
                    CustomerID INT NOT NULL,
                    OrderDate DATE NOT NULL,
                    TotalAmount DECIMAL(10, 2) NOT NULL,
                    OrderStatus VARCHAR(50) DEFAULT 'Pending',
                    ShippingAddress TEXT,
                    CreatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                    UpdatedAt TIMESTAMP DEFAULT CURRENT_TIMESTAMP
                )
                """;
        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.executeUpdate();
            System.out.println("✅ Orders table created or already exists.");
        }
    }

    static void insertOrder(Connection con, Scanner scanner) throws SQLException {
        int customerId = getIntInput(scanner, "Enter Customer ID: ");
        System.out.print("Enter Order Status: ");
        String status = scanner.nextLine();
        double totalAmount = Double.parseDouble(scanner.nextLine());
        System.out.print("Enter Shipping Address: ");
        String address = scanner.nextLine();

        String sql = """
                INSERT INTO Orders (CustomerID, OrderDate, TotalAmount, OrderStatus, ShippingAddress)
                VALUES (?, ?, ?, ?, ?)
                """;
        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setInt(1, customerId);
            pstmt.setDate(2, Date.valueOf(LocalDate.now()));
            pstmt.setBigDecimal(3, new java.math.BigDecimal(totalAmount));
            pstmt.setString(4, status);
            pstmt.setString(5, address);

            int rows = pstmt.executeUpdate();
            System.out.println("✅ " + rows + " order(s) inserted successfully.");
        }
    }

    static void updateOrder(Connection con, Scanner scanner) throws SQLException {
        int customerId = getIntInput(scanner, "Enter Customer ID to update: ");
        System.out.print("Enter New Order Status: ");
        String newStatus = scanner.nextLine();

        String sql = """
                UPDATE Orders
                SET OrderStatus = ?
                WHERE CustomerID = ?
                """;
        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setString(1, newStatus);
            pstmt.setInt(2, customerId);

            int rows = pstmt.executeUpdate();
            System.out.println("✅ " + rows + " order(s) updated successfully.");
        }
    }

    static void readOrders(Connection con) throws SQLException {
        String sql = "SELECT * FROM Orders";
        try (PreparedStatement pstmt = con.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {

            System.out.println("\n================== Orders ==================");
            while (rs.next()) {
                System.out.printf(
                        "OrderID: %-5d | CustomerID: %-5d | Date: %-10s | Amount: %-10.2f | Status: %-10s | Address: %s%n",
                        rs.getInt("OrderID"),
                        rs.getInt("CustomerID"),
                        rs.getDate("OrderDate"),
                        rs.getBigDecimal("TotalAmount"),
                        rs.getString("OrderStatus"),
                        rs.getString("ShippingAddress")
                );
            }
            System.out.println("=============================================\n");
        }
    }

    static void deleteOrder(Connection con, Scanner scanner) throws SQLException {
        int orderId = getIntInput(scanner, "Enter Order ID to delete: ");

        String sql = "DELETE FROM Orders WHERE OrderID = ?";
        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setInt(1, orderId);

            int rows = pstmt.executeUpdate();
            System.out.println("✅ " + rows + " order(s) deleted successfully.");
        }
    }

    static void searchOrderByCustomerId(Connection con, Scanner scanner) throws SQLException {
        int customerId = getIntInput(scanner, "Enter Customer ID to search: ");

        String sql = "SELECT * FROM Orders WHERE CustomerID = ?";
        try (PreparedStatement pstmt = con.prepareStatement(sql)) {
            pstmt.setInt(1, customerId);

            try (ResultSet rs = pstmt.executeQuery()) {
                System.out.println("\n========== Orders for Customer ID " + customerId + " ==========");
                boolean found = false;
                while (rs.next()) {
                    found = true;
                    System.out.printf(
                            "OrderID: %-5d | Date: %-10s | Amount: %-10.2f | Status: %-10s | Address: %s%n",
                            rs.getInt("OrderID"),
                            rs.getDate("OrderDate"),
                            rs.getBigDecimal("TotalAmount"),
                            rs.getString("OrderStatus"),
                            rs.getString("ShippingAddress")
                    );
                }
                if (!found) {
                    System.out.println("❗ No orders found for Customer ID: " + customerId);
                }
                System.out.println("================================================\n");
            }
        }
    }
}
