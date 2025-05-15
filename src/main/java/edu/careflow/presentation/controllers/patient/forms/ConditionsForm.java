package edu.careflow.presentation.controllers.patient.forms;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import org.controlsfx.control.SearchableComboBox;
import edu.careflow.repository.dao.ConditionDAO;
import edu.careflow.repository.entities.Condition;
import javafx.animation.FadeTransition;
import javafx.scene.control.Alert;
import javafx.scene.control.RadioButton;
import javafx.util.Duration;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

public class ConditionsForm {


    @FXML
    private Button cancelBtn;

    @FXML
    private Button closeButton;

    @FXML
    private SearchableComboBox<String> conditionsCombo;

    @FXML
    private VBox idContainerPatient1;

    @FXML
    private DatePicker onSetDateField;

    @FXML
    private Button saveButton;

    @FXML
    private ToggleGroup statusCondition;

    @FXML
    private javafx.scene.control.ScrollPane scrollPaneConditions;

    private int currentPatientId;
    private final ConditionDAO conditionDAO = new ConditionDAO();

    private static final List<String> COMMON_CONDITIONS = Arrays.asList(
            "Hypertension", "Diabetes", "Asthma", "COPD", "Heart Disease",
            "Arthritis", "Cancer", "Chronic Kidney Disease", "Depression", "Anxiety"
    );

    @FXML
    private void initialize() {
        // Populate the combo box
        conditionsCombo.getItems().addAll(COMMON_CONDITIONS);
        conditionsCombo.setEditable(true);

        // Set up buttons
        closeButton.setOnAction(event -> handleClose());
        cancelBtn.setOnAction(event -> handleClose());
        saveButton.setOnAction(event -> handleSave());
    }

    private void handleSave() {
        if (!validateForm()) return;
        try {
            String conditionName = conditionsCombo.getValue();
            LocalDate onsetDate = onSetDateField.getValue();
            RadioButton selectedStatus = (RadioButton) statusCondition.getSelectedToggle();
            String status = selectedStatus != null ? selectedStatus.getText() : "Active";

            Condition condition = new Condition(
                    conditionDAO.generateUniqueConditionId(), // conditionId (auto-generated)
                    currentPatientId,
                    conditionName,
                    "", // description (not in form)
                    onsetDate,
                    status
            );
            boolean success = conditionDAO.addCondition(condition);
            if (success) {
                // Floating success label logic (like VitalsBioForm)
                javafx.scene.Scene scene = scrollPaneConditions.getScene();
                if (scene != null) {
                    javafx.scene.layout.StackPane container = (javafx.scene.layout.StackPane) scene.lookup("#stackPaneContainer");
                    if (container != null) {
                        javafx.scene.control.Label successLabel = new javafx.scene.control.Label("Condition saved successfully");
                        successLabel.getStyleClass().add("success-label");
                        successLabel.setStyle(
                                "-fx-background-color: #4CAF50;" +
                                        "-fx-text-fill: white;" +
                                        "-fx-padding: 10 20;" +
                                        "-fx-background-radius: 5;" +
                                        "-fx-translate-y: 300; -fx-font-family: 'Gilroy-SemiBold'; -fx-font-size: 16px"
                        );
                        javafx.animation.FadeTransition fadeIn = new javafx.animation.FadeTransition(javafx.util.Duration.millis(300), successLabel);
                        fadeIn.setFromValue(0.0);
                        fadeIn.setToValue(1.0);
                        container.getChildren().add(successLabel);
                        fadeIn.play();
                        javafx.animation.Timeline timeline = new javafx.animation.Timeline(
                                new javafx.animation.KeyFrame(javafx.util.Duration.seconds(2), e -> {
                                    javafx.animation.FadeTransition fadeOut = new javafx.animation.FadeTransition(javafx.util.Duration.millis(300), successLabel);
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
                showError("Failed to save condition");
            }
        } catch (SQLException e) {
            showError("Database error: " + e.getMessage());
        }
    }

    private boolean validateForm() {
        StringBuilder error = new StringBuilder();
        if (conditionsCombo.getValue() == null || conditionsCombo.getValue().trim().isEmpty()) {
            error.append("Please select or enter a condition.\n");
        }
        if (onSetDateField.getValue() == null) {
            error.append("Please select an onset date.\n");
        }
        if (statusCondition.getSelectedToggle() == null) {
            error.append("Please select a status.\n");
        }
        if (error.length() > 0) {
            showError(error.toString());
            return false;
        }
        return true;
    }

    public void setPatientId(int patientId) {
        this.currentPatientId = patientId;
    }

    private void handleClose() {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(150), scrollPaneConditions);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> {
            if (scrollPaneConditions.getParent() instanceof VBox parentBox) {
                parentBox.getChildren().remove(scrollPaneConditions);
            } else if (scrollPaneConditions.getParent() != null) {
                ((javafx.scene.layout.Pane) scrollPaneConditions.getParent()).getChildren().remove(scrollPaneConditions);
            }
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
}
