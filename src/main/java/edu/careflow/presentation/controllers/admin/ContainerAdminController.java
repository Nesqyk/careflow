package edu.careflow.presentation.controllers.admin;

import com.dlsc.gemsfx.AvatarView;
import edu.careflow.presentation.controllers.Controller;
import edu.careflow.repository.dao.UserDAO;
import edu.careflow.repository.entities.User;
import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class ContainerAdminController extends Controller {
    @FXML private Button addUserBtn;
    @FXML private Label adminName;
    @FXML private AvatarView avatarView;
    @FXML private Button dashboardBtn;
    @FXML private Button logoutBtn;
    @FXML private VBox mainContainer;
    @FXML private BorderPane mainHomeLayout;
    @FXML private ScrollPane pageContainer;
    @FXML private VBox patientCardsProperties;
    @FXML private Button usersListBtn;
    @FXML private Label receptionistRole;
    @FXML private VBox rightBoxContainer;
    @FXML private Button settingsBtn;
    @FXML private StackPane stackPaneContainer;
    @FXML private VBox topBoxContainer;
    @FXML private Label adminRole;

    // Navigation buttons
    @FXML private Button usersBtn;
    @FXML private Button rolesBtn;
    @FXML private Button auditBtn;
    
    private final UserDAO userDAO = new UserDAO();
    private User currentUser;
    private List<User> allUsers;
    private Button currentActiveButton = null;

    @FXML
    public void initialize() {
        setupNavigationButtons();
        handleDashboardNavigation();
        addUserBtn.setOnAction(e -> handleAddUser());
    }

    private void setupNavigationButtons() {
        dashboardBtn.setOnAction(e -> handleDashboardNavigation());
        usersListBtn.setOnAction(e -> handleUsersListNavigation());
        settingsBtn.setOnAction(e -> handleSettings());
        logoutBtn.setOnAction(e -> handleLogout());
    }

    private void handleDashboardNavigation() {
        resetAllBtnStyle();
        loadPage("dashboardPage");
        animateButtonSelection(dashboardBtn);
    }

    private void handleUsersListNavigation() {
        resetAllBtnStyle();
        loadPage("patientList");
        animateButtonSelection(usersListBtn);
    }

    private void handleSettings() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/edu/careflow/fxml/admin/settings.fxml"));
            Parent settings = loader.load();
            showInRightContainer(settings);
        } catch (IOException e) {
            showError("Failed to load settings: " + e.getMessage());
        }
    }

    private void handleLogout() {
        try {
            mainHomeLayout.setOpacity(0.5);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/edu/careflow/fxml/components/user/logoutCard.fxml"));
            Parent logoutCard = loader.load();

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
                } catch (IOException ex) {
                    showError("Failed to load login page: " + ex.getMessage());
                }
            });

            noButton.setOnAction(e -> {
                mainHomeLayout.setOpacity(1.0);
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

        } catch (IOException e) {
            showError("Failed to load logout confirmation: " + e.getMessage());
        }
    }

    private void resetAllBtnStyle() {
        dashboardBtn.getStyleClass().setAll("nav-button");
        usersListBtn.getStyleClass().setAll("nav-button");
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

        ParallelTransition parallelTransition = new ParallelTransition(scaleTransition, fadeTransition);
        parallelTransition.play();

        parallelTransition.setOnFinished(e -> {
            button.getStyleClass().remove("nav-button-animating");
        });
    }

    private void loadPage(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/edu/careflow/fxml/admin/" + fxmlFile + ".fxml")
            );
            Parent pageContent = loader.load();
            Object controller = loader.getController();

            if (controller instanceof Controller) {
                ((Controller) controller).initializeData(currentUser.getUser_id());
            }

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

        } catch (IOException e) {
            showError("Failed to load " + fxmlFile + " page: " + e.getMessage());
        }
    }

    private void showInRightContainer(Parent content) {
        rightBoxContainer.getChildren().clear();
        rightBoxContainer.getChildren().add(content);
    }

    public void setCurrentUser(User user) {
        this.currentUser = user;
        if (user != null) {
            adminName.setText(user.getFirstName() + " " + user.getLastName());
            adminRole.setText("Administrator");
        }
    }

    @Override
    public void initializeData(int userId) {
        try {
            User user = userDAO.getUserById(userId);
            setCurrentUser(user);
        } catch (SQLException e) {
            showError("Failed to load user data: " + e.getMessage());
        }
    }

    private void showError(String message) {
        System.err.println("Error: " + message);
        new Exception().printStackTrace();

        Scene scene = stackPaneContainer.getScene();
        if (scene != null) {
            StackPane container = (StackPane) scene.lookup("#stackPaneContainer");
            if (container != null) {
                Label errorLabel = new Label(message);
                errorLabel.getStyleClass().add("error-label");
                errorLabel.setStyle(
                    "-fx-background-color: #f44336;" +
                    "-fx-text-fill: white;" +
                    "-fx-padding: 10 20;" +
                    "-fx-background-radius: 5;" +
                    "-fx-translate-y: 300;" +
                    "-fx-font-family: 'Gilroy-SemiBold';" +
                    "-fx-font-size: 16px"
                );

                FadeTransition fadeIn = new FadeTransition(Duration.millis(300), errorLabel);
                fadeIn.setFromValue(0.0);
                fadeIn.setToValue(1.0);

                container.getChildren().add(errorLabel);
                fadeIn.play();

                Timeline timeline = new Timeline(
                    new KeyFrame(Duration.seconds(3), e -> {
                        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), errorLabel);
                        fadeOut.setFromValue(1.0);
                        fadeOut.setToValue(0.0);
                        fadeOut.setOnFinished(event -> container.getChildren().remove(errorLabel));
                        fadeOut.play();
                    })
                );
                timeline.play();
            }
        }
    }

    private void handleAddUser() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/edu/careflow/fxml/components/user/addUserForm.fxml"));
            Parent addUserForm = loader.load();
            showInRightContainer(addUserForm);
        } catch (IOException e) {
            showError("Failed to load Add User form: " + e.getMessage());
        }
    }
} 