package edu.careflow.presentation.controllers.patient;

import edu.careflow.presentation.controllers.patient.cards.AppointmentCardLongController;
import edu.careflow.repository.dao.AppointmentDAO;
import edu.careflow.repository.dao.PatientDAO;
import edu.careflow.repository.dao.VitalsDAO;
import edu.careflow.repository.entities.Appointment;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Pagination;
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
    private Pagination appointmentPagination;

    @FXML
    private VBox appointmentsTable;

    @FXML
    private VBox mainContainerAppointment;

    @FXML
    private HBox tableHeaderContainer;


    private AppointmentDAO appointmentDAO = new AppointmentDAO();
    private PatientDAO patientDAO = new PatientDAO();
    private VitalsDAO vitalsDAO = new VitalsDAO();

    private int cardsPerPage = 5;
    private int currentPatientId;
    private List<Appointment> patientAppointments;

    public void initializeData(int patientId) {
        try {
            currentPatientId = patientId;

            // Load appointments and set up pagination
            patientAppointments = appointmentDAO.getAppointmentsByPatientId(patientId);
            setupPagination();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void setupPagination() {
        if (patientAppointments == null || patientAppointments.isEmpty()) {
            try {
                URL fxmlResource = getClass().getResource("/edu/careflow/fxml/components/states/emptyAppointmentCard.fxml");
                Parent cardContent = FXMLLoader.load(fxmlResource);
                mainContainerAppointment.getChildren().add(cardContent);
                tableHeaderContainer.setVisible(false);
                appointmentPagination.setVisible(false);
                return;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        int totalPages = (int) Math.ceil((double) patientAppointments.size() / cardsPerPage);
        appointmentPagination.setPageCount(totalPages);
        appointmentPagination.setCurrentPageIndex(0);
        appointmentPagination.setPageFactory(this::createPage);
    }

    private Node createPage(int pageIndex) {
        VBox pageBox = new VBox();
        pageBox.setSpacing(10);

        int startIndex = pageIndex * cardsPerPage;
        int endIndex = Math.min(startIndex + cardsPerPage, patientAppointments.size());

        try {
            for (int i = startIndex; i < endIndex; i++) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/edu/careflow/fxml/components/patient/appointmentCardLong.fxml"));
                Parent cardContent = loader.load();
                AppointmentCardLongController controller = loader.getController();
                controller.initializeData(patientAppointments.get(i));
                pageBox.getChildren().add(cardContent);
            }

            // Add fade-in transition
            FadeTransition fadeIn = new FadeTransition(Duration.millis(500), pageBox);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);
            fadeIn.play();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return pageBox;
    }
//
//    private void loadVitalsCard(int patientId, Vitals recentVitals) {
//        try {
//            if (recentVitals != null) {
//                FXMLLoader loader = new FXMLLoader(getClass().getResource("/edu/careflow/fxml/components/patient/vitalsCard.fxml"));
//                Parent cardContent = loader.load();
//                VitalsCardsController controller = loader.getController();
//                controller.initializeData(patientId);
//                vitalsCard.getChildren().add(cardContent);
//            } else {
//                URL fxmlResource = getClass().getResource("/edu/careflow/fxml/components/states/emptyVitalCard.fxml");
//                Parent cardContent = FXMLLoader.load(fxmlResource);
//                vitalsCard.getChildren().add(cardContent);
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    @FXML
//    private void initialize() {
//        // Nothing needed here - pagination is configured in setupPagination method
//    }
}