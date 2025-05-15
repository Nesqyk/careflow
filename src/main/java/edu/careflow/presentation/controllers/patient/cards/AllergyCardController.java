package edu.careflow.presentation.controllers.patient.cards;

import edu.careflow.repository.entities.Allergy;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class AllergyCardController {

    @FXML
    private Label allergenNameLabel;

    @FXML
    private Label commentLabel;

    @FXML
    private Button editButton;

    @FXML
    private Label severityNameLabel;

    public void initializeData(Allergy allergy) {
        allergenNameLabel.setText(allergy.getAllergen());
        commentLabel.setText(allergy.getComment());
        severityNameLabel.setText(allergy.getSeverity());

        // Set the edit button action
    }
}
