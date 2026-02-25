package lt.example.insurance;

import lt.example.insurance.citizen.dto.CitizenCreateRequest;
import lt.example.insurance.citizen.dto.CitizenEligibilityResponse;
import lt.example.insurance.citizen.dto.CitizenResponse;
import lt.example.insurance.common.api.ErrorResponse;
import lt.example.insurance.contribution.dto.ContributionCreateRequest;
import lt.example.insurance.contribution.dto.ContributionResponse;
import lt.example.insurance.employer.dto.EmployerCreateRequest;
import lt.example.insurance.employer.dto.EmployerResponse;
import lt.example.insurance.testsupport.DatabaseCleaner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Tag("integration")
class ApiIntegrationIT {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private DatabaseCleaner databaseCleaner;

    @BeforeEach
    void cleanDatabase() {
        databaseCleaner.clean();
    }

    @Test
    void createCitizenThenGetById() {
        CitizenCreateRequest createRequest = new CitizenCreateRequest();
        createRequest.setPersonalCode("98765432109");
        createRequest.setFirstName("Alice");
        createRequest.setLastName("Smith");
        createRequest.setDateOfBirth(LocalDate.of(1995, 5, 10));

        ResponseEntity<CitizenResponse> createResponse =
                restTemplate.postForEntity("/api/v1/citizens", createRequest, CitizenResponse.class);

        assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(createResponse.getBody()).isNotNull();
        UUID id = createResponse.getBody().getId();

        ResponseEntity<CitizenResponse> getResponse =
                restTemplate.getForEntity("/api/v1/citizens/" + id, CitizenResponse.class);

        assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResponse.getBody()).isNotNull();
        assertThat(getResponse.getBody().getPersonalCode()).isEqualTo(createRequest.getPersonalCode());
    }

    @Test
    void duplicateContributionForSameCitizenEmployerAndMonthReturnsConflict() {
        CitizenResponse citizen = createCitizen("12345678901");
        EmployerResponse employer = createEmployer("EMP999");

        LocalDate monthDate = LocalDate.now().withDayOfMonth(1);

        ContributionCreateRequest first = new ContributionCreateRequest();
        first.setCitizenId(citizen.getId());
        first.setEmployerId(employer.getId());
        first.setMonthDate(monthDate);
        first.setAmount(BigDecimal.valueOf(100));
        first.setCurrency("EUR");
        first.setPaidAt(Instant.now());

        ResponseEntity<ContributionResponse> firstResponse =
                restTemplate.postForEntity("/api/v1/contributions", first, ContributionResponse.class);

        assertThat(firstResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        ContributionCreateRequest duplicate = new ContributionCreateRequest();
        duplicate.setCitizenId(citizen.getId());
        duplicate.setEmployerId(employer.getId());
        duplicate.setMonthDate(monthDate);
        duplicate.setAmount(BigDecimal.valueOf(100));
        duplicate.setCurrency("EUR");
        duplicate.setPaidAt(Instant.now());

        ResponseEntity<ErrorResponse> duplicateResponse =
                restTemplate.postForEntity("/api/v1/contributions", duplicate, ErrorResponse.class);

        assertThat(duplicateResponse.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(duplicateResponse.getBody()).isNotNull();
        ErrorResponse error = duplicateResponse.getBody();
        assertThat(error.getStatus()).isEqualTo(409);
        assertThat(error.getError()).isEqualTo("Conflict");
        assertThat(error.getMessage()).contains("Contribution already exists");
    }

    @Test
    void eligibilityReflectsPaidMonthsOnly() {
        CitizenResponse citizen = createCitizen("55555555555");
        EmployerResponse employer = createEmployer("EMP777");

        UUID citizenId = citizen.getId();

        LocalDate currentMonth = LocalDate.now().withDayOfMonth(1);

        // three paid contributions in the last 6 months (including current)
        for (int i = 0; i < 3; i++) {
            LocalDate monthDate = currentMonth.minusMonths(i);
            ContributionCreateRequest req = new ContributionCreateRequest();
            req.setCitizenId(citizenId);
            req.setEmployerId(employer.getId());
            req.setMonthDate(monthDate);
            req.setAmount(BigDecimal.valueOf(100));
            req.setCurrency("EUR");
            req.setPaidAt(Instant.now());
            restTemplate.postForEntity("/api/v1/contributions", req, ContributionResponse.class);
        }

        // future month, unpaid – should not affect monthsWithPayments
        ContributionCreateRequest future = new ContributionCreateRequest();
        future.setCitizenId(citizenId);
        future.setEmployerId(employer.getId());
        future.setMonthDate(currentMonth.plusMonths(1));
        future.setAmount(BigDecimal.valueOf(100));
        future.setCurrency("EUR");
        future.setPaidAt(null);
        restTemplate.postForEntity("/api/v1/contributions", future, ContributionResponse.class);

        String url = String.format("/api/v1/citizens/%s/eligibility?monthsBack=6&minMonthsPaid=3", citizenId);
        ResponseEntity<CitizenEligibilityResponse> response =
                restTemplate.getForEntity(url, CitizenEligibilityResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        CitizenEligibilityResponse body = response.getBody();

        assertThat(body.isEligible()).isTrue();
        assertThat(body.getMonthsWithPayments()).isEqualTo(3);
        assertThat(body.getRequiredMonths()).isEqualTo(3);
    }

    @Test
    void eligibilityRequestWithMoreRequiredMonthsThanWindowReturnsBadRequest() {
        CitizenResponse citizen = createCitizen("66666666666");

        UUID citizenId = citizen.getId();

        String url = String.format("/api/v1/citizens/%s/eligibility?monthsBack=3&minMonthsPaid=4", citizenId);
        ResponseEntity<ErrorResponse> response =
                restTemplate.getForEntity(url, ErrorResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        ErrorResponse error = response.getBody();
        assertThat(error.getStatus()).isEqualTo(400);
        assertThat(error.getMessage()).contains("minMonthsPaid cannot be greater than monthsBack");
    }

    @Test
    void contributionsListSupportsPagination() {
        CitizenResponse citizen = createCitizen("77777777777");
        EmployerResponse employer = createEmployer("EMP001");

        UUID citizenId = citizen.getId();
        LocalDate baseMonth = LocalDate.now().withDayOfMonth(1);

        for (int i = 0; i < 5; i++) {
            ContributionCreateRequest req = new ContributionCreateRequest();
            req.setCitizenId(citizenId);
            req.setEmployerId(employer.getId());
            req.setMonthDate(baseMonth.minusMonths(i));
            req.setAmount(BigDecimal.valueOf(100 + i));
            req.setCurrency("EUR");
            req.setPaidAt(Instant.now());
            restTemplate.postForEntity("/api/v1/contributions", req, ContributionResponse.class);
        }

        ResponseEntity<Map> page0 = restTemplate.getForEntity(
                "/api/v1/contributions?page=0&size=2", Map.class);
        assertThat(page0.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(page0.getBody()).isNotNull();

        List<?> content0 = (List<?>) page0.getBody().get("content");
        assertThat(content0).hasSize(2);
        assertThat(page0.getBody().get("totalElements")).isIn(List.of(5, 5L));
        assertThat(page0.getBody().get("totalPages")).isEqualTo(3);
        assertThat(page0.getBody().get("size")).isEqualTo(2);
        assertThat(page0.getBody().get("number")).isEqualTo(0);

        ResponseEntity<Map> page1 = restTemplate.getForEntity(
                "/api/v1/contributions?page=1&size=2", Map.class);
        assertThat(page1.getStatusCode()).isEqualTo(HttpStatus.OK);
        List<?> content1 = (List<?>) page1.getBody().get("content");
        assertThat(content1).hasSize(2);
        assertThat(page1.getBody().get("number")).isEqualTo(1);

        ResponseEntity<Map> filtered = restTemplate.getForEntity(
                "/api/v1/contributions?citizenId=" + citizenId + "&page=0&size=10", Map.class);
        assertThat(filtered.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(filtered.getBody().get("totalElements")).isIn(List.of(5, 5L));
        assertThat((List<?>) filtered.getBody().get("content")).hasSize(5);
    }

    private CitizenResponse createCitizen(String personalCode) {
        CitizenCreateRequest request = new CitizenCreateRequest();
        request.setPersonalCode(personalCode);
        request.setFirstName("Test");
        request.setLastName("Citizen");
        request.setDateOfBirth(LocalDate.of(1990, 1, 1));

        ResponseEntity<CitizenResponse> response =
                restTemplate.postForEntity("/api/v1/citizens", request, CitizenResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        return response.getBody();
    }

    private EmployerResponse createEmployer(String companyCode) {
        EmployerCreateRequest request = new EmployerCreateRequest();
        request.setCompanyCode(companyCode);
        request.setName("Test Employer");

        ResponseEntity<EmployerResponse> response =
                restTemplate.postForEntity("/api/v1/employers", request, EmployerResponse.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        return response.getBody();
    }
}

