package edu.careflow.presentation.controllers.patient;

import edu.careflow.presentation.controllers.patient.cards.AppointmentCardLongController;
import edu.careflow.presentation.controllers.patient.cards.VitalsCardsController;
import edu.careflow.repository.dao.AppointmentDAO;
import edu.careflow.repository.dao.PatientDAO;
import edu.careflow.repository.dao.VitalsDAO;
import edu.careflow.repository.entities.Appointment;
import edu.careflow.repository.entities.Vitals;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;

public class PatientAppointmentController {

    @FXML
    private VBox appointmentContainer;

    @FXML
    private Button appointmentLeft;

    @FXML
    private Button appointmentRight;

    @FXML
    private VBox appointmentsTable;

    @FXML
    private VBox mainContainerAppointment;

    @FXML
    private HBox paginationContainer;

    @FXML
    private HBox tableHeaderContainer;

    @FXML
    private VBox vitalsCard;

    private AppointmentDAO appointmentDAO = new AppointmentDAO();
    private PatientDAO patientDAO = new PatientDAO();
    private VitalsDAO vitalsDAO = new VitalsDAO();

    private int currentPage = 1;
    private int cardsPerPage = 15;
    private int currentPatientId; // Declare currentPatientId
    private Label pageLabel = new Label();

    public void initializeData(int patientId) {
        try {
            currentPatientId = patientId; // Initialize currentPatientId
            Vitals recentVitals = vitalsDAO.getLatestVitals(patientId);

            loadVitalsCard(patientId, recentVitals);
            loadAppointmentCard(patientId, null);

            // Add the page label to the pagination container
            pageLabel.getStyleClass().add("page-label");
            paginationContainer.getChildren().add(pageLabel);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void loadVitalsCard(int patientId, Vitals recentVitals) {
        try {
            if (recentVitals != null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/edu/careflow/fxml/components/patient/vitalsCard.fxml"));
                Parent cardContent = loader.load();
                VitalsCardsController controller = loader.getController();
                controller.initializeData(patientId);
                vitalsCard.getChildren().add(cardContent);
            } else {
                URL fxmlResource = getClass().getResource("/edu/careflow/fxml/components/states/emptyVitalCard.fxml");
                Parent cardContent = FXMLLoader.load(fxmlResource);
                vitalsCard.getChildren().add(cardContent);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadAppointmentCard(int patientId, List<Appointment> preSortedAppointments) {
        try {
            List<Appointment> appointments = preSortedAppointments != null ?
                    preSortedAppointments : appointmentDAO.getAppointmentsByPatientId(patientId);

            if (appointments == null || appointments.isEmpty()) {
                URL fxmlResource = getClass().getResource("/edu/careflow/fxml/components/states/emptyAppointmentCard.fxml");
                Parent cardContent = FXMLLoader.load(fxmlResource);
                mainContainerAppointment.getChildren().add(cardContent);
                tableHeaderContainer.setVisible(false);
                paginationContainer.setVisible(false);
                return;
            }

            int totalPages = (int) Math.ceil((double) appointments.size() / cardsPerPage);
            int startIndex = (currentPage - 1) * cardsPerPage;
            int endIndex = Math.min(startIndex + cardsPerPage, appointments.size());

            mainContainerAppointment.getChildren().clear();
            for (int i = startIndex; i < endIndex; i++) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/edu/careflow/fxml/components/patient/appointmentCardLong.fxml"));
                Parent cardContent = loader.load();
                AppointmentCardLongController controller = loader.getController();
                controller.initializeData(appointments.get(i));
                mainContainerAppointment.getChildren().add(cardContent);
            }

            // Add fade-in transition
            FadeTransition fadeIn = new FadeTransition(Duration.millis(500), mainContainerAppointment);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            fadeIn.play();

            updatePaginationButtons(totalPages);
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }

    private void goToPreviousPage() throws SQLException {
        if (currentPage > 1) {
            currentPage--;
            loadAppointmentCard(currentPatientId, null);
            updatePaginationButtons((int) Math.ceil((double) appointmentDAO.getAppointmentsByPatientId(currentPatientId).size() / cardsPerPage));
        }
    }

    private void goToNextPage() {
        try {
            List<Appointment> appointments = appointmentDAO.getAppointmentsByPatientId(currentPatientId);
            int totalPages = (int) Math.ceil((double) appointments.size() / cardsPerPage);
            if (currentPage < totalPages) {
                currentPage++;
                loadAppointmentCard(currentPatientId, null);
                updatePaginationButtons(totalPages);
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

    @FXML
    private void initialize() {
        appointmentLeft.setOnAction(e -> {
            try {
                goToPreviousPage();
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        });
        appointmentRight.setOnAction(e -> goToNextPage());

        try {
            List<Appointment> appointments = appointmentDAO.getAppointmentsByPatientId(currentPatientId);
            int totalPages = (int) Math.ceil((double) appointments.size() / cardsPerPage);
            updatePaginationButtons(totalPages);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}