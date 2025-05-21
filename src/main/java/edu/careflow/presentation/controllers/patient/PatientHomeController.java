package edu.careflow.presentation.controllers.patient;

import edu.careflow.presentation.controllers.patient.cards.*;
import edu.careflow.repository.dao.*;
import edu.careflow.repository.entities.Appointment;
import edu.careflow.repository.entities.Condition;
import edu.careflow.repository.entities.Prescription;
import edu.careflow.repository.entities.PrescriptionDetails;
import edu.careflow.repository.entities.Vitals;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.Pagination;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class PatientHomeController {


    @FXML
    private VBox activeMediationContainer;

    @FXML
    private Pagination activeMedicationPagination;

    @FXML
    private VBox appointmentContainer;

    @FXML
    private VBox appointmentMainContainer;

    @FXML
    private HBox buttonsAptFilter;

    @FXML
    private Button cancelledAptBtn;

    @FXML
    private VBox conditionsContainer;

    @FXML
    private Pagination conditionsPagination;

    @FXML
    private Label latestVitalDate;

    @FXML
    private VBox medicationContainer;

    @FXML
    private Pagination paginationUpcomingApt;

    @FXML
    private VBox patientContainerHome;

    @FXML
    private Button pendingBtnApt;

    @FXML
    private Button scheduledAptBtn;

    @FXML
    private VBox secondContainer;

    @FXML
    private Button tableViewBtn;

    @FXML
    private Button vitalChartBtn;

    @FXML
    private VBox vitalsCard;

    @FXML
    private VBox vitalsCardContainer;

    @FXML
    private Hyperlink vitalsHistoryBtn;


    private Label pageLabel = new Label();

    private final VitalsDAO vitalsDAO = new VitalsDAO();
    private final PatientDAO patientDAO = new PatientDAO();
    private final AppointmentDAO appointmentDAO = new AppointmentDAO();
    private final PrescriptionDAO prescriptionDAO = new PrescriptionDAO();
    private final ConditionDAO conditionDAO = new ConditionDAO();

    private int currentPatientId;
    private Vitals latestVitals;
    private List<Appointment> patientAppointments;
    private Button currentActiveFilterButton;
    private static final int CARDS_PER_PAGE = 4;





    @FXML
    private void initialize() {
        tableViewBtn.setOnAction(e -> switchToTableView());
        vitalChartBtn.setOnAction(e -> switchToChartView());

        tableViewBtn.getStyleClass().addAll("view-button", "active");
        vitalChartBtn.getStyleClass().add("view-button");

        appointmentContainer.prefHeightProperty().bind(secondContainer.heightProperty());

        // Setup filter buttons
        setupFilterButtons();
    }

    private void setupFilterButtons() {
        scheduledAptBtn.setOnAction(e -> handleFilterAppointments(scheduledAptBtn, "Scheduled"));
        pendingBtnApt.setOnAction(e -> handleFilterAppointments(pendingBtnApt, "Pending"));
        cancelledAptBtn.setOnAction(e -> handleFilterAppointments(cancelledAptBtn, "Cancelled"));
    }

    public void initializeData(int patientId) {
        this.currentPatientId = patientId;
        try {
            patientAppointments = appointmentDAO.getAppointmentsByPatientId(patientId);
            latestVitals = vitalsDAO.getLatestVitals(patientId);
            handleFilterAppointments(pendingBtnApt, "Pending"); // Set initial filter
            loadVitalsCard(patientId, latestVitals);
            loadConditionsCard(patientId);
            loadActiveMedicationsCard(patientId);
        } catch (SQLException e) {
            handleDatabaseError(e);
        }
    }

    private void handleFilterAppointments(Button clickedButton, String status) {
        updateFilterButtonStyle(clickedButton);

        List<Appointment> filteredAppointments = patientAppointments.stream()
                .filter(apt -> apt.getStatus().equalsIgnoreCase(status))
                .collect(Collectors.toList());

        setupPagination(filteredAppointments);
    }

    private void updateFilterButtonStyle(Button clickedButton) {
        if (currentActiveFilterButton != null) {
            currentActiveFilterButton.setStyle("-fx-background-color: transparent; -fx-text-fill: #757575;");
        }
        clickedButton.setStyle("-fx-background-color: #0078D4; -fx-text-fill: white;");
        currentActiveFilterButton = clickedButton;
    }

    private void setupPagination(List<Appointment> appointments) {
        if (appointments == null || appointments.isEmpty()) {
            showEmptyState();
            return;
        }

        int totalPages = (int) Math.ceil((double) appointments.size() / CARDS_PER_PAGE);
        paginationUpcomingApt.setPageCount(totalPages);
        paginationUpcomingApt.setCurrentPageIndex(0);

        VBox initialPage = createPage(0, appointments);
        appointmentMainContainer.getChildren().clear();
        appointmentMainContainer.getChildren().add(initialPage);

        paginationUpcomingApt.setPageFactory(pageIndex -> {
            VBox page = createPage(pageIndex, appointments);
            appointmentMainContainer.getChildren().clear();
            appointmentMainContainer.getChildren().add(page);
            return new VBox(); // Return empty VBox for pagination
        });
        paginationUpcomingApt.setVisible(true);
    }

    private VBox createPage(int pageIndex, List<Appointment> appointments) {
        VBox page = new VBox(10);
        page.setStyle("-fx-padding: 10 0 10 0;");

        int start = pageIndex * CARDS_PER_PAGE;
        int end = Math.min(start + CARDS_PER_PAGE, appointments.size());

        for (int i = start; i < end; i++) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/edu/careflow/fxml/components/patient/appointmentCard.fxml"));
                Parent cardContent = loader.load();
                AppointmentCardController controller = loader.getController();
                controller.initializeData(appointments.get(i));
                page.getChildren().add(cardContent);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        addFadeTransition(page);
        return page;
    }

    private void showEmptyState() {
        try {
            URL fxmlResource = getClass().getResource("/edu/careflow/fxml/components/states/emptyAppointmentCard.fxml");
            Parent cardContent = FXMLLoader.load(fxmlResource);
            appointmentMainContainer.getChildren().clear();
            appointmentMainContainer.getChildren().add(cardContent);
            paginationUpcomingApt.setVisible(false);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addFadeTransition(VBox page) {
        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), page);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        fadeIn.play();
    }

    private void switchToTableView() {
        vitalsCardContainer.getChildren().clear();

        loadVitalsCard(currentPatientId, latestVitals);
        // Update button styles
        vitalChartBtn.getStyleClass().remove("active");
        tableViewBtn.getStyleClass().add("active");

        FadeTransition fadeIn = new FadeTransition(Duration.millis(500), vitalsCardContainer);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();
    }
    
    private void switchToChartView() {

        try {
            List<Vitals> vitalsList = vitalsDAO.getVitalsByPatientId(currentPatientId);
            
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/edu/careflow/fxml/chart/vitalChart.fxml"));
            Parent chartCard = loader.load();
            VitalsChartCardController controller = loader.getController();
            controller.setupVitalsChart(vitalsList);

            vitalsCardContainer.getChildren().clear();
            vitalsCardContainer.getChildren().add(chartCard);
            // Update button styles
            tableViewBtn.getStyleClass().remove("active");
            vitalChartBtn.getStyleClass().add("active");


            FadeTransition fadeIn = new FadeTransition(Duration.millis(500), vitalsCardContainer);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            fadeIn.play();
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }



    // Modified loadAppointmentCard method



    private void loadConditionsCard(int patientId) {
        try {
            List<Condition> conditions = ConditionDAO.getConditionsByPatientId(patientId);

            if (conditions.isEmpty()) {
                showEmptyState(conditionsContainer, "No Conditions Found");
                conditionsPagination.setVisible(false);
                conditionsContainer.setStyle("-fx-alignment: center;");
                return;
            }

            int totalPages = (int) Math.ceil((double) conditions.size() / CARDS_PER_PAGE);
            conditionsPagination.setPageCount(totalPages);
            conditionsPagination.setCurrentPageIndex(0);

            // Create and show initial page
            VBox initialPage = createConditionsPage(0, conditions);
            conditionsContainer.getChildren().clear();
            conditionsContainer.getChildren().add(initialPage);

            // Set up pagination
            conditionsPagination.setPageFactory(pageIndex -> {
                VBox page = createConditionsPage(pageIndex, conditions);
                conditionsContainer.getChildren().clear();
                conditionsContainer.getChildren().add(page);
                return new VBox();
            });
            conditionsPagination.setVisible(true);
        } catch (SQLException e) {
            handleDatabaseError(e);
        }
    }

    private VBox createConditionsPage(int pageIndex, List<Condition> conditions) {
        VBox page = new VBox(10);
        page.setStyle("-fx-padding: 10 0 10 0;");

        int start = pageIndex * CARDS_PER_PAGE;
        int end = Math.min(start + CARDS_PER_PAGE, conditions.size());

        for (int i = start; i < end; i++) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/edu/careflow/fxml/components/patient/conditionsCard.fxml"));
                Parent cardContent = loader.load();
                ConditionCardController controller = loader.getController();
                controller.initializeData(conditions.get(i));
                page.getChildren().add(cardContent);
            } catch (IOException e) {
                handleFxmlLoadingError(e);
            }
        }

        addFadeTransition(page);
        return page;
    }

    private void loadActiveMedicationsCard(int patientId) {
        try {
            List<Prescription> activeMedications = prescriptionDAO.getActivePrescriptions(patientId);

            if (activeMedications.isEmpty()) {
                showEmptyState(activeMediationContainer, "No Active Medications Found");
                activeMedicationPagination.setVisible(false);
                activeMediationContainer.setStyle("-fx-alignment: center;");
                return;
            }

            int totalPages = (int) Math.ceil((double) activeMedications.size() / CARDS_PER_PAGE);
            activeMedicationPagination.setPageCount(totalPages);
            activeMedicationPagination.setCurrentPageIndex(0);

            // Create and show initial page
            VBox initialPage = createActiveMedicationsPage(0, activeMedications);
            activeMediationContainer.getChildren().clear();
            activeMediationContainer.getChildren().add(initialPage);

            // Set up pagination
            activeMedicationPagination.setPageFactory(pageIndex -> {
                VBox page = createActiveMedicationsPage(pageIndex, activeMedications);
                activeMediationContainer.getChildren().clear();
                activeMediationContainer.getChildren().add(page);
                return new VBox();
            });
            activeMedicationPagination.setVisible(true);
        } catch (SQLException e) {
            handleDatabaseError(e);
        }
    }

    private VBox createActiveMedicationsPage(int pageIndex, List<Prescription> medications) {
        VBox page = new VBox(10);
        page.setStyle("-fx-padding: 10 0 10 0;");

        int start = pageIndex * CARDS_PER_PAGE;
        int end = Math.min(start + CARDS_PER_PAGE, medications.size());

        for (int i = start; i < end; i++) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/edu/careflow/fxml/components/patient/prescriptionCard.fxml"));
                Parent cardContent = loader.load();
                PrescriptionCardController controller = loader.getController();
                Prescription prescription = medications.get(i);
                try {
                    PrescriptionDetails details = prescriptionDAO.getPrescriptionDetails(prescription.getPrescriptionId());
                    controller.initializeData(prescription, details);
                } catch (SQLException e) {
                    controller.initializeData(prescription, null);
                }
                page.getChildren().add(cardContent);
            } catch (IOException e) {
                handleFxmlLoadingError(e);
            }
        }

        addFadeTransition(page);
        return page;
    }

    private void showEmptyState(VBox container, String message) {
        try {
            URL fxmlResource = getClass().getResource("/edu/careflow/fxml/components/states/emptyRecordstCard.fxml");
            Parent cardContent = FXMLLoader.load(fxmlResource);
            Label emptyLabel = (Label) cardContent.lookup("#pageTitle");
            emptyLabel.setText(message);
            
            container.getChildren().clear();
            container.getChildren().add(cardContent);
        } catch (IOException e) {
            handleFxmlLoadingError(e);
        }
    }

    private void loadPrescriptionsCard(int patientId) {
        try {
            List<Prescription> prescriptions = prescriptionDAO.getPrescriptionsByPatientId(patientId);
            if (prescriptions.isEmpty()) {
                URL fxmlResource = getClass().getResource("/edu/careflow/fxml/components/states/emptyRecordstCard.fxml");
                Parent cardContent = FXMLLoader.load(fxmlResource);

                Label emptyLabel = (Label) cardContent.lookup("#pageTitle");
                emptyLabel.setText("No Prescriptions Found");
                medicationContainer.getChildren().add(cardContent);
                return;
            }

            for (Prescription prescription : prescriptions) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/edu/careflow/fxml/components/patient/prescriptionCard.fxml"));
                Parent cardContent = loader.load();
                PrescriptionCardController controller = loader.getController();
                try {
                    PrescriptionDetails details = prescriptionDAO.getPrescriptionDetails(prescription.getPrescriptionId());
                    controller.initializeData(prescription, details);
                } catch (SQLException e) {
                    controller.initializeData(prescription, null);
                }
                medicationContainer.getChildren().add(cardContent);
            }

        } catch(SQLException | IOException e) {
            e.printStackTrace();
        }
    }

    private void loadVitalsCard(int patientId, Vitals recentVitals) {
        try {

            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("hh:mm a");
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("EEEE, hh:mm a");

            if (recentVitals != null && recentVitals.getRecordedAt().toLocalDate().equals(java.time.LocalDate.now())) {
                latestVitalDate.setText("Today at " + recentVitals.getRecordedAt().format(timeFormatter));
            } else {
                latestVitalDate.setText(recentVitals != null ? recentVitals.getRecordedAt().format(dateFormatter) : "No recent vitals");
            }

            if (recentVitals != null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/edu/careflow/fxml/components/patient/vitalsCard.fxml"));
                Parent cardContent = loader.load();
                VitalsCardsController controller = loader.getController();
                controller.initializeData(patientId);
                vitalsCardContainer.getChildren().add(cardContent);
            } else {
                URL fxmlResource = getClass().getResource("/edu/careflow/fxml/components/states/emptyVitalCard.fxml");
                Parent cardContent = FXMLLoader.load(fxmlResource);
                vitalsCardContainer.getChildren().add(cardContent);
            }

        } catch (IOException e) {
            handleFxmlLoadingError(e);
        }
    }

    private URL getFxmlResource(Vitals recentVitals) {
        if (recentVitals != null) {
            return getClass().getResource("/edu/careflow/fxml/components/patient/vitalsCard.fxml");
        }
        return getClass().getResource("/edu/careflow/fxml/components/states/emptyVitalCard.fxml");
    }

    private void handleDatabaseError(SQLException e) {
        System.err.println("Database error: " + e.getMessage());
        e.printStackTrace();
    }

    private void handleFxmlLoadingError(IOException e) {
        System.err.println("FXML loading error: " + e.getMessage());
        e.printStackTrace();
    }
}
