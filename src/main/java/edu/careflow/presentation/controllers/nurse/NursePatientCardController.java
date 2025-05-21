package edu.careflow.presentation.controllers.nurse;

import edu.careflow.presentation.controllers.patient.forms.VitalsBioForm;
import edu.careflow.repository.dao.PatientDAO;
import edu.careflow.repository.entities.Appointment;
import edu.careflow.repository.entities.Patient;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.io.IOException;
import java.sql.SQLException;

public class NursePatientCardController {
    @FXML private Label patientIdLabel;
    @FXML private Label patientNameLabel;
    @FXML private Label statusLabel;
    @FXML private MenuItem addVitalsMenuItem;
    @FXML private MenuItem vitalsHistoryMenuItem;
    @FXML private MenuItem downloadVitalsMenuItem;


    private PatientDAO patientDAO = new PatientDAO();
    private Appointment currentAppointment;

    public void initializeData(Appointment appointment) {
        this.currentAppointment = appointment;
        try {
            Patient patient = patientDAO.getPatientById(appointment.getPatientId());
            patientIdLabel.setText("ID: " + appointment.getAppointmentId());
            patientNameLabel.setText(patient.getFirstName() + " " + patient.getLastName());
            statusLabel.setText(appointment.getStatus());

            // Setup menu item actions
            addVitalsMenuItem.setOnAction(e -> handleAddVitals());
            vitalsHistoryMenuItem.setOnAction(e -> handleVitalsHistory());
            downloadVitalsMenuItem.setOnAction(e -> handleDownloadVitals());
        } catch (SQLException e) {
            e.printStackTrace();
            patientIdLabel.setText("ID: Error");
            patientNameLabel.setText("Error loading patient");
            statusLabel.setText("Unknown");
        }
    }

    private void handleAddVitals() {
        loadFormIntoRightContainer("/edu/careflow/fxml/components/patient/vitalAptForm.fxml");

    }

    private void loadFormIntoRightContainer(String formPath) {
        try {
            // Get the scene from any control (using formBtn here)
            Scene currentScene = patientIdLabel.getScene();

            // Look up the right container
            VBox rightBoxContainer = (VBox) currentScene.lookup("#rightBoxContainer");

            if (rightBoxContainer != null) {
                // Clear existing content
                rightBoxContainer.getChildren().clear();

                // Load and add new form
                FXMLLoader loader = new FXMLLoader(getClass().getResource(formPath));
                Parent formContent = loader.load();

                // Get the controller and set patient/doctor IDs if needed
                Object controller = loader.getController();

                if(controller instanceof VitalsBioForm vitalsBioForm) {
                    vitalsBioForm.initializeData(currentAppointment.getPatientId());
                    vitalsBioForm.setIds(currentAppointment.getPatientId(), currentAppointment.getNurseId(), currentAppointment.getAppointmentId());
                }

                // Add the form to container with fade transition
                FadeTransition fadeIn = new FadeTransition(Duration.millis(300), formContent);
                fadeIn.setFromValue(0.0);
                fadeIn.setToValue(1.0);

                rightBoxContainer.getChildren().add(formContent);
                fadeIn.play();

            } else {
                throw new RuntimeException("Right container not found in scene");
            }
        } catch (IOException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Failed to load form").show();
        }
    }

    private void handleVitalsHistory() {
        try {
            // Find the stack pane container in the scene
            StackPane stackPane = (StackPane) patientIdLabel.getScene().lookup("#stackPaneContainer");
            if (stackPane == null) {
                System.err.println("StackPane with fx:id 'stackPaneContainer' not found in scene.");
                return;
            }
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/edu/careflow/fxml/patient/vitalsBiometricsPage.fxml"));
            Parent vitalsPage = loader.load();
            // Pass patientId to the controller if possible
            Object controller = loader.getController();
            if (controller != null) {
                try {
                    controller.getClass().getMethod("initializeData", int.class).invoke(controller, currentAppointment.getPatientId());
                } catch (NoSuchMethodException ignored) {
                    // If the method doesn't exist, skip
                }
            }
            VBox vBox = new VBox();

            HBox hBox = new HBox();
            hBox.setAlignment(Pos.CENTER_RIGHT);
            hBox.setPadding(new Insets(32, 0, 32, 32));

            Button closeButton = new Button("Close");
            closeButton.getStyleClass().add("action-button");
            hBox.getChildren().add(closeButton);

            vBox.setPadding(new Insets(0, 128, 128, 128));
            vBox.getChildren().add(hBox);
            vBox.getChildren().add(vitalsPage);

            ScrollPane scrollPane = new ScrollPane(vBox);
            scrollPane.setFitToWidth(true);
            scrollPane.setFitToHeight(true);
            scrollPane.setStyle("-fx-background-color: transparent;");
            scrollPane.setPrefViewportWidth(800);
            scrollPane.setPrefViewportHeight(700);

            // Add stylesheet for consistent styling
            vBox.getStylesheets().add(getClass().getResource("/edu/careflow/css/styles.css").toExternalForm());

            // Close button logic
            closeButton.setOnAction(e -> {
                FadeTransition fadeOut = new FadeTransition(Duration.millis(200), scrollPane);
                fadeOut.setFromValue(1.0);
                fadeOut.setToValue(0.0);
                fadeOut.setOnFinished(event -> stackPane.getChildren().remove(scrollPane));
                fadeOut.play();
            });

            stackPane.getChildren().add(scrollPane);
            FadeTransition fade = new FadeTransition(Duration.millis(200), scrollPane);
            fade.setFromValue(0);
            fade.setToValue(1);
            ScaleTransition scale = new ScaleTransition(Duration.millis(200), scrollPane);
            scale.setFromX(0.95);
            scale.setFromY(0.95);
            scale.setToX(1);
            scale.setToY(1);
            fade.play();
            scale.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleDownloadVitals() {

    }

    private void setNursedId(int id) {
        currentAppointment.setNurseId(id);
    }


} 