package edu.careflow.presentation.controllers.doctor.cards;

import com.dlsc.gemsfx.AvatarView;
import edu.careflow.presentation.controllers.patient.PatientContainerController;
import edu.careflow.presentation.controllers.patient.forms.BookAptForm;
import edu.careflow.repository.dao.AppointmentDAO;
import edu.careflow.repository.dao.PatientDAO;
import edu.careflow.repository.entities.Appointment;
import edu.careflow.repository.entities.Patient;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PatientListCard {


    @FXML
    private Button addAppointment;

    @FXML
    private Label lastVisitDate;

    @FXML
    private AvatarView patientAvatar;

    @FXML
    private VBox patientCardContainer;

    @FXML
    private Label patientId;

    @FXML
    private Label patientName;

    @FXML
    private Button showDetailsBtn;

    @FXML
    private FontIcon showDetailsIcon;

    @FXML
    private Label visitCount;

    @FXML
    private Button visitPatient;

    private int doctorId;


    private static final PatientDAO patientDAO = new PatientDAO();
    private static final AppointmentDAO appointmentDAO = new AppointmentDAO();
    private VBox detailsCard;
    private boolean isExpanded = false;
    private Patient currentPatient;

    public void initializeBasicInfo(Patient patient) {
        this.currentPatient = patient;
        
        // Set patient information
        patientName.setText(patient.getFirstName() + " " + patient.getLastName());
        patientId.setText("ID: " + patient.getPatientId());
        
        // Update visit information
        updateVisitInformation(patient.getPatientId());

        // Setup button actions
        visitPatient.setOnAction(e -> handleStartVisit());
        addAppointment.setOnAction(e -> handleAddAppointment());
        showDetailsBtn.setOnAction(e -> handleShowDetails());
    }

    private void updateVisitInformation(int patientId) {
        try {
            // Get all appointments for this patient
            List<Appointment> appointments = appointmentDAO.getAppointmentsByPatientId(patientId);
            
            // Count completed visits
            long completedVisits = appointments.stream()
                .filter(apt -> "Completed".equalsIgnoreCase(apt.getStatus()))
                .count();
            visitCount.setText(String.valueOf(completedVisits));

            // Get last visit date
            LocalDateTime lastVisit = appointments.stream()
                .filter(apt -> "Completed".equalsIgnoreCase(apt.getStatus()))
                .map(Appointment::getAppointmentDate)
                .max(LocalDateTime::compareTo)
                .orElse(null);

            if (lastVisit != null) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy");
                lastVisitDate.setText("Last Visit: " + lastVisit.format(formatter));
            } else {
                lastVisitDate.setText("No previous visits");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            lastVisitDate.setText("Error loading visit data");
            visitCount.setText("0");
        }
    }

    public void loadAvatar(Patient patient) throws SQLException {
        try {
            // Load patient avatar
            Image avatar = patientDAO.loadAvatar(patient.getPatientId());
            if (avatar != null) {
                patientAvatar.setImage(avatar);
            } else {
                System.out.println("No avatar found for patient " + patient.getPatientId());
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }
    }

    private String formatDate(LocalDateTime dateTime) {
        return dateTime.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"));
    }

    private void handleViewProfile() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/edu/careflow/fxml/patient/patientProfile.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            // TODO: Set the scene in the main container
        } catch (IOException e) {
            e.printStackTrace();
            // TODO: Show error dialog
        }
    }

    private void handleStartVisit() {
        try {
            visitPatient.setDisable(true);
            visitPatient.setText("Loading...");

            // Load the patient container
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/edu/careflow/fxml/patient/patientContainer.fxml"));
            Parent patientContainer = loader.load();
            PatientContainerController controller = loader.getController();

            Scene currentScene = patientCardContainer.getScene();
            FadeTransition fadeOut = new FadeTransition(Duration.millis(200), currentScene.getRoot());
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);

            fadeOut.setOnFinished(e -> {
                currentScene.setRoot(patientContainer);

                VBox rightBoxContainer = (VBox) patientContainer.lookup("#rightBoxContainer");
                VBox topBoxContainer = (VBox) patientContainer.lookup("#topBoxContainer");

                try {
                    // Load top navbar with proper controller initialization
                    FXMLLoader topBoxLoader = new FXMLLoader(getClass().getResource("/edu/careflow/fxml/components/doctor/doctorNavBar.fxml"));
                    Parent topBoxContent = topBoxLoader.load();
                    NavigationBar topBarController = topBoxLoader.getController();

                    // Set patient ID after controller is loaded
                    topBarController.initialize();
                    topBarController.setPatientId(currentPatient.getPatientId());
                    topBarController.setNavigationButtonsState(true);
                    topBarController.setDoctorId(getDoctorId());

                    if (topBoxContainer != null) {
                        topBoxContainer.getChildren().add(topBoxContent);
                        // Initialize the controller after adding to scene
                        topBarController.initialize();
                    }

                    // Initialize patient data
                    controller.initializePatientData(String.valueOf(currentPatient.getPatientId()));

                    FadeTransition fadeIn = new FadeTransition(Duration.millis(300), patientContainer);
                    fadeIn.setFromValue(0.0);
                    fadeIn.setToValue(1.0);
                    fadeIn.play();
                } catch (IOException ex) {
                    ex.printStackTrace();
                    new Alert(Alert.AlertType.ERROR, "Failed to load navigation components").show();
                }
            });

            fadeOut.play();

        } catch (IOException e) {
            e.printStackTrace();
            visitPatient.setDisable(false);
            visitPatient.setText("Visit");
            new Alert(Alert.AlertType.ERROR, "Failed to load patient page").show();
        }
    }

    private void handleShowDetails() {
        isExpanded = !isExpanded;
        showDetailsIcon.setIconLiteral(isExpanded ? "fas-chevron-up" : "fas-chevron-down");
        
        if (isExpanded) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/edu/careflow/fxml/components/doctor/patientViewCardWDetailsCard.fxml"));
                detailsCard = loader.load();
                PatientCardDetails controller = loader.getController();
                controller.initializeData(currentPatient);
                patientCardContainer.getChildren().add(detailsCard);
            } catch (IOException e) {
                e.printStackTrace();
                // TODO: Show error dialog
            }
        } else {
            patientCardContainer.getChildren().remove(detailsCard);
        }
    }

    private void handleAddAppointment() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/edu/careflow/fxml/components/patient/bookAptForm.fxml"));
            Parent bookForm = loader.load();
            
            // Get the controller and set the patient ID
            BookAptForm controller = loader.getController();
            controller.setPatientId(currentPatient.getPatientId());
            
            // Get the scene from any node in the current view
            Scene currentScene = patientCardContainer.getScene();
            if (currentScene != null) {
                // Find the right box container in the scene
                VBox rightBoxContainer = (VBox) currentScene.lookup("#rightBoxContainer");
                if (rightBoxContainer != null) {
                    // Fade out existing content if any
                    if (!rightBoxContainer.getChildren().isEmpty()) {
                        FadeTransition fadeOut = new FadeTransition(Duration.millis(150), rightBoxContainer.getChildren().get(0));
                        fadeOut.setFromValue(1.0);
                        fadeOut.setToValue(0.0);
                        fadeOut.setOnFinished(e -> {
                            rightBoxContainer.getChildren().setAll(bookForm);
                            
                            // Fade in new content with slide up effect
                            FadeTransition fadeIn = new FadeTransition(Duration.millis(200), bookForm);
                            fadeIn.setFromValue(0.0);
                            fadeIn.setToValue(1.0);
                            
                            TranslateTransition slideIn = new TranslateTransition(Duration.millis(200), bookForm);
                            slideIn.setFromY(20);
                            slideIn.setToY(0);
                            
                            ParallelTransition parallelIn = new ParallelTransition(fadeIn, slideIn);
                            parallelIn.play();
                        });
                        fadeOut.play();
                    } else {
                        rightBoxContainer.getChildren().setAll(bookForm);
                        
                        // Simple fade in for first load
                        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), bookForm);
                        fadeIn.setFromValue(0.0);
                        fadeIn.setToValue(1.0);
                        fadeIn.play();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            // Show error dialog
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Navigation Error");
            alert.setContentText("Failed to load appointment form: " + e.getMessage());
            alert.showAndWait();
        }
    }

    // getter and setter for doctorid
    public void setDoctorId(int doctorId) {
        this.doctorId = doctorId;
    }

    public int getDoctorId() {
        return doctorId;
    }
} 