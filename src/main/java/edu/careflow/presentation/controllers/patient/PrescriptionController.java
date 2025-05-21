package edu.careflow.presentation.controllers.patient;

import edu.careflow.presentation.controllers.Controller;
import edu.careflow.presentation.controllers.patient.cards.PrescriptionCardController;
import edu.careflow.repository.dao.MedicationDAO;
import edu.careflow.repository.dao.PrescriptionDAO;
import edu.careflow.repository.entities.Prescription;
import edu.careflow.repository.entities.PrescriptionDetails;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Pagination;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PrescriptionController extends Controller {
    private static final int CARDS_PER_PAGE = 4;

    @FXML
    private VBox prescriptionHistory;

    @FXML
    private VBox prescriptionRowsContainer;

    @FXML
    private Pagination prescriptionPagination;

    @FXML
    private Button activePrescriptionBtn;

    @FXML
    private Button pastPrescriptionBtn;

    @FXML
    private Button tableViewBtn;

    @FXML
    private HBox buttonsPrescriptionFilter;

    private PrescriptionDAO prescriptionDAO = new PrescriptionDAO();
    private MedicationDAO medicationDAO = new MedicationDAO();
    private List<Prescription> allPrescriptions = new ArrayList<>();
    private Button currentActiveFilterButton;

    @Override
    public void initializeData(int patientId) {
        setupFilterButtons();
        loadAllPrescriptions(patientId);
    }

    private void setupFilterButtons() {
        activePrescriptionBtn.setOnAction(e -> handleFilterPrescriptions(activePrescriptionBtn, "Active"));
        pastPrescriptionBtn.setOnAction(e -> handleFilterPrescriptions(pastPrescriptionBtn, "Past"));
        
        // Set initial active button
        currentActiveFilterButton = activePrescriptionBtn;
        updateFilterButtonStyle(activePrescriptionBtn);
    }

    private void handleFilterPrescriptions(Button clickedButton, String status) {
        updateFilterButtonStyle(clickedButton);
        
        List<Prescription> filteredPrescriptions;
        LocalDate today = LocalDate.now();
        
        if ("Active".equals(status)) {
            filteredPrescriptions = allPrescriptions.stream()
                .filter(p -> p.getValidUntil() != null && !p.getValidUntil().isBefore(today))
                .collect(Collectors.toList());
        } else {
            filteredPrescriptions = allPrescriptions.stream()
                .filter(p -> p.getValidUntil() == null || p.getValidUntil().isBefore(today))
                .collect(Collectors.toList());
        }
        
        setupPagination(filteredPrescriptions);
    }

    private void updateFilterButtonStyle(Button clickedButton) {
        if (currentActiveFilterButton != null) {
            currentActiveFilterButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #757575;");
        }
        clickedButton.setStyle("-fx-background-color: #0078D4; -fx-text-fill: white;");
        currentActiveFilterButton = clickedButton;
    }

    private void loadAllPrescriptions(int patientId) {
        prescriptionRowsContainer.getChildren().clear();
        try {
            allPrescriptions = medicationDAO.getPrescriptionsByPatientId(patientId);
            
            // Set initial filter to Active
            LocalDate today = LocalDate.now();
            List<Prescription> activePrescriptions = allPrescriptions.stream()
                .filter(p -> p.getValidUntil() != null && !p.getValidUntil().isBefore(today))
                .collect(Collectors.toList());
                
            setupPagination(activePrescriptions);
        } catch (Exception e) {
            Label errorLabel = new Label("Failed to load prescriptions: " + e.getMessage());
            errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 14px;");
            prescriptionRowsContainer.getChildren().add(errorLabel);
        }
    }

    private void setupPagination(List<Prescription> prescriptions) {
        if (prescriptions.isEmpty()) {
            showEmptyState("No prescriptions found");
            prescriptionPagination.setVisible(false);
            return;
        }
        
        int pageCount = (int) Math.ceil((double) prescriptions.size() / CARDS_PER_PAGE);
        prescriptionPagination.setPageCount(Math.max(pageCount, 1));
        prescriptionPagination.setCurrentPageIndex(0);
        prescriptionPagination.setPageFactory(pageIndex -> createPage(pageIndex, prescriptions));
        prescriptionPagination.setVisible(true);
    }

    private VBox createPage(int pageIndex, List<Prescription> prescriptions) {
        VBox pageBox = new VBox(0);
        int start = pageIndex * CARDS_PER_PAGE;
        int end = Math.min(start + CARDS_PER_PAGE, prescriptions.size());
        
        for (int i = start; i < end; i++) {
            Prescription prescription = prescriptions.get(i);
            try {
                List<PrescriptionDetails> details = medicationDAO.getPrescriptionDetailsByPrescriptionId(prescription.getPrescriptionId());
                PrescriptionDetails mainDetail = details.isEmpty() ? null : details.get(0);
                Node card = createPrescriptionCard(prescription, mainDetail);
                pageBox.getChildren().add(card);
            } catch (Exception e) {
                Label errorLabel = new Label("Error loading prescription: " + e.getMessage());
                errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 14px;");
                pageBox.getChildren().add(errorLabel);
            }
        }
        
        prescriptionRowsContainer.getChildren().setAll(pageBox.getChildren());
        return pageBox;
    }

    private Node createPrescriptionCard(Prescription prescription, PrescriptionDetails detail) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/edu/careflow/fxml/components/patient/prescriptionCard.fxml"));
        Node card = loader.load();
        PrescriptionCardController controller = loader.getController();
        controller.initializeData(prescription, detail);
        return card;
    }

    private void showEmptyState(String message) {
        try {
            URL fxmlResource = getClass().getResource("/edu/careflow/fxml/components/states/emptyRecordstCard.fxml");
            Parent cardContent = FXMLLoader.load(fxmlResource);

            Label emptyLabel = (Label) cardContent.lookup("#pageTitle");
            emptyLabel.setText(message);
            
            prescriptionRowsContainer.getChildren().clear();
            prescriptionRowsContainer.setStyle("-fx-alignment: center;");
            prescriptionRowsContainer.getChildren().add(cardContent);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
