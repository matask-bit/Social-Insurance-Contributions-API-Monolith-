package lt.example.insurance.contribution;

import lt.example.insurance.citizen.CitizenEntity;
import lt.example.insurance.citizen.CitizenRepository;
import lt.example.insurance.citizen.dto.CitizenEligibilityResponse;
import lt.example.insurance.common.exception.BadRequestException;
import lt.example.insurance.common.exception.ConflictException;
import lt.example.insurance.common.exception.NotFoundException;
import lt.example.insurance.contribution.dto.ContributionCreateRequest;
import lt.example.insurance.contribution.dto.ContributionResponse;
import lt.example.insurance.employer.EmployerEntity;
import lt.example.insurance.employer.EmployerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class ContributionService {

    private final ContributionRepository contributionRepository;
    private final CitizenRepository citizenRepository;
    private final EmployerRepository employerRepository;
    private final Clock clock;

    public ContributionService(ContributionRepository contributionRepository,
                               CitizenRepository citizenRepository,
                               EmployerRepository employerRepository,
                               Clock clock) {
        this.contributionRepository = contributionRepository;
        this.citizenRepository = citizenRepository;
        this.employerRepository = employerRepository;
        this.clock = clock;
    }

    @Transactional
    public ContributionResponse create(ContributionCreateRequest request) {
        UUID citizenId = request.getCitizenId();
        UUID employerId = request.getEmployerId();

        CitizenEntity citizen = citizenRepository.findById(citizenId)
                .orElseThrow(() -> new NotFoundException("Citizen not found with id: " + citizenId));
        EmployerEntity employer = employerRepository.findById(employerId)
                .orElseThrow(() -> new NotFoundException("Employer not found with id: " + employerId));

        if (contributionRepository.existsByCitizen_IdAndEmployer_IdAndMonthDate(citizenId, employerId, request.getMonthDate())) {
            throw new ConflictException("Contribution already exists for citizen, employer and month");
        }

        ContributionEntity entity = new ContributionEntity();
        entity.setCitizen(citizen);
        entity.setEmployer(employer);
        entity.setMonthDate(request.getMonthDate());
        entity.setAmount(request.getAmount());
        String normalizedCurrency = request.getCurrency().trim().toUpperCase(Locale.ROOT);
        entity.setCurrency(normalizedCurrency);
        entity.setPaidAt(request.getPaidAt());

        ContributionEntity saved = contributionRepository.save(entity);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public ContributionResponse getById(UUID id) {
        ContributionEntity entity = contributionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Contribution not found with id: " + id));
        return toResponse(entity);
    }

    @Transactional(readOnly = true)
    public List<ContributionResponse> findByCitizenAndPeriod(UUID citizenId, LocalDate from, LocalDate to) {
        // Ensure citizen exists to return 404 if not
        citizenRepository.findById(citizenId)
                .orElseThrow(() -> new NotFoundException("Citizen not found with id: " + citizenId));

        List<ContributionEntity> entities = contributionRepository
                .findByCitizen_IdAndMonthDateBetween(citizenId, from, to);

        return entities.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public Page<ContributionResponse> findPage(UUID citizenId, Pageable pageable) {
        if (citizenId != null) {
            citizenRepository.findById(citizenId)
                    .orElseThrow(() -> new NotFoundException("Citizen not found with id: " + citizenId));
            return contributionRepository.findByCitizen_Id(citizenId, pageable).map(this::toResponse);
        }
        return contributionRepository.findAll(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public CitizenEligibilityResponse calculateEligibility(UUID citizenId, int monthsBack, int minMonthsPaid) {
        if (monthsBack < 1 || minMonthsPaid < 1) {
            throw new BadRequestException("monthsBack and minMonthsPaid must be at least 1");
        }
        if (minMonthsPaid > monthsBack) {
            throw new BadRequestException("minMonthsPaid cannot be greater than monthsBack");
        }

        citizenRepository.findById(citizenId)
                .orElseThrow(() -> new NotFoundException("Citizen not found with id: " + citizenId));

        LocalDate now = LocalDate.now(clock);
        LocalDate windowTo = now.withDayOfMonth(1);
        LocalDate windowFrom = windowTo.minusMonths(monthsBack - 1L);

        long distinctMonths = contributionRepository.countDistinctPaidMonths(citizenId, windowFrom, windowTo);

        CitizenEligibilityResponse response = new CitizenEligibilityResponse();
        response.setCitizenId(citizenId);
        response.setWindowFrom(windowFrom);
        response.setWindowTo(windowTo);
        response.setMonthsWithPayments((int) distinctMonths);
        response.setRequiredMonths(minMonthsPaid);
        response.setEligible(distinctMonths >= minMonthsPaid);
        return response;
    }

    @Transactional
    public void delete(UUID id) {
        ContributionEntity entity = contributionRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Contribution not found with id: " + id));
        contributionRepository.delete(entity);
    }

    private ContributionResponse toResponse(ContributionEntity entity) {
        ContributionResponse response = new ContributionResponse();
        response.setId(entity.getId());
        response.setMonthDate(entity.getMonthDate());
        response.setAmount(entity.getAmount());
        response.setCurrency(entity.getCurrency());
        response.setPaidAt(entity.getPaidAt());
        response.setCreatedAt(entity.getCreatedAt());
        response.setUpdatedAt(entity.getUpdatedAt());

        ContributionResponse.CitizenSummary citizenSummary = new ContributionResponse.CitizenSummary();
        citizenSummary.setId(entity.getCitizen().getId());
        citizenSummary.setPersonalCode(entity.getCitizen().getPersonalCode());
        citizenSummary.setFirstName(entity.getCitizen().getFirstName());
        citizenSummary.setLastName(entity.getCitizen().getLastName());
        response.setCitizen(citizenSummary);

        ContributionResponse.EmployerSummary employerSummary = new ContributionResponse.EmployerSummary();
        employerSummary.setId(entity.getEmployer().getId());
        employerSummary.setCompanyCode(entity.getEmployer().getCompanyCode());
        employerSummary.setName(entity.getEmployer().getName());
        response.setEmployer(employerSummary);

        return response;
    }
}

