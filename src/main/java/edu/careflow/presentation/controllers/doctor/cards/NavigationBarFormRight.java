package edu.careflow.presentation.controllers.doctor.cards;

import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class NavigationBarFormRight {

    @FXML
    private Button addAllergyBtn;

    @FXML
    private Button addConditionBtn;

    @FXML
    private Button addPrescriptionBtn;

    @FXML
    private Button addVitalsBtn;

    @FXML
    private Button visitNoteBtn;

    private int patientId;
    private int doctorId;

    @FXML
    public void initialize() {
        addAllergyBtn.setOnAction(event -> handleAllergyForm());
        addConditionBtn.setOnAction(event -> handleConditionForm());
        addPrescriptionBtn.setOnAction(event -> handlePrescriptionForm());
        addVitalsBtn.setOnAction(event -> handleVitalsForm());
        visitNoteBtn.setOnAction(event -> handleVisitNoteForm());
    }

    public void handleAllergyForm() {

    }

    public void handleConditionForm() {

    }

    public void handlePrescriptionForm() {

    }

    public void handleVitalsForm() {

    }

    public void handleVisitNoteForm() {

    }

    public void setPatientId(int patientId) {
        this.patientId = patientId;
    }

    public void setDoctorId(int doctorId) {
        this.doctorId = doctorId;
    }

    public int getPatientId() {
        return patientId;
    }

    public int getDoctorId() {
        return doctorId;
    }
}
