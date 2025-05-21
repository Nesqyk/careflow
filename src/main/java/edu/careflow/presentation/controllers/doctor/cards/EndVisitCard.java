package edu.careflow.presentation.controllers.doctor.cards;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;

public class EndVisitCard {

    @FXML
    private Button endVisitBtn;

    @FXML
    private Button cancelBtn;

    private Runnable onEndVisit;
    private StackPane container;

    @FXML
    public void initialize() {
        setupButtons();
    }

    private void setupButtons() {
        endVisitBtn.setOnAction(e -> handleEndVisit());
        cancelBtn.setOnAction(e -> handleCancel());
    }

    private void handleEndVisit() {
        if (onEndVisit != null) {
            onEndVisit.run();
        }
        closeDialog();
    }

    private void handleCancel() {
        closeDialog();
    }

    private void closeDialog() {
        if (container != null) {
            container.getChildren().remove(container.lookup("#endVisitCard"));
        }
    }

    public void setOnEndVisit(Runnable onEndVisit) {
        this.onEndVisit = onEndVisit;
    }

    public void setContainer(StackPane container) {
        this.container = container;
    }
} 