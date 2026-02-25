package lt.example.insurance.contribution;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public interface ContributionRepository extends JpaRepository<ContributionEntity, UUID> {

    boolean existsByCitizen_IdAndEmployer_IdAndMonthDate(UUID citizenId, UUID employerId, LocalDate monthDate);

    List<ContributionEntity> findByCitizen_IdAndMonthDateBetween(UUID citizenId, LocalDate from, LocalDate to);

    Page<ContributionEntity> findByCitizen_Id(UUID citizenId, Pageable pageable);

    List<ContributionEntity> findByCitizen_IdAndMonthDateBetweenAndPaidAtIsNotNull(UUID citizenId, LocalDate from, LocalDate to);

    @Query("""
            select count(distinct c.monthDate)
            from ContributionEntity c
            where c.citizen.id = :citizenId
              and c.monthDate between :from and :to
              and c.paidAt is not null
            """)
    long countDistinctPaidMonths(
            @Param("citizenId") UUID citizenId,
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);
}

