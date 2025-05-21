package edu.careflow.presentation.controllers.nurse;

import com.dlsc.gemsfx.AvatarView;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

public class ContainerNurseController {

    @FXML
    private AvatarView avatarView;

    @FXML
    private Button dashboardBtn;

    @FXML
    private Button logoutBtn;

    @FXML
    private VBox mainContainer;

    @FXML
    private Label nurseName;

    @FXML
    private Label nurseRole;

    @FXML
    private Button patientListBtn;

    @FXML
    private TextField patientSearchField;

    @FXML
    private VBox patientViewCardsContainer;

    @FXML
    private VBox rightBoxContainer;

    @FXML
    private Button settingsBtn;

    @FXML
    private StackPane stackPaneContainer;


    private Button currentActiveButton = null;
    private int currentNurseId;

    @FXML
    public void initialize() {
        dashboardBtn.setOnAction(e -> handleDashboardNavigation());
        patientListBtn.setOnAction(e -> handlePatientListNavigation());

        settingsBtn.setOnAction(e -> handleSettings());
        logoutBtn.setOnAction(e -> handleLogout());
        handleDashboardNavigation();
    }

    private void handleDashboardNavigation() {
        resetAllBtnStyle();
        loadPage("nurseDashboard");
        animateButtonSelection(dashboardBtn);
    }

    private void handlePatientListNavigation() {
        resetAllBtnStyle();
        loadPage("nursePatientList");
        animateButtonSelection(patientListBtn);
    }

//    private void handleVitalsNavigation() {
//        resetAllBtnStyle();
//        loadPage("vitalsHistory");
//        animateButtonSelection(vitalsBtn);
//    }

    private void handleSettings() {
        resetAllBtnStyle();
        loadPage("nurse-settings-form");
        animateButtonSelection(settingsBtn);
    }

    private void handleLogout() {
        try {
            // Set main layout opacity
            mainContainer.setOpacity(0.5);

            // Load the logout confirmation card
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/edu/careflow/fxml/components/user/logoutCard.fxml"));
            Parent logoutCard = loader.load();

            // Add to StackPane
            stackPaneContainer.getChildren().add(logoutCard);
            StackPane.setAlignment(logoutCard, Pos.CENTER);

            Button yesButton = (Button) logoutCard.lookup("#logoutBtn");
            Button noButton = (Button) logoutCard.lookup("#cancelBtn");

            yesButton.setOnAction(e -> {
                try {
                    FXMLLoader loginLoader = new FXMLLoader(getClass().getResource("/edu/careflow/fxml/loginPageNew.fxml"));
                    Parent loginPage = loginLoader.load();
                    Scene currentScene = mainContainer.getScene();
                    currentScene.setRoot(loginPage);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    new Alert(Alert.AlertType.ERROR, "Failed to load login page").show();
                }
            });

            noButton.setOnAction(e -> {
                mainContainer.setOpacity(1.0);
                FadeTransition fadeOut = new FadeTransition(Duration.millis(200), logoutCard);
                fadeOut.setFromValue(1.0);
                fadeOut.setToValue(0.0);
                fadeOut.setOnFinished(event -> stackPaneContainer.getChildren().remove(logoutCard));
                fadeOut.play();
            });

            FadeTransition fadeIn = new FadeTransition(Duration.millis(200), logoutCard);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();

        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Failed to load logout confirmation").show();
        }
    }

    private void resetAllBtnStyle() {
        dashboardBtn.getStyleClass().setAll("nav-button");
        patientListBtn.getStyleClass().setAll("nav-button");
        settingsBtn.getStyleClass().setAll("nav-button");
    }

    private void animateButtonSelection(Button button) {
        if (currentActiveButton != null) {
            currentActiveButton.getStyleClass().remove("nav-button-active");
            currentActiveButton.getStyleClass().add("nav-button");
        }
        currentActiveButton = button;
        button.getStyleClass().setAll("nav-button", "nav-button-active");

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

        button.getStyleClass().add("nav-button-animating");

        ParallelTransition parallelTransition = new ParallelTransition(scaleTransition, fadeTransition);
        parallelTransition.play();

        parallelTransition.setOnFinished(e -> button.getStyleClass().remove("nav-button-animating"));
    }

    private void loadPage(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/edu/careflow/fxml/nurse/" + fxmlFile + ".fxml"));
            Parent pageContent = loader.load();
            // Optionally: pass nurseId to sub-controllers here

            Object controller = loader.getController();

            if(controller instanceof NurseDashboardController) {
                ((NurseDashboardController) controller).initializeData(currentNurseId);
            }
//            } else if (controller instanceof NursePatientListController) {
//                ((NursePatientListController) controller).setNurseId(currentNurseId);
//            } else if (controller instanceof VitalsHistoryController) {
//                ((VitalsHistoryController) controller).setNurseId(currentNurseId);
//            } else if (controller instanceof NurseSettingsController) {
//                ((NurseSettingsController) controller).setNurseId(currentNurseId);
//            }

            if (!mainContainer.getChildren().isEmpty()) {
                Parent currentContent = (Parent) mainContainer.getChildren().get(0);
                FadeTransition fadeOut = new FadeTransition(Duration.millis(150), currentContent);
                fadeOut.setFromValue(1.0);
                fadeOut.setToValue(0.0);
                fadeOut.setOnFinished(e -> {
                    mainContainer.getChildren().clear();
                    mainContainer.getChildren().add(pageContent);

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
                mainContainer.getChildren().add(pageContent);
                FadeTransition fadeIn = new FadeTransition(Duration.millis(300), pageContent);
                fadeIn.setFromValue(0.0);
                fadeIn.setToValue(1.0);
                fadeIn.play();
            }
        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Failed to load " + fxmlFile + " page").show();
        }
    }

    public void setNurseId(int id) {
        this.currentNurseId = id;
    }
} 