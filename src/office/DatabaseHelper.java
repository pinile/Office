package office;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Добавить в зависимости проекта jar: ./lib/h2-1.4.199.jar
 */
public class DatabaseHelper {
    private static final String URL = "jdbc:h2:tcp://localhost:9092/./Office";
    private static final String USER = "";
    private static final String PASSWORD = "";

    // Получение соединения
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    // Выполнениe универсального запроса
    public static <T> List<T> executeQuery(String query, ResultSetMapper<T> mapper, Object... params) throws SQLException {
        List<T> results = new ArrayList<>();
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {

            setParams(ps, params);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    results.add(mapper.map(rs));
                }
            }
        }
        return results;
    }

    // Выполнение запроса для одного результата
    public static <T> T executeQuerySingle(String query, ResultSetMapper<T> mapper, Object... params) throws SQLException {
        List<T> results = executeQuery(query, mapper, params);
        return results.isEmpty() ? null : results.getFirst();
    }

    // Выполнение update/insert/delete
    public static int executeUpdate(String query, Object... params) throws SQLException {
        try (Connection con = getConnection();
             PreparedStatement ps = con.prepareStatement(query)) {
            setParams(ps, params);
            return ps.executeUpdate();
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

    // Установка параметров в PreparedStatement
    private static void setParams(PreparedStatement ps, Object... params) throws SQLException {
        IntStream.range(0, params.length).forEach(i -> {
            try {
                ps.setObject(i + 1, params[i]);
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }


    // Интерфейс для маппинга ResultSet в объекты
    public interface ResultSetMapper<T> {
        T map(ResultSet rs) throws SQLException;
    }
}
