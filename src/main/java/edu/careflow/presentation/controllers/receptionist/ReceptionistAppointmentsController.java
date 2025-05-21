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
import javafx.scene.layout.VBox;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ReceptionistAppointmentsController {
    @FXML private TextField searchField;
    @FXML private Button addAppointmentBtn;
    @FXML private Button pendingAptBtn;
    @FXML private Button scheduleAptBtn;
    @FXML private Button completedAptBtn;
    @FXML private Button cancelledAptBtn;
    @FXML private Button noShowAptBtn;
    @FXML private VBox appointmentsContainer;
    @FXML private Pagination pagination;

    private final AppointmentDAO appointmentDAO = new AppointmentDAO();
    private final PatientDAO patientDAO = new PatientDAO();
    private List<Appointment> allAppointments = new ArrayList<>();
    private List<Appointment> filteredAppointments = new ArrayList<>();
    private static final int CARDS_PER_PAGE = 6;
    private Button currentActiveFilterButton;
    private String currentStatusFilter = "Pending";
    private String currentSearch = "";

    @FXML
    public void initialize() {
        try {
            allAppointments = appointmentDAO.getAllAppointments();
            setupFilterButtons();
            setupSearch();
            // Default filter: Pending
            handleFilterAppointments(pendingAptBtn, "Pending");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        addAppointmentBtn.setOnAction(e -> handleAddAppointment());
    }

    private void setupFilterButtons() {
        pendingAptBtn.setOnAction(e -> handleFilterAppointments(pendingAptBtn, "Pending"));
        scheduleAptBtn.setOnAction(e -> handleFilterAppointments(scheduleAptBtn, "Scheduled"));
        completedAptBtn.setOnAction(e -> handleFilterAppointments(completedAptBtn, "Completed"));
        cancelledAptBtn.setOnAction(e -> handleFilterAppointments(cancelledAptBtn, "Cancelled"));
        noShowAptBtn.setOnAction(e -> handleFilterAppointments(noShowAptBtn, "No-Show"));
    }

    private void setupSearch() {
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            currentSearch = newVal == null ? "" : newVal.trim().toLowerCase();
            filterAndPaginate();
        });
    }

    private void handleFilterAppointments(Button filterBtn, String status) {
        if (currentActiveFilterButton != null) {
            currentActiveFilterButton.getStyleClass().remove("nav-button-active");
        }
        currentActiveFilterButton = filterBtn;
        filterBtn.getStyleClass().add("nav-button-active");
        currentStatusFilter = status;
        filterAndPaginate();
    }

    private void filterAndPaginate() {
        filteredAppointments = allAppointments.stream()
                .filter(a -> currentStatusFilter.equalsIgnoreCase(a.getStatus()))
                .filter(a -> matchesSearch(a))
                .collect(Collectors.toList());
        setupPagination();
    }

    private boolean matchesSearch(Appointment appointment) {
        if (currentSearch.isEmpty()) return true;
        try {
            Patient patient = patientDAO.getPatientById(appointment.getPatientId());
            String patientName = patient != null ? (patient.getFirstName() + " " + patient.getLastName()).toLowerCase() : "";
            return patientName.contains(currentSearch)
                    || ("APT-" + appointment.getAppointmentId()).toLowerCase().contains(currentSearch)
                    || (appointment.getStatus() != null && appointment.getStatus().toLowerCase().contains(currentSearch));
        } catch (Exception e) {
            return false;
        }
    }

    private void setupPagination() {
        appointmentsContainer.getChildren().clear();
        if (filteredAppointments.isEmpty()) {
            Label emptyLabel = new Label("No appointments found.");
            emptyLabel.setStyle("-fx-font-size: 16px; -fx-text-fill: #828282; -fx-font-family: 'Gilroy-SemiBold';");
            appointmentsContainer.getChildren().add(emptyLabel);
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
            appointmentsContainer.getChildren().add(page);
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
                if (editMenuItem != null) editMenuItem.setOnAction(e -> handleEditAppointment(appointment));
                if (downloadMenuItem != null) downloadMenuItem.setOnAction(e -> handleDownloadAppointment(appointment));
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
                    filterAndPaginate();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            });
    
            // Add to overlay container (e.g., a StackPane in your main scene)
            StackPane overlayContainer = (StackPane) appointmentsContainer.getScene().lookup("#stackPaneContainer");
            if (overlayContainer != null) {
                overlayContainer.getChildren().add(form);
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

    private void handleAddAppointment() {
        // TODO: Open appointment form overlay for new appointment
        Alert alert = new Alert(Alert.AlertType.INFORMATION, "Add new appointment (stub)");
        alert.showAndWait();
    }
} 