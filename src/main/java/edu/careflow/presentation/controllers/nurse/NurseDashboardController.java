package edu.careflow.presentation.controllers.nurse;

import edu.careflow.repository.dao.AppointmentDAO;
import edu.careflow.repository.dao.PatientDAO;
import edu.careflow.repository.entities.Appointment;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Pagination;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class NurseDashboardController {
    @FXML private Label activePatientsCount;
    @FXML private VBox scheduledTodayContainer;
    @FXML private Label scheduledTodayCount;
    @FXML private Label totalVisitsCount;
    @FXML private Button cancelledAptBtn;
    @FXML private Button completedAptBtn;
    @FXML private Button downloadBtn;
    @FXML private Button noShowAptBtn;
    @FXML private Button pendingAptBtn;
    @FXML private Button scheduleAptBtn;

    @FXML
    private Pagination paginationContainer;


    private AppointmentDAO appointmentDAO = new AppointmentDAO();
    private PatientDAO patientDAO = new PatientDAO();
    private List<Appointment> allAppointments;
    private int currentPage = 0;
    private static final int CARDS_PER_PAGE = 5;
    private Button currentActiveFilterButton;
    private int currentNurseId;
    private List<Appointment> filteredAppointments;

    public void initializeData(int nurseId) {
        this.currentNurseId = nurseId;
        try {
            allAppointments = appointmentDAO.getAllAppointments();
            setupFilterButtons();
            updateStatistics();
            // Set scheduled as initial filter since we're showing scheduled appointments
            handleFilterAppointments(scheduleAptBtn, "Scheduled");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void setupFilterButtons() {
        pendingAptBtn.setOnAction(e -> handleFilterAppointments(pendingAptBtn, "Pending"));
        scheduleAptBtn.setOnAction(e -> handleFilterAppointments(scheduleAptBtn, "Scheduled"));
        completedAptBtn.setOnAction(e -> handleFilterAppointments(completedAptBtn, "Completed"));
        cancelledAptBtn.setOnAction(e -> handleFilterAppointments(cancelledAptBtn, "Cancelled"));
        noShowAptBtn.setOnAction(e -> handleFilterAppointments(noShowAptBtn, "No-Show"));

        downloadBtn.setOnAction(e -> handleDownload());
    }

    private void handleFilterAppointments(Button button, String status) {
        if (currentActiveFilterButton != null) {
            currentActiveFilterButton.getStyleClass().remove("nav-button-active");
            currentActiveFilterButton.getStyleClass().add("nav-button");
        }
        currentActiveFilterButton = button;
        button.getStyleClass().remove("nav-button");
        button.getStyleClass().add("nav-button-active");

        currentPage = 0;
        filteredAppointments = allAppointments.stream()
            .filter(appointment -> status.equals(appointment.getStatus()))
            .collect(Collectors.toList());

        int pageCount = (int) Math.ceil((double) filteredAppointments.size() / CARDS_PER_PAGE);
        paginationContainer.setPageCount(Math.max(pageCount, 1));
        paginationContainer.setCurrentPageIndex(0);
        paginationContainer.setPageFactory(this::createPage);

        updateStatistics();
    }

    private VBox createPage(int pageIndex) {
        scheduledTodayContainer.getChildren().clear();
        int startIndex = pageIndex * CARDS_PER_PAGE;
        int endIndex = Math.min(startIndex + CARDS_PER_PAGE, filteredAppointments.size());
        for (int i = startIndex; i < endIndex; i++) {
            try {
                Appointment appointment = filteredAppointments.get(i);
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/edu/careflow/fxml/nurse/nursePatientCard.fxml"));
                Parent card = loader.load();
                NursePatientCardController controller = loader.getController();
                controller.initializeData(appointment);
                scheduledTodayContainer.getChildren().add(card);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        // Return an empty node since we don't want Pagination to display anything itself
        return new VBox();
    }

    private void updateStatistics() {
        LocalDate today = LocalDate.now();
        List<Appointment> todayAppointments = allAppointments.stream()
            .filter(appointment -> appointment.getAppointmentDate().toLocalDate().equals(today))
            .collect(Collectors.toList());

        long totalVisits = todayAppointments.size();
        long activePatients = todayAppointments.stream()
            .filter(appointment -> "Active".equals(appointment.getStatus()))
            .count();
        long scheduledToday = todayAppointments.stream()
            .filter(appointment -> "Scheduled".equals(appointment.getStatus()))
            .count();

        totalVisitsCount.setText(String.valueOf(totalVisits));
        activePatientsCount.setText(String.valueOf(activePatients));
        scheduledTodayCount.setText(String.valueOf(scheduledToday));
    }

    private void handleDownload() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Appointments Report");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Excel Files", "*.xlsx")
        );
        fileChooser.setInitialFileName("appointments_report.xlsx");

        File file = fileChooser.showSaveDialog(downloadBtn.getScene().getWindow());
        if (file != null) {
            try {
                // Create a simple text file instead of Excel for now
                try (FileOutputStream fileOut = new FileOutputStream(file)) {
                    StringBuilder content = new StringBuilder();
                    content.append("Patient Name,Date,Time,Status,Service Type,Notes\n");
                    
                    for (Appointment appointment : allAppointments) {
                        try {
                            String patientName = patientDAO.getPatientById(appointment.getPatientId()).getFirstName() + " " +
                                patientDAO.getPatientById(appointment.getPatientId()).getLastName();
                            content.append(String.format("%s,%s,%s,%s,%s,%s\n",
                                patientName,
                                appointment.getAppointmentDate().toLocalDate(),
                                appointment.getAppointmentDate().toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")),
                                appointment.getStatus(),
                                appointment.getServiceType(),
                                appointment.getNotes()));
                        } catch (SQLException e) {
                            content.append(String.format("Unknown Patient,%s,%s,%s,%s,%s\n",
                                appointment.getAppointmentDate().toLocalDate(),
                                appointment.getAppointmentDate().toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")),
                                appointment.getStatus(),
                                appointment.getServiceType(),
                                appointment.getNotes()));
                        }
                    }
                    fileOut.write(content.toString().getBytes());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void refreshAppointmentsAndStats() {
        try {
            allAppointments = appointmentDAO.getAllAppointments();
            updateStatistics();
            // Optionally, re-apply the current filter
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
} 