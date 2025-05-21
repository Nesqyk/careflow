package edu.careflow.presentation.controllers.admin;

import edu.careflow.repository.dao.UserDAO;
import edu.careflow.repository.entities.User;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Pagination;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class AdminDashboardController {
    @FXML
    private VBox appointmentContainer;

    @FXML
    private HBox buttonsAptFilter;

    @FXML
    private Button doctorUsers;

    @FXML
    private Button downloadBtn;

    @FXML
    private VBox mainContainer;

    @FXML
    private Button nurseUsers;

    @FXML
    private VBox patientContainerHome;

    @FXML
    private Button receptionistUsers;

    @FXML
    private HBox tableHeader;

    @FXML
    private Label totalDoctors;

    @FXML
    private Label totalNurses;

    @FXML
    private Label totalStaffs;

    @FXML
    private Pagination paginationContainer;

    private UserDAO userDAO;
    private List<User> allUsers;
    private Button currentActiveButton;
    private static final int CARDS_PER_PAGE = 5;
    private int currentPage = 0;
    private int currentRoleFilter = 1; // default to nurse
    
    @FXML
    public void initialize() {
        try {
            userDAO = new UserDAO();
            allUsers = userDAO.getAllUsers();
            
            // Set initial active button to nurse
            currentActiveButton = nurseUsers;
            nurseUsers.setStyle("-fx-background-color: #0078D4; -fx-text-fill: white;");
            
            // Update statistics
            updateCardCounts();
            
            // Setup filter buttons
            setupFilterButtons();
            
            // Setup download button
            downloadBtn.setOnAction(e -> handleDownload());
            
            // Load initial nurse cards
            filterUsersByRole(1); // 1 is nurse role
        } catch (SQLException e) {
            e.printStackTrace();
            // Show error state
            showError("Failed to load users: " + e.getMessage());
        }
    }

    private void setupFilterButtons() {
        nurseUsers.setOnAction(e -> {
            resetButtonStyles();
            nurseUsers.setStyle("-fx-background-color: #0078D4; -fx-text-fill: white;");
            currentActiveButton = nurseUsers;
            filterUsersByRole(1); // 1 is nurse role
        });

        doctorUsers.setOnAction(e -> {
            resetButtonStyles();
            doctorUsers.setStyle("-fx-background-color: #0078D4; -fx-text-fill: white;");
            currentActiveButton = doctorUsers;
            filterUsersByRole(0); // 0 is doctor role
        });

        receptionistUsers.setOnAction(e -> {
            resetButtonStyles();
            receptionistUsers.setStyle("-fx-background-color: #0078D4; -fx-text-fill: white;");
            currentActiveButton = receptionistUsers;
            filterUsersByRole(2); // 2 is receptionist role
        });
    }

    private void resetButtonStyles() {
        nurseUsers.setStyle("-fx-background-color: transparent; -fx-text-fill: #333333;");
        doctorUsers.setStyle("-fx-background-color: transparent; -fx-text-fill: #333333;");
        receptionistUsers.setStyle("-fx-background-color: transparent; -fx-text-fill: #333333;");
    }

    private void filterUsersByRole(int roleId) {
        mainContainer.getChildren().clear();
        currentRoleFilter = roleId;
        currentPage = 0;
        List<User> filteredUsers = allUsers.stream()
                .filter(user -> user.getRoleId() == roleId)
                .toList();
        System.out.println("Filtered users for role " + roleId + ": " + filteredUsers.size());
        updatePagination(filteredUsers);
    }

    private void updatePagination(List<User> filteredUsers) {
        int pageCount = (int) Math.ceil(filteredUsers.size() / (double) CARDS_PER_PAGE);
        paginationContainer.setPageCount(Math.max(pageCount, 1));
        paginationContainer.setCurrentPageIndex(0);
        
        mainContainer.getChildren().clear();
        
        if (filteredUsers.isEmpty()) {
            System.out.println("No users to display on this page.");
            showEmptyState();
            return;
        }
        
        int start = currentPage * CARDS_PER_PAGE;
        int end = Math.min(start + CARDS_PER_PAGE, filteredUsers.size());
        
        for (int i = start; i < end; i++) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/edu/careflow/fxml/components/user/userCard.fxml"));
                Parent card = loader.load();
                Object controllerObj = loader.getController();
                if (controllerObj instanceof edu.careflow.presentation.controllers.components.user.UserCardController controller) {
                    controller.setUser(filteredUsers.get(i));
                    System.out.println("Added user card for: " + filteredUsers.get(i).getUsername());
                } else {
                    System.out.println("Controller is null or not UserCardController for user: " + filteredUsers.get(i).getUsername());
                }
                mainContainer.getChildren().add(card);
            } catch (Exception e) {
                System.out.println("Error loading user card FXML: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void showEmptyState() {
        Label emptyLabel = new Label("No users found for this role");
        emptyLabel.getStyleClass().add("empty-state-label");
        mainContainer.getChildren().add(emptyLabel);
    }

    private void showError(String message) {
        Label errorLabel = new Label(message);
        errorLabel.getStyleClass().add("error-label");
        mainContainer.getChildren().add(errorLabel);
    }

    private void updateCardCounts() {
        long doctorCount = allUsers.stream().filter(u -> u.getRoleId() == 0).count();
        long nurseCount = allUsers.stream().filter(u -> u.getRoleId() == 1).count();
        long receptionistCount = allUsers.stream().filter(u -> u.getRoleId() == 2).count();
        long staffCount = doctorCount + nurseCount + receptionistCount;

        totalDoctors.setText(String.valueOf(doctorCount));
        totalNurses.setText(String.valueOf(nurseCount));
        totalStaffs.setText(String.valueOf(staffCount));
    }

    private void handleDownload() {
        try {
            // Get filtered users based on current role
            List<User> filteredUsers = allUsers.stream()
                    .filter(user -> user.getRoleId() == currentRoleFilter)
                    .toList();

            // Create file chooser
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save User List");
            fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV Files", "*.csv")
            );

            // Set default filename with timestamp
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String roleName = getRoleName(currentRoleFilter);
            fileChooser.setInitialFileName(roleName + "_Users_" + timestamp + ".csv");

            // Show save dialog
            Stage stage = (Stage) downloadBtn.getScene().getWindow();
            File file = fileChooser.showSaveDialog(stage);

            if (file != null) {
                // Write to CSV file
                try (FileWriter writer = new FileWriter(file)) {
                    // Write header
                    writer.write("User ID,Username,First Name,Last Name,Role,Created At\n");

                    // Write data
                    for (User user : filteredUsers) {
                        writer.write(String.format("%d,%s,%s,%s,%s,%s\n",
                            user.getUser_id(),
                            user.getUsername(),
                            user.getFirstName(),
                            user.getLastName(),
                            getRoleName(user.getRoleId()),
                            user.getCreatedAt()
                        ));
                    }
                }

                showSuccess("User list exported successfully to: " + file.getName());
            }
        } catch (IOException e) {
            showError("Failed to export user list: " + e.getMessage());
        }
    }

    private String getRoleName(int roleId) {
        switch (roleId) {
            case 0: return "Doctor";
            case 1: return "Nurse";
            case 2: return "Admin/Receptionist";
            default: return "Unknown";
        }
    }

    private void showSuccess(String message) {
        Label successLabel = new Label(message);
        successLabel.getStyleClass().add("success-label");
        mainContainer.getChildren().add(0, successLabel); // Add at the top
        
        // Remove the label after 3 seconds
        new Thread(() -> {
            try {
                Thread.sleep(3000);
                javafx.application.Platform.runLater(() -> 
                    mainContainer.getChildren().remove(successLabel)
                );
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }).start();
    }
} 