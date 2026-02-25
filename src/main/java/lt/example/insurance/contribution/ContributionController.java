package lt.example.insurance.contribution;

import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lt.example.insurance.contribution.dto.ContributionCreateRequest;
import lt.example.insurance.contribution.dto.ContributionResponse;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Contributions", description = "Contribution management and queries")
public class ContributionController {

    private final ContributionService contributionService;

    public ContributionController(ContributionService contributionService) {
        this.contributionService = contributionService;
    }

    @PostMapping("/contributions")
    @Operation(summary = "Create a new contribution")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Contribution created"),
            @ApiResponse(responseCode = "400", description = "Validation failed"),
            @ApiResponse(responseCode = "404", description = "Citizen or employer not found"),
            @ApiResponse(responseCode = "409", description = "Contribution already exists for citizen, employer and month")
    })
    public ResponseEntity<ContributionResponse> create(@Valid @RequestBody ContributionCreateRequest request) {
        ContributionResponse created = contributionService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/contributions")
    @Operation(summary = "List contributions with optional citizen filter and pagination")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Contributions returned"),
            @ApiResponse(responseCode = "404", description = "Citizen not found (when citizenId is provided)")
    })
    public Page<ContributionResponse> list(
            @RequestParam(name = "citizenId", required = false) UUID citizenId,
            @PageableDefault(page = 0, size = 20) Pageable pageable) {
        return contributionService.findPage(citizenId, pageable);
    }

    @GetMapping("/contributions/{id}")
    @Operation(summary = "Get contribution by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Contribution found"),
            @ApiResponse(responseCode = "404", description = "Contribution not found")
    })
    public ContributionResponse getById(@PathVariable UUID id) {
        return contributionService.getById(id);
    }

    @GetMapping("/citizens/{citizenId}/contributions")
    @Operation(summary = "Get contributions for a citizen in a date range")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Contributions returned"),
            @ApiResponse(responseCode = "404", description = "Citizen not found")
    })
    public List<ContributionResponse> getByCitizenAndPeriod(
            @PathVariable UUID citizenId,
            @RequestParam("from") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam("to") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return contributionService.findByCitizenAndPeriod(citizenId, from, to);
    }

    @DeleteMapping("/contributions/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a contribution")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Contribution deleted"),
            @ApiResponse(responseCode = "404", description = "Contribution not found")
    })
    public void delete(@PathVariable UUID id) {
        contributionService.delete(id);
    }
}

