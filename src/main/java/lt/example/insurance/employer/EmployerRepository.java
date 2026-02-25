package lt.example.insurance.employer;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EmployerRepository extends JpaRepository<EmployerEntity, UUID> {

    boolean existsByCompanyCode(String companyCode);

    List<EmployerEntity> findByNameContainingIgnoreCase(String name);
}

