package edu.careflow.presentation.controllers.patient.forms;

import edu.careflow.repository.dao.VisitNoteDAO;
import edu.careflow.repository.entities.VisitNote;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class VisitNoteForm {
    @FXML private ScrollPane scrollPaneVisitNote;
    @FXML private DatePicker visitDatePicker;
    @FXML private ComboBox<String> primaryDiagnosisCombo;
    @FXML private ComboBox<String> secondaryDiagnosisCombo;
    @FXML private TextArea noteField;
    @FXML private Button addImageBtn;
    @FXML private Button discardBtn;
    @FXML private Button saveBtn;
    @FXML private Button closeButton;
    @FXML private Label diagnosisInfoLabel;

    private int patientId;
    private int doctorId;
    private int appointmentId;
    private final VisitNoteDAO visitNoteDAO = new VisitNoteDAO();
    private VisitNote currentNote;

    private static final List<String> COMMON_DIAGNOSES = List.of(
        "Hypertension", "Diabetes Type 2", "Asthma", "Arthritis",
        "Migraine", "Anxiety", "Depression", "Common Cold",
        "Influenza", "Gastroenteritis", "Urinary Tract Infection",
        "Sinusitis", "Bronchitis", "Pneumonia", "Ear Infection"
    );

    @FXML
    private void initialize() {
        setupComboBoxes();
        setupEventHandlers();
        setupValidation();
    }

    private void setupComboBoxes() {
        primaryDiagnosisCombo.getItems().addAll(COMMON_DIAGNOSES);
        secondaryDiagnosisCombo.getItems().addAll(COMMON_DIAGNOSES);

        primaryDiagnosisCombo.setEditable(true);
        secondaryDiagnosisCombo.setEditable(true);

        primaryDiagnosisCombo.valueProperty().addListener((obs, oldVal, newVal) -> updateDiagnosisInfo());
        secondaryDiagnosisCombo.valueProperty().addListener((obs, oldVal, newVal) -> updateDiagnosisInfo());
    }

    private void updateDiagnosisInfo() {
        String primary = primaryDiagnosisCombo.getValue();
        String secondary = secondaryDiagnosisCombo.getValue();
        
        if (primary != null && !primary.isEmpty()) {
            diagnosisInfoLabel.setText("Primary: " + primary + 
                (secondary != null && !secondary.isEmpty() ? " | Secondary: " + secondary : ""));
        } else {
            diagnosisInfoLabel.setText("No diagnosis selected — Enter a diagnosis below");
        }
    }

    private void setupEventHandlers() {
        closeButton.setOnAction(e -> handleClose());
        discardBtn.setOnAction(e -> handleClose());
        saveBtn.setOnAction(e -> handleSave());
        addImageBtn.setOnAction(e -> handleAddImage());
    }

    private void setupValidation() {
        visitDatePicker.setValue(LocalDate.now());
        
        visitDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && newVal.isAfter(LocalDate.now())) {
                showFloatingMessage("Visit date cannot be in the future", false);
                visitDatePicker.setValue(oldVal);
            }
        });
    }

    private void handleAddImage() {
        // TODO: Implement image capture/upload functionality
        showFloatingMessage("Image upload functionality coming soon", false);
    }

    private void handleSave() {
        if (!validateForm()) {
            return;
        }

        try {
            LocalDateTime now = LocalDateTime.now();
            VisitNote visitNote = new VisitNote(
                currentNote != null ? currentNote.getVisitNoteId() : 0,
                patientId,
                doctorId,
                visitDatePicker.getValue(),
                primaryDiagnosisCombo.getValue(),
                secondaryDiagnosisCombo.getValue(),
                noteField.getText(),
                null, // Image data will be added later
                currentNote != null ? currentNote.getCreatedAt() : now,
                now,
                "Final",
                appointmentId
            );

            boolean success;
            if (currentNote != null) {
                success = visitNoteDAO.updateVisitNote(visitNote);
            } else {
                success = visitNoteDAO.saveVisitNote(visitNote);
            }

            if (success) {
                showFloatingMessage("Visit note saved successfully", true);
                handleClose();
            } else {
                showFloatingMessage("Failed to save visit note", false);
            }
        } catch (SQLException e) {
            showFloatingMessage("Error saving visit note: " + e.getMessage(), false);
        }
    }

    private boolean validateForm() {
        if (visitDatePicker.getValue() == null) {
            showFloatingMessage("Please select a visit date", false);
            return false;
        }

        if (primaryDiagnosisCombo.getValue() == null || primaryDiagnosisCombo.getValue().trim().isEmpty()) {
            showFloatingMessage("Please enter a primary diagnosis", false);
            return false;
        }

        if (noteField.getText().trim().isEmpty()) {
            showFloatingMessage("Please enter some notes", false);
            return false;
        }

        return true;
    }

    public void handleClose() {
        FadeTransition fadeOut = new FadeTransition(Duration.millis(150), scrollPaneVisitNote);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> {
            if (scrollPaneVisitNote.getParent() instanceof VBox parentBox) {
                parentBox.getChildren().remove(scrollPaneVisitNote);
            } else if (scrollPaneVisitNote.getParent() != null) {
                ((javafx.scene.layout.Pane) scrollPaneVisitNote.getParent()).getChildren().remove(scrollPaneVisitNote);
            }
            scrollPaneVisitNote = null;
        });
        fadeOut.play();
    }

    private void showFloatingMessage(String message, boolean isSuccess) {
        Scene scene = scrollPaneVisitNote.getScene();
        if (scene != null) {
            StackPane container = (StackPane) scene.lookup("#stackPaneContainer");
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

                FadeTransition fadeIn = new FadeTransition(Duration.millis(300), messageLabel);
                fadeIn.setFromValue(0.0);
                fadeIn.setToValue(1.0);

                container.getChildren().add(messageLabel);
                fadeIn.play();

                Timeline timeline = new Timeline(
                    new KeyFrame(Duration.seconds(2), e -> {
                        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), messageLabel);
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

    public void setIds(int patientId, int doctorId, int appointmentId) {
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.appointmentId = appointmentId;
    }

    public void loadExistingNote(VisitNote note) {
        this.currentNote = note;
        visitDatePicker.setValue(note.getVisitDate());
        primaryDiagnosisCombo.setValue(note.getPrimaryDiagnosis());
        secondaryDiagnosisCombo.setValue(note.getSecondaryDiagnosis());
        noteField.setText(note.getNotes());
    }
} 