package edu.careflow.presentation.controllers.patient.forms;

import com.dlsc.gemsfx.CalendarPicker;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import org.controlsfx.control.CheckComboBox;

public class AddPatientForm {

    @FXML
    private Button addPatientBtn;

    @FXML
    private TextField addressField;

    @FXML
    private Button cancelBtn;

    @FXML
    private Button closeBtn;

    @FXML
    private CalendarPicker dateOfBirthField;

    @FXML
    private TextField emailAddressField;

    @FXML
    private CheckComboBox<?> emergencyBoxField;

    @FXML
    private TextField emergencyContactField;

    @FXML
    private TextField emergencyNameField;

    @FXML
    private TextField firstNameField;

    @FXML
    private TextField lastNameField;

    @FXML
    private TextField middleInitialField;

    @FXML
    private Label patientHeaderLabel;

    @FXML
    private TextField patientNumberField;

    @FXML
    private ToggleGroup sexRadio;

}
