package edu.careflow.presentation.controllers.patient;

// ...existing imports...

import edu.careflow.presentation.controllers.patient.cards.ConsultationCardController;
import edu.careflow.repository.dao.AppointmentDAO;
import edu.careflow.repository.entities.Appointment;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Pagination;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class ConsultationController  {
    private static final int CARDS_PER_PAGE = 7;
    private List<Appointment> consultations;

    @FXML
    private VBox consultationHistory;
    @FXML
    private HBox headerConsultation;
    @FXML
    private VBox containerMain;
    @FXML
    private Button newConsultBtn;
    @FXML
    private Button oldConsultBtn;
    @FXML
    private Button tableViewBtn;
    @FXML
    private Pagination consultationPagination;

    private final AppointmentDAO appointmentDAO = new AppointmentDAO();

    @FXML
    public void initialize() {
        newConsultBtn.setOnAction(e -> handleFilterConsultations(newConsultBtn, "New"));
        oldConsultBtn.setOnAction(e -> handleFilterConsultations(oldConsultBtn, "Old"));
    }


    public void initializeData(int patientId) {
        try {
            consultations = appointmentDAO.getAppointmentsByPatientId(patientId);
            handleFilterConsultations(newConsultBtn, "New");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void handleFilterConsultations(Button clickedButton, String filter) {
        newConsultBtn.getStyleClass().remove("active-filter");
        oldConsultBtn.getStyleClass().remove("active-filter");
        clickedButton.getStyleClass().add("active-filter");

        List<Appointment> filteredConsultations = filterConsultations(filter);
        displayConsultations(filteredConsultations);
    }

    private List<Appointment> filterConsultations(String filter) {
        LocalDate thirtyDaysAgo = LocalDate.now().minusDays(30);
        return consultations.stream()
                .filter(appointment -> {
                    LocalDate appointmentDate = appointment.getAppointmentDate().toLocalDate();
                    return filter.equals("New") ? 
                           appointmentDate.isAfter(thirtyDaysAgo) : 
                           appointmentDate.isBefore(thirtyDaysAgo);
                })
                .collect(Collectors.toList());
    }

    private void displayConsultations(List<Appointment> filteredConsultations) {
        if (filteredConsultations.isEmpty()) {
            showEmptyState();
            return;
        }

        headerConsultation.setVisible(true);
        int totalPages = (int) Math.ceil((double) filteredConsultations.size() / CARDS_PER_PAGE);
        consultationPagination.setPageCount(totalPages);
        consultationPagination.setCurrentPageIndex(0);

        consultationPagination.setPageFactory(pageIndex -> {
            VBox page = createConsultationPage(pageIndex, filteredConsultations);
            containerMain.getChildren().clear();
            containerMain.getChildren().add(page);
            return new VBox();
        });
    }

    private VBox createConsultationPage(int pageIndex, List<Appointment> consultations) {
        VBox page = new VBox(4);
        int start = pageIndex * CARDS_PER_PAGE;
        int end = Math.min(start + CARDS_PER_PAGE, consultations.size());

        for (int i = start; i < end; i++) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/edu/careflow/fxml/components/patient/consultationCard.fxml"));
                Parent root = loader.load();
                ConsultationCardController controller = loader.getController();
                controller.initializeData(consultations.get(i));
                page.getChildren().add(root);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return page;
    }

    private void showEmptyState() {
        try {
            headerConsultation.setVisible(false);
            consultationPagination.setVisible(false);
            
            URL fxmlResource = getClass().getResource("/edu/careflow/fxml/components/states/emptyRecordstCard.fxml");
            Parent cardContent = FXMLLoader.load(fxmlResource);

            Label emptyLabel = (Label) cardContent.lookup("#pageTitle");
            emptyLabel.setText("No consultations found");
            
            containerMain.getChildren().clear();
            containerMain.getChildren().add(cardContent);
            containerMain.setStyle("-fx-alignment: center;");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
