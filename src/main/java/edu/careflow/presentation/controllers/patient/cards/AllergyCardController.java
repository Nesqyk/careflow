package edu.careflow.presentation.controllers.patient.cards;

import edu.careflow.repository.entities.Allergy;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import java.util.List;

public class AllergyCardController {

    @FXML
    private Label allergenNameLabel;


    @FXML
    private Button editButton;

    @FXML
    private Label severityNameLabel;


    private Allergy allergy;

    @FXML
    public void initialize() {
        editButton.setOnAction(e -> showAllergyOverview());
    }

    public void initializeData(Allergy allergy) {
        this.allergy = allergy;
        allergenNameLabel.setText(allergy.getAllergen());
        
        // Handle comment
        // Handle reactions
        List<String> reactions = allergy.getReactions();
        
        severityNameLabel.setText(allergy.getSeverity());
    }

    private void showAllergyOverview() {
        try {
            Scene currentScene = editButton.getScene();
            VBox rightBoxContainer = (VBox) currentScene.lookup("#rightBoxContainer");
            
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/edu/careflow/fxml/components/patient/allergyOverview.fxml"));
            Parent overviewRoot = loader.load();
            
            AllergyOverviewController controller = loader.getController();
            controller.loadAllergyData(allergy);
            controller.setOnCloseCallback(() -> {
                rightBoxContainer.getChildren().remove(overviewRoot);
            });
            
            rightBoxContainer.getChildren().add(overviewRoot);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
