package edu.careflow.presentation.controllers.patient;

import edu.careflow.presentation.controllers.patient.cards.*;
import edu.careflow.repository.dao.*;
import edu.careflow.repository.entities.Appointment;
import edu.careflow.repository.entities.Condition;
import edu.careflow.repository.entities.Prescription;
import edu.careflow.repository.entities.Vitals;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PatientHomeController {

    @FXML
    private VBox appointmentContainer;

    @FXML
    private Button appointmentLeft;

    @FXML
    private VBox appointmentMainContainer;

    @FXML
    private Button appointmentRight;

    @FXML
    private HBox buttonsAptFilter;

    @FXML
    private VBox conditionsContainer;

    @FXML
    private Label latestVitalDate;

    @FXML
    private VBox medicationContainer;

    @FXML
    private HBox paginationContainer;

    @FXML
    private Button pastButtonApt;

    @FXML
    private VBox patientContainerHome;

    @FXML
    private VBox secondContainer;

    @FXML
    private Button tableViewBtn;

    @FXML
    private Button todayButtonApt;

    @FXML
    private Button upcomingButtonApt;

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

    private int currentPage = 1;
    private int cardsPerPage = 6;

    @FXML
    private void initialize() {
        tableViewBtn.setOnAction(e -> switchToTableView());
        vitalChartBtn.setOnAction(e -> switchToChartView());

        tableViewBtn.getStyleClass().addAll("view-button", "active");
        vitalChartBtn.getStyleClass().add("view-button");

        appointmentLeft.setOnAction(e -> goToPreviousPage());
        appointmentRight.setOnAction(e -> goToNextPage());

        // Add the page label to the pagination container
        pageLabel.getStyleClass().add("page-label");
        paginationContainer.getChildren().add(pageLabel);

        appointmentContainer.prefHeightProperty().bind(secondContainer.heightProperty());

        try {
            List<Appointment> appointments = appointmentDAO.getAppointmentsByPatientId(currentPatientId);
            int totalPages = (int) Math.ceil((double) appointments.size() / cardsPerPage);
            updatePaginationButtons(totalPages);
        } catch (SQLException e) {
            e.printStackTrace();
        }
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
            controller.initializeData(vitalsList);

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

    public void initializeData(int patientId) {
        this.currentPatientId = patientId;
        try {
            this.latestVitals = vitalsDAO.getLatestVitals(patientId);
            loadVitalsCard(patientId, latestVitals);
            loadAppointmentCard(patientId, null); // Default load without sorting
            loadConditionsCard(patientId);
            loadPrescriptionsCard(patientId);
        } catch (SQLException e) {
            handleDatabaseError(e);
        }
    }

    // Add sorting method
    public void sortAppointmentsByStatus(int patientId) {
        try {
            List<Appointment> appointments = appointmentDAO.getAppointmentsByPatientId(patientId);
            if (appointments != null) {
                appointments.sort((a1, a2) -> a1.getStatus().compareTo(a2.getStatus()));
            }
            loadAppointmentCard(patientId, appointments);
        } catch (SQLException e) {
            handleDatabaseError(e);
        }
    }

    // Modified loadAppointmentCard method

    private void loadAppointmentCard(int patientId, List<Appointment> preSortedAppointments) {
        try {
            List<Appointment> appointments = preSortedAppointments != null ?
                    preSortedAppointments : appointmentDAO.getAppointmentsByPatientId(patientId);

            if (appointments == null || appointments.isEmpty()) {
                URL fxmlResource = getClass().getResource("/edu/careflow/fxml/components/states/emptyAppointmentCard.fxml");
                Parent cardContent = FXMLLoader.load(fxmlResource);
                appointmentMainContainer.setStyle("-fx-alignment: center;");
                appointmentMainContainer.getChildren().add(cardContent);
                paginationContainer.setVisible(false);
                buttonsAptFilter.setVisible(false);
                return;
            }

            int totalPages = (int) Math.ceil((double) appointments.size() / cardsPerPage);
            int startIndex = (currentPage - 1) * cardsPerPage;
            int endIndex = Math.min(startIndex + cardsPerPage, appointments.size());

            appointmentMainContainer.getChildren().clear();
            for (int i = startIndex; i < endIndex; i++) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/edu/careflow/fxml/components/patient/appointmentCard.fxml"));
                Parent cardContent = loader.load();
                AppointmentCardController controller = loader.getController();
                controller.initializeData(appointments.get(i));
                appointmentMainContainer.getChildren().add(cardContent);
            }

            // Add fade-in transition
            FadeTransition fadeIn = new FadeTransition(Duration.millis(500), appointmentMainContainer);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            fadeIn.play();

            updatePaginationButtons(totalPages);
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }

    private void goToPreviousPage() {
        if (currentPage > 1) {
            currentPage--;
            loadAppointmentCard(currentPatientId, null);
        }
    }

    private void goToNextPage() {
        List<Appointment> appointments;
        try {
            appointments = appointmentDAO.getAppointmentsByPatientId(currentPatientId);
            int totalPages = (int) Math.ceil((double) appointments.size() / cardsPerPage);
            if (currentPage < totalPages) {
                currentPage++;
                loadAppointmentCard(currentPatientId, null);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void updatePaginationButtons(int totalPages) {
        appointmentLeft.setDisable(currentPage == 1);
        appointmentRight.setDisable(currentPage == totalPages);

        pageLabel.setText("Page " + currentPage + " of " + totalPages);
    }

    private void loadConditionsCard(int patientId) {
        try {
            List<Condition> conditions = ConditionDAO.getConditionsByPatientId(patientId);

            if (conditions.isEmpty()) {
                URL fxmlResource = getClass().getResource("/edu/careflow/fxml/components/states/emptyRecordstCard.fxml");
                Parent cardContent = FXMLLoader.load(fxmlResource);

                Label emptyLabel = (Label) cardContent.lookup("#pageTitle");
                emptyLabel.setText("No Conditions Found");
                conditionsContainer.getChildren().add(cardContent);
                return;
            }

            for(Condition condition : conditions) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/edu/careflow/fxml/components/patient/conditionsCard.fxml"));
                Parent cardContent = loader.load();
                ConditionCardController controller = loader.getController();
                controller.initializeData(condition);
                conditionsContainer.getChildren().add(cardContent);
            }
        } catch (SQLException | IOException e ) {
            e.printStackTrace();
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
                controller.initializeData(prescription);
                medicationContainer.getChildren().add(cardContent);
            }

        } catch(SQLException | IOException e) {
            e.printStackTrace();
        }
    }

    private void loadVitalsCard(int patientId, Vitals recentVitals) {
        try {
            URL fxmlResource = getFxmlResource(recentVitals);
            FXMLLoader loader = new FXMLLoader(fxmlResource);
            Parent cardContent = loader.load();

            DateTimeFormatter todayFormatter = DateTimeFormatter.ofPattern("'Today at' hh:mm a");

            System.out.println(recentVitals);

            latestVitalDate.setText(recentVitals != null ? recentVitals.getRecordedAt().format(todayFormatter) : "No recent vitals");

            if (recentVitals != null) {
                VitalsCardsController controller = loader.getController();
                controller.initializeData(patientId);
            }

            vitalsCardContainer.getChildren().add(cardContent);
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
