package edu.careflow.presentation.controllers.receptionist;

import edu.careflow.repository.entities.Billing;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import edu.careflow.presentation.controllers.doctor.GenerateInvoiceController;
import java.time.format.DateTimeFormatter;

public class ReceptionistInvoiceCardController {
    @FXML public Label invoiceNumberLabel;
    @FXML public Label patientNameLabel;
    @FXML public Label amountLabel;
    @FXML public Label statusLabel;
    @FXML public Label dateLabel;
    @FXML private Button viewBtn;
    @FXML private Button editBtn;
    @FXML private Button downloadBtn;

    private Billing billing;
    private Runnable onEdit;
    private Runnable onDownload;
    private Runnable onView;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    @FXML
    public void initialize() {
        setupEventHandlers();
    }

    private void setupEventHandlers() {
        if (viewBtn != null) {
            viewBtn.setOnAction(e -> handleView());
        }
        if (editBtn != null) {
            editBtn.setOnAction(e -> handleEdit());
        }
        if (downloadBtn != null) {
            downloadBtn.setOnAction(e -> handleDownload());
        }
    }

    public void initializeData(Billing billing, Runnable onEdit, Runnable onDownload, Runnable onView) {
        this.billing = billing;
        this.onEdit = onEdit;
        this.onDownload = onDownload;
        this.onView = onView;

        // Update UI with billing data
        updateUI();
    }

    private void updateUI() {
        if (billing != null) {
            invoiceNumberLabel.setText("INV-" + billing.getBillingId());
            patientNameLabel.setText("Patient #" + billing.getPatientId());
            amountLabel.setText(billing.getAmount() != null ? String.format("₱%.2f", billing.getAmount()) : "-");
            statusLabel.setText(billing.getStatus());
            dateLabel.setText(billing.getBillingDate() != null ? 
                billing.getBillingDate().toLocalDate().format(dateFormatter) : "-");
        }
    }

    @FXML
    private void handleView() {
        if (onView != null) {
            onView.run();
        } else {
            openInvoiceOverview();
        }
    }

    @FXML
    private void handleEdit() {
        if (onEdit != null) {
            onEdit.run();
        } else {
            openEditForm();
        }
    }

    @FXML
    private void handleDownload() {
        if (onDownload != null) {
            onDownload.run();
        }
    }

    private void openInvoiceOverview() {
        try {
            Scene currentScene = invoiceNumberLabel.getScene();
            VBox rightBoxContainer = (VBox) currentScene.lookup("#rightBoxContainer");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/edu/careflow/fxml/components/patient/invoiceOverviewPatient.fxml"));
            Parent overviewRoot = loader.load();
            
            // Get the controller and set the billing data
            edu.careflow.presentation.controllers.patient.InvoiceOverviewPatientController controller = loader.getController();
            controller.loadInvoiceData(billing.getAppointmentId());
            
            if (rightBoxContainer != null) {
                rightBoxContainer.getChildren().clear();
                rightBoxContainer.getChildren().add(overviewRoot);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void openEditForm() {
        try {
            Scene currentScene = invoiceNumberLabel.getScene();
            VBox rightBoxContainer = (VBox) currentScene.lookup("#rightBoxContainer");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/edu/careflow/fxml/forms/generate-invoice-form.fxml"));
            Parent formRoot = loader.load();
            GenerateInvoiceController formController = loader.getController();
            formController.setEditMode(billing);
            formController.setOnSuccessCallback(() -> {
                // Refresh the invoice list after editing
                if (rightBoxContainer != null) {
                    rightBoxContainer.getChildren().clear();
                }
            });
            if (rightBoxContainer != null) {
                rightBoxContainer.getChildren().clear();
                rightBoxContainer.getChildren().add(formRoot);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
} 