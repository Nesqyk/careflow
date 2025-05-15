package edu.careflow.presentation.controllers.patient.cards;

import edu.careflow.repository.entities.Appointment;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.kordamp.ikonli.javafx.FontIcon;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AppointmentCardController {



    @FXML
    private Label appointmentDate;

    @FXML
    private Label appointmentPeriod;

    @FXML
    private Label appointmentTime;

    @FXML
    private Label appointmentType;

    @FXML
    private VBox container;

    @FXML
    private Label doctorName;

    @FXML
    private Button joinMeetingBtn;

    @FXML
    private FontIcon statusIcon;


    public void initializeData(Appointment appointment) {
        // Format time
        LocalDateTime appointmentDateTime = appointment.getAppointmentDate();
        appointmentTime.setText(appointmentDateTime.format(DateTimeFormatter.ofPattern("hh:mm")));
        appointmentPeriod.setText(appointmentDateTime.format(DateTimeFormatter.ofPattern("a")));

        // Format date
        appointmentDate.setText(appointmentDateTime.format(DateTimeFormatter.ofPattern("MMMM d, yyyy")));

        // Set doctor name
        doctorName.setText("Dr. " + appointment.getDoctorId()); // Replace with actual doctor name when available

        // Set appointment type
        appointmentType.setText(appointment.getServiceType());

        // Set status icon color
        switch (appointment.getStatus().toLowerCase()) {
            case "scheduled":
                statusIcon.setIconColor(Color.web("#0762F2")); // Blue
                break;
            case "completed":
                statusIcon.setIconColor(Color.web("#2E7D32")); // Green
                break;
            case "cancelled":
                statusIcon.setIconColor(Color.web("#D32F2F")); // Red
                break;
            case "pending":
                statusIcon.setIconColor(Color.web("#ED6C02")); // Orange
                break;
            default:
                statusIcon.setIconColor(Color.web("#757575")); // Gray
                break;
        }

       // System.out.print(appointment.getStatus());
       // System.out.print(appointment.getAppointmentType());

        if ("Scheduled".equalsIgnoreCase(appointment.getStatus()) &&
            "Online".equalsIgnoreCase(appointment.getAppointmentType())) {
            joinMeetingBtn.setVisible(true);
            joinMeetingBtn.setOnAction(e -> {
                String url = appointment.getMeetingLink();
                if (url != null && !url.isEmpty()) {
                    // Find the parent scene and stackPaneContainer
                    StackPane stackPaneContainer = (StackPane) container.getScene().lookup("#stackPaneContainer");
                    if (stackPaneContainer != null) {
                        try {
                            FXMLLoader loader = new FXMLLoader(getClass().getResource("/edu/careflow/fxml/components/patient/videoContainerPatient.fxml"));
                            Parent videoContainer = loader.load();

                            // Set the meeting link label
                            Label meetingLinkLabel = (Label) videoContainer.lookup("#meetingLinkLabel");
                            if (meetingLinkLabel != null) {
                                meetingLinkLabel.setText("Meeting Link: " + url);
                            }

                            // Wire up the openInBrowserBtn
                            Button openInBrowserBtn = (Button) videoContainer.lookup("#openInBrowserBtn");
                            if (openInBrowserBtn != null) {
                                openInBrowserBtn.setOnAction(ev -> {
                                    try {
                                        // Note: If running on Java 17+, you may need to add --add-opens java.desktop/java.awt=ALL-UNNAMED to your JVM args for java.awt.Desktop
                                        java.awt.Desktop.getDesktop().browse(new java.net.URI(url));
                                    } catch (Exception ex) {
                                        ex.printStackTrace();
                                    }
                                });
                            }

                            // Wire up the closeBtn
                            Button closeBtn = (Button) videoContainer.lookup("#closeBtn");
                            if (closeBtn != null) {
                                closeBtn.setOnAction(ev -> stackPaneContainer.getChildren().remove(videoContainer));
                            }

                            // Wire up the endCallBtn (optional: also closes the overlay)
                            Button endCallBtn = (Button) videoContainer.lookup("#endCallBtn");
                            if (endCallBtn != null) {
                                endCallBtn.setOnAction(ev -> stackPaneContainer.getChildren().remove(videoContainer));
                            }

                            stackPaneContainer.getChildren().add(videoContainer);
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                }
            });
        } else {
            joinMeetingBtn.setVisible(false);
        }
    }
}