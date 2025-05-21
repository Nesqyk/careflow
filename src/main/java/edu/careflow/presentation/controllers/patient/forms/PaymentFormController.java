package edu.careflow.presentation.controllers.patient.forms;

import edu.careflow.repository.dao.BillingDAO;
import edu.careflow.repository.entities.Billing;
import edu.careflow.repository.dao.AppointmentDAO;
import edu.careflow.repository.entities.Appointment;
import edu.careflow.repository.dao.BillRateDAO;
import edu.careflow.repository.entities.BillRate;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

public class PaymentFormController {
    @FXML
    private TextField accountNameField;

    @FXML
    private TextField accountNumberField;

    @FXML
    private ComboBox<?> bankSelection;

    @FXML
    private ToggleButton bankTransferBtn;

    @FXML
    private VBox bankTransferForm;

    @FXML
    private Button cancelButton;

    @FXML
    private TextField cardNameField;

    @FXML
    private TextField cardNumberField;

    @FXML
    private ToggleButton cardPaymentBtn;

    @FXML
    private VBox cardPaymentForm;

    @FXML
    private TextField cvvField;

    @FXML
    private TextField expiryField;

    @FXML
    private Button payButton;

    @FXML
    private Label paymentAmountLabel;

    @FXML
    private Label serviceNameLabel;

    @FXML
    private VBox formContainer;

    @FXML
    private Label paymentSummaryLabel;

    private Runnable onPaymentSuccess;
    private int billingId;
    private Integer appointmentId;
    private Integer patientId;
    private final BillingDAO billingDAO = new BillingDAO();

    public void setPaymentInfo(String amount, String service) {
        paymentAmountLabel.setText(amount);
        serviceNameLabel.setText(service);
    }

    public void setBillingInfo(int billingId, Integer appointmentId) {
        this.billingId = billingId;
        this.appointmentId = appointmentId;
        
        try {
            // Try to get the billing record
            Billing billing = billingDAO.getBillingById(billingId);
            
            // If billing doesn't exist and we have an appointmentId, create a new billing record
            if (billing == null && appointmentId != null) {
                billing = new Billing();
                billing.setBillingId(billingId);
                billing.setAppointmentId(appointmentId);
                billing.setStatus("UNPAID");
                billing.setBillingDate(LocalDateTime.now());
                billing.setDueDate(LocalDateTime.now().plusDays(30));
                
                // Get the appointment details to set patient and doctor IDs
                AppointmentDAO appointmentDAO = new AppointmentDAO();
                Appointment appointment = appointmentDAO.getAppointmentById(appointmentId);
                if (appointment != null) {
                    billing.setPatientId(appointment.getPatientId());
                    billing.setDoctorId(appointment.getDoctorId());
                    
                    // Get the service rate for this appointment
                    BillRateDAO billRateDAO = new BillRateDAO();
                    List<BillRate> doctorRates = billRateDAO.getBillRatesByDoctorId(appointment.getDoctorId());
                    for (BillRate rate : doctorRates) {
                        if (rate.getServiceType().equalsIgnoreCase(appointment.getServiceType())) {
                            billing.setServiceRateId(rate.getRateId());
                            billing.setAmount(rate.getRateAmount());
                            billing.setSubtotal(rate.getRateAmount());
                            break;
                        }
                    }
                }
                
                // Add the new billing record
                billingDAO.addBilling(billing);
            }
            
            this.patientId = billing != null ? billing.getPatientId() : null;
            updateTotal();
        } catch (SQLException ex) {
            ex.printStackTrace();
            showFloatingMessage("Error initializing billing information.", false);
        }
    }

    public void setOnPaymentSuccess(Runnable callback) {
        this.onPaymentSuccess = callback;
    }

    @FXML
    public void initialize() {
        payButton.setOnAction(e -> handlePay());
        cancelButton.setOnAction(e -> handleCancel());
    }

    private void handlePay() {
        try {
            // Only update the specific billing record that was selected
            Billing billing = billingDAO.getBillingById(billingId);
            if (billing != null) {
                billing.setStatus("PAID");
                billingDAO.updateBilling(billing);
                showFloatingMessage("Payment successful!", true);
                if (onPaymentSuccess != null) onPaymentSuccess.run();
            } else {
                showFloatingMessage("Billing record not found.", false);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            showFloatingMessage("Payment failed. Please try again.", false);
            return;
        }
        closeForm();
    }

    private void handleCancel() {
        showFloatingMessage("Payment cancelled.", false);
        closeForm();
    }

    private void closeForm() {
        if (formContainer.getParent() instanceof VBox parentBox) {
            parentBox.getChildren().remove(formContainer);
        } else if (formContainer.getScene() != null) {
            Stage stage = (Stage) formContainer.getScene().getWindow();
            stage.close();
        }
    }

    private void updateTotal() {
        try {
            List<Billing> bills;
            if (appointmentId != null) {
                bills = billingDAO.getBillingsByAppointmentId(appointmentId);
                if (paymentSummaryLabel != null) {
                    BigDecimal total = bills.stream().map(Billing::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
                    paymentSummaryLabel.setText("Total for appointment: ₱" + total.setScale(2, BigDecimal.ROUND_HALF_UP));
                }
            } else if (patientId != null) {
                bills = billingDAO.getBillingsByPatientId(patientId);
                if (paymentSummaryLabel != null) {
                    BigDecimal total = bills.stream().map(Billing::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
                    paymentSummaryLabel.setText("Total for patient: ₱" + total.setScale(2, BigDecimal.ROUND_HALF_UP));
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void showFloatingMessage(String message, boolean isSuccess) {
        Scene scene = cancelButton.getParent().getScene();
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
        // Try to find a StackPane parent for floating message
    }
} 