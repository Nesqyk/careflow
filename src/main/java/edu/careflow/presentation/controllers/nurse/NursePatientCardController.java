package edu.careflow.presentation.controllers.nurse;

import edu.careflow.presentation.controllers.patient.forms.VitalsBioForm;
import edu.careflow.repository.dao.PatientDAO;
import edu.careflow.repository.dao.VitalsDAO;
import edu.careflow.repository.entities.Appointment;
import edu.careflow.repository.entities.Patient;
import edu.careflow.repository.entities.Vitals;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.util.Duration;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class NursePatientCardController {
    @FXML private Label patientIdLabel;
    @FXML private Label patientNameLabel;
    @FXML private Label statusLabel;
    @FXML private MenuItem addVitalsMenuItem;
    @FXML private MenuItem vitalsHistoryMenuItem;
    @FXML private MenuItem downloadVitalsMenuItem;


    private PatientDAO patientDAO = new PatientDAO();
    private VitalsDAO vitalsDAO = new VitalsDAO();
    private Appointment currentAppointment;

    public void initializeData(Appointment appointment) {
        this.currentAppointment = appointment;
        try {
            Patient patient = patientDAO.getPatientById(appointment.getPatientId());
            patientIdLabel.setText("ID: " + appointment.getAppointmentId());
            patientNameLabel.setText(patient.getFirstName() + " " + patient.getLastName());
            statusLabel.setText(appointment.getStatus());

            // Setup menu item actions
            addVitalsMenuItem.setOnAction(e -> handleAddVitals());
            vitalsHistoryMenuItem.setOnAction(e -> handleVitalsHistory());
            downloadVitalsMenuItem.setOnAction(e -> handleDownloadVitals());
        } catch (SQLException e) {
            e.printStackTrace();
            patientIdLabel.setText("ID: Error");
            patientNameLabel.setText("Error loading patient");
            statusLabel.setText("Unknown");
        }
    }

    private void handleAddVitals() {
        loadFormIntoRightContainer("/edu/careflow/fxml/components/patient/vitalAptForm.fxml");

    }

    private void loadFormIntoRightContainer(String formPath) {
        try {
            // Get the scene from any control (using formBtn here)
            Scene currentScene = patientIdLabel.getScene();

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

                if(controller instanceof VitalsBioForm vitalsBioForm) {
                    vitalsBioForm.initializeData(currentAppointment.getPatientId());
                    vitalsBioForm.setIds(currentAppointment.getPatientId(), currentAppointment.getNurseId(), currentAppointment.getAppointmentId());
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

    private void handleVitalsHistory() {
        try {
            // Find the stack pane container in the scene
            StackPane stackPane = (StackPane) patientIdLabel.getScene().lookup("#stackPaneContainer");
            if (stackPane == null) {
                System.err.println("StackPane with fx:id 'stackPaneContainer' not found in scene.");
                return;
            }
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/edu/careflow/fxml/patient/vitalsBiometricsPage.fxml"));
            Parent vitalsPage = loader.load();
            // Pass patientId to the controller if possible
            Object controller = loader.getController();
            if (controller != null) {
                try {
                    controller.getClass().getMethod("initializeData", int.class).invoke(controller, currentAppointment.getPatientId());
                } catch (NoSuchMethodException ignored) {
                    // If the method doesn't exist, skip
                }
            }
            VBox vBox = new VBox();

            HBox hBox = new HBox();
            hBox.setAlignment(Pos.CENTER_RIGHT);
            hBox.setPadding(new Insets(32, 0, 32, 32));

            Button closeButton = new Button("Close");
            closeButton.getStyleClass().add("action-button");
            hBox.getChildren().add(closeButton);

            vBox.setPadding(new Insets(0, 128, 128, 128));
            vBox.getChildren().add(hBox);
            vBox.getChildren().add(vitalsPage);

            ScrollPane scrollPane = new ScrollPane(vBox);
            scrollPane.setFitToWidth(true);
            scrollPane.setFitToHeight(true);
            scrollPane.setStyle("-fx-background-color: transparent;");
            scrollPane.setPrefViewportWidth(800);
            scrollPane.setPrefViewportHeight(700);

            // Add stylesheet for consistent styling
            vBox.getStylesheets().add(getClass().getResource("/edu/careflow/css/styles.css").toExternalForm());

            // Close button logic
            closeButton.setOnAction(e -> {
                FadeTransition fadeOut = new FadeTransition(Duration.millis(200), scrollPane);
                fadeOut.setFromValue(1.0);
                fadeOut.setToValue(0.0);
                fadeOut.setOnFinished(event -> stackPane.getChildren().remove(scrollPane));
                fadeOut.play();
            });

            stackPane.getChildren().add(scrollPane);
            FadeTransition fade = new FadeTransition(Duration.millis(200), scrollPane);
            fade.setFromValue(0);
            fade.setToValue(1);
            ScaleTransition scale = new ScaleTransition(Duration.millis(200), scrollPane);
            scale.setFromX(0.95);
            scale.setFromY(0.95);
            scale.setToX(1);
            scale.setToY(1);
            fade.play();
            scale.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleDownloadVitals() {
        try {
            // Get patient information
            Patient patient = patientDAO.getPatientById(currentAppointment.getPatientId());
            if (patient == null) {
                showAlert(Alert.AlertType.ERROR, "Error", "Patient not found");
                return;
            }

            // Get vitals history
            List<Vitals> vitalsList = vitalsDAO.getVitalsByPatientId(currentAppointment.getPatientId());
            if (vitalsList.isEmpty()) {
                showAlert(Alert.AlertType.INFORMATION, "No Data", "No vitals records found for this patient");
                return;
            }

            // Create file chooser
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Vitals Report");
            fileChooser.setInitialFileName(patient.getFirstName() + "_" + patient.getLastName() + "_Vitals.html");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("HTML Files", "*.html"));
            
            // Show save dialog
            File file = fileChooser.showSaveDialog(patientIdLabel.getScene().getWindow());
            if (file == null) {
                return; // User cancelled
            }

            // Generate HTML content
            String htmlContent = generateHtmlReport(patient, vitalsList);
            
            // Write to file
            try (FileWriter writer = new FileWriter(file)) {
                writer.write(htmlContent);
            }

            showAlert(Alert.AlertType.INFORMATION, "Success", "Vitals report has been saved to: " + file.getAbsolutePath());
            
        } catch (SQLException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Database Error", "Failed to retrieve vitals data: " + e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "File Error", "Failed to save vitals report: " + e.getMessage());
        }
    }

    private String generateHtmlReport(Patient patient, List<Vitals> vitalsList) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy", Locale.US);
        SimpleDateFormat timeFormat = new SimpleDateFormat("hh:mm a", Locale.US);
        
        StringBuilder html = new StringBuilder();
        html.append("<!DOCTYPE html>\n");
        html.append("<html lang=\"en\">\n");
        html.append("<head>\n");
        html.append("    <meta charset=\"UTF-8\">\n");
        html.append("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n");
        html.append("    <title>Vitals Report - ").append(patient.getFirstName()).append(" ").append(patient.getLastName()).append("</title>\n");
        html.append("    <style>\n");
        html.append("        body { font-family: Arial, sans-serif; margin: 0; padding: 20px; color: #333; }\n");
        html.append("        .header { text-align: center; margin-bottom: 30px; }\n");
        html.append("        .patient-info { margin-bottom: 30px; }\n");
        html.append("        .patient-info h3 { margin-bottom: 10px; color: #2c3e50; }\n");
        html.append("        .patient-info p { margin: 5px 0; }\n");
        html.append("        table { width: 100%; border-collapse: collapse; margin-bottom: 30px; }\n");
        html.append("        th, td { padding: 12px 15px; text-align: left; border-bottom: 1px solid #ddd; }\n");
        html.append("        th { background-color: #f8f9fa; color: #2c3e50; }\n");
        html.append("        tr:nth-child(even) { background-color: #f2f2f2; }\n");
        html.append("        tr:hover { background-color: #e9ecef; }\n");
        html.append("        .footer { text-align: center; margin-top: 30px; font-size: 0.8em; color: #6c757d; }\n");
        html.append("        .highlight { font-weight: bold; color: #e74c3c; }\n");
        html.append("    </style>\n");
        html.append("</head>\n");
        html.append("<body>\n");
        
        // Header
        html.append("    <div class=\"header\">\n");
        html.append("        <h1>CareFlow Vitals Report</h1>\n");
        html.append("        <p>Generated on: ").append(dateFormat.format(new java.util.Date())).append("</p>\n");
        html.append("    </div>\n");
        
        // Patient Information
        html.append("    <div class=\"patient-info\">\n");
        html.append("        <h3>Patient Information</h3>\n");
        html.append("        <p><strong>Name:</strong> ").append(patient.getFirstName()).append(" ").append(patient.getLastName()).append("</p>\n");
        html.append("        <p><strong>ID:</strong> ").append(patient.getPatientId()).append("</p>\n");
        html.append("        <p><strong>Date of Birth:</strong> ").append(patient.getDateOfBirth() != null ? dateFormat.format(patient.getDateOfBirth()) : "Not available").append("</p>\n");
        html.append("        <p><strong>Gender:</strong> ").append(patient.getGender() != null ? patient.getGender() : "Not available").append("</p>\n");
        html.append("    </div>\n");
        
        // Vitals Table
        html.append("    <h3>Vitals History</h3>\n");
        html.append("    <table>\n");
        html.append("        <thead>\n");
        html.append("            <tr>\n");
        html.append("                <th>Date</th>\n");
        html.append("                <th>Time</th>\n");
        html.append("                <th>Blood Pressure</th>\n");
        html.append("                <th>Heart Rate</th>\n");
        html.append("                <th>Temperature</th>\n");
        html.append("                <th>Respiratory Rate</th>\n");
        html.append("                <th>Oxygen Saturation</th>\n");
        html.append("                <th>Weight</th>\n");
        html.append("                <th>Height</th>\n");
        html.append("                <th>BMI</th>\n");
        html.append("                <th>Notes</th>\n");
        html.append("            </tr>\n");
        html.append("        </thead>\n");
        html.append("        <tbody>\n");
        
        // Add vitals rows
        for (Vitals vitals : vitalsList) {
            html.append("            <tr>\n");
            html.append("                <td>").append(vitals.getRecordedAt() != null ? dateFormat.format(java.util.Date.from(vitals.getRecordedAt().atZone(java.time.ZoneId.systemDefault()).toInstant())) : "N/A").append("</td>\n");
            html.append("                <td>").append(vitals.getRecordedAt() != null ? timeFormat.format(java.util.Date.from(vitals.getRecordedAt().atZone(java.time.ZoneId.systemDefault()).toInstant())) : "N/A").append("</td>\n");
            html.append("                <td>").append(vitals.getBloodPressure() != null ? vitals.getBloodPressure() : "N/A").append("</td>\n");
            html.append("                <td>").append(vitals.getHeartRate()).append("</td>\n");
            html.append("                <td>").append(vitals.getTemperature()).append("°C</td>\n");
            html.append("                <td>").append(vitals.getRespiratoryRate()).append("</td>\n");
            html.append("                <td>").append(vitals.getOxygenSaturation()).append("%</td>\n");
            html.append("                <td>").append(vitals.getWeightKg()).append(" kg</td>\n");
            html.append("                <td>").append(vitals.getHeightCm()).append(" cm</td>\n");
            
            // Calculate BMI
            double heightInMeters = vitals.getHeightCm() / 100.0;
            double weightInKg = vitals.getWeightKg();
            double bmi = weightInKg / (heightInMeters * heightInMeters);
            
            html.append("                <td>").append(String.format("%.1f", bmi)).append("</td>\n");
            html.append("                <td>N/A</td>\n"); // Notes field not available in Vitals entity
            html.append("            </tr>\n");
        }
        
        html.append("        </tbody>\n");
        html.append("    </table>\n");
        
        // Footer
        html.append("    <div class=\"footer\">\n");
        html.append("        <p>This report was generated by CareFlow Healthcare Management System</p>\n");
        html.append("        <p>© ").append(new java.util.Date().getYear() + 1900).append(" CareFlow. All rights reserved.</p>\n");
        html.append("    </div>\n");
        
        html.append("</body>\n");
        html.append("</html>");
        
        return html.toString();
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void setNursedId(int id) {
        currentAppointment.setNurseId(id);
    }
} 