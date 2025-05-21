package edu.careflow.presentation.controllers.patient.cards;

import edu.careflow.repository.entities.Allergy;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.geometry.Insets;

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

        // Set up reactions container styling
        reactionsContainer.setSpacing(8);
        reactionsContainer.setPadding(new Insets(10));
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
            // Add a header label
            Label reactionsHeader = new Label("Reactions:");
            reactionsHeader.setFont(Font.font("Gilroy-Bold", 14));
            reactionsHeader.setStyle("-fx-text-fill: #333333;");
            reactionsContainer.getChildren().add(reactionsHeader);

            // Add each reaction with bullet point
            for (String reaction : allergy.getReactions()) {
                VBox reactionBox = new VBox();
                reactionBox.setSpacing(4);
                
                Label reactionLabel = new Label("• " + reaction);
                reactionLabel.setFont(Font.font("Gilroy-Regular", 14));
                reactionLabel.setWrapText(true);
                reactionLabel.setStyle("-fx-text-fill: #4A4A4A;");
                
                reactionBox.getChildren().add(reactionLabel);
                reactionsContainer.getChildren().add(reactionBox);
            }
        } else {
            Label noReactionsLabel = new Label("No reactions recorded");
            noReactionsLabel.setFont(Font.font("Gilroy-Regular", 14));
            noReactionsLabel.setStyle("-fx-text-fill: #828282;");
            reactionsContainer.getChildren().add(noReactionsLabel);
        }

        // Set recorded date and doctor information
        if (allergy.getAppointmentId() > 0) {
            recordedDateLabel.setText("Recorded during appointment #" + allergy.getAppointmentId());
        } else {
            recordedDateLabel.setText("Not available");
        }
        recordedByLabel.setText("Not available");
    }
}