package office;

import db.DatabaseHelper;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

public class Tests {

    private static final DatabaseHelper.ResultSetMapper<Employee> EMPLOYEE_MAPPER = rs ->
            new Employee(rs.getInt("id"), rs.getString("name"), rs.getInt("departmentid"));
    private static final DatabaseHelper.ResultSetMapper<Department> DEPARTMENT_MAPPER = rs ->
            new Department(rs.getInt("id"), rs.getString("name"));

    @Test
    void testConnection() throws SQLException {
        try (Connection connection = DatabaseHelper.getConnection()) {
            System.out.println("Connected to database");

            DatabaseMetaData metaData = connection.getMetaData();
            System.out.println("DB: " + metaData.getDatabaseProductName() + " " + metaData.getDatabaseProductVersion());
            System.out.println("URL: " + metaData.getURL());
            System.out.println("Driver: " + metaData.getDriverName());
        } catch (SQLException e) {
            System.out.println("Connection failed: " + e.getMessage());
        }
    }

    /**
     * 1.
     *
     * 1. Найдите ID сотрудника с именем Ann.
     * 2. Если такой сотрудник только один, то установите его департамент в HR.
     */
    @Test
    void searchAnn() throws SQLException {
        String name = "Ann";

        List<Employee> employees = findEmployeeByName(name);

        if (employees.isEmpty()) {
            System.out.println("Не найден сотрудник с именем: " + name);
            return;
        }

        if (employees.size() > 1) {
            System.out.println("Найдено несколько сотрудников с именем " + name + ":");
            employees.forEach(e -> System.out.println(" - id: " + e.getEmployeeId()));
        }

        // 1. Найдите ID сотрудника с именем Ann.
        Employee ann = employees.getFirst();
        System.out.printf("Найден сотрудник %s с id - %d\n", name, ann.getEmployeeId());

        // 2. Если такой сотрудник только один, то установите его департамент в HR.
        Department hr = findDepartmentByName("HR");
        if (hr == null) {
            System.out.println("Департмент не найден");
            return;
        }

        String query = """
                UPDATE employee
                SET departmentId = ?
                WHERE id = ?
                """;

        int rowsUpdated = DatabaseHelper.executeUpdate(query, hr.getDepartmentID(), ann.getEmployeeId());
        System.out.println(rowsUpdated > 0
                ? "Сотрудник переведен в департамент HR."
                : "Не удалось обновить департамент для сотрудника.");
    }

    /**
     * 2.
     *
     * 1. Проверьте имена всех сотрудников.
     * 2. Если чьё-то имя написано с маленькой буквы, исправьте её на большую.
     * 3. Выведите на экран количество исправленных имён.
     */
    @Test
    void checkAllEmployeesNames() throws SQLException {
        List<Employee> employees = findAllEmployees();
        int count = 0;

        for (Employee employee : employees) {
            String name = employee.getName();
            if (name != null && !name.isEmpty() && Character.isLowerCase(name.charAt(0))) {
                System.out.println("Сотрудник, у которого имя написано с маленькой буквы: " + name);
                String correctedName = Character.toUpperCase(name.charAt(0)) + name.substring(1);

                employee.setName(correctedName);

                String query = """
                        UPDATE employee
                        SET name = ?
                        WHERE id = ?
                        """;
                int rowsUpdated = DatabaseHelper.executeUpdate(query, correctedName, employee.getEmployeeId());
                if (rowsUpdated > 0) {
                    count++;
                }
            }
        }
        System.out.println("Количество исправленных имён: " + count);

//        employees.forEach(System.out::println);
    }

    /**
     * 3.
     *
     * 1. Выведите на экран количество сотрудников в IT-отделе
     */
    @Test
    void countAllEmployeesInItDepartment() throws SQLException {
        List<Employee> employees = findEmployeesInDepartment("IT");

        if (employees.isEmpty()) {
            System.out.println("Департамент не найден.");
            return;
        }

        long count = employees.stream().filter(Objects::nonNull).count();
        System.out.println("Сотрудников в IT отделе: " + count);
    }


    private static List<Employee> findEmployeeByName(String name) throws SQLException {
        String query = """
                SELECT id, name, departmentid
                FROM employee
                WHERE name = ?""";
        return DatabaseHelper.executeQuery(query, EMPLOYEE_MAPPER, name);

    }

    private static Employee findEmployeeById(int id) throws SQLException {
        String query = """
                SELECT id, name, departmentid
                FROM employee
                WHERE id = ?""";
        return DatabaseHelper.executeQuerySingle(query, EMPLOYEE_MAPPER, id);

    }

    private static List<Employee> findAllEmployees() throws SQLException {
        String query = """
                SELECT id, name, departmentid
                FROM employee""";
        return DatabaseHelper.executeQuery(query, EMPLOYEE_MAPPER);

    }

    private static Department findDepartmentById(int id) throws SQLException {
        String query = """
                SELECT id, name
                FROM department
                WHERE id = ?""";
        return DatabaseHelper.executeQuerySingle(query, DEPARTMENT_MAPPER, id);

    }

    private static Department findDepartmentByName(String name) throws SQLException {
        String query = """
                SELECT id, name
                FROM department
                WHERE name = ?""";
        return DatabaseHelper.executeQuerySingle(query, DEPARTMENT_MAPPER, name);

    }

    private static List<Employee> findEmployeesInDepartment(String department) throws SQLException {
        String query = """
                SELECT id, name, departmentid
                FROM employee
                WHERE departmentId in (SELECT id FROM department WHERE name = ?)""";
        return DatabaseHelper.executeQuery(query, EMPLOYEE_MAPPER, department);

    }
}
