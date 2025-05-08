package edu.careflow.presentation.controllers.patient.cards;

import edu.careflow.repository.entities.Vitals;
import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class BioCardLongController {

    @FXML
    private Label bmiBioLabel;

    @FXML
    private Label dateBioLabel;

    @FXML
    private Label weightBioLabel;

    @FXML
    private Label heightBioLabel;

    public void initializeData(Vitals vitals) {
        // Set the labels with the data from the Vitals object
        dateBioLabel.setText(vitals.getRecordedAt() != null ? vitals.getRecordedAt().toString() : "--");

        double weight = vitals.getWeightKg();
        weightBioLabel.setText(weight > 0 ? String.format("%.1f kg", weight) : "--");
        heightBioLabel.setText(vitals.getHeightCm() > 0 ? String.format("%.1f cm", vitals.getHeightCm()) : "--");

        double height = vitals.getHeightCm();
        if (height > 0 && weight > 0) {
            // Convert height from cm to m for BMI calculation
            double heightInMeters = height / 100.0;
            double bmi = weight / (heightInMeters * heightInMeters);
            bmiBioLabel.setText(String.format("%.2f", bmi));
        } else {
            bmiBioLabel.setText("--");
        }
    }

}
