package edu.careflow.presentation.controllers.components.user;

import edu.careflow.presentation.controllers.Controller;
import edu.careflow.repository.dao.UserDAO;
import edu.careflow.repository.dao.DoctorDAO;
import edu.careflow.repository.dao.NurseDAO;
import edu.careflow.repository.entities.User;
import edu.careflow.presentation.controllers.patient.forms.AddUserForm;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.VBox;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.Modality;

import java.io.IOException;
import java.sql.SQLException;

public class UserDetailsCardController extends Controller {
    @FXML private VBox detailsContainer;
    @FXML private Label userFullName;
    @FXML private Label userRole;
    @FXML private Label userUsername;
    @FXML private Label userId;
    @FXML private Button editButton;
    @FXML private Button deleteButton;

    private User user;
    private UserDAO userDAO;
    private DoctorDAO doctorDAO;
    private NurseDAO nurseDAO;
    private Runnable onUserDeleted;

    @Override
    public void initializeData(int userId) {
        // Not needed for user details card
    }

    public void setUser(User user) {
        this.user = user;

        // set style for both edit and dlete into "action"
        editButton.getStyleClass().add("action-button");
        deleteButton.getStyleClass().add("action-button");

        System.out.println(user.getUser_id());
        if (user != null) {
            userFullName.setText(user.getFirstName() + " " + user.getLastName());
            userRole.setText(getRoleName(user.getRoleId()));
            userUsername.setText(user.getUsername());
            userId.setText(String.valueOf(user.getUser_id()));
        }
    }

    public void setOnUserDeleted(Runnable callback) {
        this.onUserDeleted = callback;
    }

    private String getRoleName(int roleId) {
        switch (roleId) {
            case 1: return "Nurse";
            case 2: return "Admin/Receptionist";
            case 4: return "Doctor";
            default: return "Unknown";
        }
    }

    @FXML
    private void handleEdit() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/edu/careflow/fxml/components/user/addUserForm.fxml"));
            Parent addUserForm = loader.load();
            AddUserForm controller = loader.getController();
            controller.setUserForEditing(user);

            // Traverse up to find the rightBoxContainer
            VBox parent = detailsContainer;
            while (parent != null && !(parent.getScene().lookup("#rightBoxContainer") instanceof VBox)) {
                parent = (VBox) parent.getParent();
            }
            VBox rightBox = null;
            if (parent != null && parent.getScene() != null) {
                rightBox = (VBox) parent.getScene().lookup("#rightBoxContainer");
            }
            if (rightBox != null) {
                rightBox.getChildren().clear();
                rightBox.getChildren().add(addUserForm);
            } else {
                showError("Could not find rightBoxContainer in the scene.");
            }
        } catch (IOException e) {
            showError("Error loading edit form: " + e.getMessage());
        }
    }

    @FXML
    private void handleDelete() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirm Delete");
        alert.setHeaderText("Delete User");
        alert.setContentText("Are you sure you want to delete this user? This action cannot be undone.");

        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    if (userDAO == null) {
                        userDAO = new UserDAO();
                    }
                    if (doctorDAO == null) {
                        doctorDAO = new DoctorDAO();
                    }
                    if (nurseDAO == null) {
                        nurseDAO = new NurseDAO();
                    }
                    
                    // Delete from role-specific table first
                    boolean roleDeleted = true;
                    if (user.getRoleId() == 4) { // Doctor
                        roleDeleted = doctorDAO.deleteDoctor(String.valueOf(user.getUser_id()));
                    } else if (user.getRoleId() == 1) { // Nurse
                        // TODO: Implement deleteNurse in NurseDAO
                        // For now, we'll just delete the user
                        roleDeleted = true;
                    }
                    
                    if (!roleDeleted) {
                        showError("Failed to delete role-specific data. Please try again.");
                        return;
                    }
                    
                    // Delete the user
                    boolean success = userDAO.deleteUser(user.getUser_id());
                    
                    if (success) {
                        // Remove this card from its parent container
                        if (detailsContainer != null && detailsContainer.getParent() != null) {
                            VBox parent = (VBox) detailsContainer.getParent();
                            parent.getChildren().remove(detailsContainer);
                        }
                        
                        // Notify parent component to refresh the user list
                        if (onUserDeleted != null) {
                            onUserDeleted.run();
                        }
                    } else {
                        showError("Failed to delete user. Please try again.");
                    }
                } catch (SQLException e) {
                    showError("Error deleting user: " + e.getMessage());
                }
            }
        });
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
} 