package jdbcat.ktor.example

import io.ktor.application.Application
import jdbcat.core.sqlNames
import jdbcat.core.sqlTemplate
import jdbcat.core.sqlValues
import jdbcat.core.tx
import jdbcat.core.txRequired
import jdbcat.ktor.example.db.dao.DepartmentDao
import jdbcat.ktor.example.db.dao.EmployeeDao
import jdbcat.ktor.example.db.model.Department
import jdbcat.ktor.example.db.model.Departments
import jdbcat.ktor.example.db.model.Employee
import jdbcat.ktor.example.db.model.Employees
import kotlinx.coroutines.experimental.runBlocking
import org.koin.ktor.ext.inject
import org.koin.ktor.ext.installKoin
import org.koin.log.Logger.SLF4JLogger
import java.util.Date
import javax.sql.DataSource

fun Application.bootstrap() {

    // Add Koin DI support for Ktor
    installKoin(
        listOf(appModule),
        logger = SLF4JLogger())

    // Create database tables (if needed)
    bootstrapDatabase()
}

fun Application.bootstrapDatabase() = runBlocking {
    val dataSource by inject<DataSource>()
    val departmentDao by inject<DepartmentDao>()
    val employeeDao by inject<EmployeeDao>()

    // Comment out if you don't want for tables to be recreated and reinitialized with dummy data
    // on every start
    dataSource.tx {
        employeeDao.dropTableIfExists()
        departmentDao.dropTableIfExists()
        departmentDao.createTableIfNotExists()
        employeeDao.createTableIfNotExists()
        createInitialDepartments(dataSource = dataSource)
        createInitialEmployees(dataSource = dataSource)
    }
}

private suspend fun createInitialDepartments(dataSource: DataSource) {
    val insertDepartmentTemplate = sqlTemplate(Departments) {
        "INSERT INTO $tableName (${columns.sqlNames}) VALUES (${columns.sqlValues})"
    }

    val departments = listOf(
        Department(code = "SEA", name = "Seattle's Office", countryCode = "USA", city = "Seattle",
            comments = "Headquarter and R&D", dateCreated = Date(Date().time - 99999999999L)),
        Department(code = "CHI", name = "Chicago's Office", countryCode = "USA", city = "Chicago",
            comments = "Financial department", dateCreated = Date(Date().time - 77777777777L)),
        Department(code = "BER", name = "Berlin's Office", countryCode = "DEU", city = "Berlin",
            comments = "R&D", dateCreated = Date(Date().time - 55555555555L)),
        Department(code = "AMS", name = "Amsterdam's Office", countryCode = "NLD", city = "Amsterdam",
            comments = "Just for fun :)", dateCreated = Date(Date().time - 33333333333L))
    )
    // TODO Add batch functionality
    dataSource.txRequired { connection ->
        for (department in departments) {
            val stmt = insertDepartmentTemplate.prepareStatement(connection) {
                it[Departments.code] = department.code
                it[Departments.name] = department.name
                it[Departments.countryCode] = department.countryCode
                it[Departments.city] = department.city
                it[Departments.comments] = department.comments
                it[Departments.dateCreated] = department.dateCreated!!
            }
            println("* Add Department SQL: $stmt")
            stmt.executeUpdate()
        }
    }
}

private suspend fun createInitialEmployees(dataSource: DataSource) {
    val insertEmployeeTemplate = sqlTemplate(Employees) {
        "INSERT INTO $tableName (${(columns - id).sqlNames}) VALUES (${(columns - id).sqlValues})"
    }
    val employees = listOf(
        Employee(firstName = "Toly", lastName = "Pochkin", age = 40, departmentCode = "SEA",
            comments = "CEO", dateCreated = Date(Date().time - 89999999999L)),
        Employee(firstName = "Jemmy", lastName = "Hyland", age = 27, departmentCode = "SEA",
            comments = "CPO", dateCreated = Date(Date().time - 79999999999L)),
        Employee(firstName = "Doreen", lastName = "Fosse", age = 35, departmentCode = "CHI",
            comments = "CFO", dateCreated = Date(Date().time - 69999999999L)),
        Employee(firstName = "Brandy", lastName = "Ashworth", age = 39, departmentCode = "BER",
            comments = "Lead engineer", dateCreated = Date(Date().time - 45555555555L)),
        Employee(firstName = "Lenny", lastName = "Matthews", age = 50, departmentCode = "AMS",
            comments = "DJ", dateCreated = Date(Date().time - 25555555555L))
    )
    // TODO Add batch functionality
    dataSource.txRequired { connection ->
        for (employee in employees) {
            val stmt = insertEmployeeTemplate.prepareStatement(connection) {
                it[Employees.firstName] = employee.firstName
                it[Employees.lastName] = employee.lastName
                it[Employees.age] = employee.age
                it[Employees.departmentCode] = employee.departmentCode
                it[Employees.comments] = employee.comments
                it[Employees.dateCreated] = employee.dateCreated!!
            }
            println("* Add Employee SQL: $stmt")
            stmt.executeUpdate()
        }
    }
}
