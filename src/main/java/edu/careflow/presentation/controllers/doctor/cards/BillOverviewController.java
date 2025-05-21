package edu.careflow.presentation.controllers.doctor.cards;

import edu.careflow.repository.dao.PatientDAO;
import edu.careflow.repository.entities.Appointment;
import edu.careflow.repository.entities.BillRate;
import edu.careflow.repository.entities.Billing;
import edu.careflow.repository.entities.Patient;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.kordamp.ikonli.javafx.FontIcon;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Consumer;

public class BillOverviewController {

    @FXML
    private Label patientNameLabel;

    @FXML
    private Label appointmentDateLabel;

    @FXML
    private Label appointmentTypeLabel;

    @FXML
    private VBox servicesContainer;

    @FXML
    private Label totalAmountLabel;

    @FXML
    private Label dueDateLabel;

    @FXML
    private Button closeButton;

    @FXML
    private Button cancelButton;

    @FXML
    private Button confirmButton;

    private Appointment appointment;
    private List<BillRate> doctorRates;
    private BigDecimal totalAmount;
    private Consumer<Billing> onConfirmCallback;
    private Runnable onCancelCallback;
    private PatientDAO patientDAO;

    @FXML
    public void initialize() {
        closeButton.setOnAction(e -> handleClose());
        cancelButton.setOnAction(e -> handleCancel());
        confirmButton.setOnAction(e -> handleConfirm());
        patientDAO = new PatientDAO();
    }

    public void setData(Appointment appointment, List<BillRate> doctorRates, BigDecimal totalAmount, 
                       String serviceDescription, Consumer<Billing> onConfirm, Runnable onCancel) {
        this.appointment = appointment;
        this.doctorRates = doctorRates;
        this.totalAmount = totalAmount;
        this.onConfirmCallback = onConfirm;
        this.onCancelCallback = onCancel;

        // Set patient information
        try {
            Patient patient = patientDAO.getPatientById(appointment.getPatientId());
            if (patient != null) {
                patientNameLabel.setText(patient.getFirstName() + " " + patient.getLastName());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        appointmentDateLabel.setText(appointment.getAppointmentDate().toString());
        appointmentTypeLabel.setText(appointment.getAppointmentType());

        // Set payment information
        totalAmountLabel.setText("$" + totalAmount.toString());
        dueDateLabel.setText(LocalDateTime.now().plusDays(30).toLocalDate().toString());

        // Add services to container
        servicesContainer.getChildren().clear();
        String[] services = serviceDescription.split("\n");
        for (String service : services) {
            if (service.startsWith("-")) {
                String serviceType = service.substring(2);
                // Find matching rate for this service
                BillRate matchingRate = doctorRates.stream()
                    .filter(rate -> rate.getServiceType().equalsIgnoreCase(serviceType))
                    .findFirst()
                    .orElse(null);
                
                String serviceText = serviceType;
                if (matchingRate != null) {
                    serviceText += " - $" + matchingRate.getRateAmount().toString();
                }
                
                HBox serviceBox = createServiceBox(serviceText);
                servicesContainer.getChildren().add(serviceBox);
            }
        }
    }

    private HBox createServiceBox(String serviceText) {
        HBox serviceBox = new HBox(8);
        serviceBox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        FontIcon icon = new FontIcon("fas-check-circle");
        icon.setIconSize(14);
        icon.getStyleClass().add("detail-icon");

        Label serviceLabel = new Label(serviceText);
        serviceLabel.getStyleClass().add("detail-value");

        serviceBox.getChildren().addAll(icon, serviceLabel);
        return serviceBox;
    }

    private void handleClose() {
        if (onCancelCallback != null) {
            onCancelCallback.run();
        }
    }

    private void handleCancel() {
        if (onCancelCallback != null) {
            onCancelCallback.run();
        }
    }

    private void handleConfirm() {
        if (onConfirmCallback != null) {
            Billing billing = new Billing();
            billing.setAppointmentId(appointment.getAppointmentId());
            billing.setPatientId(appointment.getPatientId());
            billing.setDoctorId(appointment.getDoctorId());
            billing.setAmount(totalAmount);
            billing.setDueDate(LocalDateTime.now().plusDays(30));
            billing.setStatus("UNPAID");
            
            // Find matching service rate if available
            for (BillRate rate : doctorRates) {
                if (rate.getServiceType().equalsIgnoreCase(appointment.getServiceType())) {
                    billing.setServiceRateId(rate.getRateId());
                    break;
                }
            }
            
            onConfirmCallback.accept(billing);
        }
    }
} 