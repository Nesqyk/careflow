package edu.careflow.presentation.controllers.receptionist;

import edu.careflow.repository.dao.AppointmentDAO;
import edu.careflow.repository.dao.PatientDAO;
import edu.careflow.repository.entities.Appointment;
import edu.careflow.repository.entities.Patient;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import javafx.stage.FileChooser;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.FileOutputStream;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.io.File;

public class ReceptionistDashboardController {
    @FXML private Label activeVisitsCount;
    @FXML private Label totalVisitsCount;
    @FXML private VBox appointmentContainer;
    @FXML private HBox tableHeader;
    @FXML private HBox buttonsAptFilter;
    @FXML private Button pendingAptBtn;
    @FXML private Button scheduleAptBtn;
    @FXML private Button completedAptBtn;
    @FXML private Button cancelledAptBtn;
    @FXML private Button noShowAptBtn;
    @FXML private Button downloadBtn;
    @FXML private VBox mainContainer;
    @FXML private Pagination pagination;

    private final AppointmentDAO appointmentDAO = new AppointmentDAO();
    private final PatientDAO patientDAO = new PatientDAO();
    private List<Appointment> allAppointments = new ArrayList<>();
    private List<Appointment> filteredAppointments = new ArrayList<>();
    private static final int CARDS_PER_PAGE = 5;
    private Button currentActiveFilterButton;

    @FXML
    public void initialize() {
        try {
            allAppointments = appointmentDAO.getAllAppointments();
            setupFilterButtons();
            updateMetrics();
            // Default filter: Pending
            handleFilterAppointments(pendingAptBtn, "Pending");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        downloadBtn.setOnAction(e -> handleDownload());
    }

    private void setupFilterButtons() {
        pendingAptBtn.setOnAction(e -> handleFilterAppointments(pendingAptBtn, "Pending"));
        scheduleAptBtn.setOnAction(e -> handleFilterAppointments(scheduleAptBtn, "Scheduled"));
        completedAptBtn.setOnAction(e -> handleFilterAppointments(completedAptBtn, "Completed"));
        cancelledAptBtn.setOnAction(e -> handleFilterAppointments(cancelledAptBtn, "Cancelled"));
        noShowAptBtn.setOnAction(e -> handleFilterAppointments(noShowAptBtn, "No-Show"));
    }

    private void updateMetrics() {
        // Pending = appointments with status 'Pending'
        long pendingCount = allAppointments.stream().filter(a -> "Pending".equalsIgnoreCase(a.getStatus())).count();
        activeVisitsCount.setText(String.valueOf(pendingCount));
        // Total visits today = appointments with today's date
        LocalDate today = LocalDate.now();
        long todayCount = allAppointments.stream().filter(a -> {
            LocalDateTime dt = a.getAppointmentDate();
            return dt != null && dt.toLocalDate().isEqual(today);
        }).count();
        totalVisitsCount.setText(String.valueOf(todayCount));
    }

    private void handleFilterAppointments(Button filterBtn, String status) {
        if (currentActiveFilterButton != null) {
            currentActiveFilterButton.getStyleClass().remove("nav-button-active");
        }
        currentActiveFilterButton = filterBtn;
        filterBtn.getStyleClass().add("nav-button-active");
        filteredAppointments = allAppointments.stream()
                .filter(a -> status.equalsIgnoreCase(a.getStatus()))
                .collect(Collectors.toList());
        setupPagination();
    }

    private void setupPagination() {
        mainContainer.getChildren().clear();
        if (filteredAppointments.isEmpty()) {
            Label emptyLabel = new Label("No appointments found for this filter.");
            emptyLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #828282; -fx-font-family: 'Gilroy-SemiBold';");
            mainContainer.getChildren().add(emptyLabel);
            if (pagination != null) pagination.setVisible(false);
            return;
        }
        int totalPages = (int) Math.ceil((double) filteredAppointments.size() / CARDS_PER_PAGE);
        if (pagination != null) {
            pagination.setPageCount(totalPages);
            pagination.setCurrentPageIndex(0);
            pagination.setPageFactory(this::createPage);
            pagination.setVisible(totalPages > 1);
        } else {
            VBox page = createPage(0);
            mainContainer.getChildren().add(page);
        }
    }

    private VBox createPage(int pageIndex) {
        VBox page = new VBox(12);
        int start = pageIndex * CARDS_PER_PAGE;
        int end = Math.min(start + CARDS_PER_PAGE, filteredAppointments.size());
        for (int i = start; i < end; i++) {
            Appointment appointment = filteredAppointments.get(i);
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/edu/careflow/fxml/components/receptionist/appointmentCardReceptionist.fxml"));
                Parent card = loader.load();
                // Populate card fields
                Label appointmentIdLabel = (Label) card.lookup("#appointmentIdLabel");
                Label patientNameLabel = (Label) card.lookup("#patientNameLabel");
                Label dateLabel = (Label) card.lookup("#dateLabel");
                Label timeLabel = (Label) card.lookup("#timeLabel");
                Label statusLabel = (Label) card.lookup("#statusLabel");
                MenuButton actionMenu = (MenuButton) card.lookup("#actionMenu");
                MenuItem editMenuItem = null;
                MenuItem downloadMenuItem = null;
                for (MenuItem item : actionMenu.getItems()) {
                    if ("editMenuItem".equals(item.getId())) editMenuItem = item;
                    if ("downloadMenuItem".equals(item.getId())) downloadMenuItem = item;
                }

                appointmentIdLabel.setText("APT-" + appointment.getAppointmentId());
                // Fetch patient name
                String patientName = "Patient";
                try {
                    Patient patient = patientDAO.getPatientById(appointment.getPatientId());
                    if (patient != null) {
                        patientName = patient.getFirstName() + " " + patient.getLastName();
                    }
                } catch (Exception ignored) {}
                patientNameLabel.setText(patientName);
                // Date and time
                LocalDateTime dt = appointment.getAppointmentDate();
                if (dt != null) {
                    dateLabel.setText(dt.toLocalDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
                    timeLabel.setText(dt.toLocalTime().format(DateTimeFormatter.ofPattern("hh:mm a")));
                } else {
                    dateLabel.setText("-");
                    timeLabel.setText("-");
                }
                statusLabel.setText(appointment.getStatus().toUpperCase());
                // Action handlers
                editMenuItem.setOnAction(e -> handleEditAppointment(appointment));
                downloadMenuItem.setOnAction(e -> handleDownloadAppointment(appointment));
                page.getChildren().add(card);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), page);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.play();
        return page;
    }

    private void handleEditAppointment(Appointment appointment) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/edu/careflow/fxml/components/patient/appointmentForm.fxml"));
            Parent form = loader.load();
            edu.careflow.presentation.controllers.patient.forms.AppointmentForm formController = loader.getController();
            formController.setAppointment(appointment);
            formController.setOnSaveCallback(() -> {
                try {
                    allAppointments = appointmentDAO.getAllAppointments();
                    updateMetrics();
                    setupPagination();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            });
            // Add to overlay container (e.g., a StackPane in your main scene)

            VBox rightBoxContainer = (VBox) mainContainer.getScene().lookup("#rightBoxContainer");
            if (rightBoxContainer != null) {
                rightBoxContainer.getChildren().add(form);
                FadeTransition fadeIn = new FadeTransition(Duration.millis(200), form);
                fadeIn.setFromValue(0.0);
                fadeIn.setToValue(1.0);
                fadeIn.play();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to load appointment form");
            alert.showAndWait();
        }
    }

    private void handleDownloadAppointment(Appointment appointment) {
        // TODO: Implement download logic (PDF, CSV, etc.)
        Alert alert = new Alert(Alert.AlertType.INFORMATION, "Download details for appointment: " + appointment.getAppointmentId());
        alert.showAndWait();
    }

    private void handleDownload() {
        try {
            // Create a file chooser
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Excel File");
            fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Excel Files", "*.xlsx")
            );
            fileChooser.setInitialFileName("appointments_report.xlsx");

            // Show save dialog
            File file = fileChooser.showSaveDialog(downloadBtn.getScene().getWindow());
            if (file != null) {
                exportToExcel(file);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showError("Error", "Failed to export appointments: " + e.getMessage());
        }
    }

    private void exportToExcel(File file) throws IOException {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Appointments");

            // Create header row
            Row headerRow = sheet.createRow(0);
            String[] columns = {
                "Appointment ID", "Patient ID", "Doctor ID", "Nurse ID", "Appointment Date", "Status", "Notes", "Created At", "Room", "Symptoms", "Diagnosis", "Service Type", "Appointment Type", "Meeting Link", "Booked By", "Preferred Contact", "Booking Time"
            };
            // Create header style
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
            // Export all filtered appointments
            List<Appointment> appointmentsToExport = filteredAppointments;
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            int rowNum = 1;
            for (Appointment appointment : appointmentsToExport) {
                Row row = sheet.createRow(rowNum++);
                int col = 0;
                row.createCell(col++).setCellValue(appointment.getAppointmentId());
                row.createCell(col++).setCellValue(appointment.getPatientId());
                row.createCell(col++).setCellValue(appointment.getDoctorId());
                row.createCell(col++).setCellValue(appointment.getNurseId());
                row.createCell(col++).setCellValue(appointment.getAppointmentDate() != null ? appointment.getAppointmentDate().format(dateTimeFormatter) : "");
                row.createCell(col++).setCellValue(appointment.getStatus());
                row.createCell(col++).setCellValue(appointment.getNotes());
                row.createCell(col++).setCellValue(appointment.getCreatedAt() != null ? appointment.getCreatedAt().format(dateTimeFormatter) : "");
                row.createCell(col++).setCellValue(appointment.getRoom());
                row.createCell(col++).setCellValue(appointment.getSymptoms());
                row.createCell(col++).setCellValue(appointment.getDiagnosis());
                row.createCell(col++).setCellValue(appointment.getServiceType());
                row.createCell(col++).setCellValue(appointment.getAppointmentType());
                row.createCell(col++).setCellValue(appointment.getMeetingLink());
                row.createCell(col++).setCellValue(appointment.getBookedBy());
                row.createCell(col++).setCellValue(appointment.getPreferredContact());
                row.createCell(col++).setCellValue(appointment.getBookingTime() != null ? appointment.getBookingTime().format(dateTimeFormatter) : "");
            }
            // Auto-size columns
            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }
            // Write to file
            try (FileOutputStream fileOut = new FileOutputStream(file)) {
                workbook.write(fileOut);
            }
            showSuccess("Success", "Appointments exported successfully!");
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