package edu.careflow.presentation.controllers.doctor.cards;

import edu.careflow.presentation.controllers.patient.PatientContainerController;
import edu.careflow.repository.dao.PatientDAO;
import edu.careflow.repository.entities.Appointment;
import edu.careflow.repository.entities.Patient;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class AppointmentCard {

    @FXML
    private Button StartVisitButton;

    @FXML
    private VBox appointmentContainer;

    @FXML
    private Label appointmentDate;

    @FXML
    private Label appointmentId;

    @FXML
    private VBox clinicRoomAppointment;

    @FXML
    private Hyperlink patientHyperLink;

    @FXML
    private Button showButton;

    @FXML
    private FontIcon showDetailsIcon;

    private PatientDAO patientDAO = new PatientDAO();
    private VBox detailsCard;



    public void initializeData(Appointment appointment) {
        try {

            patientHyperLink.setOnAction(event -> {handleStartVisit(appointment);});
            System.out.println(appointment);
            if (appointment != null) {
                DateTimeFormatter defaultFormatter = DateTimeFormatter.ofPattern("M/d/yyyy @ h a");
                DateTimeFormatter todayFormatter = DateTimeFormatter.ofPattern("'Today at' h a");

                Patient patient = patientDAO.getPatientById(appointment.getPatientId());
                appointmentId.setText("ID: " + appointment.getAppointmentId());
                System.out.println("Patient ID: " + appointment.getPatientId());

                if (appointment.getAppointmentDate() != null) {
                    LocalDate appointmentDateValue = appointment.getAppointmentDate().toLocalDate();
                    if (appointmentDateValue.isEqual(LocalDate.now())) {
                        appointmentDate.setText(appointment.getAppointmentDate().format(todayFormatter));
                    } else {
                        appointmentDate.setText(appointment.getAppointmentDate().format(defaultFormatter));
                    }
                } else {
                    appointmentDate.setText("--");
                }

                String name = patient.getFirstName() + " " + patient.getLastName();
                patientHyperLink.setText(name);
                setupShowButton(appointment);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void setupShowButton(Appointment appointment) {
        showButton.setOnAction(event -> {
            if (detailsCard == null) {
                showDetailsIcon.setIconLiteral("fas-chevron-up");
                showButton.getStyleClass().add("nav-button-active");
                showDetailsIcon.setIconSize(12);
                showDetailsCard(appointment);
            } else {
                showDetailsIcon.setIconLiteral("fas-chevron-down");
                showButton.getStyleClass().remove("nav-button-active");
                hideDetailsCard();
            }
        });
    }


    private void handleStartVisit(Appointment appointment) {
        try {
            StartVisitButton.setDisable(true);
            StartVisitButton.setText("Loading...");

            // Load the patient container
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/edu/careflow/fxml/patient/patientContainer.fxml"));
            Parent patientContainer = loader.load();
            PatientContainerController controller = loader.getController();

            Scene currentScene = appointmentContainer.getScene();
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
                    topBarController.setPatientId(appointment.getPatientId());
                    topBarController.setAppointmentId(appointment.getAppointmentId());

                    if (topBoxContainer != null) {
                        topBoxContainer.getChildren().add(topBoxContent);
                        // Initialize the controller after adding to scene
                        topBarController.initialize();
                    }

                    // Initialize patient data
                    controller.initializePatientData(String.valueOf(appointment.getPatientId()));

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
            StartVisitButton.setDisable(false);
            StartVisitButton.setText("Start Visit");
            new Alert(Alert.AlertType.ERROR, "Failed to load patient page").show();
        }
    }

//    private void showDetailsCard(Appointment appointment) {
//        try {
//            if (detailsCard == null) {
//                FXMLLoader loader = new FXMLLoader(getClass().getResource("/edu/careflow/fxml/components/doctor/appointmentDetailsCard.fxml"));
//                detailsCard = loader.load();
//                AppointmentDetailsCard detailsController = loader.getController();
//                detailsController.initializeData(appointment);
//
//                detailsCard.setTranslateY(-20);
//                detailsCard.setOpacity(0);
//                appointmentContainer.getChildren().add(detailsCard);
//            }
//
//            TranslateTransition slideDown = new TranslateTransition(Duration.millis(200), detailsCard);
//            slideDown.setToY(0);
//            detailsCard.setOpacity(1);
//            slideDown.play();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

    private void showDetailsCard(Appointment appointment) {
        try {
            if (detailsCard == null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/edu/careflow/fxml/components/doctor/appointmentDetailsCard.fxml"));
                detailsCard = loader.load();
                AppointmentDetailsCard detailsController = loader.getController();
                detailsController.initializeData(appointment);

                appointmentContainer.getChildren().add(detailsCard);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//    private void hideDetailsCard() {
//        if (detailsCard != null) {
//            TranslateTransition slideUp = new TranslateTransition(Duration.millis(200), detailsCard);
//            slideUp.setToY(-20);
//
//            slideUp.setOnFinished(event -> {
//                detailsCard.setOpacity(0);
//                appointmentContainer.getChildren().remove(detailsCard);
//                detailsCard = null;
//            });
//
//            slideUp.play();
//        }
//    }

    private void hideDetailsCard() {
        if (detailsCard != null) {
            appointmentContainer.getChildren().remove(detailsCard);
            detailsCard = null;
        }
    }
}