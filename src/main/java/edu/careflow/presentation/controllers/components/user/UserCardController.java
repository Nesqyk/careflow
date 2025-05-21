package edu.careflow.presentation.controllers.components.user;

import edu.careflow.presentation.controllers.Controller;
import edu.careflow.repository.entities.User;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import java.io.IOException;

public class UserCardController extends Controller {
    @FXML private VBox userCard;
    @FXML private Label userNameLabel;
    @FXML private Label userRoleLabel;
    @FXML private Label userEmailLabel;
    @FXML private Button viewDetailsBtn;

    private User user;
    private VBox detailsCard;
    private boolean isExpanded = false;

    @Override
    public void initializeData(int userId) {
        // Not needed for user cards
    }

    public void setUser(User user) {
        this.user = user;
        if (user != null) {
            userNameLabel.setText(user.getFirstName() + " " + user.getLastName());
            userRoleLabel.setText(getRoleName(user.getRoleId()));
            userEmailLabel.setText(user.getUsername());
            
            // Setup view details button
            viewDetailsBtn.setOnAction(e -> handleViewDetails());
        }
    }

    private void handleViewDetails() {
        isExpanded = !isExpanded;
        
        if (isExpanded) {
            try {
                // Load the user details card FXML
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/edu/careflow/fxml/components/user/userDetailsCard.fxml"));
                detailsCard = loader.load();
                UserDetailsCardController controller = loader.getController();
                controller.setUser(user);
                
                // Add the details card to the user card
                userCard.getChildren().add(detailsCard);
                
                // Animate the expansion
                FadeTransition fadeIn = new FadeTransition(Duration.millis(200), detailsCard);
                fadeIn.setFromValue(0.0);
                fadeIn.setToValue(1.0);
                fadeIn.play();
                
                // Update button text
                viewDetailsBtn.setText("Hide Details");
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            // Animate the collapse
            FadeTransition fadeOut = new FadeTransition(Duration.millis(200), detailsCard);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            fadeOut.setOnFinished(e -> userCard.getChildren().remove(detailsCard));
            fadeOut.play();
            
            // Update button text
            viewDetailsBtn.setText("View Details");
        }
    }

    private String getRoleName(int roleId) {
        switch (roleId) {
            case 1: return "Nurse";
            case 2: return "Receptionist";
            case 3: return "Admin";
            case 0: return "Doctor";
            default: return "Unknown";
        }
    }
} 