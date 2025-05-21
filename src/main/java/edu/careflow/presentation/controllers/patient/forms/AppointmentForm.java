package edu.careflow.presentation.controllers.patient.forms;

import edu.careflow.repository.dao.AppointmentDAO;
import edu.careflow.repository.dao.DoctorDAO;
import edu.careflow.repository.dao.NurseDAO;
import edu.careflow.repository.dao.PatientDAO;
import edu.careflow.repository.entities.Appointment;
import edu.careflow.repository.entities.Doctor;
import edu.careflow.repository.entities.Nurse;
import edu.careflow.repository.entities.Patient;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public class AppointmentForm {
    @FXML private VBox formContainer;
    @FXML private Button closeButton;
    @FXML private Button saveBtn;
    @FXML private Button cancelBtn;

    // Basic Information
    @FXML private ComboBox<Patient> patientComboBox;
    @FXML private ComboBox<Doctor> doctorComboBox;
    @FXML private DatePicker datePicker;
    @FXML private TextField timeField;

    // Service Details
    @FXML private ComboBox<String> serviceTypeComboBox;
    @FXML private ComboBox<String> appointmentTypeComboBox;
    @FXML private TextField roomField;
    @FXML private ComboBox<String> statusComboBox;

    // Contact Information
    @FXML private TextField meetingLinkField;
    @FXML private TextField preferredContactField;
    @FXML private TextField bookedByField;
    @FXML private ComboBox<Nurse> nurseComboBox;

    // Medical Details
    @FXML private TextArea symptomsArea;
    @FXML private TextArea diagnosisArea;
    @FXML private TextArea notesArea;

    @FXML private ScrollPane scrollPaneAppointment;

    private AppointmentDAO appointmentDAO;
    private PatientDAO patientDAO;
    private DoctorDAO doctorDAO;
    private NurseDAO nurseDAO;
    private Appointment currentAppointment;
    private Runnable onSaveCallback;

    @FXML
    public void initialize() {
        initializeDAOs();
        setupComboBoxes();
        setupEventHandlers();
        setupValidation();
        // Set up close/cancel button actions
        if (closeButton != null) closeButton.setOnAction(e -> handleClose());
        if (cancelBtn != null) cancelBtn.setOnAction(e -> handleClose());
    }

    private void initializeDAOs() {
        appointmentDAO = new AppointmentDAO();
        patientDAO = new PatientDAO();
        doctorDAO = new DoctorDAO();
        nurseDAO = new NurseDAO();
    }

    private void setupComboBoxes() {
        // Service Types
        serviceTypeComboBox.getItems().addAll(
            "Consultation",
            "Check-up",
            "Follow-up",
            "Emergency",
            "Routine Check"
        );

        // Appointment Types
        appointmentTypeComboBox.getItems().addAll(
            "In-person",
            "Online",
            "Phone"
        );

        // Status Options
        statusComboBox.getItems().addAll(
            "Scheduled",
            "Pending",
            "Completed",
            "Cancelled"
        );

        // Load dynamic data
        loadPatients();
        loadDoctors();
        loadNurses();
    }

    private void loadPatients() {
        try {
            List<Patient> patients = patientDAO.getAllPatients();
            patientComboBox.getItems().addAll(patients);
            patientComboBox.setCellFactory(param -> new ListCell<Patient>() {
                @Override
                protected void updateItem(Patient patient, boolean empty) {
                    super.updateItem(patient, empty);
                    if (empty || patient == null) {
                        setText(null);
                    } else {
                        setText(patient.getFirstName() + " " + patient.getLastName());
                    }
                }
            });
        } catch (SQLException e) {
            showError("Error", "Failed to load patients: " + e.getMessage());
        }
    }

    private void loadDoctors() {
        try {
            List<Doctor> doctors = doctorDAO.getAllDoctors();
            doctorComboBox.getItems().addAll(doctors);
            doctorComboBox.setCellFactory(param -> new ListCell<Doctor>() {
                @Override
                protected void updateItem(Doctor doctor, boolean empty) {
                    super.updateItem(doctor, empty);
                    if (empty || doctor == null) {
                        setText(null);
                    } else {
                        setText("Dr. " + doctor.getFirstName() + " " + doctor.getLastName());
                    }
                }
            });
        } catch (SQLException e) {
            showError("Error", "Failed to load doctors: " + e.getMessage());
        }
    }

    private void loadNurses() {
        try {
            List<Nurse> nurses = nurseDAO.getAllNurses();
            nurseComboBox.getItems().addAll(nurses);
            nurseComboBox.setCellFactory(param -> new ListCell<Nurse>() {
                @Override
                protected void updateItem(Nurse nurse, boolean empty) {
                    super.updateItem(nurse, empty);
                    if (empty || nurse == null) {
                        setText(null);
                    } else {
                        setText(nurse.getFirstName() + " " + nurse.getLastName());
                    }
                }
            });
        } catch (SQLException e) {
            showError("Error", "Failed to load nurses: " + e.getMessage());
        }
    }

    private void setupEventHandlers() {
        closeButton.setOnAction(e -> closeForm());
        cancelBtn.setOnAction(e -> closeForm());
        saveBtn.setOnAction(e -> saveAppointment());

        // Add listeners for appointment type to show/hide meeting link
        appointmentTypeComboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
            meetingLinkField.setVisible("Online".equals(newVal));
        });
    }

    private void setupValidation() {
        // Add validation listeners
        timeField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.isEmpty()) {
                try {
                    LocalTime.parse(newVal, DateTimeFormatter.ofPattern("HH:mm"));
                    timeField.setStyle("");
                } catch (DateTimeParseException e) {
                    timeField.setStyle("-fx-border-color: red;");
                }
            }
        });
    }

    public void setAppointment(Appointment appointment) {
        this.currentAppointment = appointment;
        if (appointment != null) {
            populateFields(appointment);
        }
    }

    public void setOnSaveCallback(Runnable callback) {
        this.onSaveCallback = callback;
    }

    private void populateFields(Appointment appointment) {
        // Basic Information
        System.out.println(appointment);
        patientComboBox.setValue(findPatient(appointment.getPatientId()));
        doctorComboBox.setValue(findDoctor(appointment.getDoctorId()));
        datePicker.setValue(LocalDate.parse(appointment.getAppointmentDate().toLocalDate().toString()));
        timeField.setText(appointment.getAppointmentDate().format(DateTimeFormatter.ofPattern("HH:mm")));

        // Service Details
        serviceTypeComboBox.setValue(appointment.getServiceType());
        appointmentTypeComboBox.setValue(appointment.getAppointmentType());
        roomField.setText(appointment.getRoom());
        statusComboBox.setValue(appointment.getStatus());

        // Contact Information
        meetingLinkField.setText(appointment.getMeetingLink());
        preferredContactField.setText(appointment.getPreferredContact());
        bookedByField.setText(appointment.getBookedBy());
        nurseComboBox.setValue(findNurse(appointment.getNurseId()));

        // Medical Details
        symptomsArea.setText(appointment.getSymptoms());
        diagnosisArea.setText(appointment.getDiagnosis());
        notesArea.setText(appointment.getNotes());
    }

    private Patient findPatient(int patientId) {
        return patientComboBox.getItems().stream()
            .filter(p -> p.getPatientId() == patientId)
            .findFirst()
            .orElse(null);
    }

    private Doctor findDoctor(int doctorId) {
        return doctorComboBox.getItems().stream()
            .filter(d -> d.getDoctorId() == doctorId)
            .findFirst()
            .orElse(null);
    }

    private Nurse findNurse(int nurseId) {
        return nurseComboBox.getItems().stream()
            .filter(n -> n.getNurseId() == nurseId)
            .findFirst()
            .orElse(null);
    }

    private void saveAppointment() {
        if (!validateForm()) {
            return;
        }

        try {
            Appointment appointment = currentAppointment;
            
            // Basic Information
            appointment.setPatientId(patientComboBox.getValue().getPatientId());
            appointment.setDoctorId(doctorComboBox.getValue().getDoctorId());
            appointment.setAppointmentDate(datePicker.getValue().atStartOfDay());

            // Service Details
            appointment.setServiceType(serviceTypeComboBox.getValue());
            appointment.setAppointmentType(appointmentTypeComboBox.getValue());
            appointment.setRoom(roomField.getText());
            appointment.setStatus(statusComboBox.getValue());

            // Contact Information
            appointment.setMeetingLink(meetingLinkField.getText());
            appointment.setPreferredContact(preferredContactField.getText());
            appointment.setBookedBy(bookedByField.getText());
            appointment.setNurseId(nurseComboBox.getValue() != null ? nurseComboBox.getValue().getNurseId() : 0);

            // Medical Details
            appointment.setSymptoms(symptomsArea.getText());
            appointment.setDiagnosis(diagnosisArea.getText());
            appointment.setNotes(notesArea.getText());

            if (currentAppointment == null) {
                appointmentDAO.addAppointment(appointment);
            } else {
                appointmentDAO.updateAppointment(appointment);
            }

            if (onSaveCallback != null) {
                onSaveCallback.run();
            }
            showSuccessLabel("Appointment saved successfully");
            handleClose();

        } catch (SQLException e) {
            showError("Error", "Failed to save appointment: " + e.getMessage());
        }
    }

    private boolean validateForm() {
        StringBuilder errors = new StringBuilder();

        // Basic Information validation
        if (patientComboBox.getValue() == null) errors.append("• Select a patient\n");
        if (doctorComboBox.getValue() == null) errors.append("• Select a doctor\n");
        if (datePicker.getValue() == null) errors.append("• Select a date\n");
        if (!isValidTime(timeField.getText())) errors.append("• Enter a valid time (HH:mm)\n");

        // Service Details validation
        if (serviceTypeComboBox.getValue() == null) errors.append("• Select a service type\n");
        if (appointmentTypeComboBox.getValue() == null) errors.append("• Select an appointment type\n");
        if (roomField.getText().trim().isEmpty()) errors.append("• Enter a room\n");
        if (statusComboBox.getValue() == null) errors.append("• Select a status\n");

        // Contact Information validation
        if (appointmentTypeComboBox.getValue().equals("Online") && meetingLinkField.getText().trim().isEmpty()) {
            errors.append("• Enter a meeting link for online appointments\n");
        }
        if (preferredContactField.getText().trim().isEmpty()) errors.append("• Enter preferred contact\n");
        if (bookedByField.getText().trim().isEmpty()) errors.append("• Enter who booked the appointment\n");

        if (errors.length() > 0) {
            showError("Validation Error", "Please fix the following errors:\n\n" + errors.toString());
            return false;
        }
        return true;
    }

    private boolean isValidTime(String time) {
        try {
            LocalTime.parse(time, DateTimeFormatter.ofPattern("HH:mm"));
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    private void closeForm() {
        Stage stage = (Stage) formContainer.getScene().getWindow();
        stage.close();
    }

    private void showError(String header, String content) {
        if (scrollPaneAppointment.getScene() != null) {
            StackPane container = (StackPane) scrollPaneAppointment.getScene().lookup("#stackPaneContainer");
            if (container != null) {
                Label errorLabel = new Label(header + (content != null && !content.isEmpty() ? ":\n" + content : ""));
                errorLabel.getStyleClass().add("error-label");
                errorLabel.setStyle(
                    "-fx-background-color: #f44336;" +
                    "-fx-text-fill: white;" +
                    "-fx-padding: 10 20;" +
                    "-fx-background-radius: 5;" +
                    "-fx-translate-y: 300; -fx-font-family: 'Gilroy-SemiBold'; -fx-font-size: 16px"
                );

                // Fade in
                FadeTransition fadeIn = new FadeTransition(javafx.util.Duration.millis(300), errorLabel);
                fadeIn.setFromValue(0.0);
                fadeIn.setToValue(1.0);

                container.getChildren().add(errorLabel);
                fadeIn.play();

                // Remove after 2 seconds with fade out
                javafx.animation.Timeline timeline = new javafx.animation.Timeline(
                    new javafx.animation.KeyFrame(javafx.util.Duration.seconds(2), e -> {
                        FadeTransition fadeOut = new FadeTransition(javafx.util.Duration.millis(300), errorLabel);
                        fadeOut.setFromValue(1.0);
                        fadeOut.setToValue(0.0);
                        fadeOut.setOnFinished(event -> container.getChildren().remove(errorLabel));
                        fadeOut.play();
                    })
                );
                timeline.play();
            }
        }
    }

    private void handleClose() {
        FadeTransition fadeOut = new FadeTransition(javafx.util.Duration.millis(150), scrollPaneAppointment);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> {
            if (scrollPaneAppointment.getParent() instanceof VBox parentBox) {
                parentBox.getChildren().remove(scrollPaneAppointment);
            } else if (scrollPaneAppointment.getParent() != null) {
                ((javafx.scene.layout.Pane) scrollPaneAppointment.getParent()).getChildren().remove(scrollPaneAppointment);
            }
        });
        fadeOut.play();
    }

    private void showSuccessLabel(String message) {
        if (scrollPaneAppointment.getScene() != null) {
            StackPane container = (StackPane) scrollPaneAppointment.getScene().lookup("#stackPaneContainer");
            if (container != null) {
                Label successLabel = new Label(message);
                successLabel.getStyleClass().add("success-label");
                successLabel.setStyle(
                    "-fx-background-color: #4CAF50;" +
                    "-fx-text-fill: white;" +
                    "-fx-padding: 10 20;" +
                    "-fx-background-radius: 5;" +
                    "-fx-translate-y: 300; -fx-font-family: 'Gilroy-SemiBold'; -fx-font-size: 16px"
                );

                // Fade in
                FadeTransition fadeIn = new FadeTransition(javafx.util.Duration.millis(300), successLabel);
                fadeIn.setFromValue(0.0);
                fadeIn.setToValue(1.0);

                container.getChildren().add(successLabel);
                fadeIn.play();

                // Remove after 2 seconds with fade out
                javafx.animation.Timeline timeline = new javafx.animation.Timeline(
                    new javafx.animation.KeyFrame(javafx.util.Duration.seconds(2), e -> {
                        FadeTransition fadeOut = new FadeTransition(javafx.util.Duration.millis(300), successLabel);
                        fadeOut.setFromValue(1.0);
                        fadeOut.setToValue(0.0);
                        fadeOut.setOnFinished(event -> container.getChildren().remove(successLabel));
                        fadeOut.play();
                    })
                );
                timeline.play();
            }
        }
    }
} 