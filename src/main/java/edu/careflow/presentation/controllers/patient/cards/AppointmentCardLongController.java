package edu.careflow.presentation.controllers.patient.cards;

import edu.careflow.repository.dao.DoctorDAO;
import edu.careflow.repository.entities.Appointment;
import edu.careflow.repository.entities.Doctor;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;

public class AppointmentCardLongController {


    @FXML
    private Label dateAppointment;

    @FXML
    private Label doctorAppointment;

    @FXML
    private Label roomAppointment;

    @FXML
    private Label statusAppointment;


    private DoctorDAO doctorDAO = new DoctorDAO();

    public void initializeData(Appointment appointment) {
        try {
            Doctor doctor = doctorDAO.getDoctorById(Integer.toString(appointment.getDoctorId()));
            DateTimeFormatter defaultFormatter = DateTimeFormatter.ofPattern("dd — MMM — yyyy, hh:mm a");

            dateAppointment.setText(appointment.getAppointmentDate().format(defaultFormatter));
            System.out.println(appointment.getAppointmentDate().format(defaultFormatter));
            doctorAppointment.setText(doctor.getFirstName() + " " + doctor.getLastName());
            roomAppointment.setText(appointment.getRoom());
            statusAppointment.setText(appointment.getStatus());

        } catch(SQLException e) {
            e.printStackTrace();
        }
        // Do your thing
        // Do you thing once again

        // Set color based on status
    }
}
