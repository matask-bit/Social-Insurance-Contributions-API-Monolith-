package lt.example.insurance.citizen;

import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lt.example.insurance.citizen.dto.CitizenCreateRequest;
import lt.example.insurance.citizen.dto.CitizenEligibilityResponse;
import lt.example.insurance.citizen.dto.CitizenResponse;
import lt.example.insurance.citizen.dto.CitizenUpdateRequest;
import lt.example.insurance.contribution.ContributionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/citizens")
@Tag(name = "Citizens", description = "Citizen management and eligibility")
public class CitizenController {

    private final CitizenService citizenService;
    private final ContributionService contributionService;

    public CitizenController(CitizenService citizenService, ContributionService contributionService) {
        this.citizenService = citizenService;
        this.contributionService = contributionService;
    }

    @PostMapping
    @Operation(summary = "Create a new citizen")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Citizen created"),
            @ApiResponse(responseCode = "400", description = "Validation failed"),
            @ApiResponse(responseCode = "409", description = "Citizen with personal code already exists")
    })
    public ResponseEntity<CitizenResponse> create(@Valid @RequestBody CitizenCreateRequest request) {
        CitizenResponse created = citizenService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get citizen by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Citizen found"),
            @ApiResponse(responseCode = "404", description = "Citizen not found")
    })
    public CitizenResponse getById(@PathVariable UUID id) {
        return citizenService.getById(id);
    }

    @GetMapping
    @Operation(summary = "Search citizens by last name")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Search completed")
    })
    public List<CitizenResponse> search(@RequestParam(name = "lastName", required = false) String lastName) {
        return citizenService.search(lastName);
    }

    @GetMapping("/{citizenId}/eligibility")
    @Operation(summary = "Check contribution-based eligibility for a citizen")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Eligibility calculated"),
            @ApiResponse(responseCode = "400", description = "Invalid parameters"),
            @ApiResponse(responseCode = "404", description = "Citizen not found")
    })
    public CitizenEligibilityResponse getEligibility(
            @PathVariable UUID citizenId,
            @RequestParam(name = "monthsBack", defaultValue = "6") int monthsBack,
            @RequestParam(name = "minMonthsPaid", defaultValue = "3") int minMonthsPaid) {
        return contributionService.calculateEligibility(citizenId, monthsBack, minMonthsPaid);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing citizen")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Citizen updated"),
            @ApiResponse(responseCode = "400", description = "Validation failed"),
            @ApiResponse(responseCode = "404", description = "Citizen not found")
    })
    public CitizenResponse update(@PathVariable UUID id,
                                  @Valid @RequestBody CitizenUpdateRequest request) {
        return citizenService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a citizen")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Citizen deleted"),
            @ApiResponse(responseCode = "404", description = "Citizen not found")
    })
    public void delete(@PathVariable UUID id) {
        citizenService.delete(id);
    }
}

