package edu.careflow.presentation.controllers.patient;

import com.dlsc.gemsfx.AvatarView;
import edu.careflow.presentation.controllers.doctor.cards.PatientCardDetails;
import edu.careflow.presentation.controllers.patient.forms.AddPatientForm;
import edu.careflow.presentation.controllers.patient.forms.BookAptForm;
import edu.careflow.repository.dao.PatientDAO;
import edu.careflow.repository.dao.VitalsDAO;
import edu.careflow.repository.entities.Patient;
import edu.careflow.repository.entities.Vitals;
import edu.careflow.utils.DateHelper;
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
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.IOException;
import java.sql.SQLException;

public class PatientContainerController {

    @FXML
    private Button actionsBtnPatient;

    @FXML
    private Button allergiesBtn;

    @FXML
    private Button appointmentBtnPatient;

    @FXML
    private Button attachmentBtn;

    @FXML
    private AvatarView avatarView;

    @FXML
    private Button billBtnPatient;

    @FXML
    private Button bookBtnUser;

    @FXML
    private VBox containerPatientAgain;

    @FXML
    private Button homeBtnPatient;

    @FXML
    private Button logoutBtn;

    @FXML
    private VBox mainContainer;

    @FXML
    private BorderPane mainHomeLayout;

    @FXML
    private Button medBtnPatient;

    @FXML
    private VBox moreDetailsContainer;

    @FXML
    private ScrollPane pageContainer;

    @FXML
    private Label patientAge;

    @FXML
    private VBox patientCardsProperties;

    @FXML
    private Label patientDatebirth;

    @FXML
    private VBox patientDetailsCard;

    @FXML
    private Label patientEmail;

    @FXML
    private FontIcon patientIconSex;

    @FXML
    private Label patientId;

    @FXML
    private Label patientName;

    @FXML
    private Label patientNameDetail;

    @FXML
    private Label patientSex;

    @FXML
    private VBox rightBoxContainer;

    @FXML
    private Button settingsBtn;

    @FXML
    private Button showMoreBtn;

    @FXML
    private StackPane stackPaneContainer;

    @FXML
    private VBox topBoxContainer;


    private int patientIdentifier;

    private final PatientDAO patientDAO = new PatientDAO();
    private final VitalsDAO vitalsDAO = new VitalsDAO();
    private Button currentActiveButton = null;

    public void initializePatientData(String id) {
        try {
            Patient patient = patientDAO.getPatientById(Integer.parseInt(id));

            patientIdentifier = Integer.parseInt(id);

            String name = patient.getFirstName() + " " + patient.getLastName();
            Integer age = DateHelper.getInstance().calculateAgeAtDate(patient.getDateOfBirth().toString());
            String sex = patient.getGender();
            String dateOfBirth = patient.getDateOfBirth().toString();

            patientNameDetail.setText(name);
            patientEmail.setText(patient.getEmail());

            // Load avatar from database
            try {
                Image avatar = patientDAO.loadAvatar(patientIdentifier);
                if (avatar != null) {
                    avatarView.setImage(avatar);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            // Ari dapita ang mga kato label
            patientName.setText(name);
            patientAge.setText(age.toString());
            patientSex.setText(sex);
            patientDatebirth.setText(dateOfBirth);
            patientId.setStyle("-fx-font-family: Gilroy-SemiBold");
            patientId.setText("Careflow Id : " + id);

            homeBtnPatient.setOnAction(event -> handleHomeNavigation());
            appointmentBtnPatient.setOnAction(event -> handleAppointmentNavigation());
            medBtnPatient.setOnAction(event -> handlePrescriptionNavigation());
            billBtnPatient.setOnAction(event -> handleBillingNavigation());
            showMoreBtn.setOnAction(event -> handleShowMoreDetail(Integer.parseInt(id)));
            bookBtnUser.setOnAction(event -> handleBookForm());
            attachmentBtn.setOnAction(event -> handleAttachmentPage());
            logoutBtn.setOnAction(event -> handleLogout());
            allergiesBtn.setOnAction(event->handleAllergyPage());
            settingsBtn.setOnAction(event -> handleSettings());

            // Initialize the home page by default
            handleHomeNavigation();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public VBox getRightBoxContainer() {
        return rightBoxContainer; // Your actual VBox field name
    }

    @FXML
    private void handleAllergyPage() {
        resetAllBtnStyle();
        setPageContent(patientIdentifier, "allergiesPage");
        animateButtonSelection(allergiesBtn);
    }

    @FXML
    private void handleSettings() {
        try {
            resetAllBtnStyle();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/edu/careflow/fxml/components/user/addPatientForm.fxml"));
            Parent settingsForm = loader.load();
            AddPatientForm controller = loader.getController();

            // Get current patient data
            Patient patient = patientDAO.getPatientById(patientIdentifier);
            
            // Set the form fields with current patient data
            controller.setPatientId(patientIdentifier);

            if(patient != null) {
                controller.setFirstName(patient.getFirstName());
                controller.setLastName(patient.getLastName());
                controller.setDateOfBirth(patient.getDateOfBirth());
                controller.setGender(patient.getGender());
                controller.setContact(patient.getContact());
                controller.setEmail(patient.getEmail());
                controller.setAddress(patient.getAddress());
            }

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

            animateButtonSelection(settingsBtn);
        } catch (IOException | SQLException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Failed to load settings form").show();
        }
    }
    @FXML
    private void handleBookForm() {
        try {
            resetAllBtnStyle();
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/edu/careflow/fxml/components/patient/bookAptForm.fxml")
            );
            Parent bookForm = loader.load();
            // get controller for the booKAptForm
            BookAptForm controller = loader.getController();
            controller.setPatientId(patientIdentifier);

//            if(!rightBoxContainer.getChildren().isEmpty()) {
//                System.out.print("ITS NOT EMPTY");
//                rightBoxContainer.setMinWidth(0);
//
//            }
            // disable pageContainer hbar policy

            // First fade out existing content if any
            if (!rightBoxContainer.getChildren().isEmpty()) {
                FadeTransition fadeOut = new FadeTransition(Duration.millis(150), rightBoxContainer.getChildren().get(0));
                fadeOut.setFromValue(1.0);
                fadeOut.setToValue(0.0);
                fadeOut.setOnFinished(e -> {
                    rightBoxContainer.getChildren().setAll(bookForm);

                    // Fade in new content with slide up effect
                    FadeTransition fadeIn = new FadeTransition(Duration.millis(200), bookForm);
                    fadeIn.setFromValue(0.0);
                    fadeIn.setToValue(1.0);

                    TranslateTransition slideIn = new TranslateTransition(Duration.millis(200), bookForm);
                    slideIn.setFromY(20);
                    slideIn.setToY(0);

                    ParallelTransition parallelIn = new ParallelTransition(fadeIn, slideIn);
                    parallelIn.play();
                });
                fadeOut.play();
            } else {
                rightBoxContainer.getChildren().setAll(bookForm);

                // Simple fade in for first load
                FadeTransition fadeIn = new FadeTransition(Duration.millis(300), bookForm);
                fadeIn.setFromValue(0.0);
                fadeIn.setToValue(1.0);
                fadeIn.play();
            }


            // set text to our blue and greyish background with border
            animateButtonSelection(bookBtnUser);
        } catch (IOException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Failed to load booking form").show();
        }
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
    public void handleAttachmentPage(){
        resetAllBtnStyle();
        setPageContent(patientIdentifier, "attachmentPage");
        animateButtonSelection(attachmentBtn);
    }


    @FXML
    private void handleHomeNavigation() {
        resetAllBtnStyle();

        setPageContent(Integer.parseInt(patientId.getText().replace("Careflow Id : ", "")), "patientHomePage");
        animateButtonSelection(homeBtnPatient);
    }

    @FXML
    private void handleShowMoreDetail(int patientId) {
        // when clicked, show more details
        // moreDetailsContainer.add
        try {
            // Load the patient card details FXML
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/edu/careflow/fxml/components/doctor/patientViewCardWDetailsCard.fxml")
            );
            VBox detailsContent = loader.load();
            PatientCardDetails controller = loader.getController();

            // Get patient data
            Patient patient = patientDAO.getPatientById(patientId);

            // Initialize the controller with patient data
            controller.initializeData(patient);

            // Toggle the visibility of more details
            if (moreDetailsContainer.getChildren().isEmpty()) {
                // Show more details
                moreDetailsContainer.getChildren().add(detailsContent);
                showMoreBtn.getStyleClass().add("nav-button-active");
                showMoreBtn.setText("Show Less");

                // Animate the expansion
                FadeTransition fadeIn = new FadeTransition(Duration.millis(200), detailsContent);
                fadeIn.setFromValue(0.0);
                fadeIn.setToValue(1.0);
                fadeIn.play();
            } else {
                // Hide details
                showMoreBtn.getStyleClass().remove("nav-button-active");
                showMoreBtn.setText("Show More");

                // Animate the collapse
                FadeTransition fadeOut = new FadeTransition(Duration.millis(200), moreDetailsContainer.getChildren().get(0));
                fadeOut.setFromValue(1.0);
                fadeOut.setToValue(0.0);
                fadeOut.setOnFinished(e -> moreDetailsContainer.getChildren().clear());
                fadeOut.play();
            }
        } catch (IOException | SQLException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Failed to load patient details").show();
        }
    }

    @FXML
    private void handleAppointmentNavigation() {
        resetAllBtnStyle();
        setPageContent(Integer.parseInt(patientId.getText().replace("Careflow Id : ", "")), "appointmentPage");
        animateButtonSelection(appointmentBtnPatient);
    }

    @FXML
    private void handlePrescriptionNavigation() {

        // reorder thigns I guess?

        resetAllBtnStyle();
        setPageContent(Integer.parseInt(patientId.getText().replace("Careflow Id : ", "")), "patientMedicalPage");
        animateButtonSelection(medBtnPatient);
    }

    @FXML
    private void handleBillingNavigation() {
        resetAllBtnStyle();
        setPageContent(Integer.parseInt(patientId.getText().replace("Careflow Id : ", "")), "patientInvoices");
        animateButtonSelection(billBtnPatient);
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

    private void resetAllBtnStyle() {
        homeBtnPatient.getStyleClass().setAll("nav-button");
        appointmentBtnPatient.getStyleClass().setAll("nav-button");
        medBtnPatient.getStyleClass().setAll("nav-button");
        billBtnPatient.getStyleClass().setAll("nav-button");
        bookBtnUser.getStyleClass().setAll("cta-button");
    }

    private void setPageContent(int patientId, String file) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/edu/careflow/fxml/patient/" + file + ".fxml")
            );
            Parent pageContent = loader.load();
            Object controller = loader.getController();

            Vitals recentVital = vitalsDAO.getLatestVitals(patientId);
            Patient patient = patientDAO.getPatientById(patientId);

            // Fill out patient details
            patientNameDetail.setText(patient.getFirstName() + " " + patient.getLastName());
            patientEmail.setText(patient.getEmail());

            // Set gender icon
            if (patient.getGender().equalsIgnoreCase("Male")) {
                patientIconSex.setIconLiteral("fas-mars");
            } else {
                patientIconSex.setIconLiteral("fas-venus");
            }

            if (controller instanceof PatientHomeController) {
                ((PatientHomeController) controller).initializeData(patientId);
            } else if (controller instanceof PatientAppointmentController) {
                ((PatientAppointmentController) controller).initializeData(patientId);
            } else if(controller instanceof PatientMedicalController) {
                ((PatientMedicalController) controller).initializeData(patientId);
            } else if(controller instanceof  AttachmentPageController) {
                ((AttachmentPageController) controller).initializeData(patientId);
            } else if(controller instanceof  AllergyPageController) {
                ((AllergyPageController) controller).initializePatient(patientId);
            } else if(controller instanceof BookAptForm) {
                ((BookAptForm) controller). setPatientId(patientId);
            } else if(controller instanceof PatientInvoicesController patientInvoicesController) {
                patientInvoicesController.setPatientId(patientId);
            }

            // Check if the current page is already the same instance
            if (mainContainer.getChildren().contains(pageContent)) {
                return;
            }

            // Use animation for page transitions
            if (!mainContainer.getChildren().isEmpty()) {
                // Fade out current content
                FadeTransition fadeOut = new FadeTransition(Duration.millis(150), mainContainer.getChildren().get(0));
                fadeOut.setFromValue(1.0);
                fadeOut.setToValue(0.0);
                fadeOut.setOnFinished(e -> {
                    mainContainer.getChildren().setAll(pageContent);

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
                // First load doesn't need fade out
                mainContainer.getChildren().setAll(pageContent);

                FadeTransition fadeIn = new FadeTransition(Duration.millis(300), pageContent);
                fadeIn.setFromValue(0.0);
                fadeIn.setToValue(1.0);
                fadeIn.play();
            }

        } catch (IOException | SQLException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Failed to load " + file + " page").show();
        }
    }
}
