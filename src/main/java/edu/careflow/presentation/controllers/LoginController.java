package edu.careflow.presentation.controllers;

import edu.careflow.manager.AuthManager;
import edu.careflow.presentation.controllers.patient.PatientContainerController;
import edu.careflow.repository.dao.UserDAO;
import edu.careflow.repository.entities.User;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;

public class LoginController  {

    // Fields for Users
    @FXML public TextField usernameTextInput;
    @FXML public PasswordField passwordTextInput;
    @FXML public Button loginButton;
    @FXML public VBox usernameContainer;
    @FXML public VBox passwordContainer;
    @FXML public VBox loginContainer;
    @FXML public CheckBox showPasswordCheckBox;


    public AuthManager authManager = new AuthManager();
    public UserDAO userDAO = new UserDAO();
    private boolean errorDisplayed = false;

    // Regular User Login Methods
    @FXML
    protected void onClickLoginButton(ActionEvent event) throws SQLException {
        loginContainer.getChildren().removeIf(node -> "loginErrorText".equals(node.getId()));
        errorDisplayed = false; // Reset the error flag

        String username = usernameTextInput.getText();
        String password = passwordTextInput.getText();

        if (username == null || username.isEmpty()) {
            if (!errorDisplayed) {
                showError("username", "Please enter your username");
                errorDisplayed = true;
            }
        } else if (password == null || password.isEmpty()) {
            if (!errorDisplayed) {
                showError("password", "Please enter your password");
                errorDisplayed = true;
            }
        } else {
            try {
                User user = userDAO.getUserByUsername(username);
                int roleID = user.getRoleId();

                switch(roleID) {
                    case 0: // Doctor
                        // Handle doctor dashboard
                        break;
                    case 1: // Patient
                        try {
                            FXMLLoader loader = new FXMLLoader(getClass().getResource("/edu/careflow/fxml/patient/patientContainer.fxml"));
                            Parent root = loader.load();

                            PatientContainerController controller = loader.getController();
                            controller.initializePatientData(username);

                            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

                            // Apply fade transition
                            FadeTransition fadeTransition = new FadeTransition(Duration.seconds(0.5), root);
                            fadeTransition.setFromValue(0.0);
                            fadeTransition.setToValue(1.0);

                            Scene scene = new Scene(root);
                            stage.setScene(scene);
                            stage.setTitle("Patient Dashboard");

                            fadeTransition.play();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    case 2: // Admin
                        // Handle admin dashboard
                        break;
                    case 3: // Nurse
                        // Handle nurse dashboard
                        break;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    protected void navigateToPatientLogin(ActionEvent event) throws IOException {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/edu/careflow/fxml/patientLoginPage.fxml"));

            URL resource = getClass().getResource("/edu/careflow/fxml/patientLoginPage.fxml");
            if (resource == null) {
                throw new IOException("FXML file not found");
            }

            Parent root = loader.load();

            Node source = (Node) event.getSource();
            Stage stage = (Stage) source.getScene().getWindow();

            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle("Patient Login");

            stage.centerOnScreen();

        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Loading Error");
            alert.setHeaderText("Cannot load patient login page");
            alert.setContentText("The system could not load the requested page. Please try again.");
            alert.showAndWait();

            System.err.println("Failed to load FXML: " + e.getMessage());
            e.printStackTrace();
        }
    }


    // Password Visibility Methods
    @FXML
    protected void showPasswordEvent() {
        Tooltip toolTip = new Tooltip();
        toolTip.setShowDelay(Duration.ZERO);
        toolTip.setAutoHide(false);
        toolTip.setMinWidth(50);

        showPasswordCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                showPasswordTooltip(toolTip);
            } else {
                toolTip.hide();
            }
        });

        passwordTextInput.setOnKeyTyped(event -> {
            if (showPasswordCheckBox.isSelected()) {
                showPasswordTooltip(toolTip);
            }
        });
    }

    private void showPasswordTooltip(Tooltip toolTip) {
        Point2D p = passwordTextInput.localToScene(passwordTextInput.getBoundsInLocal().getMaxX(), passwordTextInput.getBoundsInLocal().getMaxY());
        toolTip.setText(passwordTextInput.getText());
        toolTip.show(passwordTextInput,
                p.getX() + passwordTextInput.getScene().getWindow().getX(),
                p.getY() + passwordTextInput.getScene().getWindow().getY());
    }

    // Error Handling Methods
    private void showError(String type, String errorMessage) {
        clearError();

        if (errorMessage != null) {
            switch (type) {
                case "username":
                    usernameTextInput.getStyleClass().add("error-border");
                    Text usernameError = new Text(errorMessage);
                    usernameError.setId("usernameErrorText");
                    usernameError.setStyle("-fx-fill: red;");
                    usernameError.setFont(Font.font("Gilroy-Regular", 16));
                    usernameContainer.getChildren().add(usernameError);
                    break;
                case "password":
                    passwordTextInput.getStyleClass().add("error-border");
                    Text passwordError = new Text(errorMessage);
                    passwordError.setStyle("-fx-fill: red");
                    passwordError.setId("passwordErrorText");
                    passwordError.setFont(Font.font("Gilroy-Regular", 16));
                    passwordContainer.getChildren().add(passwordError);
                    break;
            }

            PauseTransition pauseTransition = new PauseTransition(Duration.seconds(2));
            pauseTransition.setOnFinished(event -> clearError());
            pauseTransition.play();
        }
    }

    private void clearError() {
        usernameTextInput.getStyleClass().remove("error-border");
        passwordTextInput.getStyleClass().remove("error-border");
        usernameContainer.getChildren().removeIf(node -> node instanceof Text);
        passwordContainer.getChildren().removeIf(node -> node instanceof Text);
        errorDisplayed = false;
    }
}