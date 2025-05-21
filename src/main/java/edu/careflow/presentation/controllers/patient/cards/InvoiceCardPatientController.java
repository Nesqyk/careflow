package edu.careflow.presentation.controllers.patient.cards;

import edu.careflow.presentation.controllers.patient.forms.PaymentFormController;
import edu.careflow.presentation.controllers.patient.InvoiceOverviewPatientController;
import edu.careflow.repository.dao.BillingDAO;
import edu.careflow.repository.entities.Billing;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

public class InvoiceCardPatientController {
    @FXML private Label invoiceNumberLabel;
    @FXML private Label serviceLabel;
    @FXML private Label dateLabel;
    @FXML private Label amountLabel;
    @FXML private Label statusLabel;
    @FXML private Button payButton;
    @FXML private Button previewButton;
    @FXML private VBox invoiceCard;

    private int appointmentId;
    private Runnable onPaidCallback;
    private final BillingDAO billingDAO = new BillingDAO();

    public void setOnPaidCallback(Runnable callback) {
        this.onPaidCallback = callback;
    }

    @FXML
    public void initialize() {
        // Add click handler to preview button
        previewButton.setOnAction(e -> showInvoiceOverview());
        payButton.setOnAction(e -> handlePay());
    }

    public void initializeData(Billing billing) {
        this.appointmentId = billing.getAppointmentId();
        invoiceNumberLabel.setText("INV-" + String.format("%05d", billing.getBillingId()));
        serviceLabel.setText(billing.getDescription() != null ? billing.getDescription() : "Service");
        dateLabel.setText(billing.getDueDate() != null ? billing.getDueDate().toLocalDate().toString() : "-");
        
        // Update to use total amount (subtotal + tax) instead of just subtotal
        amountLabel.setText(billing.getAmount() != null ? 
            billing.getAmount().setScale(2, java.math.RoundingMode.HALF_UP).toString() : "0.00");
        statusLabel.setText(billing.getStatus() != null ? billing.getStatus() : "UNPAID");

        // Simplified status handling
        boolean isPaid = "PAID".equalsIgnoreCase(billing.getStatus());

        // Update status label styling
        statusLabel.getStyleClass().removeAll("paid-status", "unpaid-status");
        statusLabel.getStyleClass().add(isPaid ? "paid-status" : "unpaid-status");

        // Update pay button state
        payButton.setVisible(!isPaid);
        payButton.setDisable(isPaid);

    }

    private void showInvoiceOverview() {
        try {
            Scene currentScene = invoiceCard.getScene();
            VBox rightBoxContainer = (VBox) currentScene.lookup("#rightBoxContainer");
            
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/edu/careflow/fxml/components/patient/invoiceOverviewPatient.fxml"));
            Parent overviewRoot = loader.load();
            
            InvoiceOverviewPatientController controller = loader.getController();
            controller.loadInvoiceData(appointmentId);
            
            rightBoxContainer.getChildren().add(overviewRoot);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void handlePay() {
        try {
            Scene currentScene = payButton.getScene();
            VBox rightBoxContainer = (VBox) currentScene.lookup("#rightBoxContainer");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/edu/careflow/fxml/forms/payment-form.fxml"));
            Parent formRoot = loader.load();
            PaymentFormController formController = loader.getController();
            formController.setPaymentInfo(amountLabel.getText(), serviceLabel.getText());
            
            // Get the billing ID from the current invoice
            Billing currentBilling = billingDAO.getBillingByAppointmentId(appointmentId);
            if (currentBilling != null) {
                formController.setBillingInfo(currentBilling.getBillingId(), appointmentId);
            }
            
            formController.setOnPaymentSuccess(() -> {
                try {
                    billingDAO.updateBillingStatusByAppointmentId(appointmentId, "PAID");
                    statusLabel.setText("PAID");
                    statusLabel.getStyleClass().removeAll("unpaid-status");
                    statusLabel.getStyleClass().add("paid-status");
                    payButton.setVisible(false);
                    if (onPaidCallback != null) onPaidCallback.run();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });
            rightBoxContainer.getChildren().add(formRoot);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
} 