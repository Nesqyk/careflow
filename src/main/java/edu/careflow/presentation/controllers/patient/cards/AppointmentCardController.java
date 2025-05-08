package edu.careflow.presentation.controllers.patient.cards;

import edu.careflow.repository.entities.Appointment;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;

import java.time.format.DateTimeFormatter;

public class AppointmentCardController  {

    @FXML
    private Label dateAppointmentPatient;

    @FXML
    private Label drAppointmentPatient;

    @FXML
    private Label roomAppointmentPatient;

    @FXML
    private Label statusAppointmentPatient;

    public void initializeData(Appointment appointment) {

        //
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd — MMM — yyyy, hh:mm a");
        dateAppointmentPatient.setText(appointment.getAppointmentDate().format(formatter));
        drAppointmentPatient.setText(String.valueOf(appointment.getDoctorId()));
        roomAppointmentPatient.setText(String.valueOf(appointment.getNurseId()));
        statusAppointmentPatient.setText(appointment.getStatus());

        // Set color based on status
        switch (appointment.getStatus().toLowerCase()) {
            case "scheduled":
                statusAppointmentPatient.setTextFill(Color.BLUE);
                break;
            case "completed":
                statusAppointmentPatient.setTextFill(Color.GREEN);
                break;
            case "cancelled":
                statusAppointmentPatient.setTextFill(Color.RED);
                break;
            case "pending":
                statusAppointmentPatient.setTextFill(Color.ORANGE);
                break;
            default:
                statusAppointmentPatient.setTextFill(Color.BLACK);
                break;
        }
    }
}