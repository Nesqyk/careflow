package edu.careflow.presentation.controllers.patient;

import edu.careflow.presentation.controllers.Controller;
import edu.careflow.presentation.controllers.patient.cards.PrescriptionCardController;
import edu.careflow.repository.dao.PrescriptionDAO;
import edu.careflow.repository.entities.Prescription;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;

public class PrescriptionController extends Controller {


    @FXML
    private VBox activeContainerMain;

    @FXML
    private VBox activeMedicationContainer;

    @FXML
    private VBox pastContainerMain;

    @FXML
    private VBox passMedicationContainer;

    private PrescriptionDAO prescriptionDAO = new PrescriptionDAO();

    @Override
    public void initializeData(int patientId) {
        loadActiveMedication(patientId);
        loadPastMedication(patientId);
    }

    public void loadActiveMedication(int patientId) {
        try {
            List<Prescription> activePrescriptions = prescriptionDAO.getActivePrescriptions(patientId);
            
            if (activePrescriptions.isEmpty()) {
                URL fxmlResource = getClass().getResource("/edu/careflow/fxml/components/states/emptyRecordstCard.fxml");
                Parent cardContent = FXMLLoader.load(fxmlResource);

                Label emptyLabel = (Label) cardContent.lookup("#pageTitle");
                emptyLabel.setText("No Recent Prescriptions");
                activeContainerMain.setStyle("-fx-alignment: center;");
                activeContainerMain.getChildren().add(cardContent);


            } else {
                for (Prescription prescription : activePrescriptions) {
                    try {
                        Node prescriptionCard = createPrescriptionCard(prescription);
                        if (prescriptionCard != null) {
                            passMedicationContainer.getChildren().add(prescriptionCard);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch(SQLException | IOException e) {
            e.printStackTrace();
            // Consider showing an error dialog to the user
        }
    }

    public void loadPastMedication(int patientId) {
        try {
            List<Prescription> pastPrescriptions = prescriptionDAO.getPastMedication(patientId);
            
            if (pastPrescriptions.isEmpty()) {
                URL fxmlResource = getClass().getResource("/edu/careflow/fxml/components/states/emptyRecordstCard.fxml");
                Parent cardContent = FXMLLoader.load(fxmlResource);

                Label emptyLabel = (Label) cardContent.lookup("#pageTitle");
                emptyLabel.setText("No Past Prescriptions");
                pastContainerMain.setStyle("-fx-alignment: center;");
                pastContainerMain.getChildren().add(cardContent);
            } else {
                for (Prescription prescription : pastPrescriptions) {
                    try {
                        Node prescriptionCard = createPrescriptionCard(prescription);
                        if (prescriptionCard != null) {
                            pastContainerMain.getChildren().add(prescriptionCard);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch(SQLException | IOException e) {
            e.printStackTrace();
            // Consider showing an error dialog to the user
        }
    }

    private Node createPrescriptionCard(Prescription prescription) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/edu/careflow/fxml/components/patient/prescriptionCard.fxml"));
        Node prescriptionCard = loader.load();
        PrescriptionCardController controller = loader.getController();
        controller.initializeData(prescription);
        return prescriptionCard;
    }
}
