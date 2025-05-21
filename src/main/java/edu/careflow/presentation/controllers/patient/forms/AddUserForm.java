package edu.careflow.presentation.controllers.patient.forms;

import com.dlsc.gemsfx.EnhancedPasswordField;
import edu.careflow.repository.dao.DoctorDAO;
import edu.careflow.repository.dao.NurseDAO;
import edu.careflow.repository.dao.UserDAO;
import edu.careflow.repository.entities.Doctor;
import edu.careflow.repository.entities.Nurse;
import edu.careflow.repository.entities.User;
import edu.careflow.repository.entities.User.Role;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ResourceBundle;

public class AddUserForm implements Initializable {
    // Demographics
    @FXML private TextField usernameField;
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField middleInitialField;

    // Authentication
    @FXML private VBox passwordContainer;
    @FXML private VBox passwordConfirmField;

    // Role selection
    @FXML private ToggleGroup roleRadio;
    @FXML private RadioButton doctorRadio;
    @FXML private RadioButton nurseRadio;
    @FXML private RadioButton adminReceptionistRadio;
    @FXML private VBox roleDetailsContainer;
    
    @FXML
    private VBox doctorDetails;
    
    @FXML
    private VBox nurseDetails;
    
    @FXML
    private ComboBox<String> specializationComboBox;
    
    @FXML
    private TextField uniqueIdField;
    
    @FXML
    private TextField yrLicensureField;
    
    @FXML
    private TextField contactField;
    
    @FXML
    private TextField nurseDepartmentField;

    // Action buttons
    @FXML private Button cancelBtn;
    @FXML private Button addUserBtn;
    @FXML private Button closeBtn;

    @FXML private ScrollPane scrollPaneContainer;

    private EnhancedPasswordField passwordField;
    private EnhancedPasswordField confirmPasswordField;

    private final UserDAO userDAO = new UserDAO();
    private final DoctorDAO doctorDAO = new DoctorDAO();
    private final NurseDAO nurseDAO = new NurseDAO();

    private User editingUser;
    private boolean isEditing = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Initialize password fields
        passwordField = new EnhancedPasswordField();
        passwordField.setPromptText("Enter password");
        passwordContainer.getChildren().add(passwordField);

        confirmPasswordField = new EnhancedPasswordField();
        confirmPasswordField.setPromptText("Confirm password");
        this.passwordConfirmField.getChildren().add(confirmPasswordField);

        // Initialize specialization ComboBox
        specializationComboBox.getItems().addAll(
            "Pediatrics",
            "Cardiology",
            "Dermatology",
            "Neurology",
            "Orthopedics",
            "Gynecology",
            "Ophthalmology",
            "ENT",
            "Dentistry"
        );

        // Set up role selection listener
        roleRadio.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == doctorRadio) {
                doctorDetails.setVisible(true);
                nurseDetails.setVisible(false);
            } else if (newVal == nurseRadio) {
                doctorDetails.setVisible(false);
                nurseDetails.setVisible(true);
            } else {
                doctorDetails.setVisible(false);
                nurseDetails.setVisible(false);
            }
        });

        // Set up button actions
        addUserBtn.setOnAction(e -> handleAddUser());
        cancelBtn.setOnAction(e -> handleCancel());
        closeBtn.setOnAction(e -> handleClose());
    }

    public void setUserForEditing(User user) {
        this.editingUser = user;
        this.isEditing = true;
        
        // Set form fields with user data
        usernameField.setText(user.getUsername());
        firstNameField.setText(user.getFirstName());
        lastNameField.setText(user.getLastName());
        
        // Set role radio button
        switch (user.getRoleId()) {
            case 0: // Doctor
                doctorRadio.setSelected(true);
                try {
                    Doctor doctor = doctorDAO.getDoctorById(String.valueOf(user.getUser_id()));
                    if (doctor != null) {
                        specializationComboBox.setValue(doctor.getSpecialization());
                        uniqueIdField.setText(doctor.getLicenseNumber());
                        contactField.setText(doctor.getContactNumber());
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                break;
            case 1: // Nurse
                nurseRadio.setSelected(true);
                try {
                    Nurse nurse = nurseDAO.getNurseById(user.getUser_id());
                    if (nurse != null) {
                        uniqueIdField.setText(nurse.getLicenseNumber());
                        nurseDepartmentField.setText(nurse.getPhone());
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
                break;
            case 2: // Admin/Receptionist
                adminReceptionistRadio.setSelected(true);
                break;
        }
        
        // Update button text
        addUserBtn.setText("Save Changes");
    }

    private boolean isDoctorDetailsComplete() {
        return specializationComboBox.getValue() != null 
               && specializationComboBox.getValue().trim().length() > 0
               && uniqueIdField.getText() != null 
               && !uniqueIdField.getText().trim().isEmpty()
               && yrLicensureField.getText() != null
               && !yrLicensureField.getText().trim().isEmpty();
    }

    private void handleAddUser() {
        // Validate required fields
        if (usernameField.getText().isEmpty() || 
            firstNameField.getText().isEmpty() || 
            lastNameField.getText().isEmpty() || 
            (!isEditing && (passwordField.getText().isEmpty() || confirmPasswordField.getText().isEmpty()))) {
            showError("Please fill in all required fields");
            return;
        }

        // Validate password match for new users
        if (!isEditing && !passwordField.getText().equals(confirmPasswordField.getText())) {
            showError("Passwords do not match");
            return;
        }

        // Get selected role
        RadioButton selectedRole = (RadioButton) roleRadio.getSelectedToggle();
        if (selectedRole == null) {
            showError("Please select a role");
            return;
        }

        try {
            if (isEditing) {
                // Update existing user
                editingUser.setUser_id(editingUser.getUser_id());
                editingUser.setPassword(passwordField.getText());
                
                userDAO.updateUser(editingUser.getUser_id(), editingUser);
                
                // Update role-specific details
                if (selectedRole == doctorRadio) {
                    Doctor doctor = doctorDAO.getDoctorById(String.valueOf(editingUser.getUser_id()));
                    if (doctor != null) {
                        doctor = new Doctor(
                            doctor.getDoctorId(),
                            firstNameField.getText(),
                            lastNameField.getText(),
                            specializationComboBox.getValue(),
                            uniqueIdField.getText(),
                            contactField.getText(),
                            doctor.getCreatedAt(),
                            LocalDateTime.now(),
                            doctor.isAvailable()
                        );
                        doctorDAO.updateDoctor(doctor);
                    }
                } else if (selectedRole == nurseRadio) {
                    Nurse nurse = nurseDAO.getNurseById(editingUser.getUser_id());
                    if (nurse != null) {
                        nurse.setFirstName(firstNameField.getText());
                        nurse.setLastName(lastNameField.getText());
                        nurse.setLicenseNumber(uniqueIdField.getText());
                        nurse.setPhone(nurseDepartmentField.getText());
                        nurseDAO.updateNurse(nurse);
                    }
                }
                
                showSuccess("Successfully Updated User");
            } else {
                // Create new user
                if (selectedRole == doctorRadio) {
                    int generatedId = userDAO.generateRandomUserId();
                    Doctor doctor = new Doctor(
                        generatedId,
                        firstNameField.getText(),
                        lastNameField.getText(),
                        specializationComboBox.getValue(),
                        uniqueIdField.getText(),
                        contactField.getText(),
                        LocalDateTime.now(),
                        LocalDateTime.now(),
                        true
                    );
                    doctorDAO.createDoctor(doctor);
                    
                    // Create the user record
                    User user = new User(
                        generatedId,
                        passwordField.getText(),
                        usernameField.getText(),
                        Role.DOCTOR.ordinal(),
                        0,
                        firstNameField.getText(),
                        lastNameField.getText()
                    );
                    userDAO.insertUser(user);
                } else if (selectedRole == nurseRadio) {
                    int generatedId = userDAO.generateRandomUserId();
                    Nurse nurse = new Nurse(
                        generatedId,
                        firstNameField.getText(),
                        lastNameField.getText(),
                        uniqueIdField.getText(),
                        nurseDepartmentField.getText(),
                        LocalDateTime.now().toLocalDate()
                    );
                    nurseDAO.createNurse(nurse);
                    
                    // Create the user record
                    User user = new User(
                        generatedId,
                        passwordField.getText(),
                        usernameField.getText(),
                        Role.NURSE.ordinal(),
                        0,
                        firstNameField.getText(),
                        lastNameField.getText()
                    );
                    userDAO.insertUser(user);
                } else {
                    int generatedId = userDAO.generateRandomUserId();
                    
                    // Create the user record for admin/receptionist
                    User user = new User(
                        generatedId,
                        passwordField.getText(),
                        usernameField.getText(),
                        Role.ADMIN.ordinal(),
                        0,
                        firstNameField.getText(),
                        lastNameField.getText()
                    );
                    userDAO.insertUser(user);
                }
                showSuccess("Successfully Added User");
            }

            // Clear form and close
            handleCancel();
            handleClose();
        } catch (Exception e) {
            showError("Error " + (isEditing ? "updating" : "creating") + " user: " + e.getMessage());
        }
    }

    private void handleCancel() {
        // Clear all fields
        usernameField.clear();
        firstNameField.clear();
        lastNameField.clear();
        middleInitialField.clear();
        passwordField.clear();
        confirmPasswordField.clear();
        specializationComboBox.setValue(null);
        uniqueIdField.clear();
        yrLicensureField.clear();
        contactField.clear();
        nurseDepartmentField.clear();
        roleRadio.selectToggle(null);
        doctorDetails.setVisible(false);
        nurseDetails.setVisible(false);
        
        // Reset editing state
        isEditing = false;
        editingUser = null;
        addUserBtn.setText("Add User");
    }

    private void handleClose(ActionEvent... event) {
        if (scrollPaneContainer == null) return;
        
        // Find the parent container
        final Node parent = scrollPaneContainer.getParent();
        if (parent == null) {
            // If we can't find the parent, try to find it through the scene
            Scene scene = usernameField.getScene();
            if (scene != null) {
                StackPane container = (StackPane) scene.lookup("#stackPaneContainer");
                if (container != null) {
                    container.getChildren().remove(scrollPaneContainer);
                    return;
                }
            }
        }

        if (parent != null) {
            // Create fade out animation
            FadeTransition fadeOut = new FadeTransition(Duration.millis(150), scrollPaneContainer);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            fadeOut.setOnFinished(e -> {
                if (parent instanceof VBox) {
                    ((VBox) parent).getChildren().remove(scrollPaneContainer);
                } else if (parent instanceof Pane) {
                    ((Pane) parent).getChildren().remove(scrollPaneContainer);
                }
            });
            fadeOut.play();
        } else {
            // If we can't find the parent, just remove the form directly
            if (scrollPaneContainer.getParent() != null) {
                ((Pane) scrollPaneContainer.getParent()).getChildren().remove(scrollPaneContainer);
            }
        }
    }

    private void showError(String msg) {
        // Print stack trace for debugging
        System.err.println("Error: " + msg);
        new Exception().printStackTrace();

        Scene scene = usernameField.getScene();
        if (scene != null) {
            StackPane container = (StackPane) scene.lookup("#stackPaneContainer");
            if (container != null) {
                Label errorLabel = new Label(msg);
                errorLabel.getStyleClass().add("error-label");
                errorLabel.setStyle("-fx-background-color: #f44336; -fx-text-fill: white; -fx-padding: 10 20; -fx-background-radius: 5; -fx-translate-y: 300; -fx-font-family: 'Gilroy-SemiBold'; -fx-font-size: 16px");

                FadeTransition fadeIn = new FadeTransition(Duration.millis(300), errorLabel);
                fadeIn.setFromValue(0.0);
                fadeIn.setToValue(1.0);

                container.getChildren().add(errorLabel);
                fadeIn.play();

                Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(3), e -> {
                    FadeTransition fadeOut = new FadeTransition(Duration.millis(300), errorLabel);
                    fadeOut.setFromValue(1.0);
                    fadeOut.setToValue(0.0);
                    fadeOut.setOnFinished(event -> container.getChildren().remove(errorLabel));
                    fadeOut.play();
                }));
                timeline.play();
            }
        }
    }

    private void showSuccess(String msg) {
        // Print stack trace for debugging
        Scene scene = usernameField.getScene();
        if (scene != null) {
            StackPane container = (StackPane) scene.lookup("#stackPaneContainer");
            if (container != null) {
                Label errorLabel = new Label(msg);
                errorLabel.getStyleClass().add("error-label");
                errorLabel.setStyle("-fx-background-color: green; -fx-text-fill: white; -fx-padding: 10 20; -fx-background-radius: 5; -fx-translate-y: 300; -fx-font-family: 'Gilroy-SemiBold'; -fx-font-size: 16px");

                FadeTransition fadeIn = new FadeTransition(Duration.millis(300), errorLabel);
                fadeIn.setFromValue(0.0);
                fadeIn.setToValue(1.0);

                container.getChildren().add(errorLabel);
                fadeIn.play();

                Timeline timeline = new Timeline(new KeyFrame(Duration.seconds(3), e -> {
                    FadeTransition fadeOut = new FadeTransition(Duration.millis(300), errorLabel);
                    fadeOut.setFromValue(1.0);
                    fadeOut.setToValue(0.0);
                    fadeOut.setOnFinished(event -> container.getChildren().remove(errorLabel));
                    fadeOut.play();
                }));
                timeline.play();
            }
        }
    }
}
