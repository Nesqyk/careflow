package edu.careflow.presentation.controllers.doctor.cards;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.StackPane;

public class EndVisitConfirmationController {

    @FXML
    private Button cancelBtn;

    @FXML
    private Button confirmBtn;

    private Runnable onConfirmCallback;
    private Runnable onCancelCallback;
    private StackPane container;

    public void setData(StackPane container, Runnable onConfirm, Runnable onCancel) {
        this.container = container;
        this.onConfirmCallback = onConfirm;
        this.onCancelCallback = onCancel;
    }

    @FXML
    private void handleConfirm() {
        if (onConfirmCallback != null) {
            onConfirmCallback.run();
        }
        if (container != null) {
            container.getChildren().remove(container.lookup("#endVisitConfirmation"));
        }
    }

    @FXML
    private void handleCancel() {
        if (onCancelCallback != null) {
            onCancelCallback.run();
        }
        if (container != null) {
            container.getChildren().remove(container.lookup("#endVisitConfirmation"));
        }
    }
} 