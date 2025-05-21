package edu.careflow.repository.entities;

import java.time.LocalDateTime;

public class Vitals {

    private int patientId;

    private int vitalsId;

    private int nurseId;

    private String bloodPressure;

    private int heartRate;

    private int respiratoryRate;

    private  double weightKg;

    private final double heightCm;

    private double temperature;

    private double oxygenSaturation;

    private LocalDateTime recordedAt;

    private LocalDateTime updatedAt;

    private int appointmentId;



    public Vitals(int patientId, int vitalsId, int nurseId, String bloodPressure, int heartRate, int respiratoryRate, double weightKg, double heightCm, double temperature, double oxygenSaturation, LocalDateTime recordedAt,  LocalDateTime updatedAt, int appointmentId) {
        this.patientId = patientId;
        this.vitalsId = vitalsId;
        this.nurseId = nurseId;
        this.bloodPressure = bloodPressure;
        this.heartRate = heartRate;
        this.respiratoryRate = respiratoryRate;
        this.weightKg = weightKg;
        this.heightCm = heightCm;
        this.temperature = temperature;
        this.oxygenSaturation = oxygenSaturation;
        this.recordedAt = recordedAt;
        this.updatedAt = updatedAt;
        this.appointmentId = appointmentId;
    }

    public double getHeightCm() {
        return heightCm;
    }

    public int getPatientId() {
        return patientId;
    }

    public int getVitalsId() {
        return vitalsId;
    }

    public int getNurseId() {
        return nurseId;
    }

    public String getBloodPressure() {
        return bloodPressure;
    }

    public int getHeartRate() {
        return heartRate;
    }

    public int getRespiratoryRate() {
        return respiratoryRate;
    }

    public double getWeightKg() {
        return weightKg;
    }

    public double getTemperature() {
        return temperature;
    }

    public double getOxygenSaturation() {
        return oxygenSaturation;
    }

    public LocalDateTime getRecordedAt() {
        return recordedAt;
    }

    public LocalDateTime getUpdateAt() { return updatedAt;}

    public void setPatientId(int patientId) {
        this.patientId = patientId;
    }

    public int getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(int appointmentId) {
        this.appointmentId = appointmentId;
    }

    public void setVitalsId(int vitalsId) {
        this.vitalsId = vitalsId;
    }
}
