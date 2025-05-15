package edu.careflow.presentation.controllers.patient.forms;

import com.dlsc.gemsfx.CalendarPicker;
import edu.careflow.repository.dao.AppointmentDAO;
import edu.careflow.repository.dao.DoctorDAO;
import edu.careflow.repository.entities.Appointment;
import edu.careflow.repository.entities.Doctor;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class BookAptForm {
    @FXML
    private Button bookBtn;
    @FXML private Button cancelBtn;
    @FXML private Button closeBtn;
    @FXML private VBox dateContainer;
    @FXML private ComboBox<Doctor> doctorCombo;
    @FXML private TextField insuranceField;
    @FXML private TextField noteField;
    @FXML private CalendarPicker pickDate;
    @FXML private ComboBox<String> pickTimeCombo;
    @FXML private ComboBox<String> typeServiceCombo;
    @FXML private ToggleGroup bookTypeRadio;
    @FXML private ScrollPane scrollPaneBook;
    @FXML private ToggleGroup prefferedContactRadio;
//    @FXML private RadioButton inPersonRadio;
//    @FXML private RadioButton onlineRadio;
//    @FXML private ComboBox<String> preferredContactCombo;
//    @FXML private TextField meetingLinkField;

    private AppointmentDAO appointmentDAO;
    private DoctorDAO doctorDAO;
    private int currentPatientId;

    @FXML
    public void initialize() {
        appointmentDAO = new AppointmentDAO();
        doctorDAO = new DoctorDAO();

        System.out.println(currentPatientId);
        setupDatePicker();
        setupComboBoxes();
        setupButtons();
        setupListeners();
        setupAppointmentTypeRadios();
    }

    private void setupDatePicker() {
        pickDate.setDateFilter(date -> !date.isBefore(LocalDate.now()) &&
                !date.isAfter(LocalDate.now().plusMonths(2)));
        pickDate.setValue(LocalDate.now());
    }

    private void setupComboBoxes() {
        // Setup time slots (9 AM to 5 PM)
        List<String> timeSlots = IntStream.rangeClosed(9, 16)
                .mapToObj(hour -> String.format("%02d:00", hour))
                .collect(Collectors.toList());
        pickTimeCombo.getItems().setAll(timeSlots);

        // Setup service types
        typeServiceCombo.getItems().addAll(
                "General Checkup",
                "Consultation",
                "Follow-up",
                "Specialist Referral"
        );

        // Load doctors
        try {
            List<Doctor> doctors = doctorDAO.getAllDoctors();
            doctorCombo.getItems().addAll(doctors);
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Failed to load doctors");
        }
    }

    private void setupButtons() {
        bookBtn.setOnAction(e -> handleBooking());
        cancelBtn.setOnAction(e -> handleCancel());
        cancelBtn.setOnAction(e -> handleClose());
        closeBtn.setOnAction(e -> handleClose());
    }

    private void setupListeners() {
        // Update available slots when date or doctor changes
        pickDate.valueProperty().addListener((obs, old, newVal) -> updateAvailableSlots());
        doctorCombo.valueProperty().addListener((obs, old, newVal) -> updateAvailableSlots());
    }

    private void updateAvailableSlots() {
        Doctor selectedDoctor = doctorCombo.getValue();
        LocalDate selectedDate = pickDate.getValue();

        if (selectedDoctor == null || selectedDate == null) {
            pickTimeCombo.getItems().clear();
            return;
        }

        try {
            // Get all appointments for the selected doctor and date
            List<Appointment> existingAppointments = appointmentDAO.getAppointmentsByDoctorId(selectedDoctor.getDoctorId());

            // Filter booked slots for the selected date
            Set<String> bookedSlots = existingAppointments.stream()
                    .filter(apt -> apt.getAppointmentDate().toLocalDate().equals(selectedDate))
                    .map(apt -> apt.getAppointmentDate().toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")))
                    .collect(Collectors.toSet());

            // Update available time slots
            List<String> availableSlots = IntStream.rangeClosed(9, 16)
                    .mapToObj(hour -> String.format("%02d:00", hour))
                    .filter(time -> !bookedSlots.contains(time))
                    .collect(Collectors.toList());

            pickTimeCombo.getItems().setAll(availableSlots);
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Failed to load available time slots");
        }
    }

    @FXML
    private void handleBooking() {
        // Clear any previous error styles
        doctorCombo.getStyleClass().remove("error-border");
        pickTimeCombo.getStyleClass().remove("error-border");
        typeServiceCombo.getStyleClass().remove("error-border");
        noteField.getStyleClass().remove("error-border");

        boolean hasError = false;

        // Validate each field and add error styling
        if (doctorCombo.getValue() == null) {
            doctorCombo.getStyleClass().add("error-border");
            hasError = true;
        }
        if (pickTimeCombo.getValue() == null) {
            pickTimeCombo.getStyleClass().add("error-border");
            hasError = true;
        }
        if (typeServiceCombo.getValue() == null) {
            typeServiceCombo.getStyleClass().add("error-border");
            hasError = true;
        }
        if (noteField.getText().trim().isEmpty()) {
            noteField.getStyleClass().add("error-border");
            hasError = true;
        }

        if (hasError) {
            // add label here

            showError("Please fill all required fields");
        }

        try {
            LocalDateTime appointmentDateTime = LocalDateTime.of(
                    pickDate.getValue(),
                    LocalTime.parse(pickTimeCombo.getValue(), DateTimeFormatter.ofPattern("HH:mm"))
            );

            RadioButton selectedType = (RadioButton) bookTypeRadio.getSelectedToggle();
            String appointmentType = selectedType.getText();

            Appointment appointment = new Appointment(
                    0,
                    currentPatientId,
                    doctorCombo.getValue().getDoctorId(),
                    0,
                    appointmentDateTime,
                    "Pending",
                    noteField.getText(),
                    LocalDateTime.now(),
                    "",
                    "",
                    "",
                    typeServiceCombo.getValue(),
                    appointmentType,
                    "",
                    "Patient",
                    ((RadioButton) prefferedContactRadio.getSelectedToggle()).getText(),
                    LocalDateTime.now()
            );
            /**
             *
             */
            boolean success = appointmentDAO.addAppointment(appointment);
            if (success) {
                // Load and show booking confirmation
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/edu/careflow/fxml/components/patient/showCaseBook.fxml"));
                    Parent bookingConfirmation = loader.load();

                    // Set the appointment details in the confirmation view
                    Label doctorNameLabel = (Label) bookingConfirmation.lookup("#doctorName");
                    Label appointmentDateLabel = (Label) bookingConfirmation.lookup("#appointmentDate");
                    Label appointmentTimeLabel = (Label) bookingConfirmation.lookup("#appointmentTime");
                    Label serviceTypeLabel = (Label) bookingConfirmation.lookup("#serviceType");
                    Label appointmentNoteLabel = (Label) bookingConfirmation.lookup("#appointmentNote");
                    Button closeBtn = (Button) bookingConfirmation.lookup("#closeBtn");



                    Doctor selectedDoctor = doctorCombo.getValue();
                    doctorNameLabel.setText("Dr. " + selectedDoctor.getFirstName() + " " + selectedDoctor.getLastName());
                    appointmentDateLabel.setText(pickDate.getValue().format(DateTimeFormatter.ofPattern("MMMM d, yyyy")));
                    appointmentTimeLabel.setText(pickTimeCombo.getValue());
                    serviceTypeLabel.setText(typeServiceCombo.getValue());
                    appointmentNoteLabel.setText(noteField.getText());

                    // Find stackPaneContainer from scene root and add confirmation

                    Scene scene = scrollPaneBook.getScene();
                    if (scene != null) {
                        StackPane container = (StackPane) scene.lookup("#stackPaneContainer");
                        if (container != null) {
                            // Add fade-in animation
                            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), bookingConfirmation);
                            fadeIn.setFromValue(0.0);
                            fadeIn.setToValue(1.0);

                            container.getChildren().add(bookingConfirmation);
                            fadeIn.play();
                        }

                        // remove the card when cancelBtn is click
                        closeBtn.setOnAction(event -> {
                            FadeTransition fadeOut = new FadeTransition(Duration.millis(300), bookingConfirmation);
                            fadeOut.setFromValue(1.0);
                            fadeOut.setToValue(0.0);
                            fadeOut.setOnFinished(e -> container.getChildren().remove(bookingConfirmation));
                            fadeOut.play();
                        });
                    }

                    handleClose(); // Close the booking form
                } catch (IOException e) {
                    e.printStackTrace();
                    showError("Failed to show booking confirmation");
                }
            } else {
                showError("Failed to book appointment");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Failed to book appointment");
        }
    }

    private boolean validateForm() {
        if (doctorCombo.getValue() == null ||
                pickDate.getValue() == null ||
                pickTimeCombo.getValue() == null ||
                typeServiceCombo.getValue() == null ||
                noteField.getText().trim().isEmpty()) {

            showError("Please fill all required fields");
            return false;
        }
        return true;
    }

    private void handleCancel() {
        // Reset form fields except date picker

        doctorCombo.setValue(null);
        pickTimeCombo.setValue(null);
        typeServiceCombo.setValue(null);
        noteField.clear();
        insuranceField.clear();
    }

    private void handleClose() {
        // Get the parent container of scrollPaneBook
        FadeTransition fadeOut = new FadeTransition(Duration.millis(150), scrollPaneBook);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> {
            // Remove the scrollPane from its parent after fade
            if (scrollPaneBook.getParent() instanceof VBox parentBox) {
                parentBox.getChildren().remove(scrollPaneBook);
            } else if (scrollPaneBook.getParent() != null) {
                ((javafx.scene.layout.Pane) scrollPaneBook.getParent()).getChildren().remove(scrollPaneBook);
            }
            // Set to null to help garbage collection
            scrollPaneBook = null;
        });
        fadeOut.play();
    }


    private void showError(String message) {
        new Alert(Alert.AlertType.ERROR, message).show();
    }

    private void showInfo(String message) {
        new Alert(Alert.AlertType.INFORMATION, message).show();
    }

    public void setPatientId(int patientId) {
        this.currentPatientId = patientId;
    }

//    private void setupPreferredContact() {
//        preferredContactCombo.getItems().addAll(
//            "Email",
//            "Phone",
//            "SMS"
//        );
//    }

    private void setupAppointmentTypeRadios() {
//        inPersonRadio.setToggleGroup(bookTypeRadio);
//        onlineRadio.setToggleGroup(bookTypeRadio);
//        inPersonRadio.setSelected(true);
//
//        // Show/hide meeting link field based on appointment type
//        meetingLinkField.setVisible(false);
//        bookTypeRadio.selectedToggleProperty().addListener((obs, old, newVal) -> {
//            meetingLinkField.setVisible(onlineRadio.isSelected());
//        });
    }
}
