package edu.careflow.presentation.controllers.doctor;

import edu.careflow.presentation.controllers.doctor.cards.AppointmentCard;
import edu.careflow.repository.dao.AppointmentDAO;
import edu.careflow.repository.entities.Appointment;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class DoctorDashboardController {
    @FXML private Label activeVisitsCount;
    @FXML private VBox appointmentContainer;
    @FXML private Button appointmentLeft;
    @FXML private Button appointmentRight;
    @FXML private HBox buttonsAptFilter;
    @FXML private Button cancelledAptBtn;
    @FXML private Button completedAptBtn;
    @FXML private Button downloadBtn;
    @FXML private VBox mainContainer;
    @FXML private Button noShowAptBtn;
    @FXML private HBox paginationContainer;
    @FXML private VBox patientContainerHome;
    @FXML private Button pendingAptBtn;
    @FXML private Button scheduleAptBtn;
    @FXML private Label scheduleVisitCount;
    @FXML private HBox tableHeader;
    @FXML private Label totalVisitsCount;

    private AppointmentDAO appointmentDAO = new AppointmentDAO();
    private List<Appointment> allAppointments;
    private int currentPage = 0;
    private static final int CARDS_PER_PAGE = 5;
    private Button currentActiveFilterButton;

    public void initializeData(int doctorId) {
        try {
            allAppointments = appointmentDAO.getAppointmentsByDoctorId(doctorId);
            setupFilterButtons();
            updateStatistics();
            // Set pending as initial filter
            handleFilterAppointments(pendingAptBtn, "Pending");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void setupFilterButtons() {
        pendingAptBtn.setOnAction(e -> handleFilterAppointments(pendingAptBtn, "Pending"));
        completedAptBtn.setOnAction(e -> handleFilterAppointments(completedAptBtn, "Completed"));
        cancelledAptBtn.setOnAction(e -> handleFilterAppointments(cancelledAptBtn, "Cancelled"));
        noShowAptBtn.setOnAction(e -> handleFilterAppointments(noShowAptBtn, "No-Show"));
        scheduleAptBtn.setOnAction(e -> handleFilterAppointments(scheduleAptBtn, "Scheduled"));

        appointmentLeft.setOnAction(e -> navigatePage(-1));
        appointmentRight.setOnAction(e -> navigatePage(1));
    }


    private void handleFilterAppointments(Button clickedButton, String status) {
        if (currentActiveFilterButton != null) {
            currentActiveFilterButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #757575;");
        }
        clickedButton.setStyle("-fx-background-color: #0078D4; -fx-text-fill: white;");
        currentActiveFilterButton = clickedButton;

        // Debug: Check if allAppointments is populated
        if (allAppointments == null || allAppointments.isEmpty()) {
            System.out.println("No appointments available to filter.");
            return;
        }

        // Filter appointments by status
        List<Appointment> filteredAppointments = allAppointments.stream()
                .filter(apt -> apt.getStatus().equalsIgnoreCase(status)) // Use equalsIgnoreCase for case-insensitive comparison
                .collect(Collectors.toList());

        // Debug: Log filtered appointments
        System.out.println("Filtered Appointments for status " + status + ": " + filteredAppointments.size());

        currentPage = 0;
        displayAppointments(filteredAppointments);
        updatePaginationButtons(filteredAppointments.size());
    }

    private void displayAppointments(List<Appointment> appointments) {
        mainContainer.getChildren().clear();

        if (appointments.isEmpty()) {
            showEmptyState();
            return;
        }

        tableHeader.setVisible(true);
        mainContainer.setStyle("");

        int start = currentPage * CARDS_PER_PAGE;
        int end = Math.min(start + CARDS_PER_PAGE, appointments.size());

        for (int i = start; i < end; i++) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/edu/careflow/fxml/components/doctor/appointmentCard.fxml"));
                Parent cardContent = loader.load();
                AppointmentCard controller = loader.getController();
                controller.initializeData(appointments.get(i));
                mainContainer.getChildren().add(cardContent);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void showEmptyState() {
        try {
            URL fxmlResource = getClass().getResource("/edu/careflow/fxml/components/states/emptyRecordstCard.fxml");
            Parent cardContent = FXMLLoader.load(fxmlResource);
            Label emptyLabel = (Label) cardContent.lookup("#pageTitle");
            emptyLabel.setText("This part of appointment is empty");
            mainContainer.setStyle("-fx-alignment: center;");
            mainContainer.getChildren().add(cardContent);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void navigatePage(int direction) {
        String currentStatus = getCurrentFilterStatus();
        List<Appointment> filteredAppointments = allAppointments.stream()
                .filter(apt -> apt.getStatus().equals(currentStatus))
                .collect(Collectors.toList());

        int maxPages = (int) Math.ceil(filteredAppointments.size() / (double) CARDS_PER_PAGE);
        currentPage = Math.max(0, Math.min(currentPage + direction, maxPages - 1));

        displayAppointments(filteredAppointments);
        updatePaginationButtons(filteredAppointments.size());
    }

    private String getCurrentFilterStatus() {
        if (currentActiveFilterButton == scheduleAptBtn) return "Scheduled";
        if (currentActiveFilterButton == completedAptBtn) return "Completed";
        if (currentActiveFilterButton == cancelledAptBtn) return "Cancelled";
        if (currentActiveFilterButton == noShowAptBtn) return "No-Show";
        return "Pending";
    }

    private void updatePaginationButtons(int totalItems) {
        int maxPages = (int) Math.ceil(totalItems / (double) CARDS_PER_PAGE);
        appointmentLeft.setDisable(currentPage == 0);
        appointmentRight.setDisable(currentPage >= maxPages - 1);
    }

    private void updateStatistics() {
        int activeVisits = (int) allAppointments.stream()
                .filter(apt -> apt.getStatus().equals("Scheduled")).count();
        int scheduledVisits = (int) allAppointments.stream()
                .filter(apt -> apt.getStatus().equals("Pending")).count();

        activeVisitsCount.setText(String.valueOf(activeVisits));
        scheduleVisitCount.setText(String.valueOf(scheduledVisits));
        totalVisitsCount.setText(String.valueOf(allAppointments.size()));
    }
}