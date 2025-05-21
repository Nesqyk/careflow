package edu.careflow.presentation.controllers.patient.cards;

import edu.careflow.repository.entities.Allergy;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

public class AllergyOverviewController {
    @FXML private Label allergenNameLabel;
    @FXML private Label severityNameLabel;
    @FXML private Label commentLabel;
    @FXML private VBox reactionsContainer;
    @FXML private Label recordedDateLabel;
    @FXML private Label recordedByLabel;
    @FXML private Button editButton;
    @FXML private Button closeButton;

    private Allergy allergy;
    private Runnable onEditCallback;
    private Runnable onCloseCallback;

    public void setOnEditCallback(Runnable callback) {
        this.onEditCallback = callback;
    }

    public void setOnCloseCallback(Runnable callback) {
        this.onCloseCallback = callback;
    }

    @FXML
    public void initialize() {
        editButton.setOnAction(e -> {
            if (onEditCallback != null) {
                onEditCallback.run();
            }
        });

        closeButton.setOnAction(e -> {
            if (onCloseCallback != null) {
                onCloseCallback.run();
            }
        });
    }

    public void loadAllergyData(Allergy allergy) {
        this.allergy = allergy;
        
        // Set basic information
        allergenNameLabel.setText(allergy.getAllergen());
        severityNameLabel.setText(allergy.getSeverity());
        commentLabel.setText(allergy.getComment() != null ? allergy.getComment() : "No comment provided");
        
        // Clear and populate reactions
        reactionsContainer.getChildren().clear();
        if (allergy.getReactions() != null && !allergy.getReactions().isEmpty()) {
            for (String reaction : allergy.getReactions()) {
                Label reactionLabel = new Label("• " + reaction);
                reactionLabel.setFont(Font.font("☞Gilroy-Regular", 14));
                reactionLabel.setWrapText(true);
                reactionsContainer.getChildren().add(reactionLabel);
            }
        } else {
            Label noReactionsLabel = new Label("No reactions recorded");
            noReactionsLabel.setFont(Font.font("☞Gilroy-Regular", 14));
            noReactionsLabel.setTextFill(javafx.scene.paint.Color.valueOf("#828282"));
            reactionsContainer.getChildren().add(noReactionsLabel);
        }

        // TODO: Add recorded date and doctor information when available in the Allergy entity
        recordedDateLabel.setText("Not available");
        recordedByLabel.setText("Not available");
    }
}