package edu.careflow.presentation.controllers.patient;

import edu.careflow.repository.dao.PatientDAO;
import edu.careflow.repository.dao.VitalsDAO;
import edu.careflow.repository.entities.Patient;
import edu.careflow.repository.entities.Vitals;
import edu.careflow.util.DateHelper;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.io.IOException;
import java.sql.SQLException;

public class PatientContainerController {

    @FXML
    private Button appointmentBtnPatient;

    @FXML
    private Button billBtnPatient;

    @FXML
    private Button homeBtnPatient;

    @FXML
    private Button logoutButton;

    @FXML
    private Button medBtnPatient;

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

    public void initializePatientData(String id) {
        try {
            Patient patient = patientDAO.getPatientById(Integer.parseInt(id));

            String name = patient.getFirstName() + " " + patient.getLastName();
            Integer age = DateHelper.getInstance().calculateAgeAtDate(patient.getDateOfBirth().toString());
            String sex = patient.getGender();
            String dateOfBirth = patient.getDateOfBirth().toString();

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
        setPageContent(Integer.parseInt(patientId.getText().replace("Careflow Id : ", "")), "patientHomePage");
    }

    @FXML
    private void handleAppointmentNavigation() {
        setPageContent(Integer.parseInt(patientId.getText().replace("Careflow Id : ", "")), "patientAppointmentPage");
    }

    @FXML
    private void handlePrescriptionNavigation() {
        setPageContent(Integer.parseInt(patientId.getText().replace("Careflow Id : ", "")), "patientPrescription");
    }

    @FXML
    private void handleBillingNavigation() {
        setPageContent(Integer.parseInt(patientId.getText().replace("Careflow Id : ", "")), "patientBilling");
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
            }

            pageContainer.setContent(pageContent);
            
            FadeTransition fadeTransition = new FadeTransition(Duration.millis(300), pageContent);
            fadeTransition.setFromValue(0.0);
            fadeTransition.setToValue(1.0);
            fadeTransition.play();

        } catch (IOException | SQLException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Failed to load " + file + " page").show();
        }
    }
}
    // if a user clicks a certain nav bar  appointment then the view will update...
// I should probably create another way for this to work.




