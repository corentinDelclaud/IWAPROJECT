package iwaproject.transaction.repository;
import iwaproject.transaction.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;


public interface EmployeeRepository extends JpaRepository<Employee, Integer> {}
