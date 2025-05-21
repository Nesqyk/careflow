package edu.careflow.presentation.controllers.patient;

import com.dlsc.gemsfx.AvatarView;
import edu.careflow.repository.dao.AppointmentDAO;
import edu.careflow.repository.dao.PatientDAO;
import edu.careflow.repository.entities.Appointment;
import edu.careflow.repository.entities.Patient;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PatientProfileController {

    @FXML
    private AvatarView patientAvatar;

    @FXML
    private Label patientName;

    @FXML
    private Label patientId;

    @FXML
    private Label patientAge;

    @FXML
    private Label patientGender;

    @FXML
    private Label patientEmail;

    @FXML
    private Label patientPhone;

    @FXML
    private Label patientAddress;

    @FXML
    private Label bloodType;

    @FXML
    private Label allergies;

    @FXML
    private Label conditions;

    @FXML
    private VBox recentVisitsContainer;

    @FXML
    private Button editProfileBtn;

    @FXML
    private Button viewAllVisitsBtn;

    @FXML
    private Button startVisitBtn;

    @FXML
    private Button backBtn;

    private PatientDAO patientDAO = new PatientDAO();
    private AppointmentDAO appointmentDAO = new AppointmentDAO();
    private int currentPatientId;

    public void initializeData(int patientId) {
        this.currentPatientId = patientId;
        try {
            Patient patient = patientDAO.getPatientById(patientId);
            if (patient != null) {
                // Set basic info
                patientName.setText(patient.getFirstName() + " " + patient.getLastName());
                this.patientId.setText("ID: " + patient.getPatientId());
                
                // Calculate age
                Period age = Period.between(patient.getDateOfBirth(), LocalDateTime.now().toLocalDate());
                patientAge.setText(age.getYears() + " years old");
                
                // Set gender
                patientGender.setText(patient.getGender());
                
                // Load avatar
                Image avatarData = patientDAO.loadAvatar(patientId);
                if (avatarData != null) {
                    patientAvatar.setImage(avatarData);
                }

                // Set contact info
                patientEmail.setText(patient.getEmail());
                patientPhone.setText(patient.getContact());
                patientAddress.setText(patient.getAddress());

                // Set medical info
//                bloodType.setText(patient.getBloodType());
//                allergies.setText(patient.getAllergies() != null ? patient.getAllergies() : "None");
//                conditions.setText(patient.getConditions() != null ? patient.getConditions() : "None");

                // Load recent visits
                loadRecentVisits(patientId);

                // Set up button actions
                setupButtons(patient);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadRecentVisits(int patientId) {
        try {
            List<Appointment> appointments = appointmentDAO.getAppointmentsByPatientId(patientId);
            recentVisitsContainer.getChildren().clear();

            // Show only last 3 visits
            int count = Math.min(appointments.size(), 3);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMM d, yyyy");

            for (int i = 0; i < count; i++) {
                Appointment appointment = appointments.get(i);
                HBox visitBox = new HBox(8);
                visitBox.setStyle("-fx-padding: 8; -fx-background-color: #F8FAFC; -fx-background-radius: 4;");

                Label dateLabel = new Label(appointment.getAppointmentDate().format(formatter));
                dateLabel.setStyle("-fx-font-family: 'Gilroy-SemiBold'; -fx-text-fill: #2D3748;");

                Label statusLabel = new Label(appointment.getStatus());
                statusLabel.setStyle("-fx-font-family: 'Gilroy-Regular'; -fx-text-fill: #718096;");

                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                Button viewBtn = new Button("View Details");
                viewBtn.getStyleClass().add("button-no-bg");
                viewBtn.setOnAction(e -> handleViewVisitDetails(appointment));

                visitBox.getChildren().addAll(dateLabel, statusLabel, spacer, viewBtn);
                recentVisitsContainer.getChildren().add(visitBox);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void setupButtons(Patient patient) {
        editProfileBtn.setOnAction(e -> handleEditProfile(patient));
        viewAllVisitsBtn.setOnAction(e -> handleViewAllVisits());
        startVisitBtn.setOnAction(e -> handleStartVisit(patient));
        backBtn.setOnAction(e -> handleBack());
    }

    private void handleEditProfile(Patient patient) {
//        try {
//            FXMLLoader loader = new FXMLLoader(getClass().getResource("/edu/careflow/fxml/patient/editPatientProfile.fxml"));
//            Parent editView = loader.load();
//            EditPatientProfileController controller = loader.getController();
//            controller.initializeData(patient);
//
//            Scene scene = backBtn.getScene();
//            VBox mainContainer = (VBox) scene.lookup("#mainContainer");
//            if (mainContainer != null) {
//                mainContainer.getChildren().clear();
//                mainContainer.getChildren().add(editView);
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    private void handleViewAllVisits() {
//        try {
//            FXMLLoader loader = new FXMLLoader(getClass().getResource("/edu/careflow/fxml/patient/patientVisits.fxml"));
//            Parent visitsView = loader.load();
//            PatientVisitsController controller = loader.getController();
//            controller.initializeData(currentPatientId);
//
//            Scene scene = backBtn.getScene();
//            VBox mainContainer = (VBox) scene.lookup("#mainContainer");
//            if (mainContainer != null) {
//                mainContainer.getChildren().clear();
//                mainContainer.getChildren().add(visitsView);
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
    }

    private void handleViewVisitDetails(Appointment appointment) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/edu/careflow/fxml/patient/visitDetails.fxml"));
            Parent detailsView = loader.load();
//            VisitDetailsController controller = loader.getController();
//            controller.initializeData(appointment);

            Scene scene = backBtn.getScene();
            VBox mainContainer = (VBox) scene.lookup("#mainContainer");
            if (mainContainer != null) {
                mainContainer.getChildren().clear();
                mainContainer.getChildren().add(detailsView);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleStartVisit(Patient patient) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/edu/careflow/fxml/patient/patientContainer.fxml"));
            Parent patientContainer = loader.load();
            
            Scene scene = backBtn.getScene();
            VBox mainContainer = (VBox) scene.lookup("#mainContainer");
            if (mainContainer != null) {
                mainContainer.getChildren().clear();
                mainContainer.getChildren().add(patientContainer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/edu/careflow/fxml/doctor/doctorContainerNew.fxml"));
            Parent doctorContainer = loader.load();
            
            Scene scene = backBtn.getScene();
            VBox mainContainer = (VBox) scene.lookup("#mainContainer");
            if (mainContainer != null) {
                mainContainer.getChildren().clear();
                mainContainer.getChildren().add(doctorContainer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
} 