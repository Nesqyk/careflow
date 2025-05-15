package edu.careflow.presentation.controllers.patient.forms;

import com.dlsc.gemsfx.CalendarPicker;
import edu.careflow.repository.dao.PatientDAO;
import edu.careflow.repository.entities.Patient;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import org.controlsfx.control.CheckComboBox;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;

public class AddPatientForm {

    @FXML private Button addPatientBtn;
    @FXML private TextField addressField;
    @FXML private VBox avatarContainer;
    @FXML private Button cancelBtn;
    @FXML private Button closeBtn;
    @FXML private CalendarPicker dateOfBirthField;
    @FXML private FontIcon defaultAvatarIcon;
    @FXML private TextField emailAddressField;
    @FXML private CheckComboBox<String> emergencyBoxField;
    @FXML private TextField emergencyContactField;
    @FXML private TextField emergencyNameField;
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField middleInitialField;
    @FXML private Label patientHeaderLabel;
    @FXML private TextField patientNumberField;
    @FXML private ToggleGroup sexRadio;
    @FXML private Button uploadAvatarBtn;
    @FXML private ScrollPane scrollPaneForm;

    private PatientDAO patientDAO;
    private File selectedImageFile;
    private ImageView avatarImageView;
    private int patientId;
    private boolean isEditMode = false;

    @FXML
    public void initialize() {
        patientDAO = new PatientDAO();
        setupAvatarContainer();
        setupEmergencyBox();
        setupButtons();
        setupValidation();
        
        // Change button text and behavior for edit mode
        if (patientId > 0) {
            isEditMode = true;
            addPatientBtn.setText("Save Changes");
            patientHeaderLabel.setText("Edit Patient");
            System.out.println("Edit mode activated for patient ID: " + patientId);
        } else {
            isEditMode = false;
            System.out.println("Add mode activated");
        }
    }

    private void setupAvatarContainer() {
        avatarImageView = new ImageView();
        avatarImageView.setFitHeight(100);
        avatarImageView.setFitWidth(100);
        avatarImageView.setPreserveRatio(true);
        avatarContainer.getChildren().add(0, avatarImageView);
        defaultAvatarIcon.setVisible(false);
    }

    private void setupEmergencyBox() {
        emergencyBoxField.getItems().addAll(
            "Spouse", "Parent", "Sibling", "Child", 
            "Relative", "Friend", "Other"
        );
    }

    private void setupButtons() {
        uploadAvatarBtn.setOnAction(e -> handleAvatarUpload());
        addPatientBtn.setOnAction(e -> handleAddPatient());
        cancelBtn.setOnAction(e -> handleCancel());
        closeBtn.setOnAction(e -> handleClose());
    }

    private void setupValidation() {
        // Add listeners to validate fields in real-time
        firstNameField.textProperty().addListener((obs, oldVal, newVal) -> validateField(firstNameField, !newVal.trim().isEmpty()));
        lastNameField.textProperty().addListener((obs, oldVal, newVal) -> validateField(lastNameField, !newVal.trim().isEmpty()));
        emailAddressField.textProperty().addListener((obs, oldVal, newVal) -> validateField(emailAddressField, newVal.matches("^[A-Za-z0-9+_.-]+@(.+)$")));
        patientNumberField.textProperty().addListener((obs, oldVal, newVal) -> validateField(patientNumberField, newVal.matches("^\\d{10,11}$")));
    }

    private void validateField(TextField field, boolean isValid) {
        field.setStyle(isValid ? "" : "-fx-border-color: red;");
    }

    private void handleAvatarUpload() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Profile Picture");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        selectedImageFile = fileChooser.showOpenDialog(uploadAvatarBtn.getScene().getWindow());
        if (selectedImageFile != null) {
            Image image = new Image(selectedImageFile.toURI().toString());
            avatarImageView.setImage(image);
            defaultAvatarIcon.setVisible(false);
        }
    }

    private void handleAddPatient() {
        if (!validateForm()) {
            showError("Please fill in all required fields correctly.");
            return;
        }

        try {
            System.out.println("Current mode: " + (isEditMode ? "Edit" : "Add") + " for patient ID: " + patientId);
            Patient patient = createPatientFromForm();
            
            if (isEditMode) {
                // Update existing patient
                System.out.println("Attempting to update patient with ID: " + patientId);
                boolean success = patientDAO.updatePatient(patientId, patient);
                if (success) {
                    // Update avatar if a new one was selected
                    if (selectedImageFile != null) {
                        try {
                            patientDAO.insertAvatar(patientId, selectedImageFile);
                        } catch (IOException e) {
                            showError("Error updating avatar: " + e.getMessage());
                            return;
                        }
                    }
                    showSuccess("Patient updated successfully!");
                    handleClose();
                } else {
                    showError("Failed to update patient");
                }
            } else {
                // Insert new patient
                System.out.println("Attempting to insert new patient");
                int newPatientId = patientDAO.insertPatient(patient);
                if (newPatientId > 0) {
                    // Insert avatar if one was selected
                    if (selectedImageFile != null) {
                        try {
                            patientDAO.insertAvatar(newPatientId, selectedImageFile);
                        } catch (IOException e) {
                            showError("Error uploading avatar: " + e.getMessage());
                            return;
                        }
                    }
                    showSuccess("Patient added successfully!");
                    handleClose();
                } else {
                    showError("Failed to add patient");
                }
            }
        } catch (SQLException e) {
            showError("Error " + (isEditMode ? "updating" : "adding") + " patient: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private boolean validateForm() {
        boolean isValid = true;
        
        // Required fields validation
        if (firstNameField.getText().trim().isEmpty()) {
            validateField(firstNameField, false);
            isValid = false;
        }
        if (lastNameField.getText().trim().isEmpty()) {
            validateField(lastNameField, false);
            isValid = false;
        }
        if (dateOfBirthField.getValue() == null) {
            dateOfBirthField.setStyle("-fx-border-color: red;");
            isValid = false;
        }
        if (sexRadio.getSelectedToggle() == null) {
            isValid = false;
        }
        if (!emailAddressField.getText().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            validateField(emailAddressField, false);
            isValid = false;
        }
        if (!patientNumberField.getText().matches("^\\d{10,11}$")) {
            validateField(patientNumberField, false);
            isValid = false;
        }

        return isValid;
    }

    private Patient createPatientFromForm() {
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String middleInitial = middleInitialField.getText().trim();
        LocalDate dateOfBirth = dateOfBirthField.getValue();
        String gender = ((RadioButton) sexRadio.getSelectedToggle()).getText();
        String contact = patientNumberField.getText().trim();
        String email = emailAddressField.getText().trim();
        String address = addressField.getText().trim();

        return new Patient(
            isEditMode ? patientId : 0, // Use existing ID in edit mode
            firstName,
            lastName,
            dateOfBirth,
            gender,
            contact,
            email,
            address,
            null, // createdAt will be set by database
            null  // updatedAt will be set by database
        );
    }

    private void showError(String message) {
        Scene scene = scrollPaneForm.getScene();
        if (scene != null) {
            StackPane container = (StackPane) scene.lookup("#stackPaneContainer");
            if (container != null) {
                Label errorLabel = new Label(message);
                errorLabel.getStyleClass().add("error-label");
                errorLabel.setStyle(
                    "-fx-background-color: #f44336;" +
                    "-fx-text-fill: white;" +
                    "-fx-padding: 10 20;" +
                    "-fx-background-radius: 5;" +
                    "-fx-translate-y: 300; -fx-font-family: 'Gilroy-SemiBold'; -fx-font-size: 16px"
                );

                // Add fade-in animation
                FadeTransition fadeIn = new FadeTransition(Duration.millis(300), errorLabel);
                fadeIn.setFromValue(0.0);
                fadeIn.setToValue(1.0);

                container.getChildren().add(errorLabel);
                fadeIn.play();

                // Remove label after 3 seconds
                Timeline timeline = new Timeline(
                    new KeyFrame(Duration.seconds(2), e -> {
                        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), errorLabel);
                        fadeOut.setFromValue(1.0);
                        fadeOut.setToValue(0.0);
                        fadeOut.setOnFinished(event -> container.getChildren().remove(errorLabel));
                        fadeOut.play();
                    })
                );
                timeline.play();
            }
        }
    }

    private void showSuccess(String message) {
        Scene scene = scrollPaneForm.getScene();
        if (scene != null) {
            StackPane container = (StackPane) scene.lookup("#stackPaneContainer");
            if (container != null) {
                Label successLabel = new Label(message);
                successLabel.getStyleClass().add("success-label");
                successLabel.setStyle(
                    "-fx-background-color: #4CAF50;" +
                    "-fx-text-fill: white;" +
                    "-fx-padding: 10 20;" +
                    "-fx-background-radius: 5;" +
                    "-fx-translate-y: 300; -fx-font-family: 'Gilroy-SemiBold'; -fx-font-size: 16px"
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
    }

    private void handleCancel() {
        // Reset form fields
        firstNameField.clear();
        lastNameField.clear();
        middleInitialField.clear();
        dateOfBirthField.setValue(null);
        sexRadio.selectToggle(null);
        patientNumberField.clear();
        emailAddressField.clear();
        addressField.clear();
        emergencyNameField.clear();
        emergencyContactField.clear();
        emergencyBoxField.getCheckModel().clearChecks();
        avatarImageView.setImage(null);
        defaultAvatarIcon.setVisible(true);
        selectedImageFile = null;
    }

    private void handleClose() {
        // Get the parent container of scrollPaneForm
        FadeTransition fadeOut = new FadeTransition(Duration.millis(150), scrollPaneForm);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> {
            if (scrollPaneForm.getParent() instanceof VBox parentBox) {
                parentBox.getChildren().remove(scrollPaneForm);
            }
            // Set to null to help garbage collection
            scrollPaneForm = null;
        });
        fadeOut.play();
    }

    public int getPatientId() {
        return this.patientId;
    }

    public void setPatientId(int patientId) {
        this.patientId = patientId;
        System.out.println("Patient ID set to: " + patientId);
        // Update edit mode status when patient ID is set
        if (patientId > 0) {
            isEditMode = true;
            if (addPatientBtn != null) {
                addPatientBtn.setText("Save Changes");
            }
            if (patientHeaderLabel != null) {
                patientHeaderLabel.setText("Edit Patient");
            }
        }
    }

    public void setFirstName(String firstName) {
        firstNameField.setText(firstName);
    }

    public void setLastName(String lastName) {
        lastNameField.setText(lastName);
    }

    public void setDateOfBirth(LocalDate dateOfBirth) {
        dateOfBirthField.setValue(dateOfBirth);
    }

    public void setGender(String gender) {
        sexRadio.getToggles().stream()
            .filter(toggle -> ((RadioButton) toggle).getText().equals(gender))
            .findFirst()
            .ifPresent(toggle -> sexRadio.selectToggle(toggle));
    }

    public void setContact(String contact) {
        patientNumberField.setText(contact);
    }

    public void setEmail(String email) {
        emailAddressField.setText(email);
    }

    public void setAddress(String address) {
        addressField.setText(address);
    }
}
