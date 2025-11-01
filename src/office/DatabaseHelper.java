package office;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Добавить в зависимости проекта jar: ./lib/h2-1.4.199.jar
 */
public class DatabaseHelper {
    private static final String H2_URL = "jdbc:h2:tcp://localhost:9092/./Office";
    private static final String H2_USER = "";
    private static final String H2_PASSWORD = "";

    // Получение соединения
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(H2_URL, H2_USER, H2_PASSWORD);
    }

    // Выполнение запроса
    public static <T> List<T> executeQuery(String query, ResultSetMapper<T> mapper, Object... params) throws SQLException {
        List<T> results = new ArrayList<>();
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            IntStream.range(0, params.length).forEach(i -> {
                try {
                    ps.setObject(i + 1, params[i]);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(mapper.map(rs));
                }
            }
        }
        return results;
    }

    // Выполнение update/insert/delete
    public static int executeUpdate(String query, Object... params) throws SQLException {
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            IntStream.range(0, params.length).forEach(i -> {
                try {
                    ps.setObject(i + 1, params[i]);
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            });

            return ps.executeUpdate(query);
        }
    }

    // Закрытие ресурсов
    public static void closeResources(AutoCloseable... resources) {
        for (AutoCloseable resource : resources) {
            if (resource != null) {
                try {
                    resource.close();
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                }
            }
        }
    }

    // Интерфейс для маппинга ResultSet в объекты
    public interface ResultSetMapper<T> {
        T map(ResultSet rs) throws SQLException;
    }
}
