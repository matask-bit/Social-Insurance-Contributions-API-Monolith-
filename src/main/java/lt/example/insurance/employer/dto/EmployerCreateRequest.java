package lt.example.insurance.employer.dto;

import jakarta.validation.constraints.NotBlank;

public class EmployerCreateRequest {

    @NotBlank
    private String companyCode;

    @NotBlank
    private String name;

    public String getCompanyCode() {
        return companyCode;
    }

    public void setCompanyCode(String companyCode) {
        this.companyCode = companyCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

