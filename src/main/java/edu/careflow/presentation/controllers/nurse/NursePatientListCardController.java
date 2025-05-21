package edu.careflow.presentation.controllers.nurse;

import com.dlsc.gemsfx.AvatarView;
import edu.careflow.repository.dao.AppointmentDAO;
import edu.careflow.repository.dao.PatientDAO;
import edu.careflow.repository.entities.Appointment;
import edu.careflow.repository.entities.Patient;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class NursePatientListCardController {
    @FXML private AvatarView patientAvatar;
    @FXML private Label patientNameLabel;
    @FXML private Label patientIdLabel;
    @FXML private Label lastVisitDateLabel;
    @FXML private Label visitCountLabel;
    @FXML private Button viewProfileBtn;
    @FXML private Button addNoteBtn;
    @FXML private Button historyBtn;

    private static final PatientDAO patientDAO = new PatientDAO();
    private static final AppointmentDAO appointmentDAO = new AppointmentDAO();
    private Patient currentPatient;

    public void initializeData(Patient patient) {
        this.currentPatient = patient;
        patientNameLabel.setText(patient.getFirstName() + " " + patient.getLastName());
        patientIdLabel.setText("ID: " + patient.getPatientId());
        updateVisitInformation(patient.getPatientId());
        loadAvatar(patient);
        // Button actions (stubbed)
        viewProfileBtn.setOnAction(e -> handleViewProfile());
        addNoteBtn.setOnAction(e -> handleAddNote());
        historyBtn.setOnAction(e -> handleHistory());
    }

    private void updateVisitInformation(int patientId) {
        try {
            List<Appointment> appointments = appointmentDAO.getAppointmentsByPatientId(patientId);
            long completedVisits = appointments.stream()
                .filter(apt -> "Completed".equalsIgnoreCase(apt.getStatus()))
                .count();
            visitCountLabel.setText(String.valueOf(completedVisits));
            LocalDateTime lastVisit = appointments.stream()
                .filter(apt -> "Completed".equalsIgnoreCase(apt.getStatus()))
                .map(Appointment::getAppointmentDate)
                .max(LocalDateTime::compareTo)
                .orElse(null);
            if (lastVisit != null) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
                lastVisitDateLabel.setText("Last Visit: " + lastVisit.format(formatter));
            } else {
                lastVisitDateLabel.setText("No previous visits");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            lastVisitDateLabel.setText("Error loading visit data");
            visitCountLabel.setText("0");
        }
    }

    private void loadAvatar(Patient patient) {
        try {
            Image avatar = patientDAO.loadAvatar(patient.getPatientId());
            if (avatar != null) {
                patientAvatar.setImage(avatar);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void handleViewProfile() {
        // TODO: Implement view profile logic (e.g., show overlay)
    }

    private void handleAddNote() {
        // TODO: Implement add note logic (e.g., show dialog)
    }

    private void handleHistory() {
        // TODO: Implement history logic (e.g., show overlay)
    }
} 