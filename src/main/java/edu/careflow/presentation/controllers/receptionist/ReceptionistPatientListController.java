package edu.careflow.presentation.controllers.receptionist;

import edu.careflow.presentation.controllers.patient.PatientMedicalController;
import edu.careflow.presentation.controllers.patient.forms.AddPatientForm;
import edu.careflow.presentation.controllers.patient.forms.BookAptForm;
import edu.careflow.repository.dao.AppointmentDAO;
import edu.careflow.repository.dao.PatientDAO;
import edu.careflow.repository.entities.Patient;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import javafx.stage.FileChooser;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ReceptionistPatientListController {
    @FXML private VBox appointmentContainer;
    @FXML private TextField patientSearchField;
    @FXML private Button addPatientBtn;
    @FXML private VBox rightBoxContainer;
    @FXML private Pagination paginationContainer;
    @FXML private VBox mainContainer;
    @FXML private Button downloadBtn;

    private List<Patient> allPatients = new ArrayList<>();
    private List<Patient> filteredPatients = new ArrayList<>();
    private static final int CARDS_PER_PAGE = 5;

    @FXML
    public void initialize() {
        try {
            allPatients = new PatientDAO().getAllPatients();
            System.out.println("Loaded " + allPatients.size() + " patients");
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Error", "Failed to load patients: " + e.getMessage());
        }
        
        if (mainContainer == null) {
            System.out.println("Warning: mainContainer is null in initialize()");
        }
        
        if (patientSearchField != null) {
            patientSearchField.textProperty().addListener((obs, oldVal, newVal) -> updatePatientCards(newVal));
        }
        if (addPatientBtn != null) {
            addPatientBtn.setOnAction(e -> handleAddPatient());
        }
    
        try {
            downloadBtn = (Button) mainContainer.getScene().lookup("#downloadBtn");
        } catch (Exception ignored) {}
        if (downloadBtn != null) {
            downloadBtn.setOnAction(e -> handleDownload());
        }
        
        updatePatientCards("");
    }

    private void updatePatientCards(String searchText) {
        System.out.println("updatePatientCards called with searchText: '" + searchText + "'");
        String lower = searchText == null ? "" : searchText.toLowerCase();
        filteredPatients = allPatients.stream()
                .filter(p -> p.getFirstName().toLowerCase().contains(lower) ||
                        p.getLastName().toLowerCase().contains(lower))
                .collect(Collectors.toList());
        System.out.println("Filtered patients count: " + filteredPatients.size());
        
        if (mainContainer == null) {
            System.out.println("Error: mainContainer is null in updatePatientCards()");
            return;
        }
        
        // Clear the mainContainer
        mainContainer.getChildren().clear();
        
        if (filteredPatients.isEmpty()) {
            VBox emptyState = new VBox(10);
            emptyState.setAlignment(javafx.geometry.Pos.CENTER);
            emptyState.setPrefHeight(200);
            
            Label emptyLabel = new Label("No patients found");
            emptyLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #666;");
            
            Button addPatientButton = new Button("Add New Patient");
            addPatientButton.setStyle("-fx-background-color: #0762F2; -fx-text-fill: white; -fx-padding: 8 16;");
            addPatientButton.setOnAction(e -> handleAddPatient());
            
            emptyState.getChildren().addAll(emptyLabel, addPatientButton);
            mainContainer.getChildren().add(emptyState);
            
            if (paginationContainer != null) paginationContainer.setVisible(false);
            return;
        }
        
        // Create a VBox to hold all patient cards
        VBox cardsContainer = new VBox(12);
        
        // Add each patient card directly
        for (Patient patient : filteredPatients) {
            try {
                System.out.println("Loading card for patient: " + patient.getFirstName() + " " + patient.getLastName());
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/edu/careflow/fxml/receptionist/receptionistPatientCard.fxml"));
                Parent card = loader.load();
                ReceptionistPatientCardController controller = loader.getController();
                controller.initializeData(
                    patient,
                    () -> handleBookAppointment(patient),
                    () -> handleEditPatient(patient),
                    () -> handleDeletePatient(patient),
                    null,
                    () -> handleAddInvoice(patient)
                );
                
                // Set patient details
                controller.patientNameLabel.setText(patient.getFirstName() + " " + patient.getLastName());
                controller.patientIdLabel.setText("ID: " + patient.getPatientId());
                
                // Get appointment data
                AppointmentDAO appointmentDAO = new AppointmentDAO();
                int visitCount = 0;
                String lastVisit = "-";
                try {
                    var appointments = appointmentDAO.getAppointmentsByPatientId(patient.getPatientId());
                    visitCount = (int) appointments.stream().filter(a -> "Completed".equalsIgnoreCase(a.getStatus())).count();
                    LocalDateTime lastVisitDate = appointments.stream()
                        .filter(a -> "Completed".equalsIgnoreCase(a.getStatus()))
                        .map(a -> a.getAppointmentDate())
                        .max(LocalDateTime::compareTo)
                        .orElse(null);
                    if (lastVisitDate != null) {
                        lastVisit = lastVisitDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy"));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                
                controller.lastVisitDateLabel.setText("Last Visit: " + lastVisit);
                controller.visitCountLabel.setText(String.valueOf(visitCount));
                
                cardsContainer.getChildren().add(card);
                System.out.println("Added card for patient: " + patient.getFirstName() + " " + patient.getLastName());
            } catch (IOException e) {
                System.out.println("Failed to load card for patient: " + patient.getFirstName() + " " + patient.getLastName());
                e.printStackTrace();
            }
        }
        
        // Add the cards container to the main container
        mainContainer.getChildren().add(cardsContainer);
        
        // Hide pagination since we're showing all cards at once
        if (paginationContainer != null) {
            paginationContainer.setVisible(false);
        }
    }

    private void handleAddPatient() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/edu/careflow/fxml/components/user/addPatientForm.fxml"));
            Parent patientForm = loader.load();
            AddPatientForm controller = loader.getController();
            controller.setOnSuccessCallback(() -> {
                try {
                    allPatients = new PatientDAO().getAllPatients();
                    updatePatientCards(patientSearchField.getText());
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            });
            if (rightBoxContainer != null) {
                rightBoxContainer.getChildren().clear();
                rightBoxContainer.getChildren().add(patientForm);
                FadeTransition fadeIn = new FadeTransition(Duration.millis(200), patientForm);
                fadeIn.setFromValue(0.0);
                fadeIn.setToValue(1.0);
                fadeIn.play();
            }
        } catch (IOException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Failed to load add patient form").show();
        }
    }

    private void handleBookAppointment(Patient patient) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/edu/careflow/fxml/components/patient/bookAptForm.fxml"));
            Parent bookForm = loader.load();
            BookAptForm controller = loader.getController();
            controller.setPatientId(patient.getPatientId());

            VBox rightBoxContainer = (VBox) appointmentContainer.getScene().lookup("#rightBoxContainer");
            if (rightBoxContainer != null) {
                rightBoxContainer.getChildren().clear();
                rightBoxContainer.getChildren().add(bookForm);
                FadeTransition fadeIn = new FadeTransition(Duration.millis(200), bookForm);
                fadeIn.setFromValue(0.0);
                fadeIn.setToValue(1.0);
                fadeIn.play();
            }
        } catch (IOException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Failed to load appointment form").show();
        }
    }

    private void handleEditPatient(Patient patient) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/edu/careflow/fxml/components/user/addPatientForm.fxml"));
            Parent form = loader.load();
            AddPatientForm formController = loader.getController();
            formController.setPatientId(patient.getPatientId());
            formController.setFirstName(patient.getFirstName());
            formController.setLastName(patient.getLastName());
            formController.setDateOfBirth(patient.getDateOfBirth());
            formController.setGender(patient.getGender());
            formController.setContact(patient.getContact());
            formController.setEmail(patient.getEmail());
            formController.setAddress(patient.getAddress());
            formController.setOnSuccessCallback(() -> {
                try {
                    allPatients = new PatientDAO().getAllPatients();
                    updatePatientCards(patientSearchField.getText());
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            });

            VBox rightBoxContainer = (VBox) appointmentContainer.getScene().lookup("#rightBoxContainer");
            if (rightBoxContainer != null) {
                rightBoxContainer.getChildren().clear();
                rightBoxContainer.getChildren().add(form);
                FadeTransition fadeIn = new FadeTransition(Duration.millis(200), form);
                fadeIn.setFromValue(0.0);
                fadeIn.setToValue(1.0);
                fadeIn.play();
            }
        } catch (IOException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Failed to load patient form").show();
        }
    }

    private void handleDeletePatient(Patient patient) {
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirm Delete");
        confirmDialog.setHeaderText("Delete Patient");
        confirmDialog.setContentText("Are you sure you want to delete " + patient.getFirstName() + " " + patient.getLastName() + "?");
        confirmDialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    if (new PatientDAO().deletePatient(patient.getPatientId())) {
                        allPatients = new PatientDAO().getAllPatients();
                        updatePatientCards(patientSearchField.getText());
                        new Alert(Alert.AlertType.INFORMATION, "Patient deleted successfully").show();
                    } else {
                        new Alert(Alert.AlertType.ERROR, "Failed to delete patient").show();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    new Alert(Alert.AlertType.ERROR, "Error deleting patient: " + e.getMessage()).show();
                }
            }
        });
    }

    private void handleViewHistory(Patient patient) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/edu/careflow/fxml/patient/patientMedical.fxml"));
            Parent historyView = loader.load();
            PatientMedicalController controller = loader.getController();
            controller.initializeData(patient.getPatientId());
            if (rightBoxContainer != null) {
                rightBoxContainer.getChildren().clear();
                rightBoxContainer.getChildren().add(historyView);
                FadeTransition fadeIn = new FadeTransition(Duration.millis(200), historyView);
                fadeIn.setFromValue(0.0);
                fadeIn.setToValue(1.0);
                fadeIn.play();
            }
        } catch (IOException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Failed to load patient history").show();
        }
    }

    private void handleAddInvoice(Patient patient) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/edu/careflow/fxml/forms/generate-invoice-form.fxml"));
            Parent invoiceForm = loader.load();
            edu.careflow.presentation.controllers.doctor.GenerateInvoiceController controller = loader.getController();
            
            // Set the patient ID directly for the new invoice
            controller.setPatientId(patient.getPatientId());
            
            // Set up success callback to refresh the invoice list
            controller.setOnSuccessCallback(() -> {
                // Refresh the patient list after adding an invoice
                try {
                    allPatients = new PatientDAO().getAllPatients();
                    updatePatientCards(patientSearchField.getText());
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            });

            VBox rightBoxContainer = (VBox) mainContainer.getScene().lookup("#rightBoxContainer");
            if (rightBoxContainer != null) {
                rightBoxContainer.getChildren().clear();
                rightBoxContainer.getChildren().add(invoiceForm);
                FadeTransition fadeIn = new FadeTransition(Duration.millis(200), invoiceForm);
                fadeIn.setFromValue(0.0);
                fadeIn.setToValue(1.0);
                fadeIn.play();
            }
        } catch (IOException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Failed to load invoice form").show();
        }
    }

    private void handleDownload() {
        try {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Patient List");
            fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Excel Files", "*.xlsx")
            );
            fileChooser.setInitialFileName("patients_report.xlsx");
            File file = fileChooser.showSaveDialog(mainContainer.getScene().getWindow());
            if (file != null) {
                exportPatientsToExcel(file);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showError("Error", "Failed to export patients: " + e.getMessage());
        }
    }

    private void exportPatientsToExcel(File file) throws Exception {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Patients");
            String[] columns = {
                "Patient ID", "First Name", "Last Name", "Date of Birth", "Gender", "Contact", "Email", "Address"
            };
            Row headerRow = sheet.createRow(0);
            CellStyle headerStyle = workbook.createCellStyle();
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            for (int i = 0; i < columns.length; i++) {
                org.apache.poi.ss.usermodel.Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, 20 * 256);
            }
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            int rowNum = 1;
            for (Patient patient : filteredPatients) {
                Row row = sheet.createRow(rowNum++);
                int col = 0;
                row.createCell(col++).setCellValue(patient.getPatientId());
                row.createCell(col++).setCellValue(patient.getFirstName());
                row.createCell(col++).setCellValue(patient.getLastName());
                row.createCell(col++).setCellValue(patient.getDateOfBirth() != null ? patient.getDateOfBirth().format(dateFormatter) : "");
                row.createCell(col++).setCellValue(patient.getGender());
                row.createCell(col++).setCellValue(patient.getContact());
                row.createCell(col++).setCellValue(patient.getEmail());
                row.createCell(col++).setCellValue(patient.getAddress());
            }
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }
            try (FileOutputStream fileOut = new FileOutputStream(file)) {
                workbook.write(fileOut);
            }
            showSuccess("Success", "Patients exported successfully!");
        }
    }

    private void showError(String header, String content) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showSuccess(String header, String content) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(javafx.scene.control.Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 