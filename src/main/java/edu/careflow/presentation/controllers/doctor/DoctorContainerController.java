package edu.careflow.presentation.controllers.doctor;

import com.dlsc.gemsfx.AvatarView;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.io.IOException;

public class DoctorContainerController {


    @FXML
    private AvatarView avatarView;

    @FXML
    private Button dashboardBtnDoctor;

    @FXML
    private Label doctorName;

    @FXML
    private Label doctorSpecilization;

    @FXML
    private Button logoutBtn;

    @FXML
    private VBox mainContainer;

    @FXML
    private ScrollPane pageContainer;

    @FXML
    private VBox patientCardsProperties;

    @FXML
    private Button patientListBtnDoctor;

    @FXML
    private TextField patientSearchField;

    @FXML
    private VBox patientViewCardsContainer;

    @FXML
    private Button recordsBtnDoctor;

    @FXML
    private Button settingsBtn;


    private Button currentActiveButton = null;
    private int currentDoctorId;

    public void initializeData(int doctorId) {
        this.currentDoctorId = doctorId;

        dashboardBtnDoctor.setOnAction(event -> handleDashboardNavigation());
        patientListBtnDoctor.setOnAction(event -> handlePatientListNavigation());
        recordsBtnDoctor.setOnAction(event -> handleRecordsNavigation());
        logoutBtn.setOnAction(event -> handleLogout());

        handleDashboardNavigation();
    }
    
    @FXML
    private void handleDashboardNavigation() {
        resetAllBtnStyle();
        loadPage("dashboardPage");
        animateButtonSelection(dashboardBtnDoctor);
    }
    
    @FXML
    private void handlePatientListNavigation() {
        resetAllBtnStyle();
        loadPage("doctorPatientList");
        animateButtonSelection(patientListBtnDoctor);
    }
    
    @FXML
    private void handleRecordsNavigation() {
        resetAllBtnStyle();
        loadPage("doctorRecords");
        animateButtonSelection(recordsBtnDoctor);
    }
    
    @FXML
    private void handleLogout() {
        // TODO: Implement logout functionality
        // This would typically clear session data and navigate to login screen
    }
    
    public void loadAppointment() {
        resetAllBtnStyle();
        loadPage("doctorAppointment");
        // No button to animate since this might be called programmatically
    }
    
    private void resetAllBtnStyle() {
        dashboardBtnDoctor.getStyleClass().setAll("nav-button");
        patientListBtnDoctor.getStyleClass().setAll("nav-button");
        recordsBtnDoctor.getStyleClass().setAll("nav-button");
    }
    
    private void animateButtonSelection(Button button) {
        // First reset current active button if any
        if (currentActiveButton != null) {
            currentActiveButton.getStyleClass().remove("nav-button-active");
            currentActiveButton.getStyleClass().add("nav-button");
        }

        // Set new active button
        currentActiveButton = button;
        button.getStyleClass().setAll("nav-button", "nav-button-active");

        // Create a subtle animation effect
        ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(150), button);
        scaleTransition.setFromX(1.0);
        scaleTransition.setFromY(1.0);
        scaleTransition.setToX(1.05);
        scaleTransition.setToY(1.05);
        scaleTransition.setCycleCount(2);
        scaleTransition.setAutoReverse(true);

        FadeTransition fadeTransition = new FadeTransition(Duration.millis(200), button);
        fadeTransition.setFromValue(0.7);
        fadeTransition.setToValue(1.0);

        // Create background color animation by changing style class
        button.getStyleClass().add("nav-button-animating");

        ParallelTransition parallelTransition = new ParallelTransition(scaleTransition, fadeTransition);
        parallelTransition.play();

        // Remove animation class after animation completes
        parallelTransition.setOnFinished(e -> {
            button.getStyleClass().remove("nav-button-animating");
        });
    }
    
    private void loadPage(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/edu/careflow/fxml/doctor/" + fxmlFile + ".fxml")
            );
            Parent pageContent = loader.load();
            Object controller = loader.getController();

            if (controller instanceof DoctorDashboardController) {
                ((DoctorDashboardController) controller).initializeData(currentDoctorId);
            }
            else if (controller instanceof DoctorAppointmentController) {
                ((DoctorAppointmentController) controller).initializeData(currentDoctorId);
            }

            // Check if the new content is already displayed
            if (!mainContainer.getChildren().isEmpty() && 
                mainContainer.getChildren().get(0) == pageContent) {
                return;
            }

            if (!mainContainer.getChildren().isEmpty()) {
                Parent currentContent = (Parent) mainContainer.getChildren().get(0);
                
                FadeTransition fadeOut = new FadeTransition(Duration.millis(150), currentContent);
                fadeOut.setFromValue(1.0);
                fadeOut.setToValue(0.0);
                fadeOut.setOnFinished(e -> {
                    mainContainer.getChildren().clear();
                    mainContainer.getChildren().add(pageContent);

                    // Fade in new content with subtle slide
                    FadeTransition fadeIn = new FadeTransition(Duration.millis(200), pageContent);
                    fadeIn.setFromValue(0.0);
                    fadeIn.setToValue(1.0);

                    TranslateTransition translateIn = new TranslateTransition(Duration.millis(200), pageContent);
                    translateIn.setFromX(10);
                    translateIn.setToX(0);

                    ParallelTransition parallelIn = new ParallelTransition(fadeIn, translateIn);
                    parallelIn.play();
                });
                fadeOut.play();
            } else {
                // First load
                mainContainer.getChildren().add(pageContent);

                FadeTransition fadeIn = new FadeTransition(Duration.millis(300), pageContent);
                fadeIn.setFromValue(0.0);
                fadeIn.setToValue(1.0);
                fadeIn.play();
            }

        } catch (IOException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Failed to load " + fxmlFile + " page").show();
        }
    }
}
