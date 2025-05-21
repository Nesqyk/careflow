package edu.careflow.presentation.controllers.nurse;

import edu.careflow.manager.DatabaseManager;
import edu.careflow.repository.dao.NurseDAO;
import edu.careflow.repository.entities.Nurse;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.animation.FadeTransition;
import javafx.util.Duration;
import javafx.scene.layout.VBox;
import javafx.scene.layout.Pane;
import java.sql.SQLException;
import java.util.Optional;

public class NurseSettingsController {
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField licenseNumberField;
    @FXML private TextField phoneField;
    @FXML private Button saveButton;
    @FXML private Button cancelButton;
    @FXML private Button closeButton;
    @FXML private ScrollPane scrollPane;

    private Nurse currentNurse;
    private NurseDAO nurseDAO;
    private boolean isDirty = false;

    @FXML
    public void initialize() {
        nurseDAO = new NurseDAO();
        setupValidation();
        loadNurseData();
        setupScrollPane();
    }

    private void setupScrollPane() {
        scrollPane.setFitToWidth(true);
        scrollPane.setFitToHeight(true);
    }

    private void setupValidation() {
        // Add listeners to track changes
        firstNameField.textProperty().addListener((obs, oldVal, newVal) -> isDirty = true);
        lastNameField.textProperty().addListener((obs, oldVal, newVal) -> isDirty = true);
        licenseNumberField.textProperty().addListener((obs, oldVal, newVal) -> isDirty = true);
        phoneField.textProperty().addListener((obs, oldVal, newVal) -> isDirty = true);
    }

    private void loadNurseData() {
        try {
            // TODO: Get current nurse ID from authentication system
            int currentNurseId = 1; // Placeholder
            currentNurse = nurseDAO.getNurseById(currentNurseId);
            populateFields();
        } catch (SQLException e) {
            showError("Error", "Failed to load nurse data: " + e.getMessage());
        }
    }

    private void populateFields() {
        if (currentNurse != null) {
            firstNameField.setText(currentNurse.getFirstName());
            lastNameField.setText(currentNurse.getLastName());
            licenseNumberField.setText(currentNurse.getLicenseNumber());
            phoneField.setText(currentNurse.getPhone());
            isDirty = false;
        }
    }

    @FXML
    private void handleSave() {
        if (validateInput()) {
            try {
                updateNurseData();
                showSuccess("Success", "Settings saved successfully");
                handleClose();
            } catch (SQLException e) {
                showError("Error", "Failed to save settings: " + e.getMessage());
            }
        }
    }

    private boolean validateInput() {
        StringBuilder errors = new StringBuilder();
        
        if (firstNameField.getText().trim().isEmpty()) {
            errors.append("First name is required\n");
        }
        if (lastNameField.getText().trim().isEmpty()) {
            errors.append("Last name is required\n");
        }
        if (licenseNumberField.getText().trim().isEmpty()) {
            errors.append("License number is required\n");
        }
        if (phoneField.getText().trim().isEmpty()) {
            errors.append("Contact number is required\n");
        }

        if (errors.length() > 0) {
            showError("Validation Error", errors.toString());
            return false;
        }
        return true;
    }

    private void updateNurseData() throws SQLException {
        if (currentNurse != null) {
            currentNurse.setFirstName(firstNameField.getText().trim());
            currentNurse.setLastName(lastNameField.getText().trim());
            currentNurse.setLicenseNumber(licenseNumberField.getText().trim());
            currentNurse.setPhone(phoneField.getText().trim());

            if (!nurseDAO.updateNurse(currentNurse)) {
                throw new SQLException("Failed to update nurse record");
            }
        }
    }

    @FXML
    private void handleCancel() {
        if (isDirty) {
            Optional<ButtonType> result = showConfirmation(
                "Unsaved Changes",
                "You have unsaved changes. Are you sure you want to cancel?"
            );
            
            if (result.isPresent() && result.get() == ButtonType.OK) {
                handleClose();
            }
        } else {
            handleClose();
        }
    }

    @FXML
    private void handleClose() {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(150), scrollPane);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> {
            if (scrollPane.getParent() instanceof VBox parentBox) {
                parentBox.getChildren().remove(scrollPane);
            } else if (scrollPane.getParent() != null) {
                ((Pane) scrollPane.getParent()).getChildren().remove(scrollPane);
            }
            scrollPane = null;
        });
        fadeOut.play();
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccess(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private Optional<ButtonType> showConfirmation(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        return alert.showAndWait();
    }
} 