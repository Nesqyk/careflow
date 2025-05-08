package edu.careflow.presentation.controllers.doctor.cards;

import edu.careflow.repository.dao.PatientDAO;
import edu.careflow.repository.entities.Appointment;
import edu.careflow.repository.entities.Patient;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class AppointmentCard {

    @FXML
    private Button StartVisitButton;

    @FXML
    private VBox appointmentContainer;

    @FXML
    private Label appointmentDate;

    @FXML
    private Label appointmentId;

    @FXML
    private VBox clinicRoomAppointment;

    @FXML
    private Hyperlink patientHyperLink;

    @FXML
    private Button showButton;

    @FXML
    private FontIcon showDetailsIcon;

    private PatientDAO patientDAO = new PatientDAO();
    private VBox detailsCard;

    public void initializeData(Appointment appointment) {
        try {
            if (appointment != null) {
                DateTimeFormatter defaultFormatter = DateTimeFormatter.ofPattern("M/d/yyyy @ h a");
                DateTimeFormatter todayFormatter = DateTimeFormatter.ofPattern("'Today at' h a");

                Patient patient = patientDAO.getPatientById(appointment.getPatientId());
                appointmentId.setText("ID: " + appointment.getAppointmentId());

                if (appointment.getAppointmentDate() != null) {
                    LocalDate appointmentDateValue = appointment.getAppointmentDate().toLocalDate();
                    if (appointmentDateValue.isEqual(LocalDate.now())) {
                        appointmentDate.setText(appointment.getAppointmentDate().format(todayFormatter));
                    } else {
                        appointmentDate.setText(appointment.getAppointmentDate().format(defaultFormatter));
                    }
                } else {
                    appointmentDate.setText("--");
                }

                patientHyperLink.setText(patient.getFirstName() + " " + patient.getLastName());
                setupShowButton(appointment);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void setupShowButton(Appointment appointment) {
        showButton.setOnAction(event -> {
            if (detailsCard == null) {
                showDetailsIcon.setIconLiteral("fas-chevron-up");
                showButton.getStyleClass().add("nav-button-active");
                showDetailsIcon.setIconSize(12);
                showDetailsCard(appointment);
            } else {
                showDetailsIcon.setIconLiteral("fas-chevron-down");
                showButton.getStyleClass().remove("nav-button-active");
                hideDetailsCard();
            }
        });
    }

//    private void showDetailsCard(Appointment appointment) {
//        try {
//            if (detailsCard == null) {
//                FXMLLoader loader = new FXMLLoader(getClass().getResource("/edu/careflow/fxml/components/doctor/appointmentDetailsCard.fxml"));
//                detailsCard = loader.load();
//                AppointmentDetailsCard detailsController = loader.getController();
//                detailsController.initializeData(appointment);
//
//                detailsCard.setTranslateY(-20);
//                detailsCard.setOpacity(0);
//                appointmentContainer.getChildren().add(detailsCard);
//            }
//
//            TranslateTransition slideDown = new TranslateTransition(Duration.millis(200), detailsCard);
//            slideDown.setToY(0);
//            detailsCard.setOpacity(1);
//            slideDown.play();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

    private void showDetailsCard(Appointment appointment) {
        try {
            if (detailsCard == null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/edu/careflow/fxml/components/doctor/appointmentDetailsCard.fxml"));
                detailsCard = loader.load();
                AppointmentDetailsCard detailsController = loader.getController();
                detailsController.initializeData(appointment);

                appointmentContainer.getChildren().add(detailsCard);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//    private void hideDetailsCard() {
//        if (detailsCard != null) {
//            TranslateTransition slideUp = new TranslateTransition(Duration.millis(200), detailsCard);
//            slideUp.setToY(-20);
//
//            slideUp.setOnFinished(event -> {
//                detailsCard.setOpacity(0);
//                appointmentContainer.getChildren().remove(detailsCard);
//                detailsCard = null;
//            });
//
//            slideUp.play();
//        }
//    }

    private void hideDetailsCard() {
        if (detailsCard != null) {
            appointmentContainer.getChildren().remove(detailsCard);
            detailsCard = null;
        }
    }
}