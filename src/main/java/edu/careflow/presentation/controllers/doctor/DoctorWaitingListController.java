package edu.careflow.presentation.controllers.doctor;

import edu.careflow.presentation.controllers.doctor.cards.AppointmentRowController;
import edu.careflow.repository.dao.AppointmentDAO;
import edu.careflow.repository.dao.PatientDAO;
import edu.careflow.repository.entities.Appointment;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Pagination;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class DoctorWaitingListController {
    @FXML private VBox appointmentsContainerMain;
    @FXML private Button scheduledBtn;
    @FXML private Button inProgressBtn;
    @FXML private Button completedBtn;
    @FXML private Button refreshBtn;
    @FXML private Pagination appointmentsPagination;
    @FXML private HBox buttonsAptFilter;
    @FXML private HBox headerAppointments;

    private AppointmentDAO appointmentDAO;
    private PatientDAO patientDAO;
    private int currentDoctorId;
    private ObservableList<Appointment> allAppointments;
    private String currentFilter = "Scheduled";
    private static final int ITEMS_PER_PAGE = 5;

    @FXML
    public void initialize() {
        appointmentDAO = new AppointmentDAO();
        patientDAO = new PatientDAO();
        allAppointments = FXCollections.observableArrayList();
        
        // Set up filter buttons
        scheduledBtn.setOnAction(e -> filterAppointments("Scheduled"));
        inProgressBtn.setOnAction(e -> filterAppointments("Pending"));
        completedBtn.setOnAction(e -> filterAppointments("Completed"));
        
        // Set up refresh button
        refreshBtn.setOnAction(e -> refreshAppointments());
        
        // Set up pagination
        appointmentsPagination.setPageCount(1);
        appointmentsPagination.currentPageIndexProperty().addListener((obs, oldIndex, newIndex) -> {
            displayAppointments(newIndex.intValue());
        });

        // Style the filter buttons container
        buttonsAptFilter.setStyle("-fx-border-color: #0762F2; -fx-border-width: 0.2; -fx-border-radius: 6px;");
    }

    public void initializeData(int doctorId) {
        this.currentDoctorId = doctorId;
        refreshAppointments();
    }

    private void refreshAppointments() {
        try {
            List<Appointment> appointments = appointmentDAO.getAppointmentsByDoctorId(currentDoctorId);
            allAppointments.clear();
            allAppointments.addAll(appointments);
            filterAppointments(currentFilter);
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Failed to load appointments", "Please try again later.");
        }
    }


    private void showEmptyState() {
        try {
            URL fxmlResource = getClass().getResource("/edu/careflow/fxml/components/states/emptyRecordstCard.fxml");
            Parent cardContent = FXMLLoader.load(fxmlResource);
            Label emptyLabel = (Label) cardContent.lookup("#pageTitle");
            emptyLabel.setText("No patient has booked yet.");
            appointmentsContainerMain.setStyle("-fx-alignment: center;");
            appointmentsContainerMain.getChildren().add(cardContent);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void filterAppointments(String status) {
        currentFilter = status;
        List<Appointment> filtered = allAppointments.stream()
            .filter(apt -> apt.getStatus().equals(status))
            .collect(Collectors.toList());
        
        int pageCount = (int) Math.ceil(filtered.size() / (double) ITEMS_PER_PAGE);
        appointmentsPagination.setPageCount(Math.max(1, pageCount));
        appointmentsPagination.setCurrentPageIndex(0);
        displayAppointments(0);
    }

    private void displayAppointments(int pageIndex) {
        appointmentsContainerMain.getChildren().clear();
        
        List<Appointment> filtered = allAppointments.stream()
            .filter(apt -> apt.getStatus().equals(currentFilter))
            .collect(Collectors.toList());

        if(filtered .isEmpty()) {
            headerAppointments.setVisible(false);
            appointmentsContainerMain.getChildren().clear();
            showEmptyState();
            return;
        }
        
        int startIndex = pageIndex * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, filtered.size());
        
        for (int i = startIndex; i < endIndex; i++) {
            Appointment apt = filtered.get(i);
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/edu/careflow/fxml/components/doctor/appointmentRow.fxml"));
                HBox row = loader.load();
                AppointmentRowController controller = loader.getController();
                
                controller.initializeData(
                    apt,
                    () -> editAppointment(apt),
                    () -> deleteAppointment(apt)
                );
                
                appointmentsContainerMain.getChildren().add(row);
            } catch (IOException e) {
                e.printStackTrace();
                showError("Error", "Failed to load appointment row: " + e.getMessage());
            }
        }
    }

    private void editAppointment(Appointment appointment) {
        // TODO: Implement edit appointment functionality
        // This could open a dialog or navigate to an edit form
        System.out.println("Edit appointment: " + appointment.getAppointmentId());
    }

    private void deleteAppointment(Appointment appointment) {
        try {
            appointmentDAO.deleteAppointment(appointment.getAppointmentId());
            refreshAppointments();
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Failed to delete appointment", "Please try again later.");
        }
    }

    private void showError(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }
} 