package edu.careflow.presentation.controllers.patient.cards;

import edu.careflow.repository.dao.PrescriptionDAO;
import edu.careflow.repository.entities.Prescription;
import edu.careflow.repository.entities.PrescriptionDetails;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import java.sql.SQLException;

public class PrescriptionCardController {


    @FXML
    private Label conditionNamePatient;

    @FXML
    private Label dosageLabel;

    @FXML
    private Button editPrescriptionBtn;

    @FXML
    private Label frequencyLabel;

    @FXML
    private Label noteLabel;

    @FXML
    private Label prescriptionNameLabel;


    private final PrescriptionDAO prescriptionDAO = new PrescriptionDAO();

    public void initializeData(Prescription prescription) {
       try {
            PrescriptionDetails details = prescriptionDAO.getPrescriptionDetails(prescription.getPrescriptionId());
            if (details != null) {
                prescriptionNameLabel.setText(details.getMedicineName());
                dosageLabel.setText(details.getDosage() != null ? details.getDosage() : "N/A");
                frequencyLabel.setText(details.getFrequency() != null ? details.getFrequency() : "N/A");
                noteLabel.setText("Secret");
            } else {
                prescriptionNameLabel.setText("No details available");
                dosageLabel.setText("N/A");
                frequencyLabel.setText("N/A");
                noteLabel.setText("N/A");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            prescriptionNameLabel.setText("Error loading prescription details");
            dosageLabel.setText("N/A");
            frequencyLabel.setText("N/A");
            noteLabel.setText("N/A");
        }
    }
}
