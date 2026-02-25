package lt.example.insurance.citizen;

import lt.example.insurance.citizen.dto.CitizenCreateRequest;
import lt.example.insurance.citizen.dto.CitizenResponse;
import lt.example.insurance.citizen.dto.CitizenUpdateRequest;
import lt.example.insurance.common.exception.ConflictException;
import lt.example.insurance.common.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class CitizenService {

    private final CitizenRepository citizenRepository;

    public CitizenService(CitizenRepository citizenRepository) {
        this.citizenRepository = citizenRepository;
    }

    @Transactional
    public CitizenResponse create(CitizenCreateRequest request) {
        if (citizenRepository.existsByPersonalCode(request.getPersonalCode())) {
            throw new ConflictException("Citizen with personal code already exists: " + request.getPersonalCode());
        }

        CitizenEntity entity = new CitizenEntity();
        entity.setPersonalCode(request.getPersonalCode());
        entity.setFirstName(request.getFirstName());
        entity.setLastName(request.getLastName());
        entity.setDateOfBirth(request.getDateOfBirth());

        CitizenEntity saved = citizenRepository.save(entity);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public CitizenResponse getById(UUID id) {
        CitizenEntity entity = citizenRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Citizen not found with id: " + id));
        return toResponse(entity);
    }

    @Transactional(readOnly = true)
    public List<CitizenResponse> search(String lastName) {
        List<CitizenEntity> entities;
        if (lastName == null || lastName.isBlank()) {
            entities = citizenRepository.findAll();
        } else {
            entities = citizenRepository.findByLastNameContainingIgnoreCase(lastName);
        }
        return entities.stream().map(this::toResponse).toList();
    }

    @Transactional
    public CitizenResponse update(UUID id, CitizenUpdateRequest request) {
        CitizenEntity entity = citizenRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Citizen not found with id: " + id));

        entity.setFirstName(request.getFirstName());
        entity.setLastName(request.getLastName());
        entity.setDateOfBirth(request.getDateOfBirth());

        CitizenEntity saved = citizenRepository.save(entity);
        return toResponse(saved);
    }

    @Transactional
    public void delete(UUID id) {
        CitizenEntity entity = citizenRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Citizen not found with id: " + id));
        citizenRepository.delete(entity);
    }

    private CitizenResponse toResponse(CitizenEntity entity) {
        CitizenResponse response = new CitizenResponse();
        response.setId(entity.getId());
        response.setPersonalCode(entity.getPersonalCode());
        response.setFirstName(entity.getFirstName());
        response.setLastName(entity.getLastName());
        response.setDateOfBirth(entity.getDateOfBirth());
        response.setCreatedAt(entity.getCreatedAt());
        response.setUpdatedAt(entity.getUpdatedAt());
        return response;
    }
}

