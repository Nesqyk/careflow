package edu.careflow.presentation.controllers.doctor.cards;

import edu.careflow.presentation.controllers.doctor.DoctorContainerController;
import edu.careflow.presentation.controllers.patient.forms.ConditionsForm;
import edu.careflow.presentation.controllers.patient.forms.PrescriptionForm;
import edu.careflow.presentation.controllers.patient.forms.RecordAllergyForm;
import edu.careflow.presentation.controllers.patient.forms.VitalsBioForm;
import edu.careflow.presentation.controllers.patient.forms.VisitNoteForm;
import edu.careflow.repository.dao.*;
import edu.careflow.repository.entities.*;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.awt.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private final VitalsDAO vitalsDAO;
    private final ConditionDAO conditionDAO;
    private final PrescriptionDAO prescriptionDAO;

    private Appointment currentAppointment;

    public NavigationBar() {
        this.vitalsDAO = new VitalsDAO();
        this.conditionDAO = new ConditionDAO();
        this.prescriptionDAO = new PrescriptionDAO();
    }

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
            MenuItem visitNoteItem = new MenuItem("Visit Note");


            this.currentAppointment = appointmentDAO.getAppointmentById(appointmentId);

            if(appointmentId < 0) {
                setNavigationButtonsState(true);
            }

            if (currentAppointment != null && ("Completed".equalsIgnoreCase(currentAppointment.getStatus())
                    || "Pending".equalsIgnoreCase(currentAppointment.getStatus())
                    || "Cancelled".equalsIgnoreCase(currentAppointment.getStatus()))) {
                setNavigationButtonsState(true);
            }

            // Check if appointment is completed and disable buttons if it is


            prescriptionItem.setOnAction(e -> handlePrescriptionForm());
            vitalsItem.setOnAction(e -> handleVitalsForm());
            allergyItem.setOnAction(e -> handleAllergyForm());
            conditionItem.setOnAction(e -> handleConditionForm());
            attachmentItem.setOnAction(e -> handleAttachmentForm());
            visitNoteItem.setOnAction(e -> handleVisitNoteForm());

            formBtn.getItems().addAll(
                    prescriptionItem,
                    vitalsItem,
                    allergyItem,
                    conditionItem,
                    attachmentItem,
                    visitNoteItem
            );

            // Add Join Meeting menu item if appointment is scheduled and online
            if (currentAppointment != null && "Scheduled".equalsIgnoreCase(currentAppointment.getStatus()) && "Online".equalsIgnoreCase(currentAppointment.getAppointmentType())) {
                formBtn.getItems().add(new SeparatorMenuItem());
                MenuItem joinMeetingItem = new MenuItem("Join Meeting");
                joinMeetingItem.setOnAction(e -> {
                    String url = currentAppointment.getMeetingLink();
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
                                        Desktop.getDesktop().browse(new URI(url));
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
                                currentAppointment.setAppointmentStatus("Completed");
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
                                            currentAppointment.setAppointmentNotes(notes);
                                            appointmentDAO.updateAppointment(currentAppointment);
                                            System.out.println(currentAppointment);
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

    /**
     * Disables or enables navigation buttons based on the current state
     * @param disable true to disable buttons, false to enable them
     */
    public void setNavigationButtonsState(boolean disable) {
        if (endVisitBtn != null) endVisitBtn.setDisable(disable);
        if (formBtn != null) formBtn.setDisable(disable);

        // Also disable all menu items in the form button
        if (formBtn != null) {
            for (MenuItem item : formBtn.getItems()) {
                if (!(item instanceof SeparatorMenuItem)) {
                    item.setDisable(disable);
                }
            }
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

    private void handleVisitNoteForm() {
        loadFormIntoRightContainer("/edu/careflow/fxml/components/patient/visitNoteForm.fxml");
    }

    // make method that looks up the rightBoxContainer (vbox) from root and sets the content depending on the parameter
    // and then loads the form


    // go back to doctor's dashboard
    private void handleBack() {
        try {
            // Load the doctor's container
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/edu/careflow/fxml/doctor/doctorContainerNew.fxml"));
            Parent dashboard = loader.load();
            DoctorContainerController containerController = loader.getController();

            Appointment appointment = appointmentDAO.getAppointmentById(appointmentId);

            int id = appointment == null ? getDoctorId() : appointment.getDoctorId();

            containerController.initializeData(id);

            // Get the current scene
            Scene currentScene = backBtn.getScene();
            if (currentScene != null) {
                // Fade out current content
                FadeTransition fadeOut = new FadeTransition(Duration.millis(200), currentScene.getRoot());
                fadeOut.setFromValue(1.0);
                fadeOut.setToValue(0.0);
                fadeOut.setOnFinished(e -> {
                    // Set the new root with fade in animation
                    currentScene.setRoot(dashboard);
                    FadeTransition fadeIn = new FadeTransition(Duration.millis(200), dashboard);
                    fadeIn.setFromValue(0.0);
                    fadeIn.setToValue(1.0);
                    fadeIn.play();
                });
                fadeOut.play();
            }
        } catch (SQLException | IOException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Failed to load doctor's dashboard").show();
        }
    }


    // mark visit as ended in the database
    private void handleEndVisit() {
        if (currentAppointment == null) {
            showAlert("Error", "No appointment selected", Alert.AlertType.ERROR);
            return;
        }

        // Get the scene and stack pane container
        Scene scene = endVisitBtn.getScene();
        StackPane stackPaneContainer = (StackPane) scene.lookup("#stackPaneContainer");
        
        if (stackPaneContainer != null) {
            try {
                // Load the confirmation dialog
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/edu/careflow/fxml/components/doctor/endVisitConfirmation.fxml"));
                Parent confirmationDialog = loader.load();
                EndVisitConfirmationController controller = loader.getController();

                // Set up the controller with callbacks
                controller.setData(
                    stackPaneContainer,
                    () -> {
                        try {
                            // Get doctor's billing rates
                            BillRateDAO billRateDAO = new BillRateDAO();
                            List<BillRate> doctorRates = billRateDAO.getBillRatesByDoctorId(currentAppointment.getDoctorId());
                            
                            if (doctorRates.isEmpty()) {
                                showAlert("Error", "No billing rates found for this doctor", Alert.AlertType.ERROR);
                                return;
                            }

                            // Calculate total amount based on services provided
                            final BigDecimal[] totalAmount = {BigDecimal.ZERO};
                            StringBuilder serviceDescription = new StringBuilder("Services provided:\n");

                            // Check for procedures (vitals and physical examination)
                            if (hasVitalsRecord(appointmentId)) {
                                BillRate procedureRate = doctorRates.stream()
                                    .filter(rate -> "Vitals Assessment".equals(rate.getServiceType()) || 
                                                  "Complete Physical Examination".equals(rate.getServiceType()))
                                    .findFirst()
                                    .orElse(null);
                                if (procedureRate != null) {
                                    totalAmount[0] = totalAmount[0].add(procedureRate.getRateAmount());
                                    serviceDescription.append("- ").append(procedureRate.getServiceType()).append(": ₱")
                                        .append(procedureRate.getRateAmount()).append("\n");
                                }
                            }

                            // Check for diagnostics (allergies and conditions)
                            if (hasAllergyRecord(appointmentId) || hasConditionRecord(appointmentId)) {
                                BillRate diagnosticRate = doctorRates.stream()
                                    .filter(rate -> "Comprehensive Health Check".equals(rate.getServiceType()))
                                    .findFirst()
                                    .orElse(null);
                                if (diagnosticRate != null) {
                                    totalAmount[0] = totalAmount[0].add(diagnosticRate.getRateAmount());
                                    serviceDescription.append("- ").append(diagnosticRate.getServiceType()).append(": ₱")
                                        .append(diagnosticRate.getRateAmount()).append("\n");
                                }
                            }

                            // Check for prescription
                            if (hasPrescriptionRecord(appointmentId)) {
                                BillRate prescriptionRate = doctorRates.stream()
                                    .filter(rate -> "Prescription".equals(rate.getServiceType()))
                                    .findFirst()
                                    .orElse(null);
                                if (prescriptionRate != null) {
                                    totalAmount[0] = totalAmount[0].add(prescriptionRate.getRateAmount());
                                    serviceDescription.append("- ").append(prescriptionRate.getServiceType()).append(": ₱")
                                        .append(prescriptionRate.getRateAmount()).append("\n");
                                }
                            }

                            // Add consultation fee based on appointment type
                            final String consultationType;
                            if (currentAppointment.getAppointmentType().equalsIgnoreCase("Online")) {
                                consultationType = "Telemedicine Consultation";
                            } else if (currentAppointment.getAppointmentType().equalsIgnoreCase("Emergency")) {
                                consultationType = "Emergency Consultation";
                            } else {
                                consultationType = "Initial Consultation";
                            }

                            BillRate consultationRate = doctorRates.stream()
                                .filter(rate -> consultationType.equals(rate.getServiceType()))
                                .findFirst()
                                .orElse(doctorRates.stream()
                                    .filter(rate -> "Initial Consultation".equals(rate.getServiceType()))
                                    .findFirst()
                                    .orElse(null));
                            
                            if (consultationRate != null) {
                                totalAmount[0] = totalAmount[0].add(consultationRate.getRateAmount());
                                serviceDescription.append("- ").append(consultationRate.getServiceType()).append(": ₱")
                                    .append(consultationRate.getRateAmount()).append("\n");
                            }

                            // If no services were provided, add a minimum consultation fee
                            if (totalAmount[0].compareTo(BigDecimal.ZERO) == 0) {
                                BillRate defaultRate = doctorRates.stream()
                                    .filter(rate -> "Initial Consultation".equals(rate.getServiceType()))
                                    .findFirst()
                                    .orElse(doctorRates.get(0));
                                totalAmount[0] = defaultRate.getRateAmount();
                                serviceDescription.append("- Basic Consultation: ₱").append(defaultRate.getRateAmount()).append("\n");
                            }

                            // Calculate tax (8% tax rate)
                            BigDecimal taxRate = new BigDecimal("0.08");
                            BigDecimal subtotal = totalAmount[0];
                            BigDecimal taxAmount = subtotal.multiply(taxRate).setScale(2, java.math.RoundingMode.HALF_UP);
                            BigDecimal totalWithTax = subtotal.add(taxAmount);

                            // Add tax information to service description
                            serviceDescription.append("\nSubtotal: ₱").append(subtotal).append("\n");
                            serviceDescription.append("Tax (8%): ₱").append(taxAmount).append("\n");
                            serviceDescription.append("Total: ₱").append(totalWithTax).append("\n");

                            // Show bill overview
                            FXMLLoader billLoader = new FXMLLoader(getClass().getResource("/edu/careflow/fxml/components/doctor/billOverview.fxml"));
                            Parent billOverview = billLoader.load();
                            BillOverviewController billController = billLoader.getController();

                            // Set up the bill controller with data and callbacks
                            billController.setData(
                                currentAppointment,
                                doctorRates,
                                totalWithTax,
                                serviceDescription.toString(),
                                billing -> {
                                    try {
                                        // Set both subtotal and total amount in the billing record
                                        billing.setSubtotal(subtotal);
                                        billing.setTaxAmount(taxAmount);
                                        billing.setAmount(totalWithTax);
                                        
                                        // Save billing record
                                        BillingDAO billingDAO = new BillingDAO();
                                        int newBillingId = billingDAO.addBilling(billing);
                                        
                                        // Set the billing ID in the controller for future updates
                                        billController.setBillingId(newBillingId);

                                        // Update appointment status
                                        currentAppointment.setStatus("Completed");
                                        appointmentDAO.updateAppointment(currentAppointment);

                                        showAlert("Success", "Visit ended and bill generated successfully.", Alert.AlertType.INFORMATION);
                                        stackPaneContainer.getChildren().remove(billOverview);
                                        closeForm();
                                    } catch (SQLException e) {
                                        e.printStackTrace();
                                        showAlert("Error", "Failed to generate bill: " + e.getMessage(), Alert.AlertType.ERROR);
                                    }
                                },
                                () -> stackPaneContainer.getChildren().remove(billOverview)
                            );

                            // Add fade transition for bill overview
                            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), billOverview);
                            fadeIn.setFromValue(0.0);
                            fadeIn.setToValue(1.0);

                            stackPaneContainer.getChildren().add(billOverview);
                            fadeIn.play();
                        } catch (SQLException | IOException e) {
                            e.printStackTrace();
                            showAlert("Error", "Failed to generate bill: " + e.getMessage(), Alert.AlertType.ERROR);
                        }
                    },
                    () -> {
                        // Cancel callback - do nothing
                    }
                );

                // Add fade transition for confirmation dialog
                FadeTransition fadeIn = new FadeTransition(Duration.millis(300), confirmationDialog);
                fadeIn.setFromValue(0.0);
                fadeIn.setToValue(1.0);

                // Add the confirmation dialog to the stack pane
                confirmationDialog.setId("endVisitConfirmation");
                stackPaneContainer.getChildren().add(confirmationDialog);
                fadeIn.play();
            } catch (IOException e) {
                e.printStackTrace();
                showAlert("Error", "Failed to load confirmation dialog", Alert.AlertType.ERROR);
            }
        }
    }

    private void generateBillForVisit(Appointment appointment) throws SQLException {
        // Get the doctor's bill rates
        BillRateDAO billRateDAO = new BillRateDAO();
        List<BillRate> doctorRates = billRateDAO.getBillRatesByDoctorId(appointment.getDoctorId());
        
        if (doctorRates.isEmpty()) {
            // If no specific rates found, get all active rates
            doctorRates = billRateDAO.getAllBillRates();
        }

        // Create a map of service types to rates for easy lookup
        Map<String, BillRate> serviceRates = new HashMap<>();
        for (BillRate rate : doctorRates) {
            serviceRates.put(rate.getServiceType().toLowerCase(), rate);
        }

        // Check for services provided during the visit
        List<String> servicesProvided = new ArrayList<>();
        StringBuilder description = new StringBuilder("Services provided:\n");

        // Check for vitals
        if (hasVitalsRecord(appointmentId)) {
            servicesProvided.add("Vitals Check");
            description.append("- Vitals Check\n");
        }

        // Check for allergies
        if (hasAllergyRecord(appointmentId)) {
            servicesProvided.add("Allergy Assessment");
            description.append("- Allergy Assessment\n");
        }

        // Check for conditions
        if (hasConditionRecord(appointmentId)) {
            servicesProvided.add("Condition Assessment");
            description.append("- Condition Assessment\n");
        }

        // Check for prescriptions
        if (hasPrescriptionRecord(appointmentId)) {
            servicesProvided.add("Prescription");
            description.append("- Prescription\n");
        }

        // Check for visit notes
        if (appointment.getNotes() != null && !appointment.getNotes().isEmpty()) {
            servicesProvided.add("Consultation");
            description.append("- Consultation\n");
        }

        // If no specific services found, add a general consultation fee
        if (servicesProvided.isEmpty()) {
            servicesProvided.add("General Consultation");
            description.append("- General Consultation\n");
        }

        // Calculate total amount
        BigDecimal totalAmount = BigDecimal.ZERO;
        for (String service : servicesProvided) {
            BillRate rate = serviceRates.get(service.toLowerCase());
            if (rate != null) {
                totalAmount = totalAmount.add(rate.getRateAmount());
            }
        }

        // If no rates found for services, use a default consultation rate
        if (totalAmount.compareTo(BigDecimal.ZERO) == 0) {
            BillRate defaultRate = serviceRates.get("consultation");
            if (defaultRate != null) {
                totalAmount = defaultRate.getRateAmount();
            } else {
                // If no default rate found, use the first available rate
                if (!doctorRates.isEmpty()) {
                    totalAmount = doctorRates.get(0).getRateAmount();
                }
            }
        }

        // Create and save the billing record
        BillingDAO billingDAO = new BillingDAO();
        Billing billing = new Billing();
        billing.setPatientId(appointment.getPatientId());
        billing.setDoctorId(appointment.getDoctorId());
        billing.setAppointmentId(appointment.getAppointmentId());
        billing.setAmount(totalAmount);
        billing.setStatus("UNPAID");
        billing.setDueDate(LocalDateTime.now().plusDays(30)); // Due in 30 days
        billing.setDescription(description.toString());

        // If there's a matching service rate, set its ID
        for (BillRate rate : doctorRates) {
            if (rate.getServiceType().equalsIgnoreCase(appointment.getServiceType())) {
                billing.setServiceRateId(rate.getRateId());
                break;
            }
        }

        int newBillingId = billingDAO.addBilling(billing);
        billing.setBillingId(newBillingId); // Update the billing ID with the newly generated one

        // Update appointment status
        currentAppointment.setStatus("Completed");
        appointmentDAO.updateAppointment(currentAppointment);

        showAlert("Success", "Visit ended and bill generated successfully.", Alert.AlertType.INFORMATION);
        closeForm();
    }

    private boolean hasVitalsRecord(int patientId) {
        try {
            List<Vitals> vitals = vitalsDAO.getVitalsByAppointmentId(appointmentId);
            return !vitals.isEmpty();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean hasAllergyRecord(int patientId) {
        try {
            List<Allergy> allergies = patientDAO.getAllergiesByAppointmentId(appointmentId);
            return !allergies.isEmpty();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean hasConditionRecord(int patientId) {
        try {
            List<Condition> conditions = conditionDAO.getConditionsByAppointmentId(appointmentId);
            return !conditions.isEmpty();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private boolean hasPrescriptionRecord(int patientId) {
        try {
            List<Prescription> prescriptions = prescriptionDAO.getPrescriptionsByAppointmentId(appointmentId);
            return !prescriptions.isEmpty();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
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
                    vitalsController.setIds(this.patientId, 0, appointmentId);
                } else if(controller instanceof ConditionsForm) {
                    ConditionsForm conditionsController = (ConditionsForm) controller;
                    conditionsController.setIds(this.patientId, appointmentId);
                } else if(controller instanceof PrescriptionForm) {
                    PrescriptionForm prescriptionController = (PrescriptionForm) controller;
                    prescriptionController.setIds(patientId, doctorId, appointmentId);
                } else if(controller instanceof RecordAllergyForm) {
                    RecordAllergyForm allergyController = (RecordAllergyForm) controller;
                    allergyController.setPatientId(this.patientId);
                    allergyController.setIds(patientId, appointmentId);
                } else if(controller instanceof VisitNoteForm) {
                    VisitNoteForm visitNoteController = (VisitNoteForm) controller;
                    visitNoteController.setIds(patientId, doctorId, appointmentId);
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

    public void setCurrentAppointment(Appointment appointment) {
        this.currentAppointment = appointment;
    }

    private void showAlert(String title, String content, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void closeForm() {
        // Get the scene and stack pane container
        Scene scene = endVisitBtn.getScene();
        StackPane stackPaneContainer = (StackPane) scene.lookup("#stackPaneContainer");
        
        if (stackPaneContainer != null) {
            // Load the doctor's container
            try {
                FXMLLoader containerLoader = new FXMLLoader(getClass().getResource("/edu/careflow/fxml/doctor/doctorContainerNew.fxml"));
                Parent doctorContainer = containerLoader.load();
                DoctorContainerController containerController = containerLoader.getController();

                containerController.initializeData(currentAppointment.getDoctorId());

                // Fade out current content
                FadeTransition fadeOut = new FadeTransition(Duration.millis(200), scene.getRoot());
                fadeOut.setFromValue(1.0);
                fadeOut.setToValue(0.0);
                fadeOut.setOnFinished(event -> {
                    // Set the new root with fade in animation
                    scene.setRoot(doctorContainer);
                    FadeTransition fadeIn = new FadeTransition(Duration.millis(300), doctorContainer);
                    fadeIn.setFromValue(0.0);
                    fadeIn.setToValue(1.0);
                    fadeIn.play();
                });
                fadeOut.play();
            } catch (IOException e) {
                e.printStackTrace();
                showAlert("Error", "Failed to close form", Alert.AlertType.ERROR);
            }
        }
    }
}