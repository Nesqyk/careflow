package edu.careflow.presentation.controllers.patient.forms;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class PrescriptionForm {

    @FXML
    private Button cancelBtn;

    @FXML
    private Button closeButton;

    @FXML
    private TextField doseField;

    @FXML
    private ComboBox<?> doseUnitsComboBox;

    @FXML
    private ComboBox<?> drugComboBox;

    @FXML
    private TextField durationField;

    @FXML
    private ComboBox<?> durationUnitsComboBox;

    @FXML
    private ComboBox<?> frequencyComboBox;

    @FXML
    private TextArea instructionsArea;

    @FXML
    private TextField quantityField;

    @FXML
    private ComboBox<?> routeComboBox;

    @FXML
    private Button saveBtn;

}
