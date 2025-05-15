package edu.careflow.presentation.controllers.patient.forms;

import edu.careflow.repository.dao.VitalsDAO;
import edu.careflow.repository.entities.Vitals;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.sql.SQLException;
import java.time.LocalDateTime;

public class VitalsBioForm {

    @FXML
    private TextField bmiField;

    @FXML
    private Button bookBtn;

    @FXML
    private Button cancelBtn;

    @FXML
    private Button closeButton;

    @FXML
    private TextField diastolicField;

    @FXML
    private TextField heartRateField;

    @FXML
    private TextField heightField;

    @FXML
    private TextField oxygenField;

    @FXML
    private TextField systolicField;

    @FXML
    private TextField temperatureField;

    @FXML
    private TextField weightField;

    @FXML
    private ScrollPane scrollPaneVitals;

    private int currentPatientId;
    private final VitalsDAO vitalsDAO = new VitalsDAO();

    @FXML
    private void initialize() {
        // Add listeners for weight and height to calculate BMI
        weightField.textProperty().addListener((obs, oldVal, newVal) -> calculateBMI());
        heightField.textProperty().addListener((obs, oldVal, newVal) -> calculateBMI());

        // Set up buttons
        closeButton.setOnAction(event -> handleClose());
        cancelBtn.setOnAction(event -> handleClose());
        bookBtn.setOnAction(event -> handleSave());

        // Add numeric validation to fields
        addNumericValidation(weightField);
        addNumericValidation(heightField);
        addNumericValidation(systolicField);
        addNumericValidation(diastolicField);
        addNumericValidation(heartRateField);
        addNumericValidation(temperatureField);
        addNumericValidation(oxygenField);
    }

    private void addNumericValidation(TextField field) {
        field.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*(\\.\\d*)?")) {
                field.setText(oldVal);
            }
        });
    }

    private void calculateBMI() {
        try {
            if (!weightField.getText().isEmpty() && !heightField.getText().isEmpty()) {
                double weight = Double.parseDouble(weightField.getText());
                double height = Double.parseDouble(heightField.getText()) / 100; // convert cm to m
                double bmi = weight / (height * height);
                bmiField.setText(String.format("%.1f", bmi));
            }
        } catch (NumberFormatException e) {
            bmiField.clear();
        }
    }

    private void handleSave() {
        try {

            int vitalId = vitalsDAO.generateUniqueVitalsId();

            // TODO : add random id generator
            Vitals vitals = new Vitals(
                    currentPatientId,
                    vitalId,
                    1,
                    formatBloodPressure(),
                    parseIntegerField(heartRateField),
                    0,
                    parseDoubleField(weightField),
                    parseDoubleField(heightField),
                    parseDoubleField(temperatureField),
                    parseDoubleField(oxygenField),
                    LocalDateTime.now(),
                    null
            );

            boolean success = vitalsDAO.addVitals(vitals);

            if (success) {
                // Create and show floating success label
                Scene scene = scrollPaneVitals.getScene();
                if (scene != null) {
                    StackPane container = (StackPane) scene.lookup("#stackPaneContainer");
                    if (container != null) {
                        Label successLabel = new Label("Vitals saved successfully");
                        successLabel.getStyleClass().add("success-label");
                        successLabel.setStyle(
                                "-fx-background-color: #4CAF50;" +
                                        "-fx-text-fill: white;" +
                                        "-fx-padding: 10 20;" +
                                        "-fx-background-radius: 5;" +
                                        "-fx-translate-y: 300; -fx-font-family: 'Gilroy-SemiBold'; -fx-font-size: 16px" // Move down
                        );

                        // Add fade-in animation
                        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), successLabel);
                        fadeIn.setFromValue(0.0);
                        fadeIn.setToValue(1.0);

                        container.getChildren().add(successLabel);
                        fadeIn.play();

                        // Remove label after 3 seconds
                        Timeline timeline = new Timeline(
                                new KeyFrame(Duration.seconds(2), e -> {
                                    FadeTransition fadeOut = new FadeTransition(Duration.millis(300), successLabel);
                                    fadeOut.setFromValue(1.0);
                                    fadeOut.setToValue(0.0);
                                    fadeOut.setOnFinished(event -> container.getChildren().remove(successLabel));
                                    fadeOut.play();
                                })
                        );
                        timeline.play();
                    }
                }
                handleClose();
            } else {
                showError("Failed to save vitals");
            }

        } catch (SQLException e) {
            showError("Database error: " + e.getMessage());
        }
    }

    private String formatBloodPressure() {
        String systolic = systolicField.getText().trim();
        String diastolic = diastolicField.getText().trim();

        if (!systolic.isEmpty() && !diastolic.isEmpty()) {
            return systolic + "/" + diastolic;
        }
        return "";
    }

    private double parseDoubleField(TextField field) {
        String value = field.getText().trim();
        return value.isEmpty() ? 0.0 : Double.parseDouble(value);
    }

    private int parseIntegerField(TextField field) {
        String value = field.getText().trim();
        return value.isEmpty() ? 0 : Integer.parseInt(value);
    }

    public void handleClose() {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(150), scrollPaneVitals);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> {
            if (scrollPaneVitals.getParent() instanceof VBox parentBox) {
                parentBox.getChildren().remove(scrollPaneVitals);
            } else if (scrollPaneVitals.getParent() != null) {
                ((javafx.scene.layout.Pane) scrollPaneVitals.getParent()).getChildren().remove(scrollPaneVitals);
            }
            scrollPaneVitals = null;
        });
        fadeOut.play();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public void initializeData(int patientId) {
        this.currentPatientId = patientId;

        // Try to load latest vitals
        try {
            Vitals latestVitals = vitalsDAO.getLatestVitals(patientId);
            if (latestVitals != null) {
                // Pre-fill fields with latest values
                weightField.setText(String.valueOf(latestVitals.getWeightKg()));
                heightField.setText(String.valueOf(latestVitals.getHeightCm()));
                String[] bp = latestVitals.getBloodPressure().split("/");
                if (bp.length == 2) {
                    systolicField.setText(bp[0]);
                    diastolicField.setText(bp[1]);
                }
                heartRateField.setText(String.valueOf(latestVitals.getHeartRate()));
                temperatureField.setText(String.valueOf(latestVitals.getTemperature()));
                oxygenField.setText(String.valueOf(latestVitals.getOxygenSaturation()));
            }
        } catch (SQLException e) {
            showError("Failed to load latest vitals");
        }
    }
}