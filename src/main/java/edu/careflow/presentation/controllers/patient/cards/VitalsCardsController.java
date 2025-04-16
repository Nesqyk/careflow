package edu.careflow.presentation.controllers.patient.cards;

import edu.careflow.repository.dao.PatientDAO;
import edu.careflow.repository.dao.VitalsDAO;
import edu.careflow.repository.entities.Vitals;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

import java.sql.SQLException;

public class VitalsCardsController  {


    @FXML
    private Label bmiPatientLabel;

    @FXML
    private Label bpPatientLabel;

    @FXML
    private Label hrPatientLabel;

    @FXML
    private Label tempPatientLabel;

    @FXML
    private Label weightPatientLabel;

    private final VitalsDAO vitalsDAO = new VitalsDAO();
    private final PatientDAO patientDAO = new PatientDAO();


    public void initializeData(int patientId) {
        try {
            Vitals recentVitals = vitalsDAO.getLatestVitals(patientId);

          //  System.out.println(recentVitals.getBloodPressure());

            double bmi = recentVitals.getHeightCm() / (recentVitals.getWeightKg() * recentVitals.getWeightKg());

            bmiPatientLabel.setText(Double.toString(bmi));
            bpPatientLabel.setText(recentVitals.getBloodPressure());
            hrPatientLabel.setText(Integer.toString(recentVitals.getHeartRate()));
            tempPatientLabel.setText(Double.toString(recentVitals.getTemperature()));
            weightPatientLabel.setText(Double.toString(recentVitals.getWeightKg()));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
