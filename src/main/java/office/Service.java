package office;

import javax.sql.DataSource;
import java.sql.*;

public class Service {

    private final DataSource dataSource;

    public Service(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public void createDB() {
        try (Connection con = dataSource.getConnection()) {
            Statement stm = con.createStatement();
            stm.executeUpdate("DROP TABLE Department IF EXISTS");
            stm.executeUpdate("CREATE TABLE Department(ID INT PRIMARY KEY, NAME VARCHAR(255))");
            stm.executeUpdate("INSERT INTO Department VALUES(1,'Accounting')");
            stm.executeUpdate("INSERT INTO Department VALUES(2,'IT')");
            stm.executeUpdate("INSERT INTO Department VALUES(3,'HR')");

            stm.executeUpdate("DROP TABLE Employee IF EXISTS");
            stm.executeUpdate("CREATE TABLE Employee(ID INT PRIMARY KEY, NAME VARCHAR(255), DepartmentID INT)");
            stm.executeUpdate("INSERT INTO Employee VALUES(1,'Pete',1)");
            stm.executeUpdate("INSERT INTO Employee VALUES(2,'Ann',1)");

            stm.executeUpdate("INSERT INTO Employee VALUES(3,'Liz',2)");
            stm.executeUpdate("INSERT INTO Employee VALUES(4,'Tom',2)");

            stm.executeUpdate("INSERT INTO Employee VALUES(5,'Todd',3)");

        } catch (SQLException e) {
            System.out.println(e);
        }
    }

    public void addDepartment(Department d) {
        try (Connection con = dataSource.getConnection()) {
            PreparedStatement stm = con.prepareStatement("INSERT INTO Department VALUES(?,?)");
            stm.setInt(1, d.departmentID);
            stm.setString(2, d.getName());
            stm.executeUpdate();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public void removeDepartment(Department d) {
        try (Connection con = dataSource.getConnection()) {
            con.setAutoCommit(false);

            try (PreparedStatement deleteDepartment = con.prepareStatement("DELETE FROM Department WHERE ID=?");
                 PreparedStatement deleteEmployees = con.prepareStatement("DELETE FROM Employee WHERE DepartmentID=?")) {
                deleteEmployees.setInt(1, d.departmentID);
                deleteEmployees.executeUpdate();

                deleteDepartment.setInt(1, d.departmentID);
                deleteDepartment.executeUpdate();

                con.commit();
            } catch (Exception e) {
                con.rollback();
                throw e;
            }
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public void addEmployee(Employee empl) {
        try (Connection con = dataSource.getConnection()) {
            PreparedStatement stm = con.prepareStatement("INSERT INTO Employee VALUES(?,?,?)");
            stm.setInt(1, empl.getEmployeeId());
            stm.setString(2, empl.getName());
            stm.setInt(3, empl.getDepartmentId());
            stm.executeUpdate();
        } catch (Exception e) {
            System.out.println(e);
        }
    }

    public void removeEmployee(Employee empl) {
        try (Connection con = dataSource.getConnection()) {
            PreparedStatement stm = con.prepareStatement("DELETE FROM Employee WHERE ID=?");
            stm.setInt(1, empl.getEmployeeId());
            stm.executeUpdate();
        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
