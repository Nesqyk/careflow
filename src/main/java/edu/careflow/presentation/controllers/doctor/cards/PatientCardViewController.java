package edu.careflow.presentation.controllers.doctor.cards;

import com.dlsc.gemsfx.AvatarView;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

public class PatientCardViewController {

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

}
