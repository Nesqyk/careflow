package edu.careflow.presentation.controllers.patient;

import edu.careflow.presentation.controllers.patient.cards.InvoiceCardPatientController;
import edu.careflow.presentation.controllers.patient.forms.PaymentFormController;
import edu.careflow.repository.dao.BillingDAO;
import edu.careflow.repository.entities.Billing;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Pagination;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class PatientInvoicesController {
    @FXML private VBox invoiceCardContainer;
    @FXML private VBox mainContainer;
    @FXML private Pagination paginationContainer;
    @FXML private Button downloadBtn;
    @FXML private VBox summaryCardContainer;

    private static final int CARDS_PER_PAGE = 4;
    private List<Billing> allInvoices;
    private Integer patientId;
    private final BillingDAO billingDAO = new BillingDAO();

    public void setPatientId(Integer patientId) {
        this.patientId = patientId;
        loadInvoicesForPatient();
    }

    @FXML
    public void initialize() {
        if (patientId != null) {
            loadInvoicesForPatient();
        }
        downloadBtn.setOnAction(e -> handleDownload());
    }

    private void loadInvoicesForPatient() {
        try {
            allInvoices = billingDAO.getBillingsByPatientId(patientId);
            createSummaryCard();
            setupPagination();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createSummaryCard() {
        if (allInvoices == null || allInvoices.isEmpty()) {
            return;
        }

        // Create summary card
        VBox summaryCard = new VBox(10);
        summaryCard.getStyleClass().add("invoice-card");
        summaryCard.setStyle("-fx-background-color: #f8f9fa; -fx-padding: 15; -fx-background-radius: 8;");

        // Header
        Label summaryTitle = new Label("Patient Billing Summary");
        summaryTitle.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");
        
        // Total amount
        BigDecimal total = getTotalForPatient();
        Label totalLabel = new Label("Total Amount: ₱" + total.setScale(2, BigDecimal.ROUND_HALF_UP));
        totalLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");
        
        // Number of invoices
        Label invoiceCountLabel = new Label("Number of Invoices: " + allInvoices.size());
        
        // Group by appointment
        Map<Integer, List<Billing>> invoicesByAppointment = allInvoices.stream()
                .collect(Collectors.groupingBy(Billing::getAppointmentId));
        
        Label appointmentCountLabel = new Label("Number of Appointments: " + invoicesByAppointment.size());
        
        // Add a separator
        Region separator = new Region();
        separator.setStyle("-fx-background-color: #dee2e6;");
        separator.setPrefHeight(1);
        HBox.setHgrow(separator, Priority.ALWAYS);
        
        // Add a pay all button if there are unpaid invoices
        boolean hasUnpaidInvoices = allInvoices.stream()
                .anyMatch(billing -> "UNPAID".equalsIgnoreCase(billing.getStatus()));
        
        if (hasUnpaidInvoices) {
            Button payAllButton = new Button("Pay All Unpaid Invoices");
            payAllButton.getStyleClass().add("primary-button");
            payAllButton.setOnAction(e -> handlePayAll());
            summaryCard.getChildren().addAll(summaryTitle, totalLabel, invoiceCountLabel, appointmentCountLabel, separator, payAllButton);
        } else {
            summaryCard.getChildren().addAll(summaryTitle, totalLabel, invoiceCountLabel, appointmentCountLabel, separator);
        }
        
        // Add the summary card to the container
        summaryCardContainer.getChildren().clear();
        summaryCardContainer.getChildren().add(summaryCard);
    }
    
    private void handlePayAll() {
        // Create a single payment for all unpaid invoices
        List<Billing> unpaidInvoices = allInvoices.stream()
                .filter(billing -> "UNPAID".equalsIgnoreCase(billing.getStatus()))
                .collect(Collectors.toList());
                
        if (unpaidInvoices.isEmpty()) {
            return;
        }

        BigDecimal totalUnpaid = unpaidInvoices.stream()
                .map(Billing::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
                
        try {
            Scene currentScene = downloadBtn.getScene();
            VBox rightBoxContainer = (VBox) currentScene.lookup("#rightBoxContainer");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/edu/careflow/fxml/forms/payment-form.fxml"));
            Node formRoot = loader.load();
            PaymentFormController formController = loader.getController();
            formController.setPaymentInfo("₱" + totalUnpaid.setScale(2, BigDecimal.ROUND_HALF_UP), "All Unpaid Invoices");
            
            // Use the first unpaid invoice's ID for the payment form
            // This is just for the payment form to work, we'll update all invoices on success
            formController.setBillingInfo(unpaidInvoices.get(0).getBillingId(), null);
            
            formController.setOnPaymentSuccess(() -> {
                try {
                    // Update all unpaid invoices to PAID
                    for (Billing billing : unpaidInvoices) {
                        billing.setStatus("PAID");
                        billing.setPaidDate(LocalDateTime.now());
                        billingDAO.updateBilling(billing);
                    }
                    // Refresh the view
                    loadInvoicesForPatient();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            });
            rightBoxContainer.getChildren().add(formRoot);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void setupPagination() {
        int pageCount = (int) Math.ceil((double) allInvoices.size() / CARDS_PER_PAGE);
        paginationContainer.setPageCount(Math.max(pageCount, 1));
        paginationContainer.setCurrentPageIndex(0);
        paginationContainer.setPageFactory(this::createPage);
    }

    private VBox createPage(int pageIndex) {
        VBox pageBox = new VBox(12);
        int start = pageIndex * CARDS_PER_PAGE;
        int end = Math.min(start + CARDS_PER_PAGE, allInvoices.size());
        for (int i = start; i < end; i++) {
            Billing billing = allInvoices.get(i);
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/edu/careflow/fxml/components/patient/invoiceCardPatient.fxml"));
                Node card = loader.load();
                InvoiceCardPatientController controller = loader.getController();
                controller.initializeData(billing);
                controller.setOnPaidCallback(this::loadInvoicesForPatient);
                pageBox.getChildren().add(card);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        mainContainer.getChildren().setAll(pageBox.getChildren());
        return pageBox;
    }

    private void handleDownload() {
        // TODO: Implement download logic (e.g., export to PDF/Excel)
        System.out.println("Download invoices clicked");
    }

    public BigDecimal getTotalForPatient() {
        if (allInvoices == null) return BigDecimal.ZERO;
        return allInvoices.stream().map(Billing::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
} 