package edu.careflow.presentation.controllers.receptionist;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.animation.FadeTransition;
import javafx.util.Duration;

public class ReceptionistSettingsController {
    @FXML private TextField nameField;
    @FXML private TextField contactField;
    @FXML private TextField emailField;
    @FXML private ComboBox<String> languageComboBox;
    @FXML private ComboBox<String> themeComboBox;
    @FXML private Button saveBtn;
    @FXML private Button cancelBtn;
    
    @FXML
    public void initialize() {
        // Initialize language options
        languageComboBox.getItems().addAll("English", "Spanish", "French");
        languageComboBox.setValue("English");
        
        // Initialize theme options
        themeComboBox.getItems().addAll("Light", "Dark", "System");
        themeComboBox.setValue("Light");
        
        // Set up button actions
        saveBtn.setOnAction(e -> handleSave());
        cancelBtn.setOnAction(e -> handleCancel());
    }
    
    private void handleSave() {
        // TODO: Implement save functionality
        // This would typically involve:
        // 1. Validating the input
        // 2. Saving to database
        // 3. Showing success message
        // 4. Closing the form
    }
    
    private void handleCancel() {
        // Find the parent container and remove this form
        VBox parent = (VBox) cancelBtn.getScene().lookup("#rightBoxContainer");
        if (parent != null) {
            FadeTransition fadeOut = new FadeTransition(Duration.millis(200), cancelBtn.getParent());
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            fadeOut.setOnFinished(e -> parent.getChildren().clear());
            fadeOut.play();
        }
    }
} 