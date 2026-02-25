package lt.example.insurance.employer.dto;

import jakarta.validation.constraints.NotBlank;

public class EmployerUpdateRequest {

    @NotBlank
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

