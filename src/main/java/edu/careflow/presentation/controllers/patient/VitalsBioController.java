package edu.careflow.presentation.controllers.patient;

import edu.careflow.presentation.controllers.patient.cards.BioCardLongController;
import edu.careflow.presentation.controllers.patient.cards.VitalCardLongController;
import edu.careflow.repository.dao.VitalsDAO;
import edu.careflow.repository.entities.Vitals;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;

public class VitalsBioController {

    @FXML
    private VBox biometricsContainerMain;

    @FXML
    private VBox biometricsHistoryContainer;

    @FXML
    private HBox headerBiometrics;

    @FXML
    private HBox headerVitals;

    @FXML
    private VBox vitalsContainerMain;

    @FXML
    private VBox vitalsHistoryContainer;



    private VitalsDAO vitalsDAO = new VitalsDAO();


    public void initializeData(int patientId) {

        loadVitalsHistory(patientId);
        loadBiometricsHistory(patientId);
    }

    public void loadVitalsHistory(int patientId) {
        try {
            List<Vitals> vitals = vitalsDAO.getVitalsByPatientId(patientId);

            if(vitals.isEmpty()) {
                URL fxmlResource = getClass().getResource("/edu/careflow/fxml/components/states/emptyRecordstCard.fxml");
                Parent cardContent = FXMLLoader.load(fxmlResource);

                Label emptyLabel = (Label) cardContent.lookup("#pageTitle");
                emptyLabel.setText("No Vitals History");
                headerVitals.setVisible(false);
                vitalsHistoryContainer.setStyle("-fx-alignment: center;");
                vitalsHistoryContainer.getChildren().add(cardContent);
            } else {
                for (Vitals vital : vitals) {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/edu/careflow/fxml/components/patient/vitalsCardLong.fxml"));
                    Parent cardContent = loader.load();
                    VitalCardLongController controller = loader.getController();
                    controller.initializeData(vital);
                    vitalsHistoryContainer.getChildren().add(cardContent);
                }
            }
        } catch(SQLException | IOException e) {
            e.printStackTrace();
        }
    }

    public void loadBiometricsHistory(int patientId) {
        try {
            List<Vitals> biometrics = vitalsDAO.getVitalsByPatientId(patientId);
            if(biometrics.isEmpty()) {
                URL fxmlResource = getClass().getResource("/edu/careflow/fxml/components/states/emptyRecordstCard.fxml");
                assert fxmlResource != null;
                Parent cardContent = FXMLLoader.load(fxmlResource);

                Label emptyLabel = (Label) cardContent.lookup("#pageTitle");
                emptyLabel.setText("No Biometrics History");
                headerBiometrics.setVisible(false);
                biometricsHistoryContainer.setStyle("-fx-alignment: center;");
                biometricsHistoryContainer.getChildren().add(cardContent);
            } else {
                for (Vitals biometric : biometrics) {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/edu/careflow/fxml/components/patient/bioCardLong.fxml"));
                    Parent cardContent = loader.load();
                    BioCardLongController controller = loader.getController();
                    controller.initializeData(biometric);
                    biometricsHistoryContainer.getChildren().add(cardContent);
                }
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }
}
