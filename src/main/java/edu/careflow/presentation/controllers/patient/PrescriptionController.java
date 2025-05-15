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
import javafx.scene.control.Pagination;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;

public class PrescriptionController extends Controller {
    private static final int CARDS_PER_PAGE = 4;

    @FXML
    private VBox activeContainerMain;

    @FXML
    private VBox activeMedicationContainer;

    @FXML
    private Pagination activeMedicationPagination;

    @FXML
    private VBox passMedicationContainer;

    @FXML
    private Pagination passMedicationPage;

    @FXML
    private VBox pastContainerMain;

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
                showEmptyState(activeContainerMain, "No Active Prescriptions");
                activeMedicationPagination.setVisible(false);
                return;
            }

            int totalPages = (int) Math.ceil((double) activePrescriptions.size() / CARDS_PER_PAGE);
            activeMedicationPagination.setPageCount(totalPages);
            activeMedicationPagination.setCurrentPageIndex(0);

            // Create and show initial page
            VBox initialPage = createPrescriptionPage(0, activePrescriptions);
            activeMedicationContainer.getChildren().clear();
            activeMedicationContainer.getChildren().add(initialPage);

            // Set up pagination
            activeMedicationPagination.setPageFactory(pageIndex -> {
                VBox page = createPrescriptionPage(pageIndex, activePrescriptions);
                activeMedicationContainer.getChildren().clear();
                activeMedicationContainer.getChildren().add(page);
                return new VBox();
            });
            activeMedicationPagination.setVisible(true);

        } catch(SQLException e) {
            e.printStackTrace();
            // Consider showing an error dialog to the user
        }
    }

    public void loadPastMedication(int patientId) {
        try {
            List<Prescription> pastPrescriptions = prescriptionDAO.getPastMedication(patientId);
            
            if (pastPrescriptions.isEmpty()) {
                showEmptyState(pastContainerMain, "No Past Prescriptions");
                passMedicationPage.setVisible(false);
                return;
            }

            int totalPages = (int) Math.ceil((double) pastPrescriptions.size() / CARDS_PER_PAGE);
            passMedicationPage.setPageCount(totalPages);
            passMedicationPage.setCurrentPageIndex(0);

            // Create and show initial page
            VBox initialPage = createPrescriptionPage(0, pastPrescriptions);
            passMedicationContainer.getChildren().clear();
            passMedicationContainer.getChildren().add(initialPage);

            // Set up pagination
            passMedicationPage.setPageFactory(pageIndex -> {
                VBox page = createPrescriptionPage(pageIndex, pastPrescriptions);
                passMedicationContainer.getChildren().clear();
                passMedicationContainer.getChildren().add(page);
                return new VBox();
            });
            passMedicationPage.setVisible(true);

        } catch(SQLException e) {
            e.printStackTrace();
            // Consider showing an error dialog to the user
        }
    }

    private VBox createPrescriptionPage(int pageIndex, List<Prescription> prescriptions) {
        VBox page = new VBox(10);
        page.setStyle("-fx-padding: 10 0 10 0;");

        int start = pageIndex * CARDS_PER_PAGE;
        int end = Math.min(start + CARDS_PER_PAGE, prescriptions.size());

        for (int i = start; i < end; i++) {
            try {
                Node prescriptionCard = createPrescriptionCard(prescriptions.get(i));
                page.getChildren().add(prescriptionCard);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return page;
    }

    private void showEmptyState(VBox container, String message) {
        try {
            URL fxmlResource = getClass().getResource("/edu/careflow/fxml/components/states/emptyRecordstCard.fxml");
            Parent cardContent = FXMLLoader.load(fxmlResource);

            Label emptyLabel = (Label) cardContent.lookup("#pageTitle");
            emptyLabel.setText(message);
            
            container.getChildren().clear();
            container.setStyle("-fx-alignment: center;");
            container.getChildren().add(cardContent);
        } catch (IOException e) {
            e.printStackTrace();
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
