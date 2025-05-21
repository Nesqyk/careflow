package edu.careflow.presentation.controllers.patient;

import edu.careflow.repository.dao.BillingDAO;
import edu.careflow.repository.dao.PatientDAO;
import edu.careflow.repository.dao.AppointmentDAO;
import edu.careflow.repository.dao.BillRateDAO;
import edu.careflow.repository.entities.Billing;
import edu.careflow.repository.entities.Patient;
import edu.careflow.repository.entities.Appointment;
import edu.careflow.repository.entities.BillRate;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.print.*;
import javafx.scene.Node;
import javafx.scene.layout.Pane;
import javafx.scene.web.WebView;
import javafx.scene.web.WebEngine;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.paint.Color;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.List;

public class InvoiceOverviewPatientController {
    @FXML private Label invoiceIdLabel;
    @FXML private Label statusLabel;
    @FXML private Label patientNameLabel;
    @FXML private Label patientIdLabel;
    @FXML private Label issueDateLabel;
    @FXML private Label dueDateLabel;
    @FXML private Label paymentMethodLabel;
    @FXML private Label paymentStatusLabel;
    @FXML private Label subtotalLabel;
    @FXML private Label taxLabel;
    @FXML private Label totalLabel;
    @FXML private TableView<Billing> servicesTable;
    @FXML private TableColumn<Billing, String> serviceColumn;
    @FXML private TableColumn<Billing, String> descriptionColumn;
    @FXML private TableColumn<Billing, Integer> quantityColumn;
    @FXML private TableColumn<Billing, BigDecimal> rateColumn;
    @FXML private TableColumn<Billing, BigDecimal> amountColumn;
    @FXML private Button closeButton;
    @FXML private Button printButton;
    @FXML private Button downloadButton;

    private final BillingDAO billingDAO = new BillingDAO();
    private final PatientDAO patientDAO = new PatientDAO();
    private final AppointmentDAO appointmentDAO = new AppointmentDAO();
    private final BillRateDAO billRateDAO = new BillRateDAO();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    private Billing currentBilling;
    private BillRate currentBillRate;

    @FXML
    public void initialize() {
        setupTableColumns();
        setupButtons();
        setupStyles();
    }

    private void setupTableColumns() {
        serviceColumn.setCellValueFactory(cellData -> {
            try {
                BillRate rate = billRateDAO.getBillRateById(cellData.getValue().getServiceRateId());
                return new SimpleStringProperty(rate != null ? rate.getServiceType() : "Unknown Service");
            } catch (Exception e) {
                return new SimpleStringProperty("Error loading service");
            }
        });
        
        descriptionColumn.setCellValueFactory(new PropertyValueFactory<>("description"));
        
        quantityColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(1).asObject());
        
        rateColumn.setCellValueFactory(cellData -> {
            try {
                BillRate rate = billRateDAO.getBillRateById(cellData.getValue().getServiceRateId());
                return new SimpleObjectProperty<>(
                    rate != null ? rate.getRateAmount() : BigDecimal.ZERO);
            } catch (Exception e) {
                return new SimpleObjectProperty<>(BigDecimal.ZERO);
            }
        });
        
        amountColumn.setCellValueFactory(cellData -> {
            Billing billing = cellData.getValue();
            BigDecimal amount = billing.getAmount();
            return new SimpleObjectProperty<>(amount != null ? amount : BigDecimal.ZERO);
        });

        rateColumn.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal amount, boolean empty) {
                super.updateItem(amount, empty);
                if (empty || amount == null) {
                    setText(null);
                } else {
                    setText(String.format("₱%.2f", amount));
                }
            }
        });

        amountColumn.setCellFactory(tc -> new TableCell<>() {
            @Override
            protected void updateItem(BigDecimal amount, boolean empty) {
                super.updateItem(amount, empty);
                if (empty || amount == null) {
                    setText(null);
                } else {
                    setText(String.format("₱%.2f", amount));
                }
            }
        });
        
        servicesTable.setSelectionModel(null);
    }

    private void setupButtons() {
        closeButton.setOnAction(e -> {
            VBox parent = (VBox) closeButton.getScene().lookup("#rightBoxContainer");
            if (parent != null) {
                parent.getChildren().remove(closeButton.getParent().getParent());
            }
        });

        printButton.setOnAction(e -> handlePrint());
        downloadButton.setOnAction(e -> handleDownload());
    }

    private void setupStyles() {
        // Add CSS classes for status labels
        statusLabel.getStyleClass().add("status-label");
        paymentStatusLabel.getStyleClass().add("status-label");
    }

    public void loadInvoiceData(int appointmentId) {
        try {
            currentBilling = billingDAO.getBillingByAppointmentId(appointmentId);
            if (currentBilling != null) {
                // Load the associated bill rate
                currentBillRate = billRateDAO.getBillRateById(currentBilling.getServiceRateId());
                
                Appointment appointment = appointmentDAO.getAppointmentById(appointmentId);
                Patient patient = null;
                
                if (appointment != null) {
                    patient = patientDAO.getPatientById(appointment.getPatientId());
                }
                
                // Set basic invoice information
                invoiceIdLabel.setText("#INV-" + String.format("%05d", currentBilling.getBillingId()));
                
                // Set status with appropriate styling
                String status = currentBilling.getStatus();
                statusLabel.setText(status);
                statusLabel.getStyleClass().removeAll("paid-status", "unpaid-status");
                statusLabel.getStyleClass().add("PAID".equalsIgnoreCase(status) ? "paid-status" : "unpaid-status");

                // Set patient information
                if (patient != null) {
                    patientNameLabel.setText(patient.getFirstName() + " " + patient.getLastName());
                    patientIdLabel.setText("PT-" + String.format("%05d", patient.getPatientId()));
                }

                // Set dates
                if (appointment != null && appointment.getAppointmentDate() != null) {
                    issueDateLabel.setText(appointment.getAppointmentDate().format(dateFormatter));
                }
                if (currentBilling.getDueDate() != null) {
                    dueDateLabel.setText(currentBilling.getDueDate().format(dateFormatter));
                }

                // Set payment information
                paymentMethodLabel.setText(currentBilling.getPaymentMethod() != null ? 
                    currentBilling.getPaymentMethod() : "Not specified");
                paymentStatusLabel.setText(status);
                paymentStatusLabel.getStyleClass().removeAll("paid-status", "unpaid-status");
                paymentStatusLabel.getStyleClass().add("PAID".equalsIgnoreCase(status) ? "paid-status" : "unpaid-status");

                // Calculate and set amounts
                BigDecimal subtotal = currentBilling.getSubtotal();
                BigDecimal tax = currentBilling.getTaxAmount();
                BigDecimal discount = currentBilling.getDiscountAmount();
                BigDecimal total = currentBilling.getAmount();

                // Ensure amounts are not null
                if (subtotal == null) subtotal = BigDecimal.ZERO;
                if (tax == null) tax = BigDecimal.ZERO;
                if (discount == null) discount = BigDecimal.ZERO;
                if (total == null) total = BigDecimal.ZERO;

                // Format and display amounts
                subtotalLabel.setText(String.format("₱%.2f", subtotal));
                taxLabel.setText(String.format("₱%.2f", tax));
                totalLabel.setText(String.format("₱%.2f", total));

                // Load services into table
                servicesTable.getItems().clear();
                servicesTable.getItems().add(currentBilling);
            } else {
                showError("No billing information found for this appointment.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showError("Failed to load invoice data: " + e.getMessage());
        }
    }

    private void handlePrint() {
        if (currentBilling == null) {
            showError("No invoice data available to print.");
            return;
        }

        try {
            // Create a print job
            PrinterJob job = PrinterJob.createPrinterJob();
            if (job != null) {
                // Show print dialog
                boolean proceed = job.showPrintDialog(closeButton.getScene().getWindow());
                if (proceed) {
                    // Get the node to print (the entire invoice view)
                    Node nodeToPrint = closeButton.getScene().lookup("#invoiceContainer");
                    if (nodeToPrint != null) {
                        // Print the node
                        boolean success = job.printPage(nodeToPrint);
                        if (success) {
                            job.endJob();
                            showSuccess("Invoice printed successfully.");
                        } else {
                            showError("Failed to print invoice.");
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showError("Error while printing: " + e.getMessage());
        }
    }

    private void handleDownload() {
        if (currentBilling == null) {
            showError("No invoice data available to download.");
            return;
        }

        try {
            // Create HTML content for the invoice
            String htmlContent = generateInvoiceHTML();
            
            // Create a temporary file
            String fileName = "Invoice_" + currentBilling.getBillingId() + ".html";
            File file = new File(System.getProperty("user.home") + File.separator + "Downloads" + File.separator + fileName);
            
            // Write HTML content to file
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(htmlContent);
            }
            
            showSuccess("Invoice downloaded successfully to: " + file.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
            showError("Failed to download invoice: " + e.getMessage());
        }
    }

    private String generateInvoiceHTML() {
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>")
            .append("<html>")
            .append("<head>")
            .append("<style>")
            .append("body { font-family: Arial, sans-serif; margin: 20px; }")
            .append(".invoice-header { text-align: center; margin-bottom: 20px; }")
            .append(".invoice-details { margin-bottom: 20px; }")
            .append(".invoice-table { width: 100%; border-collapse: collapse; }")
            .append(".invoice-table th, .invoice-table td { border: 1px solid #ddd; padding: 8px; }")
            .append(".invoice-total { text-align: right; margin-top: 20px; }")
            .append("</style>")
            .append("</head>")
            .append("<body>")
            .append("<div class='invoice-header'>")
            .append("<h1>Medical Invoice</h1>")
            .append("<p>Invoice #: ").append(invoiceIdLabel.getText()).append("</p>")
            .append("</div>")
            .append("<div class='invoice-details'>")
            .append("<p><strong>Patient:</strong> ").append(patientNameLabel.getText()).append("</p>")
            .append("<p><strong>Patient ID:</strong> ").append(patientIdLabel.getText()).append("</p>")
            .append("<p><strong>Issue Date:</strong> ").append(issueDateLabel.getText()).append("</p>")
            .append("<p><strong>Due Date:</strong> ").append(dueDateLabel.getText()).append("</p>")
            .append("</div>")
            .append("<table class='invoice-table'>")
            .append("<tr><th>Service</th><th>Description</th><th>Quantity</th><th>Rate</th><th>Amount</th></tr>");

        // Add service rows
        for (Billing billing : servicesTable.getItems()) {
            try {
                BillRate rate = billRateDAO.getBillRateById(billing.getServiceRateId());
                html.append("<tr>")
                    .append("<td>").append(rate != null ? rate.getServiceType() : "Unknown Service").append("</td>")
                    .append("<td>").append(billing.getDescription()).append("</td>")
                    .append("<td>1</td>")
                    .append("<td>").append(rate != null ? String.format("₱%.2f", rate.getRateAmount()) : "₱0.00").append("</td>")
                    .append("<td>").append(String.format("₱%.2f", billing.getAmount())).append("</td>")
                    .append("</tr>");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        html.append("</table>")
            .append("<div class='invoice-total'>")
            .append("<p><strong>Subtotal:</strong> ").append(subtotalLabel.getText()).append("</p>")
            .append("<p><strong>Tax (10%):</strong> ").append(taxLabel.getText()).append("</p>")
            .append("<p><strong>Total:</strong> ").append(totalLabel.getText()).append("</p>")
            .append("</div>")
            .append("</body>")
            .append("</html>");

        return html.toString();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
} 