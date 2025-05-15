package edu.careflow.presentation.controllers.doctor.cards;

import edu.careflow.presentation.controllers.patient.forms.ConditionsForm;
import edu.careflow.presentation.controllers.patient.forms.VitalsBioForm;
import edu.careflow.repository.dao.AppointmentDAO;
import edu.careflow.repository.dao.PatientDAO;
import edu.careflow.repository.entities.Appointment;
import edu.careflow.repository.entities.Patient;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.io.IOException;
import java.sql.SQLException;

public class NavigationBar {

    @FXML
    private Button backBtn;

    @FXML
    private Button endVisitBtn;

    @FXML
    private MenuButton formBtn;

    @FXML
    private Label patientNameLabel;

    private int patientId;
    private int doctorId;
    private int appointmentId;

    public VBox rightBoxContainer;

    private PatientDAO patientDAO = new PatientDAO();
    private AppointmentDAO appointmentDAO = new AppointmentDAO();

    @FXML
    public void initialize() {

        try {

            Patient patient = patientDAO.getPatientById(this.getPatientId());

            String firstName = patient == null ? "--" : patient.getFirstName();
            String lastName = patient == null ? "--" : patient.getLastName();

            patientNameLabel.setText(firstName + " " + lastName);
            formBtn.setStyle("-fx-text-fill: #0762F2;");

            formBtn.getItems().clear();
            MenuItem prescriptionItem = new MenuItem("Prescription");
            MenuItem vitalsItem = new MenuItem("Vitals");
            MenuItem allergyItem = new MenuItem("Allergy");
            MenuItem conditionItem = new MenuItem("Condition");
            MenuItem attachmentItem = new MenuItem("Attachment");

            Appointment appointment = appointmentDAO.getAppointmentById(appointmentId);

            prescriptionItem.setOnAction(e -> handlePrescriptionForm());
            vitalsItem.setOnAction(e -> handleVitalsForm());
            allergyItem.setOnAction(e -> handleAllergyForm());
            conditionItem.setOnAction(e -> handleConditionForm());
            attachmentItem.setOnAction(e -> handleAttachmentForm());

            formBtn.getItems().addAll(
                    prescriptionItem,
                    vitalsItem,
                    allergyItem,
                    conditionItem,
                    attachmentItem
            );

            // Add Join Meeting menu item if appointment is scheduled and online
            if (appointment != null && "Scheduled".equalsIgnoreCase(appointment.getStatus()) && "Online".equalsIgnoreCase(appointment.getAppointmentType())) {
                formBtn.getItems().add(new SeparatorMenuItem());
                MenuItem joinMeetingItem = new MenuItem("Join Meeting");
                joinMeetingItem.setOnAction(e -> {
                    String url = appointment.getMeetingLink();
                    Scene scene = formBtn.getScene();
                    StackPane stackPaneContainer = (StackPane) scene.lookup("#stackPaneContainer");
                    if (stackPaneContainer != null) {
                        try {
                            FXMLLoader loader = new FXMLLoader(getClass().getResource("/edu/careflow/fxml/components/doctor/videoContainerDoctor.fxml"));
                            Parent videoContainer = loader.load();

                            // Set meeting link label
                            Label meetingLinkLabel = (Label) videoContainer.lookup("#meetingLinkLabel");
                            if (meetingLinkLabel != null) {
                                meetingLinkLabel.setText("Meeting Link: " + url);
                            }

                            // Wire up openInBrowserBtn
                            Button openInBrowserBtn = (Button) videoContainer.lookup("#openInBrowserBtn");
                            if (openInBrowserBtn != null) {
                                openInBrowserBtn.setOnAction(ev -> {
                                    try {
                                        java.awt.Desktop.getDesktop().browse(new java.net.URI(url));
                                    } catch (Exception ex) {
                                        ex.printStackTrace();
                                    }
                                });
                            }

                            // Wire up closeBtn
                            Button closeBtn = (Button) videoContainer.lookup("#closeBtn");
                            if (closeBtn != null) {
                                closeBtn.setOnAction(ev -> stackPaneContainer.getChildren().remove(videoContainer));
                            }

                            // Wire up endCallBtn
                            Button endCallBtn = (Button) videoContainer.lookup("#endCallBtn");
                            if (endCallBtn != null) {
                                endCallBtn.setOnAction(ev -> stackPaneContainer.getChildren().remove(videoContainer));
                                appointment.setAppointmentStatus("Completed");
                            }

                            // Set patient info
                            Label patientNameLabel = (Label) videoContainer.lookup("#patientNameLabel");
                            Label patientIdLabel = (Label) videoContainer.lookup("#patientIdLabel");
                            if (patientNameLabel != null) patientNameLabel.setText(patient.getFirstName() + " " + patient.getLastName());
                            if (patientIdLabel != null) patientIdLabel.setText("Patient ID: " + patient.getPatientId());

                            // Optionally, wire up saveNotesBtn and consultationNotes
                            Button saveNotesBtn = (Button) videoContainer.lookup("#saveNotesBtn");
                            TextArea consultationNotes = (TextArea) videoContainer.lookup("#consultationNotes");
                            if (saveNotesBtn != null && consultationNotes != null) {
                                saveNotesBtn.setOnAction(ev -> {
                                    String notes = consultationNotes.getText();
                                    if(notes.length() > 0) {
                                        try{
                                            consultationNotes.clear();
                                            System.out.println(notes);
                                            appointment.setAppointmentNotes(notes);
                                            appointmentDAO.updateAppointment(appointment);
                                            System.out.println(appointment);
                                            // Show floating success label
                                            showFloatingLabel(stackPaneContainer, "Notes saved successfully", "#4CAF50", "white");
                                        } catch(SQLException sqlException) {
                                            sqlException.printStackTrace();
                                        }
                                    } else {
                                        // Show floating warning label
                                        showFloatingLabel(stackPaneContainer, "Please enter some notes before saving.", "#ED6C02", "white");
                                    }
                                });
                            }

                            stackPaneContainer.getChildren().add(videoContainer);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                });
                formBtn.getItems().add(joinMeetingItem);
            }

            backBtn.setOnAction(e -> handleBack());
            endVisitBtn.setOnAction(e -> handleEndVisit());

            // Add a listener to wait for scene to be available
            backBtn.sceneProperty().addListener((observable, oldScene, newScene) -> {
                if (newScene != null) {
                    setupRightContainer();
                    hidePatientOnlyButtons();
                }
            });

        } catch(SQLException e) {
            e.printStackTrace();
        }

    }

    private void setupRightContainer() {
        if (backBtn.getScene() != null) {
            this.rightBoxContainer = (VBox) backBtn.getScene().getRoot().lookup("#rightBoxContainer");
        }
    }

    private void hidePatientOnlyButtons() {
        Scene scene = backBtn.getScene();
        if (scene != null) {
            Button logoutBtn = (Button) scene.lookup("#logoutBtn");
            Button bookBtnUser = (Button) scene.lookup("#bookBtnUser");
            if (logoutBtn != null) logoutBtn.setVisible(false);
            if (bookBtnUser != null) bookBtnUser.setVisible(false);
        }
    }

    private void handlePrescriptionForm() {
        loadFormIntoRightContainer("/edu/careflow/fxml/components/patient/prescriptForm.fxml");
    }

    private void handleVitalsForm() {
        loadFormIntoRightContainer("/edu/careflow/fxml/components/patient/vitalAptForm.fxml");
    }

    private void handleAllergyForm() {
        loadFormIntoRightContainer("/edu/careflow/fxml/components/patient/recordAllergyForm.fxml");
    }

    private void handleConditionForm() {
        loadFormIntoRightContainer("/edu/careflow/fxml/components/patient/conditionsForm.fxml");
    }

    private void handleAttachmentForm() {
        loadFormIntoRightContainer("/edu/careflow/fxml/components/doctor/addAttachmentCard.fxml");
    }

    // make method that looks up the rightBoxContainer (vbox) from root and sets the content depending on the parameter
    // and then loads the form


    // go back to doctor's dashboard
    private void handleBack() {
        // TODO: Implement back navigation
    }


    // mark visit as ended in the database
    private void handleEndVisit() {
        // TODO: Implement end visit logic
    }

    private void loadFormIntoRightContainer(String formPath) {
        try {
            // Get the scene from any control (using formBtn here)
            Scene currentScene = formBtn.getScene();

            // Look up the right container
            VBox rightBoxContainer = (VBox) currentScene.lookup("#rightBoxContainer");

            if (rightBoxContainer != null) {
                // Clear existing content
                rightBoxContainer.getChildren().clear();

                // Load and add new form
                FXMLLoader loader = new FXMLLoader(getClass().getResource(formPath));
                Parent formContent = loader.load();

                // Get the controller and set patient/doctor IDs if needed
                Object controller = loader.getController();
                if (controller instanceof NavigationBarFormRight) {
                    NavigationBarFormRight formController = (NavigationBarFormRight) controller;
                    formController.setPatientId(this.patientId);
                    formController.setDoctorId(this.doctorId);
                } else if (controller instanceof AttachmentViewController) {
                    AttachmentViewController attachmentController = (AttachmentViewController) controller;
                    attachmentController.setPatientId(this.patientId);
                    attachmentController.setDoctorId(this.doctorId);
                } else if(controller instanceof VitalsBioForm) {
                    // cont...
                    VitalsBioForm vitalsController = (VitalsBioForm) controller;
                    vitalsController.initializeData(this.patientId);
                } else if(controller instanceof ConditionsForm) {
                    ConditionsForm conditionsController = (ConditionsForm) controller;
                    conditionsController.setPatientId(this.patientId);
                }

                // Add the form to container with fade transition
                FadeTransition fadeIn = new FadeTransition(Duration.millis(300), formContent);
                fadeIn.setFromValue(0.0);
                fadeIn.setToValue(1.0);

                rightBoxContainer.getChildren().add(formContent);
                fadeIn.play();

            } else {
                throw new RuntimeException("Right container not found in scene");
            }
        } catch (IOException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Failed to load form").show();
        }
    }

    private void showFloatingLabel(StackPane container, String message, String bgColor, String textColor) {
        Label floatingLabel = new Label(message);
        floatingLabel.setStyle(
                "-fx-background-color: " + bgColor + ";" +
                "-fx-text-fill: " + textColor + ";" +
                "-fx-padding: 10 20;" +
                "-fx-background-radius: 5;" +
                "-fx-translate-y: 300; -fx-font-family: 'Gilroy-SemiBold'; -fx-font-size: 16px"
        );
        floatingLabel.getStyleClass().add("success-label");
        FadeTransition fadeIn = new FadeTransition(Duration.millis(300), floatingLabel);
        fadeIn.setFromValue(0.0);
        fadeIn.setToValue(1.0);
        container.getChildren().add(floatingLabel);
        fadeIn.play();
        javafx.animation.Timeline timeline = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(Duration.seconds(2), e -> {
                    FadeTransition fadeOut = new FadeTransition(Duration.millis(300), floatingLabel);
                    fadeOut.setFromValue(1.0);
                    fadeOut.setToValue(0.0);
                    fadeOut.setOnFinished(event -> container.getChildren().remove(floatingLabel));
                    fadeOut.play();
                })
        );
        timeline.play();
    }

    public void setAppointmentId(int appointmentId) {
        this.appointmentId = appointmentId;
    }

    public int getAppointmentId() {
        return this.appointmentId;
    }

    public void setPatientName(String name) {
        patientNameLabel.setText(name);
    }

    public int getPatientId() {
        return patientId;
    }

    public void setPatientId(int patientId) {
        this.patientId = patientId;
    }

    public int getDoctorId() {
        return doctorId;
    }

    public void setDoctorId(int doctorId) {
        this.doctorId = doctorId;
    }
}