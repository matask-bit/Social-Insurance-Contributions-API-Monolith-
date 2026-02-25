package lt.example.insurance.employer;

import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lt.example.insurance.employer.dto.EmployerCreateRequest;
import lt.example.insurance.employer.dto.EmployerResponse;
import lt.example.insurance.employer.dto.EmployerUpdateRequest;
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
@RequestMapping("/api/v1/employers")
@Tag(name = "Employers", description = "Employer management")
public class EmployerController {

    private final EmployerService employerService;

    public EmployerController(EmployerService employerService) {
        this.employerService = employerService;
    }

    @PostMapping
    @Operation(summary = "Create a new employer")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Employer created"),
            @ApiResponse(responseCode = "400", description = "Validation failed"),
            @ApiResponse(responseCode = "409", description = "Employer with company code already exists")
    })
    public ResponseEntity<EmployerResponse> create(@Valid @RequestBody EmployerCreateRequest request) {
        EmployerResponse created = employerService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get employer by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Employer found"),
            @ApiResponse(responseCode = "404", description = "Employer not found")
    })
    public EmployerResponse getById(@PathVariable UUID id) {
        return employerService.getById(id);
    }

    @GetMapping
    @Operation(summary = "Search employers by name")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Search completed")
    })
    public List<EmployerResponse> search(@RequestParam(name = "name", required = false) String name) {
        return employerService.search(name);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update an existing employer")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Employer updated"),
            @ApiResponse(responseCode = "400", description = "Validation failed"),
            @ApiResponse(responseCode = "404", description = "Employer not found")
    })
    public EmployerResponse update(@PathVariable UUID id,
                                   @Valid @RequestBody EmployerUpdateRequest request) {
        return employerService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete an employer")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Employer deleted"),
            @ApiResponse(responseCode = "404", description = "Employer not found")
    })
    public void delete(@PathVariable UUID id) {
        employerService.delete(id);
    }
}

