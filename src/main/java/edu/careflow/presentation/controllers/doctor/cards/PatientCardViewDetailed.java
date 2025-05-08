package edu.careflow.presentation.controllers.doctor.cards;

import com.dlsc.gemsfx.AvatarView;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;

public class PatientCardViewDetailed {

    @FXML
    private VBox containerDetails;

    @FXML
    private Label patientAge;

    @FXML
    private AvatarView patientAvatar;

    @FXML
    private Label patientDateCreated;

    @FXML
    private Label patientDateofBirth;

    @FXML
    private Label patientEmail;

    @FXML
    private HBox patientListenerContainer;

    @FXML
    private Label patientName;

    @FXML
    private FontIcon showDetailsIcon;

    @FXML
    private Button viewPatientDetails;

}
