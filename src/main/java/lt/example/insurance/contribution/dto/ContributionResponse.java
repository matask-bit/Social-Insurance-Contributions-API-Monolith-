package lt.example.insurance.contribution.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public class ContributionResponse {

    private UUID id;
    private LocalDate monthDate;
    private BigDecimal amount;
    private String currency;
    private Instant paidAt;
    private Instant createdAt;
    private Instant updatedAt;
    private CitizenSummary citizen;
    private EmployerSummary employer;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public LocalDate getMonthDate() {
        return monthDate;
    }

    public void setMonthDate(LocalDate monthDate) {
        this.monthDate = monthDate;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Instant getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(Instant paidAt) {
        this.paidAt = paidAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public CitizenSummary getCitizen() {
        return citizen;
    }

    public void setCitizen(CitizenSummary citizen) {
        this.citizen = citizen;
    }

    public EmployerSummary getEmployer() {
        return employer;
    }

    public void setEmployer(EmployerSummary employer) {
        this.employer = employer;
    }

    public static class CitizenSummary {
        private UUID id;
        private String personalCode;
        private String firstName;
        private String lastName;

        public UUID getId() {
            return id;
        }

        public void setId(UUID id) {
            this.id = id;
        }

        public String getPersonalCode() {
            return personalCode;
        }

        public void setPersonalCode(String personalCode) {
            this.personalCode = personalCode;
        }

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }
    }

    public static class EmployerSummary {
        private UUID id;
        private String companyCode;
        private String name;

        public UUID getId() {
            return id;
        }

        public void setId(UUID id) {
            this.id = id;
        }

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
}

