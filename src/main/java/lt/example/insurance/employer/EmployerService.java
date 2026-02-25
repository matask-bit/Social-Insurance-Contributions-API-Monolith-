package lt.example.insurance.employer;

import lt.example.insurance.employer.dto.EmployerCreateRequest;
import lt.example.insurance.employer.dto.EmployerResponse;
import lt.example.insurance.employer.dto.EmployerUpdateRequest;
import lt.example.insurance.common.exception.ConflictException;
import lt.example.insurance.common.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class EmployerService {

    private final EmployerRepository employerRepository;

    public EmployerService(EmployerRepository employerRepository) {
        this.employerRepository = employerRepository;
    }

    @Transactional
    public EmployerResponse create(EmployerCreateRequest request) {
        if (employerRepository.existsByCompanyCode(request.getCompanyCode())) {
            throw new ConflictException("Employer with company code already exists: " + request.getCompanyCode());
        }

        EmployerEntity entity = new EmployerEntity();
        entity.setCompanyCode(request.getCompanyCode());
        entity.setName(request.getName());

        EmployerEntity saved = employerRepository.save(entity);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public EmployerResponse getById(UUID id) {
        EmployerEntity entity = employerRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Employer not found with id: " + id));
        return toResponse(entity);
    }

    @Transactional(readOnly = true)
    public List<EmployerResponse> search(String name) {
        List<EmployerEntity> entities;
        if (name == null || name.isBlank()) {
            entities = employerRepository.findAll();
        } else {
            entities = employerRepository.findByNameContainingIgnoreCase(name);
        }
        return entities.stream().map(this::toResponse).toList();
    }

    @Transactional
    public EmployerResponse update(UUID id, EmployerUpdateRequest request) {
        EmployerEntity entity = employerRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Employer not found with id: " + id));

        entity.setName(request.getName());

        EmployerEntity saved = employerRepository.save(entity);
        return toResponse(saved);
    }

    @Transactional
    public void delete(UUID id) {
        EmployerEntity entity = employerRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Employer not found with id: " + id));
        employerRepository.delete(entity);
    }

    private EmployerResponse toResponse(EmployerEntity entity) {
        EmployerResponse response = new EmployerResponse();
        response.setId(entity.getId());
        response.setCompanyCode(entity.getCompanyCode());
        response.setName(entity.getName());
        response.setCreatedAt(entity.getCreatedAt());
        response.setUpdatedAt(entity.getUpdatedAt());
        return response;
    }
}

