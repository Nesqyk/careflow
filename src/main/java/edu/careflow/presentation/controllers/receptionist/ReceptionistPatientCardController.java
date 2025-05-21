package edu.careflow.presentation.controllers.receptionist;

import com.dlsc.gemsfx.AvatarView;
import edu.careflow.presentation.controllers.patient.forms.AddPatientForm;
import edu.careflow.repository.dao.PatientDAO;
import edu.careflow.repository.entities.Patient;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;

import java.sql.SQLException;

public class ReceptionistPatientCardController {
    @FXML public AvatarView patientAvatar;
    @FXML public Label patientNameLabel;
    @FXML public Label patientIdLabel;
    @FXML public Label lastVisitDateLabel;
    @FXML public Label visitCountLabel;
    @FXML public Button bookAptBtn;
    @FXML public Button editBtn;
    @FXML public Button deleteBtn;
    @FXML public Button addInvoiceBtn;

    private Patient currentPatient;
    private Runnable onBookAppointment;
    private Runnable onEditPatient;
    private Runnable onDeletePatient;
    private Runnable onViewHistory;
    private Runnable onAddInvoice;
    private final PatientDAO patientDAO = new PatientDAO();

    public void initializeData(Patient patient, Runnable onBookAppointment, Runnable onEditPatient, 
                             Runnable onDeletePatient, Runnable onViewHistory, Runnable onAddInvoice) {
        this.currentPatient = patient;
        this.onBookAppointment = onBookAppointment;
        this.onEditPatient = onEditPatient;
        this.onDeletePatient = onDeletePatient;
        this.onViewHistory = onViewHistory;
        this.onAddInvoice = onAddInvoice;

        // Set up button actions
        bookAptBtn.setOnAction(e -> handleBookAppointment());
        editBtn.setOnAction(e -> handleEdit());
        deleteBtn.setOnAction(e -> handleDelete());
        if (addInvoiceBtn != null) {
            addInvoiceBtn.setOnAction(e -> handleAddInvoice());
        }
    }

    @FXML
    private void handleBookAppointment() {
        if (onBookAppointment != null) {
            onBookAppointment.run();
        }
    }

    @FXML
    private void handleEdit() {
        try {
            // Load the patient form
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/edu/careflow/fxml/components/user/addPatientForm.fxml"));
            Parent settingsForm = loader.load();
            AddPatientForm controller = loader.getController();

            // Set the form fields with current patient data
            controller.setPatientId(currentPatient.getPatientId());
            controller.setFirstName(currentPatient.getFirstName());
            controller.setLastName(currentPatient.getLastName());
            controller.setDateOfBirth(currentPatient.getDateOfBirth());
            controller.setGender(currentPatient.getGender());
            controller.setContact(currentPatient.getContact());
            controller.setEmail(currentPatient.getEmail());
            controller.setAddress(currentPatient.getAddress());

            // Load and set the avatar
            try {
                Image avatar = patientDAO.loadAvatar(currentPatient.getPatientId());
                if (avatar != null) {
                    ImageView avatarImageView = (ImageView) settingsForm.lookup("#avatarImageView");
                    if (avatarImageView != null) {
                        avatarImageView.setImage(avatar);
                        FontIcon defaultAvatarIcon = (FontIcon) settingsForm.lookup("#defaultAvatarIcon");
                        if (defaultAvatarIcon != null) {
                            defaultAvatarIcon.setVisible(false);
                        }
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            // Set up success callback to refresh the patient list
            controller.setOnSuccessCallback(() -> {
                if (onEditPatient != null) {
                    onEditPatient.run();
                }
            });

            // Get the right container
            VBox rightBoxContainer = (VBox) editBtn.getScene().lookup("#rightBoxContainer");
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
        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Failed to load edit form").show();
        }
    }

    @FXML
    private void handleDelete() {
        if (onDeletePatient != null) {
            onDeletePatient.run();
        }
    }

    @FXML
    private void handleHistory() {
        if (onViewHistory != null) {
            onViewHistory.run();
        }
    }

    @FXML
    private void handleAddInvoice() {
        try {
            // Load the invoice form
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/edu/careflow/fxml/forms/generate-invoice-form.fxml"));
            Parent invoiceForm = loader.load();
            edu.careflow.presentation.controllers.doctor.GenerateInvoiceController controller = loader.getController();
            
            // Set the patient ID directly for the new invoice
            controller.setPatientId(currentPatient.getPatientId());
            
            // Set up success callback to refresh the invoice list
            controller.setOnSuccessCallback(() -> {
                if (onAddInvoice != null) {
                    onAddInvoice.run();
                }
            });

            // Get the right container
            VBox rightBoxContainer = (VBox) addInvoiceBtn.getScene().lookup("#rightBoxContainer");
            if (rightBoxContainer != null) {
                // First fade out existing content if any
                if (!rightBoxContainer.getChildren().isEmpty()) {
                    FadeTransition fadeOut = new FadeTransition(Duration.millis(150), rightBoxContainer.getChildren().get(0));
                    fadeOut.setFromValue(1.0);
                    fadeOut.setToValue(0.0);
                    fadeOut.setOnFinished(e -> {
                        rightBoxContainer.getChildren().setAll(invoiceForm);

                        // Fade in new content with slide up effect
                        FadeTransition fadeIn = new FadeTransition(Duration.millis(200), invoiceForm);
                        fadeIn.setFromValue(0.0);
                        fadeIn.setToValue(1.0);

                        TranslateTransition slideIn = new TranslateTransition(Duration.millis(200), invoiceForm);
                        slideIn.setFromY(20);
                        slideIn.setToY(0);

                        ParallelTransition parallelIn = new ParallelTransition(fadeIn, slideIn);
                        parallelIn.play();
                    });
                    fadeOut.play();
                } else {
                    rightBoxContainer.getChildren().setAll(invoiceForm);

                    // Simple fade in for first load
                    FadeTransition fadeIn = new FadeTransition(Duration.millis(300), invoiceForm);
                    fadeIn.setFromValue(0.0);
                    fadeIn.setToValue(1.0);
                    fadeIn.play();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Failed to load invoice form").show();
        }
    }
} 