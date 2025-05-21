package edu.careflow.presentation.controllers.receptionist;

import edu.careflow.presentation.controllers.patient.PatientMedicalController;
import edu.careflow.presentation.controllers.patient.forms.AddPatientForm;
import edu.careflow.presentation.controllers.patient.forms.BookAptForm;
import edu.careflow.repository.dao.PatientDAO;
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

public class ReceptionistContainerController {
    @FXML private StackPane stackPaneContainer;
    @FXML private BorderPane mainHomeLayout;
    @FXML private VBox patientCardsProperties;
    @FXML private VBox rightBoxContainer;
    @FXML private VBox topBoxContainer;
    @FXML private Button dashboardBtnReceptionist;
    @FXML private Button patientListReceptionist;
    @FXML private Button invoicesBtnReceptionist;
    @FXML private Button settingsBtn;
    @FXML private Button logoutBtn;
    @FXML private Button addPatientBtn;
    @FXML private Label receptionistName;
    @FXML private Label receptionistRole;
    @FXML private VBox patientViewCardsContainer;
    @FXML private TextField patientSearchField;
    @FXML private VBox mainContainer;

    private List<Patient> allPatients = new ArrayList<>();
    private Button currentActiveButton = null;
    private boolean showingAllPatients = false;

    public void initializeData(String receptionistNameStr, String receptionistRoleStr) {
        receptionistName.setText(receptionistNameStr);
        receptionistRole.setText(receptionistRoleStr);
        dashboardBtnReceptionist.setOnAction(event -> handleDashboardNavigation());
        patientListReceptionist.setOnAction(event -> handlePatientListNavigation());
        invoicesBtnReceptionist.setOnAction(event -> handleInvoicesNavigation());
        settingsBtn.setOnAction(event -> handleSettingsNavigation());
        logoutBtn.setOnAction(event -> handleLogout());
        addPatientBtn.setOnAction(event -> handleAddPatient());
        // Patient search
        if (patientSearchField != null) {
            patientSearchField.textProperty().addListener((obs, oldVal, newVal) -> updatePatientCards(newVal));
        }
        // Load all patients
        try {
            allPatients = new PatientDAO().getAllPatients();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        handleDashboardNavigation();
    }

    private void updatePatientCards(String searchText) {
        if (patientViewCardsContainer == null) return;
        patientViewCardsContainer.getChildren().clear();
        String lower = searchText == null ? "" : searchText.toLowerCase();
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
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/edu/careflow/fxml/receptionist/receptionistPatientCard.fxml"));
                Parent card = loader.load();
                ReceptionistPatientCardController controller = loader.getController();
                controller.patientNameLabel.setText(patient.getFirstName() + " " + patient.getLastName());
                controller.patientIdLabel.setText("ID: " + patient.getPatientId());
                controller.lastVisitDateLabel.setText("Last Visit: -"); // TODO: set actual last visit
                controller.visitCountLabel.setText("0"); // TODO: set actual visit count
                // TODO: set avatar if available
                patientViewCardsContainer.getChildren().add(card);
                controller.bookAptBtn.setOnAction(e -> handleBookAppointment(patient));
                controller.editBtn.setOnAction(e -> handleEditPatient(patient));
                controller.deleteBtn.setOnAction(e -> handleDeletePatient(patient));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        // Add 'View More' or 'View Less' button
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
        animateButtonSelection(dashboardBtnReceptionist);
    }

    @FXML
    private void handlePatientListNavigation() {
        resetAllBtnStyle();
        loadPage("receptionistPatientList");
        animateButtonSelection(patientListReceptionist);
    }

    @FXML
    private void handleInvoicesNavigation() {
        resetAllBtnStyle();
        loadPage("receptionistInvoices");
        animateButtonSelection(invoicesBtnReceptionist);
    }

    @FXML
    private void handleSettingsNavigation() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/edu/careflow/fxml/receptionist/receptionistSettings.fxml"));
            Parent settingsForm = loader.load();
            
            // Get the right container
            if (rightBoxContainer != null) {
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
            }
            
            resetAllBtnStyle();
            animateButtonSelection(settingsBtn);
        } catch (IOException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Failed to load settings form").show();
        }
    }

    @FXML
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
                    Scene currentScene = mainHomeLayout.getScene();
                    currentScene.setRoot(loginPage);
                } catch (IOException ex) {
                    ex.printStackTrace();
                    new Alert(Alert.AlertType.ERROR, "Failed to load login page").show();
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
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Failed to load logout confirmation").show();
        }
    }

    @FXML
    private void handleAddPatient() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/edu/careflow/fxml/components/user/addPatientForm.fxml"));
            Parent patientForm = loader.load();
            
            // Get the controller and set up any necessary data
            AddPatientForm controller = loader.getController();
            
            // Set up success callback to refresh patient list
            controller.setOnSuccessCallback(() -> {
                try {
                    allPatients = new PatientDAO().getAllPatients();
                    updatePatientCards(patientSearchField.getText());
                } catch (SQLException e) {
                    e.printStackTrace();
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText("Failed to refresh patient list");
                    alert.setContentText("An error occurred while refreshing the patient list. Please try again.");
                    alert.showAndWait();
                }
            });
            
            // Add form to the stack pane with fade in animation
            FadeTransition fadeIn = new FadeTransition(Duration.millis(200), patientForm);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);
            
            rightBoxContainer.getChildren().add(patientForm);
            StackPane.setAlignment(patientForm, Pos.CENTER);
            fadeIn.play();
            
            // Set up close button handler
            Button closeBtn = (Button) patientForm.lookup("#closeBtn");
            if (closeBtn != null) {
                closeBtn.setOnAction(e -> handleCloseOverlay(patientForm));
            }
            
            // Set up cancel button handler
            Button cancelBtn = (Button) patientForm.lookup("#cancelBtn");
            if (cancelBtn != null) {
                cancelBtn.setOnAction(e -> handleCloseOverlay(patientForm));
            }
        } catch (IOException e) {
            e.printStackTrace();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Failed to load add patient form");
            alert.setContentText("An error occurred while loading the form. Please try again.");
            alert.showAndWait();
        }
    }

    public void handleCloseOverlay(Parent overlay) {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(200), overlay);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(event -> stackPaneContainer.getChildren().remove(overlay));
        fadeOut.play();
    }

    private void resetAllBtnStyle() {
        dashboardBtnReceptionist.getStyleClass().setAll("nav-button");
        patientListReceptionist.getStyleClass().setAll("nav-button");
        invoicesBtnReceptionist.getStyleClass().setAll("nav-button");
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
        parallelTransition.setOnFinished(e -> {
            button.getStyleClass().remove("nav-button-animating");
        });
    }

    private void loadPage(String fxmlFile) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/edu/careflow/fxml/receptionist/" + fxmlFile + ".fxml"));
            Parent pageContent = loader.load();
            // Check if the new content is already displayed
            if (mainContainer != null && !mainContainer.getChildren().isEmpty() && mainContainer.getChildren().get(0) == pageContent) {
                return;
            }
            if (mainContainer != null && !mainContainer.getChildren().isEmpty()) {
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
            } else if (mainContainer != null) {
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

    private void handleBookAppointment(Patient patient) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/edu/careflow/fxml/components/patient/bookAptForm.fxml"));
            Parent bookForm = loader.load();
            BookAptForm controller = loader.getController();
            controller.setPatientId(patient.getPatientId());

            VBox rightBoxContainer = (VBox) patientViewCardsContainer.getScene().lookup("#rightBoxContainer");
            if (rightBoxContainer != null) {
                rightBoxContainer.getChildren().clear();
                rightBoxContainer.getChildren().add(bookForm);
                FadeTransition fadeIn = new FadeTransition(Duration.millis(200), bookForm);
                fadeIn.setFromValue(0.0);
                fadeIn.setToValue(1.0);
                fadeIn.play();
            }
        } catch (IOException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Failed to load appointment form").show();
        }
    }

    private void handleEditPatient(Patient patient) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/edu/careflow/fxml/components/user/addPatientForm.fxml"));
            Parent form = loader.load();
            AddPatientForm formController = loader.getController();
            formController.setPatientId(patient.getPatientId());
            formController.setFirstName(patient.getFirstName());
            formController.setLastName(patient.getLastName());
            formController.setDateOfBirth(patient.getDateOfBirth());
            formController.setGender(patient.getGender());
            formController.setContact(patient.getContact());
            formController.setEmail(patient.getEmail());
            formController.setAddress(patient.getAddress());
            formController.setOnSuccessCallback(() -> {
                try {
                    allPatients = new PatientDAO().getAllPatients();
                    updatePatientCards(patientSearchField.getText());
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            });

            VBox rightBoxContainer = (VBox) patientViewCardsContainer.getScene().lookup("#rightBoxContainer");
            if (rightBoxContainer != null) {
                rightBoxContainer.getChildren().clear();
                rightBoxContainer.getChildren().add(form);
                FadeTransition fadeIn = new FadeTransition(Duration.millis(200), form);
                fadeIn.setFromValue(0.0);
                fadeIn.setToValue(1.0);
                fadeIn.play();
            }
        } catch (IOException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Failed to load patient form").show();
        }
    }

    private void handleDeletePatient(Patient patient) {
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirm Delete");
        confirmDialog.setHeaderText("Delete Patient");
        confirmDialog.setContentText("Are you sure you want to delete " + patient.getFirstName() + " " + patient.getLastName() + "?");

        confirmDialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    if (new PatientDAO().deletePatient(patient.getPatientId())) {
                        allPatients = new PatientDAO().getAllPatients();
                        updatePatientCards(patientSearchField.getText());
                        new Alert(Alert.AlertType.INFORMATION, "Patient deleted successfully").show();
                    } else {
                        new Alert(Alert.AlertType.ERROR, "Failed to delete patient").show();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                    new Alert(Alert.AlertType.ERROR, "Error deleting patient: " + e.getMessage()).show();
                }
            }
        });
    }

    private void handleViewHistory(Patient patient) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/edu/careflow/fxml/patient/patientMedical.fxml"));
            Parent historyView = loader.load();
            PatientMedicalController controller = loader.getController();
            controller.initializeData(patient.getPatientId());

            VBox rightBoxContainer = (VBox) patientViewCardsContainer.getScene().lookup("#rightBoxContainer");
            if (rightBoxContainer != null) {
                rightBoxContainer.getChildren().clear();
                rightBoxContainer.getChildren().add(historyView);
                FadeTransition fadeIn = new FadeTransition(Duration.millis(200), historyView);
                fadeIn.setFromValue(0.0);
                fadeIn.setToValue(1.0);
                fadeIn.play();
            }
        } catch (IOException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Failed to load patient history").show();
        }
    }
} 