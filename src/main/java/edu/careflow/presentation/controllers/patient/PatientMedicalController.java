package edu.careflow.presentation.controllers.patient;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.io.IOException;

public class PatientMedicalController {

    @FXML
    private VBox mainContainer;

    @FXML
    private HBox patientHeaderChoice;

    @FXML
    private Button prescriptionBtn;

    @FXML
    private Button recordsBtn;

    @FXML
    private Button vitalsBtn;

    @FXML
    private Button patientNotesBtn;


    private Button currentActiveButton = null;

    public void initializeData(int patientId) {
        prescriptionBtn.setOnAction(event -> handlePrescriptionNavigation(patientId));
        recordsBtn.setOnAction(event -> handleRecordsNavigation(patientId));
        vitalsBtn.setOnAction(event -> handleVitalsNavigation(patientId));
        patientNotesBtn.setOnAction(event -> {handleVisitNotesNavigation(patientId);});

        // look up at root of scene then lookup mainContainer then add the patientHeaderChoice

        // how about we add the

        // Load prescription page by default
        handlePrescriptionNavigation(patientId);
    }

    private void handleVisitNotesNavigation(int patientId) {
        resetAllBtnStyle();
        setPageContent(patientId, "notesPage");
        animateButtonSelection(patientNotesBtn);
    }

    private void handlePrescriptionNavigation(int patientId) {
        resetAllBtnStyle();
        setPageContent(patientId, "prescriptionPage");
        animateButtonSelection(prescriptionBtn);
    }

    private void handleRecordsNavigation(int patientId) {
        resetAllBtnStyle();
        setPageContent(patientId, "consultationPage");
        animateButtonSelection(recordsBtn);
    }

    private void handleVitalsNavigation(int patientId) {
        resetAllBtnStyle();
        setPageContent(patientId, "vitalsBiometricsPage");
        animateButtonSelection(vitalsBtn);
    }

    private void animateButtonSelection(Button button) {
        if (currentActiveButton != null) {
            currentActiveButton.getStyleClass().remove("nav-button-active");
            currentActiveButton.getStyleClass().add("nav-button-underline");
        }

        currentActiveButton = button;
        button.getStyleClass().setAll("nav-button-underline", "nav-button-active");

        ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(150), button);
        scaleTransition.setFromX(1.0);
        scaleTransition.setFromY(1.0);
        scaleTransition.setToX(1.05);
        scaleTransition.setToY(1.05);
        scaleTransition.setCycleCount(2);
        scaleTransition.setAutoReverse(true);

        FadeTransition fadeTransition = new FadeTransition(Duration.millis(200), button);
        fadeTransition.setFromValue(0.7);
        fadeTransition.setToValue(1.0);

        button.getStyleClass().add("nav-button-animating");

        ParallelTransition parallelTransition = new ParallelTransition(scaleTransition, fadeTransition);
        parallelTransition.play();

        parallelTransition.setOnFinished(e -> button.getStyleClass().remove("nav-button-animating"));
    }

    private void resetAllBtnStyle() {
        prescriptionBtn.getStyleClass().setAll("nav-button-underline");
        recordsBtn.getStyleClass().setAll("nav-button-underline");
        vitalsBtn.getStyleClass().setAll("nav-button-underline");
    }

    private void setPageContent(int patientId, String file) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/edu/careflow/fxml/patient/" + file + ".fxml")
            );
            Parent pageContent = loader.load();
            Object controller = loader.getController();

            if (controller instanceof PrescriptionController) {
                ((PrescriptionController) controller).initializeData(patientId);
            } else if(controller instanceof  ConsultationController) {
                ((ConsultationController) controller).initializeData(patientId);
            } else if(controller instanceof  VitalsBioController) {
                ((VitalsBioController) controller).initializeData(patientId);
            } else if(controller instanceof PatientNotesPageController) {
                ((PatientNotesPageController) controller).setPatientId(patientId);
            }

            if (!mainContainer.getChildren().isEmpty() && mainContainer.getChildren().get(0) == pageContent) {
                return;
            }

            if (!mainContainer.getChildren().isEmpty()) {
                Parent currentContent = (Parent) mainContainer.getChildren().get(0);
                FadeTransition fadeOut = new FadeTransition(Duration.millis(150), currentContent);
                fadeOut.setFromValue(1.0);
                fadeOut.setToValue(0.0);
                fadeOut.setOnFinished(e -> {
                    mainContainer.getChildren().setAll(pageContent);

                    FadeTransition fadeIn = new FadeTransition(Duration.millis(200), pageContent);
                    fadeIn.setFromValue(0.0);
                    fadeIn.setToValue(1.0);

                    TranslateTransition translateIn = new TranslateTransition(Duration.millis(200), pageContent);
                    translateIn.setFromX(10);
                    translateIn.setToX(0);

                    ParallelTransition parallelIn = new ParallelTransition(fadeIn, translateIn);
                    parallelIn.play();
                });
                fadeOut.play();
            } else {
                mainContainer.getChildren().setAll(pageContent);

                FadeTransition fadeIn = new FadeTransition(Duration.millis(300), pageContent);
                fadeIn.setFromValue(0.0);
                fadeIn.setToValue(1.0);
                fadeIn.play();
            }

        } catch (IOException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Failed to load " + file + " page").show();
        }
    }
}
