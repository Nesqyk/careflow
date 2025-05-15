package edu.careflow.presentation.controllers.doctor.cards;

import edu.careflow.repository.dao.PatientDAO;
import edu.careflow.repository.entities.Patient;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

public class PatientCardDetails {

    @FXML
    private VBox detailsAppointment;

    @FXML
    private Label patientAddress;

    @FXML
    private Label patientEmail;

    @FXML
    private Label patientGender;

    @FXML
    private Label patientNumber;

    @FXML
    private Button visitPatientBtn;

    @FXML
    private ImageView patientAvatar;

    private final PatientDAO patientDAO = new PatientDAO();

    public void initializeData(Patient patient) {
        // set the label
        patientEmail.setText(patient.getEmail() != null ? patient.getEmail() : "--");
        patientGender.setText(patient.getGender() != null ? patient.getGender() : "--");
        patientNumber.setText(patient.getContact() != null ? patient.getContact() : "--");
        patientAddress.setText(patient.getAddress() != null ? patient.getAddress() : "--");

        // Load and set avatar
        try {
            Image avatar = patientDAO.loadAvatar(patient.getPatientId());
            if (avatar != null) {
                patientAvatar.setImage(avatar);
            } else {
                // Set default avatar based on gender
                String defaultAvatarPath = patient.getGender().equalsIgnoreCase("Male") 
                    ? "/edu/careflow/images/default-male-avatar.png"
                    : "/edu/careflow/images/default-female-avatar.png";
                patientAvatar.setImage(new Image(getClass().getResourceAsStream(defaultAvatarPath)));
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Set default avatar if there's an error
            String defaultAvatarPath = "/edu/careflow/images/default-male-avatar.png";
            patientAvatar.setImage(new Image(getClass().getResourceAsStream(defaultAvatarPath)));
        }
    }
}
