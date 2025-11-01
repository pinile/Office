package office;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Чтобы работало добавить в зависимости проекта jar: ./lib/h2-1.4.199.jar
 * */
public class OfficeDataBaseTester {
    private static final String H2_URL = "jdbc:h2:tcp://localhost:9092/./Office";
    private static final String H2_USER = "";
    private static final String H2_PASSWORD = "";

    public static void main(String[] args) throws SQLException {
        testConnection();
    }

        private static void testConnection() throws SQLException {

        /*
        // явное добавление драйвера
        try {
            Class.forName("org.h2.Driver");
        } catch (ClassNotFoundException e) {
            System.out.println("Driver not found: " + e.getMessage());
            return;
        }
        */

        try (Connection connection = DriverManager.getConnection(H2_URL, H2_USER, H2_PASSWORD)) {
            System.out.println("Connected to database");

            DatabaseMetaData metaData = connection.getMetaData();
            System.out.println("DB: " + metaData.getDatabaseProductName() + " " + metaData.getDatabaseProductVersion());
            System.out.println("URL: " + metaData.getURL());
            System.out.println("Driver: " + metaData.getDriverName());
        } catch (SQLException e) {
            System.out.println("Connection failed: " + e.getMessage());
        }
    }



}
