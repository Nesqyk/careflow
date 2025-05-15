package edu.careflow.presentation.controllers.patient.cards;

import edu.careflow.repository.entities.Vitals;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import java.time.format.DateTimeFormatter;

public class VitalCardLongController {

    @FXML
    private Label bpLabel;

    @FXML
    private Label dateVitalsLabel;

    @FXML
    private Label hrLabel;

    @FXML
    private Label o2Label;

    @FXML
    private Label tempLabel;

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd — MMM — yyyy, hh:mm a");

    public void initializeData(Vitals vital) {
        // don't forget to add a null check -> if its null then put something
        dateVitalsLabel.setText(vital.getRecordedAt() != null ? vital.getRecordedAt().format(dateFormatter) : "--");
        hrLabel.setText(vital.getHeartRate() > 0 ? String.format("%d", vital.getHeartRate()) : "--");
        bpLabel.setText(vital.getBloodPressure() != null ? vital.getBloodPressure() : "--");
        tempLabel.setText(vital.getTemperature() > 0 ? String.format("%.1f", vital.getTemperature()) : "--");
        o2Label.setText(vital.getOxygenSaturation() > 0 ? String.format("%.1f", vital.getOxygenSaturation()) : "--");
    }
}
