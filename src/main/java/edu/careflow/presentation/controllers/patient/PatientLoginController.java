package edu.careflow.presentation.controllers.patient;

import com.dlsc.gemsfx.CalendarPicker;
import edu.careflow.manager.AuthManager;
import edu.careflow.repository.dao.PatientDAO;
import edu.careflow.repository.entities.Patient;
import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;

public class PatientLoginController {

    @FXML
    private VBox dateContainerPatient;

    @FXML
    private CalendarPicker dateOfBirthInput;

    @FXML
    private VBox idContainerPatient;


    @FXML
    private VBox loginPatientContainer;

    @FXML
    private TextField patientIdTextInput;


    public void initialize() {
        dateOfBirthInput.setMinHeight(42);
        dateOfBirthInput.setStyle("-fx-font-size: 16px;");

    }

    public AuthManager authManager = new AuthManager();
    public PatientDAO patientDAO = new PatientDAO();

    private boolean errorDisplayed = false;

    @FXML
    public void navigateToLoginPage(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/edu/careflow/fxml/loginPageNew.fxml"));

            URL resource = getClass().getResource("/edu/careflow/fxml/loginPageNew.fxml");
            if (resource == null) {
                throw new IOException("FXML file not found");
            }

            Parent root = loader.load();

            Node source = (Node) event.getSource();
            Stage stage = (Stage) source.getScene().getWindow();

            Scene scene = new Scene(root);
            stage.setScene(scene);

            stage.centerOnScreen();
            stage.setTitle("CareFlow | User Login");
        } catch(IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    protected void onClickPatientLoginButton(ActionEvent event) {
        loginPatientContainer.getChildren().removeIf(node -> "loginErrorText".equals(node.getId()));
        errorDisplayed = false;

        String patientId = patientIdTextInput.getText();
        LocalDate dateOfBirth = dateOfBirthInput.getValue();

        if (patientId == null || patientId.isEmpty()) {
            if (!errorDisplayed) {
                showError("patientId", "Please enter your patient ID");
                errorDisplayed = true;
            }
        } else if (dateOfBirth == null) {
            if (!errorDisplayed) {
                showError("dateOfBirth", "Please enter your date of birth");
                errorDisplayed = true;
            }
        } else {
            try {
                int patientIdInt = Integer.parseInt(patientId);
                System.out.println(patientIdInt);

                System.out.println(dateOfBirth);
                boolean test = patientDAO.validatePatient(patientIdInt, dateOfBirth);
                System.out.print(test);
                if (patientDAO.validatePatient(patientIdInt, dateOfBirth)) {

                    FXMLLoader loadingLoader = new FXMLLoader(getClass().getResource("/edu/careflow/fxml/loadingState.fxml"));
                    Parent loadingRoot = loadingLoader.load();

                    FadeTransition fadeTransition = new FadeTransition(Duration.seconds(1), loadingRoot);
                    fadeTransition.setFromValue(0.0); // Start with fully transparent
                    fadeTransition.setToValue(1.0);   // End with fully opaque

                    PauseTransition pauseTransition = getPauseTransition(event, loadingRoot, patientIdInt);
                    pauseTransition.play();
                    fadeTransition.play();
                } else {
                    showLoginError();
                }
            } catch (NumberFormatException e) {
                showError("patientId", "Invalid patient ID format");
            } catch (Exception e) {
                e.printStackTrace();
                showError("general", "An error occurred during login");
            }
        }
    }

    private PauseTransition getPauseTransition(ActionEvent event, Parent loadingRoot, int patientIdInt) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        Scene loadingScene = new Scene(loadingRoot);
        stage.setScene(loadingScene);
        stage.setTitle("Loading...");

        // Create a PauseTransition for 4 seconds
        PauseTransition pauseTransition = new PauseTransition(Duration.seconds(4));
        pauseTransition.setOnFinished(e -> {
            try {
                // Load the patient container scene after the pause
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/edu/careflow/fxml/patient/patientContainer.fxml"));
                Parent root = loader.load();
                PatientContainerController controller = loader.getController();
                controller.initializePatientData(String.valueOf(patientIdInt));

                // Create a FadeTransition
                FadeTransition fadeTransition = new FadeTransition(Duration.seconds(1), root);
                fadeTransition.setFromValue(0.0); // Start with fully transparent
                fadeTransition.setToValue(1.0);   // End with fully opaque

                Scene scene = new Scene(root);
                stage.setScene(scene);
                Patient patient = patientDAO.getPatientById(patientIdInt);
                stage.setTitle("Patient Dashboard | " + patient.getPatientId() + " | " + patient.getFirstName() + " " + patient.getLastName());


                fadeTransition.play();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
        return pauseTransition;
    }


    private void showLoginError() {
        patientIdTextInput.getStyleClass().add("error-border");
        dateOfBirthInput.getStyleClass().add("error-border");

        Text errorInfo = new Text("Login failed. Incorrect patient ID or date of birth.");
        errorInfo.setStyle("-fx-fill: red; -fx-padding: 32px");
        errorInfo.setFont(Font.font("Gilroy-Regular", 16));
        errorInfo.setId("loginErrorText");
        loginPatientContainer.getChildren().add(errorInfo);
    }

    private void showError(String type, String errorMessage) {
        clearError();

        if (errorMessage != null) {

            switch (type) {
                case "patientId":
                    patientIdTextInput.getStyleClass().add("error-border");
                    Text patientIdError = new Text(errorMessage);
                    patientIdError.setId("patientIdErrorText");
                    patientIdError.setStyle("-fx-fill: red;");
                    patientIdError.setFont(Font.font("Gilroy-Regular", 16));
                    idContainerPatient.getChildren().add(patientIdError);
                    break;
                case "dateOfBirth":
                    dateOfBirthInput.getStyleClass().add("error-border");
                    Text dateOfBirthError = new Text(errorMessage);
                    dateOfBirthError.setId("dateOfBirthErrorText");
                    dateOfBirthError.setStyle("-fx-fill: red;");
                    dateOfBirthError.setFont(Font.font("Gilroy-Regular", 16));
                    dateContainerPatient.getChildren().add(dateOfBirthError);
                    break;
            }

            PauseTransition pauseTransition = new PauseTransition(Duration.seconds(2));
            pauseTransition.setOnFinished(event -> clearError());
            pauseTransition.play();
        }
    }

    private void clearError() {
        patientIdTextInput.getStyleClass().remove("error-border");
        dateOfBirthInput.getStyleClass().remove("error-border");
        idContainerPatient.getChildren().removeIf(node -> node instanceof Text);
        dateContainerPatient.getChildren().removeIf(node -> node instanceof Text);
        errorDisplayed = false;
    }
}
