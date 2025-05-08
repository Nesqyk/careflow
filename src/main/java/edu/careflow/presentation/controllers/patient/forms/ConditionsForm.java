package edu.careflow.presentation.controllers.patient.forms;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.DatePicker;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import org.controlsfx.control.SearchableComboBox;

public class ConditionsForm {


    @FXML
    private Button cancelBtn;

    @FXML
    private Button closeButton;

    @FXML
    private SearchableComboBox<?> conditionsCombo;

    @FXML
    private VBox idContainerPatient1;

    @FXML
    private DatePicker onSetDateField;

    @FXML
    private Button saveButton;

    @FXML
    private ToggleGroup statusCondition;


}
