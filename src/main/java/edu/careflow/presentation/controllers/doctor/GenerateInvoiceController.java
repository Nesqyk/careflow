package edu.careflow.presentation.controllers.doctor;

import edu.careflow.repository.dao.*;
import edu.careflow.repository.entities.*;
import javafx.animation.FadeTransition;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GenerateInvoiceController {
    @FXML private Label patientNameLabel;
    @FXML private Label appointmentDateLabel;
    @FXML private ComboBox<BillRate> serviceRateComboBox;
    @FXML private ListView<BillRate> selectedServicesListView;
    @FXML private Button addServiceButton;
    @FXML private DatePicker invoiceDatePicker;
    @FXML private ComboBox<String> statusComboBox;
    @FXML private ComboBox<String> paymentMethodComboBox;
    @FXML private TextArea consultationNotesField;
    @FXML private Label subtotalLabel;
    @FXML private Label taxLabel;
    @FXML private Label totalAmountLabel;
    @FXML private Label errorLabel;
    @FXML private Button closeBtn;
    @FXML private Button cancelButton;
    @FXML private Button generateButton;
    @FXML private Label formTitleLabel;
    @FXML private ComboBox<Appointment> appointmentComboBox;
    @FXML private ComboBox<Integer> doctorComboBox;
    @FXML private VBox appointmentSelectionBox;
    @FXML private VBox doctorSelectionBox;

    private final AppointmentDAO appointmentDAO = new AppointmentDAO();
    private final PatientDAO patientDAO = new PatientDAO();
    private final BillRateDAO billRateDAO = new BillRateDAO();
    private final BillingDAO billingDAO = new BillingDAO();
    private final DoctorDAO doctorDAO = new DoctorDAO();
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    private Integer appointmentId;
    private Integer patientId;
    private Integer doctorId;
    private Billing editingBilling;
    private Runnable onSuccessCallback;
    private boolean isEditMode = false;
    private ObservableList<BillRate> selectedServices = FXCollections.observableArrayList();
    private boolean isDirectInvoice = false;

    @FXML
    public void initialize() {
        setupComboBoxes();
        setupEventHandlers();
        setupValidation();
        setupListView();
        
        // Initially hide appointment and doctor selection boxes
        if (appointmentSelectionBox != null) {
            appointmentSelectionBox.setVisible(false);
        }
        if (doctorSelectionBox != null) {
            doctorSelectionBox.setVisible(false);
        }
    }

    private void setupListView() {
        selectedServicesListView.setItems(selectedServices);
        selectedServicesListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(BillRate item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    HBox container = new HBox(8);
                    Label serviceLabel = new Label(item.getServiceType() + " - ₱" + item.getRateAmount().setScale(2, RoundingMode.HALF_UP));
                    Button removeButton = new Button();
                    FontIcon removeIcon = new FontIcon("fas-times");
                    removeIcon.setIconSize(14);
                    removeButton.setGraphic(removeIcon);
                    removeButton.getStyleClass().add("icon-button");
                    removeButton.setOnAction(e -> {
                        selectedServices.remove(item);
                        updateTotals();
                        updateDescription();
                    });
                    container.getChildren().addAll(serviceLabel, removeButton);
                    setGraphic(container);
                }
            }
        });
    }

    private void setupComboBoxes() {
        // Setup status options
        statusComboBox.getItems().addAll("UNPAID", "PAID", "OVERDUE");
        statusComboBox.setValue("UNPAID");

        // Setup payment method options
        paymentMethodComboBox.getItems().addAll("CASH", "CREDIT CARD", "BANK TRANSFER", "GCASH");
        paymentMethodComboBox.setValue("CASH");

        // Setup service rate combo box
        serviceRateComboBox.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(BillRate item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getServiceType() + " - ₱" + item.getRateAmount().setScale(2, RoundingMode.HALF_UP));
                }
            }
        });
        serviceRateComboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(BillRate item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getServiceType() + " - ₱" + item.getRateAmount().setScale(2, RoundingMode.HALF_UP));
                }
            }
        });
        
        // Setup appointment combo box
        if (appointmentComboBox != null) {
            appointmentComboBox.setCellFactory(param -> new ListCell<>() {
                @Override
                protected void updateItem(Appointment item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        setText("Appointment #" + item.getAppointmentId() + " - " + 
                               (item.getAppointmentDate() != null ? 
                                item.getAppointmentDate().format(dateFormatter) : "No date"));
                    }
                }
            });
            appointmentComboBox.setButtonCell(new ListCell<>() {
                @Override
                protected void updateItem(Appointment item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText("Select an appointment");
                    } else {
                        setText("Appointment #" + item.getAppointmentId() + " - " + 
                               (item.getAppointmentDate() != null ? 
                                item.getAppointmentDate().format(dateFormatter) : "No date"));
                    }
                }
            });
            
            // Add listener to appointment selection
            appointmentComboBox.setOnAction(e -> {
                Appointment selectedAppointment = appointmentComboBox.getValue();
                if (selectedAppointment != null) {
                    appointmentId = selectedAppointment.getAppointmentId();
                    doctorId = selectedAppointment.getDoctorId();
                    
                    // Load service rates for the selected doctor
                    try {
                        List<BillRate> rates = billRateDAO.getBillRatesByDoctorId(doctorId);
                        serviceRateComboBox.getItems().setAll(rates);
                    } catch (Exception ex) {
                        showError("Error loading service rates: " + ex.getMessage());
                    }
                }
            });
        }
        
        // Setup doctor combo box
        if (doctorComboBox != null) {
            doctorComboBox.setCellFactory(param -> new ListCell<>() {
                @Override
                protected void updateItem(Integer item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                    } else {
                        try {
                            // Get doctor name from database
                            String doctorName = "Doctor #" + item;
                            doctorComboBox.setValue(item);
                            setText(doctorName);
                        } catch (Exception e) {
                            setText("Doctor #" + item);
                        }
                    }
                }
            });
            doctorComboBox.setButtonCell(new ListCell<>() {
                @Override
                protected void updateItem(Integer item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText("Select a doctor");
                    } else {
                        setText("Doctor #" + item);
                    }
                }
            });
            
            // Add listener to doctor selection
            doctorComboBox.setOnAction(e -> {
                Integer selectedDoctorId = doctorComboBox.getValue();
                if (selectedDoctorId != null) {
                    doctorId = selectedDoctorId;
                    
                    // Load service rates for the selected doctor
                    try {
                        List<BillRate> rates = billRateDAO.getBillRatesByDoctorId(doctorId);
                        serviceRateComboBox.getItems().setAll(rates);
                    } catch (Exception ex) {
                        showError("Error loading service rates: " + ex.getMessage());
                    }
                }
            });
        }
    }

    private void setupEventHandlers() {
        // Close button handler
        closeBtn.setOnAction(e -> handleClose());

        // Cancel button handler
        cancelButton.setOnAction(e -> handleCancel());

        // Generate button handler
        generateButton.setOnAction(e -> handleSave());

        // Add service button handler
        addServiceButton.setOnAction(e -> {
            BillRate selectedRate = serviceRateComboBox.getValue();
            if (selectedRate != null && !selectedServices.contains(selectedRate)) {
                selectedServices.add(selectedRate);
                updateTotals();
                updateDescription();
            }
        });

        // Add listener to selectedServices list to update description when services change
        selectedServices.addListener((javafx.collections.ListChangeListener.Change<? extends BillRate> change) -> {
            while (change.next()) {
                if (change.wasAdded() || change.wasRemoved()) {
                    updateDescription();
                }
            }
        });
    }

    private void setupValidation() {
        // Add validation listeners
        selectedServices.addListener((javafx.collections.ListChangeListener.Change<? extends BillRate> change) -> validateForm());
        invoiceDatePicker.valueProperty().addListener((obs, oldVal, newVal) -> validateForm());
        statusComboBox.valueProperty().addListener((obs, oldVal, newVal) -> validateForm());
        paymentMethodComboBox.valueProperty().addListener((obs, oldVal, newVal) -> validateForm());
    }

    private void updateTotals() {
        BigDecimal subtotal = selectedServices.stream()
            .map(BillRate::getRateAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal tax = subtotal.multiply(new BigDecimal("0.12")); // 12% tax
        BigDecimal total = subtotal.add(tax);

        subtotalLabel.setText("₱" + subtotal.setScale(2, RoundingMode.HALF_UP));
        taxLabel.setText("₱" + tax.setScale(2, RoundingMode.HALF_UP));
        totalAmountLabel.setText("₱" + total.setScale(2, RoundingMode.HALF_UP));
    }

    private boolean validateForm() {
        boolean isValid = true;
        StringBuilder errorMessage = new StringBuilder();

        if (selectedServices.isEmpty()) {
            isValid = false;
            errorMessage.append("Please add at least one service.\n");
        }

        if (invoiceDatePicker.getValue() == null) {
            isValid = false;
            errorMessage.append("Please select an invoice date.\n");
        }

        if (statusComboBox.getValue() == null) {
            isValid = false;
            errorMessage.append("Please select a status.\n");
        }

        if (paymentMethodComboBox.getValue() == null) {
            isValid = false;
            errorMessage.append("Please select a payment method.\n");
        }

        errorLabel.setText(errorMessage.toString());
        errorLabel.setVisible(!isValid);
        generateButton.setDisable(!isValid);

        return isValid;
    }

    private void handleSave() {
        if (!validateForm()) {
            return;
        }

        try {
            Billing billing = isEditMode ? editingBilling : new Billing();
            
            // Set basic information
            billing.setPatientId(patientId != null ? patientId : 0);
            billing.setDoctorId(doctorId != null ? doctorId : 0);
            billing.setAppointmentId(appointmentId);
            
            // Calculate totals
            BigDecimal subtotal = selectedServices.stream()
                .map(BillRate::getRateAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal taxAmount = subtotal.multiply(new BigDecimal("0.12")); // 12% tax
            BigDecimal total = subtotal.add(taxAmount);

            billing.setSubtotal(subtotal);
            billing.setTaxAmount(taxAmount);
            billing.setAmount(total);

            billing.setSubtotal(subtotal);
            billing.setTaxAmount(taxAmount);
            billing.setAmount(total);
            billing.setStatus(statusComboBox.getValue());
            billing.setPaymentMethod(paymentMethodComboBox.getValue());
            billing.setDescription(consultationNotesField.getText());
            
            // Set dates
            LocalDateTime invoiceDate = invoiceDatePicker.getValue().atStartOfDay();
            billing.setBillingDate(invoiceDate);
            billing.setDueDate(invoiceDate.plusDays(30)); // Due in 30 days
            
            if ("PAID".equals(statusComboBox.getValue())) {
                billing.setPaidDate(LocalDateTime.now());
            }

            // Save to database
            if (isEditMode) {
                billingDAO.updateBilling(billing);
            } else {
                int newBillingId = billingDAO.addBilling(billing);
                billing.setBillingId(newBillingId); // Update the billing ID with the newly generated one
            }

            // Save selected services
            for (BillRate service : selectedServices) {
                billingDAO.addBillingService(billing.getBillingId(), service.getRateId());
            }

            // Show success message
            showSuccessMessage(isEditMode ? "Invoice updated successfully" : "Invoice generated successfully");

            // Call success callback if set
            if (onSuccessCallback != null) {
                onSuccessCallback.run();
            }

            // Clear the right container

            handleClose();


        } catch (Exception e) {
            showError("Error saving invoice: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void setAppointmentId(Integer appointmentId) {
        this.appointmentId = appointmentId;
        this.isEditMode = false;
        formTitleLabel.setText("Generate New Invoice");
        loadAppointmentData();
    }

    public void setPatientId(Integer patientId) {
        this.patientId = patientId;
        this.appointmentId = null;
        this.isEditMode = false;
        this.isDirectInvoice = true;
        formTitleLabel.setText("Generate New Invoice");
        
        try {
            // Load patient data
            Patient patient = patientDAO.getPatientById(patientId);
            if (patient != null) {
                patientNameLabel.setText(patient.getFirstName() + " " + patient.getLastName());
                appointmentDateLabel.setText("Direct Invoice");
                
                // Set default invoice date to today
                invoiceDatePicker.setValue(LocalDate.now());
                
                // Show appointment and doctor selection boxes
                if (appointmentSelectionBox != null) {
                    appointmentSelectionBox.setVisible(true);
                }
                if (doctorSelectionBox != null) {
                    doctorSelectionBox.setVisible(true);
                }
                
                // Load appointments for this patient
                if (appointmentComboBox != null) {
                    List<Appointment> appointments = appointmentDAO.getAppointmentsByPatientId(patientId);
                    appointmentComboBox.getItems().setAll(appointments);
                }
                
                // Load all doctors from the database
                if (doctorComboBox != null) {
                    List<Doctor> doctors = doctorDAO.getAllDoctors();
                    List<Integer> doctorIds = new ArrayList<>();
                    Map<Integer, String> doctorNames = new HashMap<>();
                    
                    for (Doctor doctor : doctors) {
                        doctorIds.add(doctor.getDoctorId());
                        doctorNames.put(doctor.getDoctorId(), 
                            "Dr. " + doctor.getFirstName() + " " + doctor.getLastName() + " - " + doctor.getSpecialization());
                    }
                    
                    doctorComboBox.getItems().setAll(doctorIds);
                    doctorComboBox.setCellFactory(param -> new ListCell<Integer>() {
                        @Override
                        protected void updateItem(Integer doctorId, boolean empty) {
                            super.updateItem(doctorId, empty);
                            if (empty || doctorId == null) {
                                setText(null);
                            } else {
                                setText(doctorNames.get(doctorId));
                            }
                        }
                    });
                    doctorComboBox.setButtonCell(new ListCell<Integer>() {
                        @Override
                        protected void updateItem(Integer doctorId, boolean empty) {
                            super.updateItem(doctorId, empty);
                            if (empty || doctorId == null) {
                                setText("Select a doctor");
                            } else {
                                setText(doctorNames.get(doctorId));
                            }
                        }
                    });
                }
                
                // Load all service rates
                List<BillRate> rates = billRateDAO.getAllBillRates();
                serviceRateComboBox.getItems().setAll(rates);
            }
        } catch (Exception e) {
            showError("Error loading patient data: " + e.getMessage());
        }
    }

    public void setEditMode(Billing billing) {
        this.editingBilling = billing;
        this.appointmentId = billing.getAppointmentId();
        this.isEditMode = true;
        formTitleLabel.setText("Edit Invoice");
        loadAppointmentData();
        populateFormWithBillingData();
    }

    public void setOnSuccessCallback(Runnable callback) {
        this.onSuccessCallback = callback;
    }

    private void loadAppointmentData() {
        try {
            if (appointmentId != null) {
                Appointment appointment = appointmentDAO.getAppointmentById(appointmentId);
                if (appointment != null) {
                    this.patientId = appointment.getPatientId();
                    this.doctorId = appointment.getDoctorId();

                    // Load patient data
                    Patient patient = patientDAO.getPatientById(patientId);
                    if (patient != null) {
                        patientNameLabel.setText(patient.getFirstName() + " " + patient.getLastName());
                    }

                    // Set appointment date
                    if (appointment.getAppointmentDate() != null) {
                        appointmentDateLabel.setText(appointment.getAppointmentDate().format(dateFormatter));
                    }

                    // Load service rates for the doctor
                    List<BillRate> rates = billRateDAO.getBillRatesByDoctorId(doctorId);
                    serviceRateComboBox.getItems().setAll(rates);

                    // Set default invoice date to today
                    invoiceDatePicker.setValue(LocalDate.now());
                }
            }
        } catch (Exception e) {
            showError("Error loading appointment data: " + e.getMessage());
        }
    }

    private void populateFormWithBillingData() {
        if (editingBilling != null) {
            // Set service rates
            for (BillRate service : editingBilling.getServices()) {
                selectedServices.add(service);
            }

            // Set other fields
            if (editingBilling.getBillingDate() != null) {
                invoiceDatePicker.setValue(editingBilling.getBillingDate().toLocalDate());
            }
            statusComboBox.setValue(editingBilling.getStatus());
            paymentMethodComboBox.setValue(editingBilling.getPaymentMethod());
            consultationNotesField.setText(editingBilling.getDescription());
        }
    }

    private void handleCancel() {
        // Clear the right container instead of closing stage
        VBox rightBoxContainer = (VBox) cancelButton.getScene().lookup("#rightBoxContainer");
        if (rightBoxContainer != null) {
            rightBoxContainer.getChildren().clear();
        }
    }

    @FXML
    private ScrollPane scrollPaneInvoice;

    private void handleClose() {
        // Clear the right container instead of closing stage
        FadeTransition fadeOut = new FadeTransition(Duration.millis(150), scrollPaneInvoice);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> {
            if (scrollPaneInvoice.getParent() instanceof VBox parentBox) {
                parentBox.getChildren().remove(scrollPaneInvoice);
            } else if (scrollPaneInvoice.getParent() != null) {
                ((Pane) scrollPaneInvoice.getParent()).getChildren().remove(scrollPaneInvoice);
            }
            scrollPaneInvoice = null;
        });
        fadeOut.play();
    }

    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
    }

    private void showSuccessMessage(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void updateDescription() {
        if (selectedServices.isEmpty()) {
            consultationNotesField.setText("");
            return;
        }

        StringBuilder description = new StringBuilder();
        for (BillRate service : selectedServices) {
            if (description.length() > 0) {
                description.append(", ");
            }
            description.append(service.getServiceType());
        }
        consultationNotesField.setText(description.toString());
    }
} 