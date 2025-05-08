package edu.careflow.repository.entities;

public class PrescriptionDetails {

    private int detailId;
    private String medicineName;
    private String dosage;
    private String frequency;
    private String duration;

    public PrescriptionDetails(int detailId, String medicineName, String dosage, String frequency, String duration) {
        this.detailId = detailId;
        this.medicineName = medicineName;
        this.dosage = dosage;
        this.frequency = frequency;
        this.duration = duration;
    }

    public int getDetailId() {
        return detailId;
    }

    public String getMedicineName() {
        return medicineName;
    }


    public String getDosage() {
        return dosage;
    }

    public String getFrequency() {
        return frequency;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }
}
