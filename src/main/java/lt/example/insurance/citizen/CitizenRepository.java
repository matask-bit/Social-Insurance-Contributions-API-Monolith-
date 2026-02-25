package lt.example.insurance.citizen;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CitizenRepository extends JpaRepository<CitizenEntity, UUID> {

    boolean existsByPersonalCode(String personalCode);

    List<CitizenEntity> findByLastNameContainingIgnoreCase(String lastName);
}

