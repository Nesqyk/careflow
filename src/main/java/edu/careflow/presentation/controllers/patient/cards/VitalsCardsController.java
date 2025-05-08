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
    private Label heightPatientLabel;

    @FXML
    private Label hrPatientLabel;

    @FXML
    private Label oxygenLevelPatient;

    @FXML
    private Label tempPatientLabel;

    @FXML
    private Label weightPatientLabel;

    private final VitalsDAO vitalsDAO = new VitalsDAO();
    private final PatientDAO patientDAO = new PatientDAO();


    public void initializeData(int patientId) {
        try {
            Vitals recentVitals = vitalsDAO.getLatestVitals(patientId);

            if (recentVitals != null) {

                double heightInMeters = recentVitals.getHeightCm() / 100.0;
                double bmi = recentVitals.getWeightKg() / (heightInMeters * heightInMeters);

                bmiPatientLabel.setText(bmi > 0 ? String.format("%.1f", bmi) : "N/A");
                bpPatientLabel.setText(recentVitals.getBloodPressure() != null ? recentVitals.getBloodPressure() : "N/A");
                hrPatientLabel.setText(recentVitals.getHeartRate() > 0 ? Integer.toString(recentVitals.getHeartRate()) : "N/A");
                tempPatientLabel.setText(recentVitals.getTemperature() > 0 ? String.format("%.1f", recentVitals.getTemperature()) : "N/A");
                oxygenLevelPatient.setText(recentVitals.getOxygenSaturation() > 0 ? String.format("%.1f", recentVitals.getOxygenSaturation()) : "N/A");
                weightPatientLabel.setText(recentVitals.getWeightKg() > 0 ? String.format("%.1f", recentVitals.getWeightKg()) : "N/A");
                heightPatientLabel.setText(recentVitals.getHeightCm() > 0 ? String.format("%.1f", recentVitals.getHeightCm()) : "N/A");
            } else {

                bmiPatientLabel.setText("N/A");
                bpPatientLabel.setText("N/A");
                hrPatientLabel.setText("N/A");
                tempPatientLabel.setText("N/A");
                oxygenLevelPatient.setText("N/A");
                weightPatientLabel.setText("N/A");
                heightPatientLabel.setText("N/A");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            bmiPatientLabel.setText("Error");
            bpPatientLabel.setText("Error");
            hrPatientLabel.setText("Error");
            tempPatientLabel.setText("Error");
            oxygenLevelPatient.setText("Error");
            weightPatientLabel.setText("Error");
            heightPatientLabel.setText("Error");
        }
    }
}
