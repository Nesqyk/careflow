package edu.careflow.presentation.controllers.nurse;

import edu.careflow.repository.dao.PatientDAO;
import edu.careflow.repository.entities.Patient;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Pagination;
import javafx.scene.layout.VBox;

import java.sql.SQLException;
import java.util.List;

public class NursePatientListController {
    @FXML private VBox mainContainer;
    @FXML private Pagination paginationContainer;
    @FXML private Button downloadBtn;

    private static final int CARDS_PER_PAGE = 5;
    private List<Patient> allPatients;

    public void initialize() {
        try {
            allPatients = new PatientDAO().getAllPatients();
            int pageCount = (int) Math.ceil((double) allPatients.size() / CARDS_PER_PAGE);
            paginationContainer.setPageCount(Math.max(pageCount, 1));
            paginationContainer.setCurrentPageIndex(0);
            paginationContainer.setPageFactory(this::createPage);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        downloadBtn.setOnAction(e -> handleDownload());
    }

    private VBox createPage(int pageIndex) {
        mainContainer.getChildren().clear();
        int startIndex = pageIndex * CARDS_PER_PAGE;
        int endIndex = Math.min(startIndex + CARDS_PER_PAGE, allPatients.size());
        VBox pageBox = new VBox(12);
        for (int i = startIndex; i < endIndex; i++) {
            try {
                Patient patient = allPatients.get(i);
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/edu/careflow/fxml/nurse/nursePatientListCard.fxml"));
                Parent card = loader.load();
                NursePatientListCardController controller = loader.getController();
                controller.initializeData(patient);
                pageBox.getChildren().add(card);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        mainContainer.getChildren().setAll(pageBox);
        return pageBox;
    }

    private void handleDownload() {
        // TODO: Implement download logic
    }
} 