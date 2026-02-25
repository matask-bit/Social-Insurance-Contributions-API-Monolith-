package lt.example.insurance.citizen.dto;

import java.time.LocalDate;
import java.util.UUID;

public class CitizenEligibilityResponse {

    private UUID citizenId;
    private LocalDate windowFrom;
    private LocalDate windowTo;
    private int monthsWithPayments;
    private int requiredMonths;
    private boolean eligible;

    public UUID getCitizenId() {
        return citizenId;
    }

    public void setCitizenId(UUID citizenId) {
        this.citizenId = citizenId;
    }

    public LocalDate getWindowFrom() {
        return windowFrom;
    }

    public void setWindowFrom(LocalDate windowFrom) {
        this.windowFrom = windowFrom;
    }

    public LocalDate getWindowTo() {
        return windowTo;
    }

    public void setWindowTo(LocalDate windowTo) {
        this.windowTo = windowTo;
    }

    public int getMonthsWithPayments() {
        return monthsWithPayments;
    }

    public void setMonthsWithPayments(int monthsWithPayments) {
        this.monthsWithPayments = monthsWithPayments;
    }

    public int getRequiredMonths() {
        return requiredMonths;
    }

    public void setRequiredMonths(int requiredMonths) {
        this.requiredMonths = requiredMonths;
    }

    public boolean isEligible() {
        return eligible;
    }

    public void setEligible(boolean eligible) {
        this.eligible = eligible;
    }
}

