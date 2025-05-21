package edu.careflow.repository.entities;

import java.time.LocalDate;

public class Condition {

    private final int conditionId;

    private final int patientId;

    private final String conditionName;

    private final String description;

    private final LocalDate onSetDate;

    private final String status;

    private final int appointmentId;


    public Condition(int conditionId, int patientId, String conditionName, String description, LocalDate onSetDate, String status, int appointmentId) {
        this.conditionId = conditionId;
        this.patientId = patientId;
        this.conditionName = conditionName;
        this.description = description;
        this.onSetDate = onSetDate;
        this.status = status;
        this.appointmentId = appointmentId;
    }


    public int getConditionId() {
        return conditionId;
    }

    public int getPatientId() {
        return patientId;
    }

    public String getConditionName() {
        return conditionName;
    }

    public String getDescription() {
        return description;
    }

    public LocalDate getOnSetDate() {
        return onSetDate;
    }

    public String getStatus() {
        return status;
    }

    public int getAppointmentId() {
        return appointmentId;
    }
}
