package edu.careflow.presentation.controllers.doctor;

import com.dlsc.gemsfx.AvatarView;
import edu.careflow.repository.dao.DoctorDAO;
import edu.careflow.repository.dao.PatientDAO;
import edu.careflow.repository.entities.Doctor;
import edu.careflow.repository.entities.Patient;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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
    private BorderPane mainHomeLayout;

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
    private VBox rightBoxContainer;

    @FXML
    private Button settingsBtn;

    @FXML
    private StackPane stackPaneContainer;

    @FXML
    private VBox topBoxContainer;

    private List<Patient> allPatients = new ArrayList<>();

    private Button currentActiveButton = null;
    private int currentDoctorId;

    private DoctorDAO doctorDAO = new DoctorDAO();

    public void initializeData(int doctorId) {


        try {

            Doctor doctor = doctorDAO.getDoctorById(Integer.toString(doctorId));
            this.currentDoctorId = doctorId;
            if(doctor != null) {
                doctorName.setText(doctor.getFirstName() + " " + doctor.getLastName());
                doctorSpecilization.setText(doctor.getSpecialization());
                // avatarView.setImage(doctor.getAvatar());
            } else {
                System.out.println("Doctor not found");
            }

            dashboardBtnDoctor.setOnAction(event -> handleDashboardNavigation());
            patientListBtnDoctor.setOnAction(event -> handlePatientListNavigation());
            recordsBtnDoctor.setOnAction(event -> handleRecordsNavigation());
            logoutBtn.setOnAction(event -> handleLogout());
            settingsBtn.setOnAction(event -> handleSettings());


            allPatients = new PatientDAO().getAllPatients();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Initial load: show all patients


        // Add search listener
        patientSearchField.textProperty().addListener((obs, oldVal, newVal) -> {
            updatePatientCards(newVal);
        });

        handleDashboardNavigation();
    }

    private boolean showingAllPatients = false;

    private void updatePatientCards(String searchText) {
        patientViewCardsContainer.getChildren().clear();
        String lower = searchText.toLowerCase();

        List<Patient> filtered = allPatients.stream()
                .filter(p -> p.getFirstName().toLowerCase().contains(lower) ||
                        p.getLastName().toLowerCase().contains(lower))
                .toList();

        if (filtered.isEmpty()) {
            Label notFound = new Label("No patients found");
            notFound.setStyle("-fx-font-size: 16px; -fx-text-fill: #828282; -fx-font-family: 'Gilroy-SemiBold';");
            patientViewCardsContainer.getChildren().add(notFound);
            return;
        }

        int maxToShow = showingAllPatients ? filtered.size() : Math.min(2, filtered.size());
        for (int i = 0; i < maxToShow; i++) {
            Patient patient = filtered.get(i);
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/edu/careflow/fxml/components/doctor/patientViewCard.fxml"));
                Parent card = loader.load();

                Label nameLabel = (Label) card.lookup("#patientName");
                Label ageLabel = (Label) card.lookup("#patientAge");
                Label dobLabel = (Label) card.lookup("#patientDateofBirth");
                Label emailLabel = (Label) card.lookup("#patientEmail");
                Label createdLabel = (Label) card.lookup("#patientDateCreated");

                if (nameLabel != null) nameLabel.setText(patient.getFirstName() + " " + patient.getLastName());
                if (ageLabel != null) ageLabel.setText("Age: " + java.time.Period.between(patient.getDateOfBirth(), java.time.LocalDate.now()).getYears());
                if (dobLabel != null) dobLabel.setText("Date of Birth: " + patient.getDateOfBirth());
                if (emailLabel != null) emailLabel.setText("Email: " + patient.getEmail());
                if (createdLabel != null && patient.getCreatedAt() != null) createdLabel.setText("Created: " + patient.getCreatedAt().toLocalDate());

                patientViewCardsContainer.getChildren().add(card);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Add 'View More' button if there are more than 5 and not already showing all
        if (!showingAllPatients && filtered.size() > 5) {
            Button viewMoreBtn = new Button("View More Patients");
            viewMoreBtn.setStyle("-fx-background-color: #0762F2; -fx-text-fill: white; -fx-font-family: 'Gilroy-SemiBold'; -fx-padding: 8 16; -fx-background-radius: 5;");
            viewMoreBtn.setOnAction(e -> {
                showingAllPatients = true;
                updatePatientCards(searchText);
            });
            patientViewCardsContainer.getChildren().add(viewMoreBtn);
        } else if (showingAllPatients && filtered.size() > 5) {
            Button viewLessBtn = new Button("View Less");
            viewLessBtn.setStyle("-fx-background-color: #828282; -fx-text-fill: white; -fx-font-family: 'Gilroy-SemiBold'; -fx-padding: 8 16; -fx-background-radius: 5;");
            viewLessBtn.setOnAction(e -> {
                showingAllPatients = false;
                updatePatientCards(searchText);
            });
            patientViewCardsContainer.getChildren().add(viewLessBtn);
        }
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
        loadPage("doctorWaitingList");
        animateButtonSelection(recordsBtnDoctor);
    }
    
    @FXML
    private void handleLogout() {
        try {
            // Set main layout opacity
            mainHomeLayout.setOpacity(0.5);

            // Load the logout confirmation card
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/edu/careflow/fxml/components/user/logoutCard.fxml"));
            Parent logoutCard = loader.load();

            // Add to StackPane instead of rightBoxContainer
            stackPaneContainer.getChildren().add(logoutCard);

            // Center the logout card in the StackPane
            StackPane.setAlignment(logoutCard, Pos.CENTER);

            // Get the yes/no buttons from the logout card
            Button yesButton = (Button) logoutCard.lookup("#logoutBtn");
            Button noButton = (Button) logoutCard.lookup("#cancelBtn");

            // Handle Yes button - return to login
            yesButton.setOnAction(e -> {
                try {
                    // Load the login page
                    FXMLLoader loginLoader = new FXMLLoader(getClass().getResource("/edu/careflow/fxml/loginPageNew.fxml"));
                    Parent loginPage = loginLoader.load();

                    // Get the current scene from any node
                    Scene currentScene = mainContainer.getScene();
                    currentScene.setRoot(loginPage);
                } catch (IOException ex) {
                    ex.printStackTrace();
                    new Alert(Alert.AlertType.ERROR, "Failed to load login page").show();
                }
            });

            // Handle No button - close the logout card and restore opacity
            noButton.setOnAction(e -> {
                // Restore opacity
                mainHomeLayout.setOpacity(1.0);

                // Remove the logout card from StackPane
                FadeTransition fadeOut = new FadeTransition(Duration.millis(200), logoutCard);
                fadeOut.setFromValue(1.0);
                fadeOut.setToValue(0.0);
                fadeOut.setOnFinished(event -> stackPaneContainer.getChildren().remove(logoutCard));
                fadeOut.play();
            });

            // Add fade-in animation for the logout card
            FadeTransition fadeIn = new FadeTransition(Duration.millis(200), logoutCard);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            fadeIn.play();

        } catch (IOException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Failed to load logout confirmation").show();
        }
    }
    
    @FXML
    private void handleSettings() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/edu/careflow/fxml/doctor/doctorRatesForm.fxml"));
            Parent settingsForm = loader.load();
            
            // Get the controller and initialize it with the current doctor ID
            DoctorRatesController controller = loader.getController();
            controller.initializeData(currentDoctorId);
            
            // First fade out existing content if any
            if (!rightBoxContainer.getChildren().isEmpty()) {
                FadeTransition fadeOut = new FadeTransition(Duration.millis(150), rightBoxContainer.getChildren().get(0));
                fadeOut.setFromValue(1.0);
                fadeOut.setToValue(0.0);
                fadeOut.setOnFinished(e -> {
                    rightBoxContainer.getChildren().setAll(settingsForm);

                    // Fade in new content with slide up effect
                    FadeTransition fadeIn = new FadeTransition(Duration.millis(200), settingsForm);
                    fadeIn.setFromValue(0.0);
                    fadeIn.setToValue(1.0);

                    TranslateTransition slideIn = new TranslateTransition(Duration.millis(200), settingsForm);
                    slideIn.setFromY(20);
                    slideIn.setToY(0);

                    ParallelTransition parallelIn = new ParallelTransition(fadeIn, slideIn);
                    parallelIn.play();
                });
                fadeOut.play();
            } else {
                rightBoxContainer.getChildren().setAll(settingsForm);

                // Simple fade in for first load
                FadeTransition fadeIn = new FadeTransition(Duration.millis(300), settingsForm);
                fadeIn.setFromValue(0.0);
                fadeIn.setToValue(1.0);
                fadeIn.play();
            }
        } catch (IOException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Failed to load settings form").show();
        }
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
        settingsBtn.getStyleClass().setAll("nav-button");
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
            } else if(controller instanceof  DoctorContainerController) {
                ((DoctorContainerController) controller).initializeData(currentDoctorId);
            } else if(controller instanceof  DoctorPatientList) {
                ((DoctorPatientList) controller).initializeData(currentDoctorId);
            } else if(controller instanceof DoctorWaitingListController) {
                ((DoctorWaitingListController) controller).initializeData(currentDoctorId);
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

    public void setDoctorId(int id) {
        this.currentDoctorId = id;
    }
}
