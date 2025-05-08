package edu.careflow.presentation.controllers.patient;

import edu.careflow.presentation.controllers.Controller;
import edu.careflow.presentation.controllers.patient.cards.ConsultationCardController;
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

public class ConsultationController extends Controller {



    @FXML
    private VBox consultationHistory;

    private final AppointmentDAO appointmentDAO = new AppointmentDAO();

    @FXML
    private HBox headerConsultation;

    @FXML
    private VBox containerMain;

    @Override
    public void initializeData(int patientId) {

        try {
            List<Appointment> appointments = appointmentDAO.getAppointmentsByPatientId(patientId);

            if (appointments.isEmpty()) {
                URL fxmlResource = getClass().getResource("/edu/careflow/fxml/components/states/emptyRecordstCard.fxml");
                Parent cardContent = FXMLLoader.load(fxmlResource);

                Label emptyLabel = (Label) cardContent.lookup("#pageTitle");
                emptyLabel.setText("No recent consultations");
                headerConsultation.setVisible(false);
                containerMain.getChildren().add(cardContent);
                containerMain.setStyle("-fx-alignment: center;");
            } else {
                for (Appointment appointment : appointments) {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/edu/careflow/fxml/components/patient/consultationCard.fxml"));
                    Parent root = loader.load();
                    ConsultationCardController controller = loader.getController();
                    controller.initializeData(appointment);
                    containerMain.getChildren().add(root);
                }
            }

        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
    }
}
