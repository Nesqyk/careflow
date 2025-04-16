package edu.careflow.repository.entities;

import java.time.LocalDate;

public class Medication {
    private String name;          // e.g., "Lisinopril"
    private String dosage;        // e.g., "10mg"
    private String frequency;     // e.g., "Once daily"
    private String route;         // e.g., "Oral"
    private int refillsLeft;      // e.g., 2
    private LocalDate startDate;
    private LocalDate endDate;
    private String purpose;       // e.g., "For hypertension"
    // Getters and setters...
    private int patientId;

    public Medication(String name, String dosage, String frequency, String route, int refillsLeft, LocalDate startDate, LocalDate endDate, String purpose, int patientId) {
        this.name = name;
        this.dosage = dosage;
        this.frequency = frequency;
        this.route = route;
        this.refillsLeft = refillsLeft;
        this.startDate = startDate;
        this.endDate = endDate;
        this.purpose = purpose;
        this.patientId = patientId;
    }

    public String getName() {
        return name;
    }


}