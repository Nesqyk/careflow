package edu.careflow.presentation.controllers.receptionist;

import edu.careflow.repository.dao.BillingDAO;
import edu.careflow.repository.entities.Billing;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ReceptionistInvoicesController {
    @FXML private VBox mainContainer;
    @FXML private Pagination paginationContainer;
    @FXML private Button downloadBtn;
    @FXML private TextField invoiceSearchField;

    private List<Billing> allInvoices = new ArrayList<>();
    private List<Billing> filteredInvoices = new ArrayList<>();
    private static final int CARDS_PER_PAGE = 5;
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    @FXML
    public void initialize() {
        try {
            allInvoices = new BillingDAO().getAllBillings();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (invoiceSearchField != null) {
            invoiceSearchField.textProperty().addListener((obs, oldVal, newVal) -> updateInvoiceCards(newVal));
        }
        if (downloadBtn != null) {
            downloadBtn.setOnAction(e -> handleDownload());
        }
        updateInvoiceCards("");
    }

    private void updateInvoiceCards(String searchText) {
        String lower = searchText == null ? "" : searchText.toLowerCase();
        filteredInvoices = allInvoices.stream()
                .filter(inv -> String.valueOf(inv.getBillingId()).contains(lower))
                .toList();
        setupPagination();
    }

    private void setupPagination() {
        mainContainer.getChildren().clear();
        if (filteredInvoices.isEmpty()) {
            mainContainer.getChildren().add(new Label("No invoices found"));
            if (paginationContainer != null) paginationContainer.setVisible(false);
            return;
        }
        int totalPages = (int) Math.ceil((double) filteredInvoices.size() / CARDS_PER_PAGE);
        if (paginationContainer != null) {
            paginationContainer.setPageCount(totalPages);
            paginationContainer.setCurrentPageIndex(0);
            paginationContainer.setPageFactory(this::createPage);
            paginationContainer.setVisible(totalPages > 1);
        } else {
            VBox page = createPage(0);
            mainContainer.getChildren().add(page);
        }
    }

    private VBox createPage(int pageIndex) {
        VBox pageBox = new VBox(12);
        int start = pageIndex * CARDS_PER_PAGE;
        int end = Math.min(start + CARDS_PER_PAGE, filteredInvoices.size());
        for (int i = start; i < end; i++) {
            Billing invoice = filteredInvoices.get(i);
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/edu/careflow/fxml/components/receptionist/invoiceCard.fxml"));
                Parent card = loader.load();
                ReceptionistInvoiceCardController controller = loader.getController();
                
                // Initialize the card with proper callbacks
                controller.initializeData(
                    invoice,
                    () -> handleEditInvoice(invoice),
                    () -> handleDownloadInvoice(invoice),
                    () -> handleViewInvoice(invoice)
                );
                
                pageBox.getChildren().add(card);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        mainContainer.getChildren().setAll(pageBox.getChildren());
        return pageBox;
    }

    private void handleEditInvoice(Billing invoice) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/edu/careflow/fxml/forms/generate-invoice-form.fxml"));
            Parent formRoot = loader.load();
            edu.careflow.presentation.controllers.doctor.GenerateInvoiceController formController = loader.getController();
            formController.setEditMode(invoice);
            formController.setOnSuccessCallback(() -> {
                // Refresh the invoice list after editing
                refreshInvoices();
                // Clear the right container
                VBox rightBoxContainer = (VBox) mainContainer.getScene().lookup("#rightBoxContainer");
                if (rightBoxContainer != null) {
                    rightBoxContainer.getChildren().clear();
                }
            });
            
            VBox rightBoxContainer = (VBox) mainContainer.getScene().lookup("#rightBoxContainer");
            if (rightBoxContainer != null) {
                rightBoxContainer.getChildren().clear();
                rightBoxContainer.getChildren().add(formRoot);
            }
        } catch (IOException e) {
            e.printStackTrace();
            showError("Error", "Failed to open edit form: " + e.getMessage());
        }
    }

    private void handleDownloadInvoice(Billing invoice) {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Invoice");
            fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Excel Files", "*.xlsx")
            );
            fileChooser.setInitialFileName("invoice_" + invoice.getBillingId() + ".xlsx");
            File file = fileChooser.showSaveDialog(mainContainer.getScene().getWindow());
            if (file != null) {
                exportSingleInvoiceToExcel(invoice, file);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showError("Error", "Failed to export invoice: " + e.getMessage());
        }
    }

    private void handleViewInvoice(Billing invoice) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/edu/careflow/fxml/components/patient/invoiceOverviewPatient.fxml"));
            Parent overviewRoot = loader.load();
            
            // Get the controller and set the billing data
            edu.careflow.presentation.controllers.patient.InvoiceOverviewPatientController controller = loader.getController();
            controller.loadInvoiceData(invoice.getAppointmentId());
            
            VBox rightBoxContainer = (VBox) mainContainer.getScene().lookup("#rightBoxContainer");
            if (rightBoxContainer != null) {
                rightBoxContainer.getChildren().clear();
                rightBoxContainer.getChildren().add(overviewRoot);
            }
        } catch (IOException e) {
            e.printStackTrace();
            showError("Error", "Failed to open invoice overview: " + e.getMessage());
        }
    }

    private void refreshInvoices() {
        try {
            allInvoices = new BillingDAO().getAllBillings();
            updateInvoiceCards(invoiceSearchField.getText());
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Error", "Failed to refresh invoices: " + e.getMessage());
        }
    }

    private void handleDownload() {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Invoice List");
            fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Excel Files", "*.xlsx")
            );
            fileChooser.setInitialFileName("invoices_report.xlsx");
            File file = fileChooser.showSaveDialog(mainContainer.getScene().getWindow());
            if (file != null) {
                exportInvoicesToExcel(file);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showError("Error", "Failed to export invoices: " + e.getMessage());
        }
    }

    private void exportSingleInvoiceToExcel(Billing invoice, File file) throws Exception {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Invoice " + invoice.getBillingId());
            String[] columns = {
                "Billing ID", "Patient ID", "Doctor ID", "Service Rate ID", "Amount", "Subtotal", 
                "Tax Amount", "Discount Amount", "Status", "Due Date", "Paid Date", "Billing Date", 
                "Description", "Payment Method", "Reference Number", "Created At", "Updated At", "Appointment ID"
            };
            
            // Create header row
            Row headerRow = sheet.createRow(0);
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, 20 * 256);
            }

            // Create data row
            Row dataRow = sheet.createRow(1);
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            int col = 0;
            
            dataRow.createCell(col++).setCellValue(invoice.getBillingId());
            dataRow.createCell(col++).setCellValue(invoice.getPatientId());
            dataRow.createCell(col++).setCellValue(invoice.getDoctorId());
            dataRow.createCell(col++).setCellValue(invoice.getServiceRateId());
            dataRow.createCell(col++).setCellValue(invoice.getAmount() != null ? invoice.getAmount().doubleValue() : 0);
            dataRow.createCell(col++).setCellValue(invoice.getSubtotal() != null ? invoice.getSubtotal().doubleValue() : 0);
            dataRow.createCell(col++).setCellValue(invoice.getTaxAmount() != null ? invoice.getTaxAmount().doubleValue() : 0);
            dataRow.createCell(col++).setCellValue(invoice.getDiscountAmount() != null ? invoice.getDiscountAmount().doubleValue() : 0);
            dataRow.createCell(col++).setCellValue(invoice.getStatus());
            dataRow.createCell(col++).setCellValue(invoice.getDueDate() != null ? invoice.getDueDate().format(dateTimeFormatter) : "");
            dataRow.createCell(col++).setCellValue(invoice.getPaidDate() != null ? invoice.getPaidDate().format(dateTimeFormatter) : "");
            dataRow.createCell(col++).setCellValue(invoice.getBillingDate() != null ? invoice.getBillingDate().format(dateTimeFormatter) : "");
            dataRow.createCell(col++).setCellValue(invoice.getDescription());
            dataRow.createCell(col++).setCellValue(invoice.getPaymentMethod());
            dataRow.createCell(col++).setCellValue(invoice.getReferenceNumber());
            dataRow.createCell(col++).setCellValue(invoice.getCreatedAt() != null ? invoice.getCreatedAt().format(dateTimeFormatter) : "");
            dataRow.createCell(col++).setCellValue(invoice.getUpdatedAt() != null ? invoice.getUpdatedAt().format(dateTimeFormatter) : "");
            dataRow.createCell(col++).setCellValue(invoice.getAppointmentId() != null ? invoice.getAppointmentId() : 0);

            try (FileOutputStream fileOut = new FileOutputStream(file)) {
                workbook.write(fileOut);
            }
            showSuccess("Success", "Invoice exported successfully!");
        }
    }

    private void exportInvoicesToExcel(File file) throws Exception {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Invoices");
            String[] columns = {
                "Billing ID", "Patient ID", "Doctor ID", "Service Rate ID", "Amount", "Subtotal", "Tax Amount", "Discount Amount", "Status", "Due Date", "Paid Date", "Billing Date", "Description", "Payment Method", "Reference Number", "Created At", "Updated At", "Appointment ID"
            };
            Row headerRow = sheet.createRow(0);
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, 20 * 256);
            }
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            int rowNum = 1;
            for (Billing invoice : filteredInvoices) {
                Row row = sheet.createRow(rowNum++);
                int col = 0;
                row.createCell(col++).setCellValue(invoice.getBillingId());
                row.createCell(col++).setCellValue(invoice.getPatientId());
                row.createCell(col++).setCellValue(invoice.getDoctorId());
                row.createCell(col++).setCellValue(invoice.getServiceRateId());
                row.createCell(col++).setCellValue(invoice.getAmount() != null ? invoice.getAmount().doubleValue() : 0);
                row.createCell(col++).setCellValue(invoice.getSubtotal() != null ? invoice.getSubtotal().doubleValue() : 0);
                row.createCell(col++).setCellValue(invoice.getTaxAmount() != null ? invoice.getTaxAmount().doubleValue() : 0);
                row.createCell(col++).setCellValue(invoice.getDiscountAmount() != null ? invoice.getDiscountAmount().doubleValue() : 0);
                row.createCell(col++).setCellValue(invoice.getStatus());
                row.createCell(col++).setCellValue(invoice.getDueDate() != null ? invoice.getDueDate().format(dateTimeFormatter) : "");
                row.createCell(col++).setCellValue(invoice.getPaidDate() != null ? invoice.getPaidDate().format(dateTimeFormatter) : "");
                row.createCell(col++).setCellValue(invoice.getBillingDate() != null ? invoice.getBillingDate().format(dateTimeFormatter) : "");
                row.createCell(col++).setCellValue(invoice.getDescription());
                row.createCell(col++).setCellValue(invoice.getPaymentMethod());
                row.createCell(col++).setCellValue(invoice.getReferenceNumber());
                row.createCell(col++).setCellValue(invoice.getCreatedAt() != null ? invoice.getCreatedAt().format(dateTimeFormatter) : "");
                row.createCell(col++).setCellValue(invoice.getUpdatedAt() != null ? invoice.getUpdatedAt().format(dateTimeFormatter) : "");
                row.createCell(col++).setCellValue(invoice.getAppointmentId() != null ? invoice.getAppointmentId() : 0);
            }
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }
            try (FileOutputStream fileOut = new FileOutputStream(file)) {
                workbook.write(fileOut);
            }
            showSuccess("Success", "Invoices exported successfully!");
        }
    }

    private void showError(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showSuccess(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 