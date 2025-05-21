package edu.careflow.presentation.controllers;

import com.dlsc.gemsfx.EnhancedPasswordField;
import edu.careflow.manager.AuthManager;
import edu.careflow.presentation.controllers.doctor.DoctorContainerController;
import edu.careflow.presentation.controllers.nurse.ContainerNurseController;
import edu.careflow.presentation.controllers.receptionist.ReceptionistContainerController;
import edu.careflow.repository.dao.UserDAO;
import edu.careflow.repository.entities.User;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
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
    @FXML private EnhancedPasswordField passwordTextInput;
    @FXML public Button loginButton;
    @FXML public VBox usernameContainer;
    @FXML public VBox passwordContainer;
    @FXML public VBox loginContainer;
    @FXML public CheckBox showPasswordCheckBox;


    public AuthManager authManager = new AuthManager();
    public UserDAO userDAO = new UserDAO();
    private boolean errorDisplayed = false;

    @FXML
    public void initialize() {
        passwordTextInput = new EnhancedPasswordField();
        passwordTextInput.setStyle("-fx-font-size: 16px; -fx-font-family: 'Gilroy-Regular';");
        passwordTextInput.setPrefHeight(42);

        passwordTextInput.setPromptText("Enter password");

        // Make sure you add it to your UI layout manually if FXML no longer references it
        passwordContainer.getChildren().add( passwordTextInput);
    }

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
                User user = userDAO.validateUser(username, password);

                if(user == null){
                    passwordTextInput.getStyleClass().add("error-border");
                    usernameTextInput.getStyleClass().add("error-border");

                    Text errorInfo = new Text("Login failed. Incorrect password or username");
                    errorInfo.setStyle("-fx-fill: red; -fx-padding: 32px");
                    errorInfo.setFont(Font.font("Gilroy-Regular", 16));
                    errorInfo.setId("loginErrorText");
                    loginContainer.getChildren().add(errorInfo);
                }

                int roleID = user != null ? user.getRoleId() : 0;

                switch(roleID) {
                    case 0: // Doctor role
                        try {
                            Stage stage = (Stage) usernameTextInput.getScene().getWindow();

                            FXMLLoader loader = new FXMLLoader(getClass().getResource("/edu/careflow/fxml/doctor/doctorContainerNew.fxml"));
                            Parent doctorRoot = loader.load();
                            Scene doctorScene = new Scene(doctorRoot, stage.getScene().getWidth(), stage.getScene().getHeight());

                            FadeTransition fadeIn = new FadeTransition(Duration.millis(400), doctorRoot);
                            fadeIn.setFromValue(0.2);
                            fadeIn.setToValue(1.0);

                           Object controller = loader.getController();

                           if(controller instanceof DoctorContainerController) {
                               ((DoctorContainerController) controller).initializeData(user.getUser_id());
                           }


                            stage.setScene(doctorScene);
                            stage.setTitle("CareFlow | Doctor Dashboard");
                            stage.centerOnScreen();

                            fadeIn.play();
                        } catch(IOException e) {
                            handleNavigationError(e, "Doctor Dashboard");
                        }
                        break;
                    case 1: // Nurse role
                        try {
                            Stage stage = (Stage) usernameTextInput.getScene().getWindow();

                            FXMLLoader loader = new FXMLLoader(getClass().getResource("/edu/careflow/fxml/nurse/containerNurse.fxml"));
                            Parent nurseRoot = loader.load();
                            Scene nurseScene = new Scene(nurseRoot, stage.getScene().getWidth(), stage.getScene().getHeight());

                            FadeTransition fadeIn = new FadeTransition(Duration.millis(400), nurseRoot);
                            fadeIn.setFromValue(0.2);
                            fadeIn.setToValue(1.0);

                            Object controller = loader.getController();

                            if(controller instanceof ContainerNurseController) {
                                ((ContainerNurseController) controller).setNurseId(user.getUser_id());
                            }

                            stage.setScene(nurseScene);
                            stage.setTitle("CareFlow | Nurse Dashboard");
                            stage.centerOnScreen();

                            fadeIn.play();
                        } catch(IOException e) {
                            handleNavigationError(e, "Nurse Dashboard");
                        }
                        break;
                    case 2: // Receptionist role
                        try {
                            Stage stage = (Stage) usernameTextInput.getScene().getWindow();

                            FXMLLoader loader = new FXMLLoader(getClass().getResource("/edu/careflow/fxml/receptionist/containerReceptionistNew.fxml"));
                            Parent receptionistRoot = loader.load();
                            Scene receptionistScene = new Scene(receptionistRoot, stage.getScene().getWidth(), stage.getScene().getHeight());

                            FadeTransition fadeIn = new FadeTransition(Duration.millis(400), receptionistRoot);
                            fadeIn.setFromValue(0.2);
                            fadeIn.setToValue(1.0);

                            Object controller = loader.getController();

                            if(controller instanceof ReceptionistContainerController) {
                                ((ReceptionistContainerController) controller).initializeData(user.getFirstName() + " " + user.getLastName(), "Receptionist");
                            }

                            stage.setScene(receptionistScene);
                            stage.setTitle("CareFlow | Receptionist Dashboard");
                            stage.centerOnScreen();

                            fadeIn.play();
                        } catch(IOException e) {
                            handleNavigationError(e, "Receptionist Dashboard");
                        }
                        break;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    protected void navigateToPatientLogin(ActionEvent event) throws IOException {
        Node sourceNode = (Node) event.getSource();
        Scene currentScene = sourceNode.getScene();
        
        // Create a loading indicator
        ProgressIndicator progressIndicator = new ProgressIndicator();
        progressIndicator.setMaxSize(50, 50);
        progressIndicator.setStyle("-fx-progress-color: #2196F3;");
        
        // Add loading indicator to the current scene - now handling any root pane type
        Parent root = currentScene.getRoot();
        
        // Position the progress indicator in the center of the scene
        if (root instanceof AnchorPane) {
            AnchorPane anchorRoot = (AnchorPane) root;
            AnchorPane.setTopAnchor(progressIndicator, (root.getBoundsInLocal().getHeight() - progressIndicator.getMaxHeight()) / 2);
            AnchorPane.setLeftAnchor(progressIndicator, (root.getBoundsInLocal().getWidth() - progressIndicator.getMaxWidth()) / 2);
            anchorRoot.getChildren().add(progressIndicator);
        } else if (root instanceof VBox) {
            VBox vboxRoot = (VBox) root;
            progressIndicator.setLayoutX((vboxRoot.getWidth() - progressIndicator.getMaxWidth()) / 2);
            progressIndicator.setLayoutY((vboxRoot.getHeight() - progressIndicator.getMaxHeight()) / 2);
            vboxRoot.getChildren().add(progressIndicator);
        } else {
            // Generic fallback for other layout types
            StackPane overlayPane = new StackPane(progressIndicator);
            overlayPane.setStyle("-fx-background-color: rgba(255, 255, 255, 0.5);");
            
            Stage stage = (Stage) currentScene.getWindow();
            Scene overlayScene = new Scene(overlayPane, stage.getWidth(), stage.getHeight());
            overlayScene.setFill(null);
            
            stage.setScene(overlayScene);
        }

        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), root);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.5);
        
        fadeOut.setOnFinished(e -> {
            Task<Parent> loadTask = new Task<Parent>() {
                @Override
                protected Parent call() throws Exception {
                    try {
                        String fxmlPath = "/edu/careflow/fxml/patientLoginPage.fxml";
                        URL resource = getClass().getResource(fxmlPath);
                        if (resource == null) {
                            throw new IOException("FXML file not found: " + fxmlPath);
                        }
                        
                        FXMLLoader loader = new FXMLLoader(resource);
                        return loader.load();
                    } catch (Exception ex) {
                        throw ex;
                    }
                }
            };
            
            loadTask.setOnSucceeded(t -> {
                try {
                    Parent newRoot = loadTask.getValue();
                    Stage stage = (Stage) currentScene.getWindow();
                    
                    // Create new scene with same dimensions
                    Scene newScene = new Scene(newRoot, currentScene.getWidth(), currentScene.getHeight());
                    
                    // Apply fade-in transition to new scene
                    FadeTransition fadeIn = new FadeTransition(Duration.millis(400), newRoot);
                    fadeIn.setFromValue(0.2);
                    fadeIn.setToValue(1.0);
                    
                    stage.setScene(newScene);
                    stage.setTitle("CareFlow | Patient Login");
                    stage.centerOnScreen();
                    
                    fadeIn.play();
                } catch (Exception ex) {
                    handleNavigationError(ex, "Patient Login");
                }
            });
            
            loadTask.setOnFailed(t -> {
                // Remove the progress indicator based on the root type
                if (root instanceof AnchorPane) {
                    ((AnchorPane) root).getChildren().remove(progressIndicator);
                } else if (root instanceof VBox) {
                    ((VBox) root).getChildren().remove(progressIndicator);
                }
                
                FadeTransition fadeBack = new FadeTransition(Duration.millis(300), root);
                fadeBack.setFromValue(0.5);
                fadeBack.setToValue(1.0);
                fadeBack.play();
                
                handleNavigationError(loadTask.getException(), "Patient Login");
            });

            Thread loadingThread = new Thread(loadTask);
            loadingThread.setDaemon(true);
            loadingThread.start();
        });
        
        fadeOut.play();
    }
    
    private void handleNavigationError(Throwable exception, String destination) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Navigation Error");
            alert.setHeaderText("Cannot navigate to " + destination);
            
            String errorDetails = "The system could not load the requested page due to: ";
            if (exception != null) {
                errorDetails += exception.getMessage();
                
                // Create expandable content for stack trace
                TextArea textArea = new TextArea();
                StringBuilder stackTraceText = new StringBuilder();
                for (StackTraceElement element : exception.getStackTrace()) {
                    stackTraceText.append(element.toString()).append("\n");
                }
                textArea.setText(stackTraceText.toString());
                textArea.setEditable(false);
                textArea.setWrapText(true);
                textArea.setMaxWidth(Double.MAX_VALUE);
                textArea.setMaxHeight(Double.MAX_VALUE);
                
                alert.getDialogPane().setExpandableContent(textArea);
            }
            
            alert.setContentText(errorDetails);
            alert.showAndWait();
            
            System.err.println("Failed to navigate to " + destination + ": " + 
                    (exception != null ? exception.getMessage() : "Unknown error"));
            if (exception != null) {
                exception.printStackTrace();
            }
        });
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

