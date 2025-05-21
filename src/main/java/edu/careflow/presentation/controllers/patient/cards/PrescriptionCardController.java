package edu.careflow.presentation.controllers.patient.cards;

import edu.careflow.presentation.controllers.patient.forms.PrescriptionForm;
import edu.careflow.repository.dao.PrescriptionDAO;
import edu.careflow.repository.entities.Prescription;
import edu.careflow.repository.entities.PrescriptionDetails;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.sql.SQLException;
import java.time.format.DateTimeFormatter;

public class PrescriptionCardController {


    @FXML
    private MenuButton actionsMenuBtn;

    @FXML
    private Label dateLabel;

    @FXML
    private MenuItem deleteMenuItem;

    @FXML
    private Label dosageLabel;

    @FXML
    private Label durationLabel;

    @FXML
    private MenuItem editMenuItem;

    @FXML
    private Label frequencyLabel;

    @FXML
    private HBox prescriptionCardContainer;

    @FXML
    private Label prescriptionNameLabel;


    private final PrescriptionDAO prescriptionDAO = new PrescriptionDAO();
    private Prescription prescription;
    private PrescriptionDetails details;
    private Runnable onRefreshCallback;

    public void initializeData(Prescription prescription, PrescriptionDetails details) {
        this.prescription = prescription;
        this.details = details;
        // Set up labels
        dateLabel.setText(prescription.getIssueDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        if (details != null) {
            prescriptionNameLabel.setText(details.getMedicineName());
            dosageLabel.setText(details.getDosage() != null ? details.getDosage() : "N/A");
            frequencyLabel.setText(details.getFrequency() != null ? details.getFrequency() : "N/A");
            durationLabel.setText(details.getDuration() != null ? details.getDuration() : "N/A");
        } else {
            try {
                details = prescriptionDAO.getPrescriptionDetails(prescription.getPrescriptionId());
                if (details != null) {
                    prescriptionNameLabel.setText(details.getMedicineName());
                    dosageLabel.setText(details.getDosage() != null ? details.getDosage() : "N/A");
                    frequencyLabel.setText(details.getFrequency() != null ? details.getFrequency() : "N/A");
                    durationLabel.setText(details.getDuration() != null ? details.getDuration() : "N/A");
                } else {
                    prescriptionNameLabel.setText("No details available");
                    dosageLabel.setText("N/A");
                    frequencyLabel.setText("N/A");
                    durationLabel.setText("N/A");
                }
            } catch (SQLException e) {
                e.printStackTrace();
                prescriptionNameLabel.setText("Error loading prescription details");
                dosageLabel.setText("N/A");
                frequencyLabel.setText("N/A");
                durationLabel.setText("N/A");
            }
        }
        setupMenuActions();
    }

    public void setOnRefreshCallback(Runnable callback) {
        this.onRefreshCallback = callback;
    }

    private void setupMenuActions() {
        editMenuItem.setOnAction(e -> handleEdit());
        deleteMenuItem.setOnAction(e -> handleDelete());
    }

    private void handleEdit() {
        try {

            Scene currentScene = durationLabel.getScene();

            // Look up the right container
            VBox rightBoxContainer = (VBox) currentScene.lookup("#rightBoxContainer");

            FXMLLoader loader = new FXMLLoader(getClass().getResource("/edu/careflow/fxml/components/patient/prescriptForm.fxml"));
            Parent formRoot = loader.load();
            PrescriptionForm formController = loader.getController();
            // Set IDs and pre-fill data
            formController.setIds(prescription.getPatientId(), prescription.getDoctorId(), prescription.getAppointmentId());
            // TODO: Add a method to PrescriptionForm to pre-fill for editing (not just new)
            // e.g., formController.prefillForEdit(prescription, details);

            formController.prefillForEdit(prescription, details);

            rightBoxContainer.getChildren().add(formRoot);
            // Show as modal dialog

            // After closing, refresh parent

        } catch (Exception ex) {
            showFloatingMessage("Error opening edit form: " + ex.getMessage(), false);
        }
    }

    private void handleDelete() {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Are you sure you want to delete this prescription?", ButtonType.YES, ButtonType.NO);
        alert.setHeaderText(null);
        alert.showAndWait().ifPresent(response -> {
            if (response == ButtonType.YES) {
                try {
                    boolean deleted = prescriptionDAO.deletePrescription(prescription.getPrescriptionId());
                    if (deleted) {
                        showFloatingMessage("Prescription deleted", true);
                        if (onRefreshCallback != null) onRefreshCallback.run();
                    } else {
                        showFloatingMessage("Failed to delete prescription", false);
                    }
                } catch (SQLException ex) {
                    showFloatingMessage("Error deleting prescription: " + ex.getMessage(), false);
                }
            }
        });
    }

    private void showFloatingMessage(String message, boolean isSuccess) {
        // Try to find a StackPane parent for floating message
        if (prescriptionCardContainer.getScene() != null) {
            StackPane container = (StackPane) prescriptionCardContainer.getScene().lookup("#stackPaneContainer");
            if (container != null) {
                Label messageLabel = new Label(message);
                messageLabel.getStyleClass().add(isSuccess ? "success-label" : "error-label");
                messageLabel.setStyle(
                    "-fx-background-color: " + (isSuccess ? "#4CAF50" : "#f44336") + ";" +
                    "-fx-text-fill: white;" +
                    "-fx-padding: 10 20;" +
                    "-fx-background-radius: 5;" +
                    "-fx-translate-y: 300;" +
                    "-fx-font-family: 'Gilroy-SemiBold';" +
                    "-fx-font-size: 16px"
                );
                container.getChildren().add(messageLabel);
                javafx.animation.FadeTransition fadeIn = new javafx.animation.FadeTransition(javafx.util.Duration.millis(300), messageLabel);
                fadeIn.setFromValue(0.0);
                fadeIn.setToValue(1.0);
                fadeIn.play();
                javafx.animation.Timeline timeline = new javafx.animation.Timeline(
                    new javafx.animation.KeyFrame(javafx.util.Duration.seconds(2), e -> {
                        javafx.animation.FadeTransition fadeOut = new javafx.animation.FadeTransition(javafx.util.Duration.millis(300), messageLabel);
                        fadeOut.setFromValue(1.0);
                        fadeOut.setToValue(0.0);
                        fadeOut.setOnFinished(event -> container.getChildren().remove(messageLabel));
                        fadeOut.play();
                    })
                );
                timeline.play();
            }
        }
    }
}
