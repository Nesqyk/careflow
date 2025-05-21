package edu.careflow.repository.entities;

import java.util.ArrayList;
import java.util.List;

public class Allergy {
    private final int allergyId;
    private final int patientId;
    private final String allergen;
    private final String severity;
    private final String comment;
    private final int appointmentId;
    private List<String> reactions; // Populated from allergy_reactions


    private List<String> SeverityList = new ArrayList<>();

    // to these
    // Constructor, getters, setters, and addReaction()

    public Allergy(int allergyId, int patientId, String allergen, String severity, String comment, int appointmentId) {
        this.allergyId = allergyId;
        this.patientId = patientId;
        this.allergen = allergen;
        this.severity = severity;
        this.comment = comment;
        this.appointmentId = appointmentId;
    }

    public int getAllergyId() { return allergyId; }
    public int getPatientId() { return patientId; }
    public String getAllergen() { return allergen; }
    public String getSeverity() { return severity; }
    public String getComment() { return comment; }
    public List<String> getReactions() { return reactions; }
    public int getAppointmentId() { return appointmentId; }

    public void addReaction(String reaction) {
        if (reactions == null) {
            reactions = new ArrayList<>();
        }
        reactions.add(reaction);
    }

}