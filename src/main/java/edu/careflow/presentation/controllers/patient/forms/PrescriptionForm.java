package edu.careflow.presentation.controllers.patient.forms;

import edu.careflow.repository.dao.MedicationDAO;
import edu.careflow.repository.entities.Prescription;
import edu.careflow.repository.entities.PrescriptionDetails;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.regex.Pattern;

public class PrescriptionForm {
    @FXML private ScrollPane scrollPane;
    @FXML private VBox rootVBox;
    @FXML private DatePicker issueDatePicker;
    @FXML private DatePicker validUntilPicker;
    @FXML private TableView<PrescriptionDetails> medicationsTable;
    @FXML private TableColumn<PrescriptionDetails, String> medicationNameCol;
    @FXML private TableColumn<PrescriptionDetails, String> dosageCol;
    @FXML private TableColumn<PrescriptionDetails, String> frequencyCol;
    @FXML private TableColumn<PrescriptionDetails, String> durationCol;
    @FXML private TableColumn<PrescriptionDetails, Void> removeCol;
    @FXML private Button addMedicationBtn;
    @FXML private Button saveBtn;
    @FXML private Button cancelBtn;
    @FXML private Button closeButton;
    @FXML private StackPane floatingMessagePane;

    private ObservableList<PrescriptionDetails> medications = FXCollections.observableArrayList();
    private int patientId;
    private int doctorId;
    private int appointmentId;
    private final MedicationDAO medicationDAO = new MedicationDAO();

    // Validation patterns
    private static final Pattern DOSAGE_PATTERN = Pattern.compile("^\\d+(\\.\\d+)?\\s*(mg|g|ml|L|IU|mcg)$");
    private static final Pattern FREQUENCY_PATTERN = Pattern.compile("^\\d+\\s*(times|x)\\s*(per|/)\\s*(day|week|month)$");
    private static final Pattern DURATION_PATTERN = Pattern.compile("^\\d+\\s*(day|week|month)s?$");

    private boolean editMode = false;
    private int editingPrescriptionId = -1;

    @FXML
    public void initialize() {
        setupTableColumns();
        setupEventHandlers();
        setupValidation();
    }

    private void setupTableColumns() {
        // Set up column value factories
        medicationNameCol.setCellValueFactory(new PropertyValueFactory<>("medicineName"));
        dosageCol.setCellValueFactory(new PropertyValueFactory<>("dosage"));
        frequencyCol.setCellValueFactory(new PropertyValueFactory<>("frequency"));
        durationCol.setCellValueFactory(new PropertyValueFactory<>("duration"));
        
        // Make columns editable
        medicationNameCol.setCellFactory(TextFieldTableCell.forTableColumn());
        dosageCol.setCellFactory(TextFieldTableCell.forTableColumn());
        frequencyCol.setCellFactory(TextFieldTableCell.forTableColumn());
        durationCol.setCellFactory(TextFieldTableCell.forTableColumn());
        
        // Set up edit commit handlers
        medicationNameCol.setOnEditCommit(e -> {
            PrescriptionDetails med = e.getRowValue();
            if (validateMedicationName(e.getNewValue())) {
                int index = medications.indexOf(med);
                medications.set(index, new PrescriptionDetails(
                    med.getDetailId(),
                    e.getNewValue(),
                    med.getDosage(),
                    med.getFrequency(),
                    med.getDuration()
                ));
                medicationsTable.refresh();
            } else {
                showFloatingMessage("Invalid medication name", false);
                medicationsTable.refresh();
            }
        });
        
        dosageCol.setOnEditCommit(e -> {
            PrescriptionDetails med = e.getRowValue();
            if (validateDosage(e.getNewValue())) {
                int index = medications.indexOf(med);
                medications.set(index, new PrescriptionDetails(
                    med.getDetailId(),
                    med.getMedicineName(),
                    e.getNewValue(),
                    med.getFrequency(),
                    med.getDuration()
                ));
                medicationsTable.refresh();
            } else {
                showFloatingMessage("Invalid dosage format (e.g., 500mg, 1g, 5ml)", false);
                medicationsTable.refresh();
            }
        });
        
        frequencyCol.setOnEditCommit(e -> {
            PrescriptionDetails med = e.getRowValue();
            if (validateFrequency(e.getNewValue())) {
                int index = medications.indexOf(med);
                medications.set(index, new PrescriptionDetails(
                    med.getDetailId(),
                    med.getMedicineName(),
                    med.getDosage(),
                    e.getNewValue(),
                    med.getDuration()
                ));
                medicationsTable.refresh();
            } else {
                showFloatingMessage("Invalid frequency format (e.g., 2 times per day)", false);
                medicationsTable.refresh();
            }
        });
        
        durationCol.setOnEditCommit(e -> {
            PrescriptionDetails med = e.getRowValue();
            if (validateDuration(e.getNewValue())) {
                int index = medications.indexOf(med);
                medications.set(index, new PrescriptionDetails(
                    med.getDetailId(),
                    med.getMedicineName(),
                    med.getDosage(),
                    med.getFrequency(),
                    e.getNewValue()
                ));
                medicationsTable.refresh();
            } else {
                showFloatingMessage("Invalid duration format (e.g., 7 days, 2 weeks)", false);
                medicationsTable.refresh();
            }
        });

        // Set up remove button column
        removeCol.setCellFactory(param -> new TableCell<>() {
            private final Button removeButton = new Button("Remove");
            {
                removeButton.getStyleClass().add("icon-button");
                removeButton.setOnAction(e -> {
                    PrescriptionDetails med = getTableView().getItems().get(getIndex());
                    medications.remove(med);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(removeButton);
                }
            }
        });
        
        // Set table properties
        medicationsTable.setEditable(true);
        medicationsTable.setItems(medications);
        
        // Set column widths
        medicationNameCol.setPrefWidth(200);
        dosageCol.setPrefWidth(100);
        frequencyCol.setPrefWidth(150);
        durationCol.setPrefWidth(100);
        removeCol.setPrefWidth(80);
    }

    private void setupEventHandlers() {
        addMedicationBtn.setOnAction(e -> handleAddMedication());
        saveBtn.setOnAction(e -> {
            try {
                handleSave();
            } catch (SQLException ex) {
                showFloatingMessage("Error saving prescription: " + ex.getMessage(), false);
            }
        });
        cancelBtn.setOnAction(e -> handleCancel());
        closeButton.setOnAction(e -> handleCancel());
    }

    private void setupValidation() {
        // Set default dates
        issueDatePicker.setValue(LocalDate.now());
        validUntilPicker.setValue(LocalDate.now().plusMonths(1));
        
        // Add date validation
        validUntilPicker.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && issueDatePicker.getValue() != null && newVal.isBefore(issueDatePicker.getValue())) {
                showFloatingMessage("Valid until date must be after issue date", false);
                validUntilPicker.setValue(oldVal);
            }
        });
    }

    private boolean validateMedicationName(String name) {
        return name != null && !name.trim().isEmpty() && name.length() <= 100;
    }

    private boolean validateDosage(String dosage) {
        return dosage != null && DOSAGE_PATTERN.matcher(dosage.trim()).matches();
    }

    private boolean validateFrequency(String frequency) {
        return frequency != null && FREQUENCY_PATTERN.matcher(frequency.trim()).matches();
    }

    private boolean validateDuration(String duration) {
        return duration != null && DURATION_PATTERN.matcher(duration.trim()).matches();
    }

    public void setIds(int patientId, int doctorId, int appointmentId) {
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.appointmentId = appointmentId;
    }

    private void handleAddMedication() {
        PrescriptionDetails newMed = new PrescriptionDetails(0, "New Medication", "500mg", "1 time per day", "7 days");
        medications.add(newMed);
        medicationsTable.getSelectionModel().select(newMed);
        medicationsTable.edit(medicationsTable.getItems().size() - 1, medicationNameCol);
    }

    private void handleSave() throws SQLException {
        if (!validateForm()) {
            return;
        }

        try {
            int prescriptionId = medicationDAO.generateUniquePrescriptionId();
            Prescription prescription = new Prescription(
                prescriptionId,
                patientId,
                doctorId,
                issueDatePicker.getValue(),
                validUntilPicker.getValue(),
                LocalDateTime.now(),
                appointmentId
            );

            boolean success = medicationDAO.savePrescription(prescription, medications);
            if (success) {
                showFloatingMessage("Prescription saved successfully", true);
                handleClose();
            } else {
                showFloatingMessage("Failed to save prescription", false);
            }
        } catch (SQLException e) {
            showFloatingMessage("Error saving prescription: " + e.getMessage(), false);
            throw e;
        }
    }

    private void handleClose() {
        FadeTransition fadeOut = new FadeTransition(javafx.util.Duration.millis(150), scrollPane);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> {
            if (scrollPane.getParent() instanceof VBox parentBox) {
                parentBox.getChildren().remove(scrollPane);
            } else if (scrollPane.getParent() != null) {
                ((javafx.scene.layout.Pane) scrollPane.getParent()).getChildren().remove(scrollPane);
            }
        });
        fadeOut.play();
    }

    private boolean validateForm() {
        if (medications.isEmpty()) {
            showFloatingMessage("Please add at least one medication", false);
            return false;
        }

        if (issueDatePicker.getValue() == null) {
            showFloatingMessage("Please select an issue date", false);
            return false;
        }

        if (validUntilPicker.getValue() == null) {
            showFloatingMessage("Please select a valid until date", false);
            return false;
        }

        if (validUntilPicker.getValue().isBefore(issueDatePicker.getValue())) {
            showFloatingMessage("Valid until date must be after issue date", false);
            return false;
        }

        // Validate all medications
        for (PrescriptionDetails med : medications) {
            if (!validateMedicationName(med.getMedicineName())) {
                showFloatingMessage("Invalid medication name", false);
                return false;
            }
            if (!validateDosage(med.getDosage())) {
                showFloatingMessage("Invalid dosage format", false);
                return false;
            }
            if (!validateFrequency(med.getFrequency())) {
                showFloatingMessage("Invalid frequency format", false);
                return false;
            }
            if (!validateDuration(med.getDuration())) {
                showFloatingMessage("Invalid duration format", false);
                return false;
            }
        }

        return true;
    }

    private void handleCancel() {
        handleClose();
    }

    private void showFloatingMessage(String message, boolean isSuccess) {
        Scene scene = saveBtn.getParent().getScene();
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

                // Add fade-in animation
                FadeTransition fadeIn = new FadeTransition(Duration.millis(300), messageLabel);
                fadeIn.setFromValue(0.0);
                fadeIn.setToValue(1.0);

                container.getChildren().add(messageLabel);
                fadeIn.play();

                // Remove label after 2 seconds
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

    /**
     * Prefill the form for editing an existing prescription
     */
    public void prefillForEdit(Prescription prescription, PrescriptionDetails details) {
        this.editMode = true;
        this.editingPrescriptionId = prescription.getPrescriptionId();
        issueDatePicker.setValue(prescription.getIssueDate());
        validUntilPicker.setValue(prescription.getValidUntil());
        // Clear and add the details (single or multiple)
        medications.clear();
        if (details != null) {
            medications.add(details);
        }
        // Optionally, update UI (e.g., change save button text)
        saveBtn.setText("Update");
    }
}
