package office;

import db.DatabaseHelper;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;

@Slf4j
public class Tests {

    private static final DatabaseHelper.ResultSetMapper<Employee> EMPLOYEE_MAPPER = rs ->
            new Employee(rs.getInt("id"), rs.getString("name"), rs.getInt("departmentid"));
    private static final DatabaseHelper.ResultSetMapper<Department> DEPARTMENT_MAPPER = rs ->
            new Department(rs.getInt("id"), rs.getString("name"));

    private static void deleteDepartment(String name) throws SQLException {
        String query = """
                DELETE FROM department
                WHERE name = ?""";
        DatabaseHelper.executeUpdate(query, name);
    }

    private static void deleteEmployeeByDepartmentId(int id) throws SQLException {
        String query = """
                DELETE FROM employee
                WHERE departmentid = ?""";
        DatabaseHelper.executeUpdate(query, id);
    }

    private static void createDepartment(int id, String name) throws SQLException {
        String query = """
                INSERT INTO department (id, name) VALUES (?, ?)""";
        DatabaseHelper.executeUpdate(query, id, name);
    }

    private static void createEmployee(int id, String name, int departmentId) throws SQLException {
        String query = """
                INSERT INTO employee (id, name, departmentId) VALUES (?, ?, ?)""";
        DatabaseHelper.executeUpdate(query, id, name, departmentId);
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

    private static List<Department> findAllDepartments() throws SQLException {
        String query = """
                SELECT id, name
                FROM department""";
        return DatabaseHelper.executeQuery(query, DEPARTMENT_MAPPER);
    }

    private static int findLastIdInDepartments() throws SQLException {
        String query = """
                SELECT COALESCE(MAX(id), 0) as max_id
                FROM department""";
        return DatabaseHelper.executeQuerySingle(query, rs -> rs.getInt("max_id"));
    }

    private static int findLastIdInEmployees() throws SQLException {
        String query = """
                SELECT COALESCE(MAX(id), 0) as max_id
                FROM employee""";
        return DatabaseHelper.executeQuerySingle(query, rs -> rs.getInt("max_id"));
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

    private static List<Employee> findEmployeesInDepartmentByName(String department) throws SQLException {
        String query = """
                SELECT id, name, departmentid
                FROM employee
                WHERE departmentId in (SELECT id FROM department WHERE name = ?)""";
        return DatabaseHelper.executeQuery(query, EMPLOYEE_MAPPER, department);
    }

    private static List<Employee> findEmployeesInDepartmentById(int id) throws SQLException {
        String query = """
                SELECT id, name, departmentid
                FROM employee
                WHERE departmentId = ?""";
        return DatabaseHelper.executeQuery(query, EMPLOYEE_MAPPER, id);
    }

    /**
     * При удалении отдела (Department)
     * информация о всех сотрудниках, работающих в этом отделе, должна быть удалена
     */
    @Test
    @DisplayName("Задание #2. JDBC Тесты. Удаление отдела.")
    void testCascadeDeleteWithDepartment() throws SQLException {
        String testDepartment = "TestDept";


        // создать тестовый департамент
        Department department = findDepartmentByName(testDepartment);

        if (department == null) {
            int lastId = findLastIdInDepartments();
            createDepartment(lastId + 1, testDepartment);
        }
        Department dp = findDepartmentByName(testDepartment);

        try {
            // создать сотрудника
            int lastEmployeeId = findLastIdInEmployees();
            String[] names = {"Alice", "Bob"};
            int departmentId = dp.getDepartmentID();

            for (String name : names) {
                lastEmployeeId++;
                createEmployee(lastEmployeeId, name, departmentId);
            }

            // проверить кол-во записей
            List<Employee> beforeDelete = findEmployeesInDepartmentByName(dp.getName());
            Assertions.assertThat(beforeDelete).hasSize(2);

            // удалить департамент
            deleteDepartment(dp.getName());

            // проверка, что сотрудники удалились
            List<Employee> afterDelete = findEmployeesInDepartmentById(dp.getDepartmentID());

            Assertions.assertThat(afterDelete)
                    .as("После удаления департамента сотрудники с этим departmentid должны быть удалены")
                    .isEmpty();
        } finally {
            deleteEmployeeByDepartmentId(dp.getDepartmentID());
        }
    }

    @Test
    @DisplayName("Тест коннекта в БД")
    void testConnection() {
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
     * 1. Найдите ID сотрудника с именем Ann.
     * 2. Если такой сотрудник только один, то установите его департамент в HR.
     */
    @Test
    @DisplayName("Задание 1")
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
     * 1. Проверьте имена всех сотрудников.
     * 2. Если чьё-то имя написано с маленькой буквы, исправьте её на большую.
     * 3. Выведите на экран количество исправленных имён.
     */
    @Test
    @DisplayName("Задание 2")
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
     * 1. Выведите на экран количество сотрудников в IT-отделе
     */
    @Test
    @DisplayName("Задание 3")
    void countAllEmployeesInItDepartment() throws SQLException {
        List<Employee> employees = findEmployeesInDepartmentByName("IT");

        if (employees.isEmpty()) {
            System.out.println("Департамент не найден.");
            return;
        }

        long count = employees.stream().filter(Objects::nonNull).count();
        System.out.println("Сотрудников в IT отделе: " + count);
    }
}
