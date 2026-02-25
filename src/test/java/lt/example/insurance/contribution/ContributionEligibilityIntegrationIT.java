package lt.example.insurance.contribution;

import lt.example.insurance.citizen.dto.CitizenEligibilityResponse;
import lt.example.insurance.citizen.dto.CitizenCreateRequest;
import lt.example.insurance.citizen.dto.CitizenResponse;
import lt.example.insurance.citizen.CitizenService;
import lt.example.insurance.contribution.dto.ContributionCreateRequest;
import lt.example.insurance.employer.dto.EmployerCreateRequest;
import lt.example.insurance.employer.dto.EmployerResponse;
import lt.example.insurance.employer.EmployerService;
import lt.example.insurance.testsupport.DatabaseCleaner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@ActiveProfiles("test")
@Tag("integration")
class ContributionEligibilityIntegrationIT {

    @TestConfiguration
    static class TestClockConfig {
        @Bean
        public Clock testClock() {
            return Clock.fixed(Instant.parse("2026-02-15T00:00:00Z"), ZoneOffset.UTC);
        }
    }

    @Autowired
    private ContributionService contributionService;
    @Autowired
    private CitizenService citizenService;
    @Autowired
    private EmployerService employerService;
    @Autowired
    private DatabaseCleaner databaseCleaner;

    @BeforeEach
    void cleanDatabase() {
        databaseCleaner.clean();
    }

    @Test
    void eligibilityCountsDistinctPaidMonthsWithinWindow() {
        CitizenCreateRequest citizenRequest = new CitizenCreateRequest();
        citizenRequest.setPersonalCode("12345678901");
        citizenRequest.setFirstName("John");
        citizenRequest.setLastName("Doe");
        citizenRequest.setDateOfBirth(LocalDate.of(1990, 1, 1));
        CitizenResponse citizen = citizenService.create(citizenRequest);

        EmployerCreateRequest employerRequest = new EmployerCreateRequest();
        employerRequest.setCompanyCode("EMP123");
        employerRequest.setName("Employer");
        EmployerResponse employer = employerService.create(employerRequest);

        UUID citizenId = citizen.getId();

        // Fixed clock is 2026-02-15, so windowTo is 2026-02-01.
        // For monthsBack=3, windowFrom is 2025-12-01.
        createContribution(citizenId, employer.getId(), LocalDate.of(2025, 12, 1),
                BigDecimal.valueOf(100), "EUR", Instant.parse("2026-01-01T00:00:00Z"));

        createContribution(citizenId, employer.getId(), LocalDate.of(2026, 1, 1),
                BigDecimal.valueOf(100), "EUR", Instant.parse("2026-02-01T00:00:00Z"));

        createContribution(citizenId, employer.getId(), LocalDate.of(2026, 2, 1),
                BigDecimal.valueOf(100), "EUR", Instant.parse("2026-03-01T00:00:00Z"));

        // unpaid contribution should be ignored
        createContribution(citizenId, employer.getId(), LocalDate.of(2025, 11, 1),
                BigDecimal.valueOf(100), "EUR", null);

        CitizenEligibilityResponse eligibleResponse =
                contributionService.calculateEligibility(citizenId, 3, 3);

        assertThat(eligibleResponse.isEligible()).isTrue();
        assertThat(eligibleResponse.getMonthsWithPayments()).isEqualTo(3);
        assertThat(eligibleResponse.getRequiredMonths()).isEqualTo(3);
        assertThat(eligibleResponse.getWindowTo()).isEqualTo(LocalDate.of(2026, 2, 1));
        assertThat(eligibleResponse.getWindowFrom()).isEqualTo(LocalDate.of(2025, 12, 1));

        assertThatThrownBy(() -> contributionService.calculateEligibility(citizenId, 3, 4))
                .isInstanceOf(lt.example.insurance.common.exception.BadRequestException.class)
                .hasMessageContaining("minMonthsPaid cannot be greater than monthsBack");
    }

    private void createContribution(UUID citizenId,
                                    UUID employerId,
                                    LocalDate monthDate,
                                    BigDecimal amount,
                                    String currency,
                                    Instant paidAt) {
        ContributionCreateRequest request = new ContributionCreateRequest();
        request.setCitizenId(citizenId);
        request.setEmployerId(employerId);
        request.setMonthDate(monthDate);
        request.setAmount(amount);
        request.setCurrency(currency);
        request.setPaidAt(paidAt);

        contributionService.create(request);
    }
}

