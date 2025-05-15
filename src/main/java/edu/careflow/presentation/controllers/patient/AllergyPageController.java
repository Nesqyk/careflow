package edu.careflow.presentation.controllers.patient;

import edu.careflow.presentation.controllers.patient.cards.AllergyCardController;
import edu.careflow.repository.dao.PatientDAO;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.util.List;

public class AllergyPageController {

    @FXML private VBox attachmentPageContainer;
    @FXML private VBox attachmentsCard;
    @FXML private VBox mainContainer;
    @FXML private Button pageLeft;
    @FXML private Button pageRight;
    @FXML private HBox paginationContainer;

    private PatientDAO patientDAO;
    private static final int CARDS_PER_PAGE = 4;
    private int currentPage = 0;
    private List<edu.careflow.repository.entities.Allergy> allergies;

    public void initializePatient(int patientId) {
        patientDAO = new PatientDAO();
        try {
            allergies = patientDAO.getAllergies(patientId);

            // Check if allergies is null or empty
            if (allergies == null || allergies.isEmpty()) {
                showEmptyState();
                return;
            }

            // Initialize pagination
            setupPagination();
            // Load first page
            loadCurrentPage();

        } catch (IOException | SQLException e) {
            // Handle error appropriately
            e.printStackTrace();
        }
    }

    private void showEmptyState() throws IOException {

        URL fxmlResource = getClass().getResource("/edu/careflow/fxml/components/states/emptyRecordstCard.fxml");
        Parent cardContent = FXMLLoader.load(fxmlResource);

        Label emptyLabel = (Label) cardContent.lookup("#pageTitle");
        emptyLabel.setText("No Allergies Found");

        mainContainer.getChildren().add(cardContent);
        paginationContainer.setVisible(false);
    }

    private void setupPagination() {
        int totalPages = (int) Math.ceil(allergies.size() / (double) CARDS_PER_PAGE);

        pageLeft.setDisable(currentPage == 0);
        pageRight.setDisable(currentPage >= totalPages - 1);

        pageLeft.setOnAction(e -> {
            if (currentPage > 0) {
                currentPage--;
                loadCurrentPage();
            }
        });

        pageRight.setOnAction(e -> {
            if (currentPage < totalPages - 1) {
                currentPage++;
                loadCurrentPage();
            }
        });
    }


    private HBox createCardRow(int startIndex) {
        HBox row = new HBox(8); // Horizontal spacing of 8px
        row.setPadding(new Insets(10));

        int endIndex = Math.min(startIndex + 2, allergies.size()); // Max 2 cards per row
        for (int i = startIndex; i < endIndex; i++) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/edu/careflow/fxml/components/patient/allergyCard.fxml"));
                Node allergyCard = loader.load();
                AllergyCardController controller = loader.getController();
                controller.initializeData(allergies.get(i));
                row.getChildren().add(allergyCard);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return row;
    }

    private void loadCurrentPage() {
        mainContainer.getChildren().clear();

        int startIndex = currentPage * CARDS_PER_PAGE;
        int endIndex = Math.min(startIndex + CARDS_PER_PAGE, allergies.size());

        // Loop through the current page's cards in steps of 2
        for (int i = startIndex; i < endIndex; i += 2) {
            HBox cardRow = createCardRow(i);
            mainContainer.getChildren().add(cardRow);
        }

        // Update pagination buttons
        pageLeft.setDisable(currentPage == 0);
        pageRight.setDisable((currentPage + 1) * CARDS_PER_PAGE >= allergies.size());
    }
}