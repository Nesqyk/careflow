package edu.careflow.presentation.controllers.doctor.cards;

import edu.careflow.presentation.controllers.patient.forms.AppointmentForm;
import edu.careflow.repository.dao.PatientDAO;
import edu.careflow.repository.entities.Appointment;
import edu.careflow.repository.entities.Patient;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.io.IOException;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;

public class AppointmentRowController {
    @FXML private HBox appointmentRowContainer;
    @FXML private Label timeLabel;
    @FXML private Label idLabel;
    @FXML private Label nameLabel;
    @FXML private Label roomLabel;
    @FXML private Label statusLabel;
    @FXML private MenuButton actionBtn;

    private PatientDAO patientDAO;
    private Appointment appointment;
    private Runnable onEditCallback;
    private Runnable onDeleteCallback;

    @FXML
    public void initialize() {
        patientDAO = new PatientDAO();
        
        // Set up menu items
        MenuItem editItem = new MenuItem("Edit");
        MenuItem deleteItem = new MenuItem("Delete");
        
        editItem.setOnAction(e -> {
            if (onEditCallback != null) {
                onEditCallback.run();
            }
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/edu/careflow/fxml/components/patient/appointmentForm.fxml"));
                Parent form = loader.load();
                AppointmentForm formController = loader.getController();
                formController.setAppointment(appointment);
                VBox rightBoxContainer = (VBox) appointmentRowContainer.getScene().lookup("#rightBoxContainer");
                if (rightBoxContainer != null) {
                    rightBoxContainer.getChildren().clear();
                    rightBoxContainer.getChildren().add(form);
                    // Fade in animation
                    javafx.animation.FadeTransition fadeIn = new javafx.animation.FadeTransition(javafx.util.Duration.millis(200), form);
                    fadeIn.setFromValue(0.0);
                    fadeIn.setToValue(1.0);
                    fadeIn.play();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        });
        
        deleteItem.setOnAction(e -> {
            if (onDeleteCallback != null) {
                onDeleteCallback.run();
            }
        });
        
        actionBtn.getItems().addAll(editItem, deleteItem);
    }

    public void initializeData(Appointment appointment, Runnable onEditCallback, Runnable onDeleteCallback) {
        this.appointment = appointment;
        this.onEditCallback = onEditCallback;
        this.onDeleteCallback = onDeleteCallback;

        timeLabel.setText(appointment.getAppointmentDate().format(DateTimeFormatter.ofPattern("M/d/yyyy @ HH:mm a")));

        // Set appointment ID
        idLabel.setText(String.format("APT-%03d", appointment.getAppointmentId()));

        // Set patient name
        try {
            Patient patient = patientDAO.getPatientById(appointment.getPatientId());
            nameLabel.setText(patient.getFirstName() + " " + patient.getLastName());
        } catch (SQLException e) {
            nameLabel.setText("Unknown Patient");
        }

        // Set room
        roomLabel.setText(appointment.getRoom());

        // Set status
        statusLabel.setText(appointment.getStatus());
        statusLabel.setTextFill(getStatusColor(appointment.getStatus()));
    }

    private Color getStatusColor(String status) {
        return switch (status) {
            case "Scheduled" -> Color.web("#0762F2"); // Blue
            case "Pending" -> Color.web("#ED6C02");   // Orange
            case "Completed" -> Color.web("#2E7D32"); // Green
            default -> Color.web("#757575");          // Gray
        };
    }

    public void setStyle(String style) {
        appointmentRowContainer.setStyle(style);
    }
} 