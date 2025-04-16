package edu.careflow.presentation.controllers.patient;

import edu.careflow.presentation.controllers.patient.cards.VitalsCardsController;
import edu.careflow.repository.dao.PatientDAO;
import edu.careflow.repository.dao.VitalsDAO;
import edu.careflow.repository.entities.Vitals;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;

public class PatientHomeController {

    @FXML
    private VBox appointmentContainer;

    @FXML
    private VBox conditionsContainer;

    @FXML
    private VBox medicationContainer;

    @FXML
    private VBox patientContainerHome;

    @FXML
    private VBox vitalsCard;

    private final VitalsDAO vitalsDAO = new VitalsDAO();
    private final PatientDAO patientDAO = new PatientDAO();

    public void initializeData(int patientId) {
        try {
            Vitals recentVitals = vitalsDAO.getLatestVitals(patientId);
            loadVitalsCard(patientId, recentVitals);
        } catch (SQLException e) {
            handleDatabaseError(e);
        }
    }

    private void loadVitalsCard(int patientId, Vitals recentVitals) {
        try {
            URL fxmlResource = getFxmlResource(recentVitals);
            FXMLLoader loader = new FXMLLoader(fxmlResource);
            Parent cardContent = loader.load();

            if (recentVitals != null) {
                VitalsCardsController controller = loader.getController();
                controller.initializeData(patientId);
            }

            vitalsCard.getChildren().add(cardContent);
        } catch (IOException e) {
            handleFxmlLoadingError(e);
        }
    }

    private URL getFxmlResource(Vitals recentVitals) {
        if (recentVitals != null) {
            return getClass().getResource("/edu/careflow/fxml/components/patient/vitalsCard.fxml");
        }
        return getClass().getResource("/edu/careflow/fxml/components/states/emptyVitalCard.fxml");
    }

    private void handleDatabaseError(SQLException e) {
        // Implement proper error handling (logging, user notification, etc.)
        System.err.println("Database error: " + e.getMessage());
        e.printStackTrace();
    }

    private void handleFxmlLoadingError(IOException e) {
        // Implement proper error handling
        System.err.println("FXML loading error: " + e.getMessage());
        e.printStackTrace();
    }
}