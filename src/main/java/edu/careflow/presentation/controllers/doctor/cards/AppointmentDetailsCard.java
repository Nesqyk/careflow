package edu.careflow.presentation.controllers.doctor.cards;

import edu.careflow.repository.dao.PatientDAO;
import edu.careflow.repository.entities.Appointment;
import edu.careflow.repository.entities.Patient;
import edu.careflow.utils.DateHelper;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.sql.SQLException;

public class AppointmentDetailsCard {

    @FXML
    private Label agePatient;

    @FXML
    private Label appointmentNotes;

    @FXML
    private Label dateOfBirthPatient;

    @FXML
    private VBox detailsAppointment;

    @FXML
    private Label genderPatient;

    @FXML
    private Label patientName;

    @FXML
    private Label serviceTypeAppointment;

    @FXML
    private Label timeAppointment;

    private PatientDAO patientDAO = new PatientDAO();

    public void initializeData(Appointment appointment) {
        // set the labels
        try {
            Patient patient = patientDAO.getPatientById(appointment.getPatientId());
            int age = DateHelper.getInstance().calculateAgeAtDate(patient.getDateOfBirth().toString());

            patientName.setText(patient.getFirstName() + " " + patient.getLastName());
            agePatient.setText(Integer.toString(age));
            dateOfBirthPatient.setText(patient.getDateOfBirth() != null ? patient.getDateOfBirth().toString() : "--");
            genderPatient.setText(patient.getGender() != null ? patient.getGender() : "--");
            timeAppointment.setText(appointment.getAppointmentDate() != null ? appointment.getAppointmentDate().toString() : "--");
            serviceTypeAppointment.setText(appointment.getServiceType());
            appointmentNotes.setText(appointment.getNotes() != null ? appointment.getNotes() : "--");
        } catch(SQLException e) {
            e.printStackTrace();
        }
    }
}
