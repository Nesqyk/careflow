package edu.careflow.presentation.controllers.patient.cards;

import edu.careflow.repository.entities.Appointment;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import java.time.format.DateTimeFormatter;

public class ConsultationCardController {


    @FXML
    private Label consultationDate;

    @FXML
    private Label consultationDiagnosis;

    @FXML
    private Label consultationNotes;

    @FXML
    private Label consultationSymptoms;

    @FXML
    private Button editBtn;

    public void initializeData(Appointment appointment) {
        // set the label

        DateTimeFormatter defaultFormatter = DateTimeFormatter.ofPattern("M/d/yyyy @ h a");

        consultationDate.setText(appointment.getAppointmentDate() != null ? appointment.getAppointmentDate().format(defaultFormatter) : "Not specified");
        consultationDiagnosis.setText(appointment.getSymptoms() != null ? appointment.getSymptoms() : "Not specified");
        consultationNotes.setText(appointment.getNotes() != null ? appointment.getNotes() : "Not specified");
        consultationSymptoms.setText(appointment.getConditions() != null ? appointment.getConditions() : "Not specified");
    }

}
