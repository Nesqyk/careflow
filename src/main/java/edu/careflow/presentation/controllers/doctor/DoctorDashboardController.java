package edu.careflow.presentation.controllers.doctor;

import edu.careflow.presentation.controllers.doctor.cards.AppointmentCard;
import edu.careflow.repository.dao.AppointmentDAO;
import edu.careflow.repository.entities.Appointment;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;

@SuppressWarnings("ALL")
public class DoctorDashboardController {


    @FXML
    private Label activeVisitsCount;

    @FXML
    private VBox appointmentContainer;

    @FXML
    private VBox patientContainerHome;

    @FXML
    private Label scheduleVisitCount;

    @FXML
    private HBox tableHeader;

    @FXML
    private Label totalVisitsCount;

    @FXML
    private VBox mainContainer;


    // Daghan ang doctor and nurse depende siya kung unsa specialty -> or there could be daghan nga doctor sad
    // whereas naay each appointment depende sa doctor id
    // i.e getAppointmentsByDoctorId
    // then it will reveal this specific doctor's all appointment
    // same process sad sa nurses
    // dba?

    // while for the billing naman
    // only bill after the appointment
    // but how can we set the prices man gud?
    // that's one thing to consider sad.

    private AppointmentDAO appointmentDAO = new AppointmentDAO();

    public void initializeData(int doctorId) {
        loadAppointment(doctorId);
        System.out.println(doctorId);
    }

    public void loadAppointment(int doctorId) {
        try {
            List<Appointment> appointments =  appointmentDAO.getAppointmentsByDoctorId(doctorId);

            System.out.println(appointments);
            System.out.println(doctorId);

            if(appointments.isEmpty()) {
                URL fxmlResource = getClass().getResource("/edu/careflow/fxml/components/states/emptyRecordstCard.fxml");
                Parent cardContent = FXMLLoader.load(fxmlResource);
                Label emptyLabel = (Label) cardContent.lookup("#pageTitle");
                emptyLabel.setText("No patient booked");
                mainContainer.setStyle("-fx-alignment: center;");
                tableHeader.setVisible(false);
                mainContainer.getChildren().add(cardContent);
            } else {
                for (Appointment appointment : appointments) {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/edu/careflow/fxml/components/doctor/appointmentCard.fxml"));
                    Parent cardContent = loader.load();
                    AppointmentCard controller = loader.getController();
                    controller.initializeData(appointment);
                    mainContainer.getChildren().add(cardContent);
                }
            }
        } catch(SQLException | IOException e) {
            e.printStackTrace();
        }
    }
}