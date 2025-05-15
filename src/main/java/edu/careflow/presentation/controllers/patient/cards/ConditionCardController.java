package edu.careflow.presentation.controllers.patient.cards;

import edu.careflow.repository.dao.ConditionDAO;
import edu.careflow.repository.entities.Condition;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import java.time.format.DateTimeFormatter;

public class ConditionCardController {


    @FXML
    private Label conditionNamePatient;

    @FXML
    private Label conditionStatusPatient;

    @FXML
    private Label dateConditionPatient;

    // for doctors only
    @FXML
    private Button editBtnCondition;

    private ConditionDAO conditionDAO = new ConditionDAO();

    public void initializeData(Condition condition) {
        conditionNamePatient.setText(condition.getConditionName());
        conditionStatusPatient.setText(condition.getStatus());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MMM/yyyy");

        dateConditionPatient.setText(condition.getOnSetDate().format(formatter));
    }
}
