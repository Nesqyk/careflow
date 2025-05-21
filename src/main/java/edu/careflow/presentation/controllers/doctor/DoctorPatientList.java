package edu.careflow.presentation.controllers.doctor;

import edu.careflow.presentation.controllers.doctor.cards.PatientListCard;
import edu.careflow.repository.dao.AppointmentDAO;
import edu.careflow.repository.entities.Patient;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Pagination;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class DoctorPatientList {
    private static final int ITEMS_PER_PAGE = 5;
    private List<Patient> allPatients = new ArrayList<>();
    private int currentDoctorId;


    @FXML
    private VBox appointmentContainer;

    @FXML
    private Button downloadBtn;

    @FXML
    private VBox mainContainer;

    @FXML
    private Pagination paginationContainer;

    @FXML
    private VBox patientContainerHome;

    @FXML
    public void initialize() {
        setupPagination();
    }

    public void initializeData(int doctorId) {
        this.currentDoctorId = doctorId;
        loadPatients();
    }

    private void showEmptyState() {
        try {
            URL fxmlResource = getClass().getResource("/edu/careflow/fxml/components/states/emptyRecordstCard.fxml");
            Parent cardContent = FXMLLoader.load(fxmlResource);
            Label emptyLabel = (Label) cardContent.lookup("#pageTitle");
            emptyLabel.setText("No patients found");
            mainContainer.setStyle("-fx-alignment: center;");
            mainContainer.getChildren().add(cardContent);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadPatients() {
        try {
            AppointmentDAO appointmentDAO = new AppointmentDAO();
            allPatients = appointmentDAO.getPatientsByDoctorId(this.currentDoctorId);
            System.out.println(allPatients);
            setupPagination();
            displayCurrentPage(0);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void setupPagination() {
        int totalPages = (int) Math.ceil((double) allPatients.size() / ITEMS_PER_PAGE);
        paginationContainer.setPageCount(totalPages);
        paginationContainer.currentPageIndexProperty().addListener((obs, oldVal, newVal) -> {
            displayCurrentPage(newVal.intValue());
        });
    }

    private void displayCurrentPage(int pageIndex) {
        mainContainer.getChildren().clear();
        mainContainer.setSpacing(16); // Add spacing between cards

        int fromIndex = pageIndex * ITEMS_PER_PAGE;
        int toIndex = Math.min(fromIndex + ITEMS_PER_PAGE, allPatients.size());

        // Create all cards first
        List<Parent> cards = new ArrayList<>();
        List<PatientListCard> controllers = new ArrayList<>();

        for (int i = fromIndex; i < toIndex; i++) {
            try {
                Patient patient = allPatients.get(i);
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/edu/careflow/fxml/components/doctor/patientListCard.fxml"));
                Parent card = loader.load();
                
                PatientListCard controller = loader.getController();
                controller.initializeBasicInfo(patient); // Only set basic info first
                controller.setDoctorId(currentDoctorId);
                cards.add(card);
                controllers.add(controller);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if(cards.isEmpty()) {
            mainContainer.getChildren().clear();
            showEmptyState();
            return;
        }
        // Add all cards to the container
        mainContainer.getChildren().addAll(cards);

        // Now load avatars sequentially
        for (int i = 0; i < controllers.size(); i++) {
            PatientListCard controller = controllers.get(i);
            Patient patient = allPatients.get(fromIndex + i);
            try {
                controller.loadAvatar(patient);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void handleDownload() {
        if (allPatients.isEmpty()) {
            showAlert("No Data", "There are no patients to download.", javafx.scene.control.Alert.AlertType.INFORMATION);
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Patient List");
        fileChooser.setInitialFileName("patient_list_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".csv");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));

        Stage stage = (Stage) downloadBtn.getScene().getWindow();
        File file = fileChooser.showSaveDialog(stage);

        if (file != null) {
            try (FileWriter writer = new FileWriter(file)) {
                // Write CSV header
                writer.write("Patient ID,First Name,Last Name,Email,Contact Number,Date of Birth,Gender,Address\n");

                // Write patient data
                for (Patient patient : allPatients) {
                    writer.write(String.format("%d,%s,%s,%s,%s,%s,%s,%s\n",
                        patient.getPatientId(),
                        escapeCsvField(patient.getFirstName()),
                        escapeCsvField(patient.getLastName()),
                        escapeCsvField(patient.getEmail()),
                        escapeCsvField(patient.getContact()),
                        escapeCsvField(patient.getDateOfBirth().toString()),
                        escapeCsvField(patient.getGender()),
                        escapeCsvField(patient.getAddress())
                    ));
                }

                showAlert("Success", "Patient list has been downloaded successfully.", javafx.scene.control.Alert.AlertType.INFORMATION);
            } catch (IOException e) {
                e.printStackTrace();
                showAlert("Error", "Failed to download patient list: " + e.getMessage(), javafx.scene.control.Alert.AlertType.ERROR);
            }
        }
    }

    private String escapeCsvField(String field) {
        if (field == null) return "";
        if (field.contains(",") || field.contains("\"") || field.contains("\n")) {
            return "\"" + field.replace("\"", "\"\"") + "\"";
        }
        return field;
    }

    private void showAlert(String title, String content, javafx.scene.control.Alert.AlertType type) {
        javafx.scene.control.Alert alert = new javafx.scene.control.Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}

