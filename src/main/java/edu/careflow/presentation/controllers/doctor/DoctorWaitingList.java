package edu.careflow.presentation.controllers.doctor;

import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public class DoctorWaitingList {

    @FXML
    private VBox appointmentContainer;

    @FXML
    private Button appointmentLeft;

    @FXML
    private Button appointmentRight;

    @FXML
    private BarChart<?, ?> barchartPatientVolume;

    @FXML
    private HBox buttonsAptFilter;

    @FXML
    private VBox clinicPatientVolumeContainer;

    @FXML
    private Button downloadBtn;

    @FXML
    private VBox mainContainer;

    @FXML
    private HBox paginationContainer;

    @FXML
    private Button pastButtonApt;

    @FXML
    private ComboBox<?> sortComboBox;

    @FXML
    private Button todayButtonApt;

    @FXML
    private Button upcomingButtonApt;

}
