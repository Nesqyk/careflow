package edu.careflow.presentation.controllers.patient.forms;

import com.dlsc.gemsfx.CalendarPicker;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;

public class BookAptForm {

    @FXML
    private Button bookBtn;

    @FXML
    private Button cancelBtn;

    @FXML
    private VBox dateContainer;

    @FXML
    private ComboBox<?> doctorCombo;

    @FXML
    private TextField insuranceField;

    @FXML
    private TextField noteField;

    @FXML
    private CalendarPicker pickDate;

    @FXML
    private ComboBox<?> pickTimeCombo;

    @FXML
    private Button closeBtn;

    @FXML
    private ComboBox<?> typeServiceCombo;

    @FXML
    private ToggleGroup userRoleRadio;

}
