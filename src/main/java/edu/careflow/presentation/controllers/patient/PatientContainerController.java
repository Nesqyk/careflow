package edu.careflow.presentation.controllers.patient;

import edu.careflow.repository.dao.PatientDAO;
import edu.careflow.repository.dao.VitalsDAO;
import edu.careflow.repository.entities.Patient;
import edu.careflow.repository.entities.Vitals;
import edu.careflow.utils.DateHelper;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;
import com.dlsc.gemsfx.AvatarView;

import java.io.IOException;
import java.sql.SQLException;

public class PatientContainerController {

    @FXML
    private AvatarView avatarView;

    @FXML
    private Button appointmentBtnPatient;

    @FXML
    private Button billBtnPatient;

    @FXML
    private FontIcon billingIcon;

    @FXML
    private FontIcon calendarIcon;

    @FXML
    private Button homeBtnPatient;

    @FXML
    private Button homeBtnPatient1;

    @FXML
    private FontIcon homeIcon;

    @FXML
    private Button medBtnPatient;

    @FXML
    private FontIcon medicalIcon;

    @FXML
    private ScrollPane pageContainer;

    @FXML
    private Label patientAge;

    @FXML
    private VBox patientCardsProperties;

    @FXML
    private Label patientDatebirth;

    @FXML
    private Label patientId;

    @FXML
    private Label patientName;

    @FXML
    private Label patientSex;

    @FXML

    private final PatientDAO patientDAO = new PatientDAO();
    private final VitalsDAO vitalsDAO = new VitalsDAO();
    private Button currentActiveButton = null;

    public void initializePatientData(String id) {
        try {
            Patient patient = patientDAO.getPatientById(Integer.parseInt(id));

            String name = patient.getFirstName() + " " + patient.getLastName();
            Integer age = DateHelper.getInstance().calculateAgeAtDate(patient.getDateOfBirth().toString());
            String sex = patient.getGender();
            String dateOfBirth = patient.getDateOfBirth().toString();

            // Ari dapita ang mga kato label
            patientName.setText(name);
            patientAge.setText(age.toString());
            patientSex.setText(sex);
            patientDatebirth.setText(dateOfBirth);
            patientId.setStyle("-fx-font-family: Gilroy-SemiBold");
            patientId.setText("Careflow Id : " + id);


            homeBtnPatient.setOnAction(event -> handleHomeNavigation());
            appointmentBtnPatient.setOnAction(event -> handleAppointmentNavigation());
            medBtnPatient.setOnAction(event -> handlePrescriptionNavigation());
            billBtnPatient.setOnAction(event -> handleBillingNavigation());

            // Initialize the home page by default
            handleHomeNavigation();


        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleHomeNavigation() {
        resetAllBtnStyle();

        setPageContent(Integer.parseInt(patientId.getText().replace("Careflow Id : ", "")), "patientHomePage");
        animateButtonSelection(homeBtnPatient);
    }

    @FXML
    private void handleAppointmentNavigation() {
        resetAllBtnStyle();
        setPageContent(Integer.parseInt(patientId.getText().replace("Careflow Id : ", "")), "appointmentPage");
        animateButtonSelection(appointmentBtnPatient);
    }

    @FXML
    private void handlePrescriptionNavigation() {
        resetAllBtnStyle();
        setPageContent(Integer.parseInt(patientId.getText().replace("Careflow Id : ", "")), "patientMedicalPage");
        animateButtonSelection(medBtnPatient);
    }

    @FXML
    private void handleBillingNavigation() {
        resetAllBtnStyle();
        setPageContent(Integer.parseInt(patientId.getText().replace("Careflow Id : ", "")), "patientBilling");
        animateButtonSelection(billBtnPatient);
    }

    private void animateButtonSelection(Button button) {
        // First reset current active button if any
        if (currentActiveButton != null) {
            currentActiveButton.getStyleClass().remove("nav-button-active");
            currentActiveButton.getStyleClass().add("nav-button");
        }

        // Set new active button
        currentActiveButton = button;
        button.getStyleClass().setAll("nav-button", "nav-button-active");

        // Create a subtle animation effect
        ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(150), button);
        scaleTransition.setFromX(1.0);
        scaleTransition.setFromY(1.0);
        scaleTransition.setToX(1.05);
        scaleTransition.setToY(1.05);
        scaleTransition.setCycleCount(2);
        scaleTransition.setAutoReverse(true);

        FadeTransition fadeTransition = new FadeTransition(Duration.millis(200), button);
        fadeTransition.setFromValue(0.7);
        fadeTransition.setToValue(1.0);

        // Create background color animation by changing style class
        button.getStyleClass().add("nav-button-animating");

        ParallelTransition parallelTransition = new ParallelTransition(scaleTransition, fadeTransition);
        parallelTransition.play();

        // Remove animation class after animation completes
        parallelTransition.setOnFinished(e -> {
            button.getStyleClass().remove("nav-button-animating");
        });
    }

    private void resetAllBtnStyle() {
        homeBtnPatient.getStyleClass().setAll("nav-button");
        appointmentBtnPatient.getStyleClass().setAll("nav-button");
        medBtnPatient.getStyleClass().setAll("nav-button");
        billBtnPatient.getStyleClass().setAll("nav-button");
    }

    private void setPageContent(int patientId, String file) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/edu/careflow/fxml/patient/" + file + ".fxml")
            );
            Parent pageContent = loader.load();
            Object controller = loader.getController();

            Vitals recentVital = vitalsDAO.getLatestVitals(patientId);

            if (controller instanceof PatientHomeController) {
                ((PatientHomeController) controller).initializeData(patientId);
            } else if (controller instanceof PatientAppointmentController) {
                // Handle initialization for appointment controller
                ((PatientAppointmentController) controller).initializeData(patientId);
            } else if(controller instanceof  PatientMedicalController) {
                ((PatientMedicalController) controller).initializeData(patientId);
            }

            // Check if the current page is already the same instance
            if (pageContainer.getContent() == pageContent) {
                return; // Do nothing if the page is already loaded
            }

            // Use a more sophisticated animation for page transitions
            if (pageContainer.getContent() != null) {
                // Fade out current content
                FadeTransition fadeOut = new FadeTransition(Duration.millis(150), pageContainer.getContent());
                fadeOut.setFromValue(1.0);
                fadeOut.setToValue(0.0);
                fadeOut.setOnFinished(e -> {
                    pageContainer.setContent(pageContent);

                    // Fade in new content with subtle slide
                    FadeTransition fadeIn = new FadeTransition(Duration.millis(200), pageContent);
                    fadeIn.setFromValue(0.0);
                    fadeIn.setToValue(1.0);

                    TranslateTransition translateIn = new TranslateTransition(Duration.millis(200), pageContent);
                    translateIn.setFromX(10);
                    translateIn.setToX(0);

                    ParallelTransition parallelIn = new ParallelTransition(fadeIn, translateIn);
                    parallelIn.play();
                });
                fadeOut.play();
            } else {
                // First load doesn't need fade out
                pageContainer.setContent(pageContent);

                FadeTransition fadeIn = new FadeTransition(Duration.millis(300), pageContent);
                fadeIn.setFromValue(0.0);
                fadeIn.setToValue(1.0);
                fadeIn.play();
            }

        } catch (IOException | SQLException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Failed to load " + file + " page").show();
        }
    }
}
