package edu.careflow.presentation.controllers.doctor;

import edu.careflow.repository.dao.BillRateDAO;
import edu.careflow.repository.dao.DoctorDAO;
import edu.careflow.repository.entities.BillRate;
import edu.careflow.repository.entities.Doctor;
import javafx.animation.FadeTransition;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.util.Duration;

import java.math.BigDecimal;
import java.net.URL;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.*;

public class DoctorRatesController implements Initializable {
    @FXML private VBox consultationRatesContainer;
    @FXML private VBox procedureRatesContainer;
    @FXML private VBox diagnosticRatesContainer;
    @FXML private VBox customRatesContainer;
    @FXML private Button closeButton;
    @FXML private Button saveBtn;
    @FXML private Button cancelBtn;
    @FXML private Button addCustomServiceBtn;
    @FXML private ComboBox<String> specializationComboBox;
    @FXML private TextField yrLicensureField;
    @FXML private TextField uniqueIdField;
    @FXML private TextField contactField;
    @FXML private ScrollPane scrollPaneRates;

    private BillRateDAO billRateDAO;
    private int doctorId;
    private Map<String, BillRate> rateMap = new HashMap<>();

    // Predefined service types
    private final Map<String, List<String>> serviceCategories = Map.of(
        "Consultation", Arrays.asList(
            "Initial Consultation",
            "Follow-up Consultation",
            "Emergency Consultation",
            "Telemedicine Consultation"
        ),
        "Procedures", Arrays.asList(
            "Complete Physical Examination",
            "Vitals Assessment",
            "Basic Health Screening"
        ),
        "Diagnostics", Arrays.asList(
            "Comprehensive Health Check",
            "Specialized Consultation",
            "Medical Certificate"
        )
    );

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        billRateDAO = new BillRateDAO();
        
        // Initialize specialization combo box
        specializationComboBox.setItems(FXCollections.observableArrayList(
            "General Medicine",
            "Pediatrics",
            "Cardiology",
            "Dermatology",
            "Neurology",
            "Orthopedics",
            "Gynecology",
            "Ophthalmology",
            "ENT",
            "Dentistry"
        ));

        // Set up button handlers
        closeButton.setOnAction(e -> closeForm());
        saveBtn.setOnAction(e -> saveAllChanges());
        cancelBtn.setOnAction(e -> closeForm());
        addCustomServiceBtn.setOnAction(e -> showAddCustomServiceDialog());

        // Add input validation with platform run later to prevent infinite loops
        yrLicensureField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d{0,2}")) {
                yrLicensureField.setText(oldVal);
            }
        });

        uniqueIdField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d{0,6}")) {
                uniqueIdField.setText(oldVal);
            }
        });
    }

    public void initializeData(int doctorId) {
        this.doctorId = doctorId;
        loadRates();
        loadDoctorDetails();
    }

    private void loadDoctorDetails() {
        try {
            DoctorDAO doctorDAO = new DoctorDAO();
            Doctor doctor = doctorDAO.getDoctorById(String.valueOf(doctorId));
            
            if (doctor != null) {
                // Set specialization if it exists
                if (doctor.getSpecialization() != null && !doctor.getSpecialization().isEmpty()) {
                    specializationComboBox.setValue(doctor.getSpecialization());
                }
                
                // Set license number if it exists (assuming format is YYXXXXXX)
                if (doctor.getLicenseNumber() != null && doctor.getLicenseNumber().length() == 8) {
                    yrLicensureField.setText(doctor.getLicenseNumber().substring(0, 2));
                    uniqueIdField.setText(doctor.getLicenseNumber().substring(2, 8));
                }
                
                // Set contact number if it exists
                if (doctor.getContactNumber() != null && !doctor.getContactNumber().isEmpty()) {
                    contactField.setText(doctor.getContactNumber());
                }
            } else {
                showError("Error", "Could not load doctor details");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Error", "Failed to load doctor details: " + e.getMessage());
        }
    }

    private void closeForm() {
        // Get the parent container of scrollPaneRates
        FadeTransition fadeOut = new FadeTransition(Duration.millis(150), scrollPaneRates);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> {
            // Remove the scrollPane from its parent after fade
            if (scrollPaneRates.getParent() instanceof VBox parentBox) {
                parentBox.getChildren().remove(scrollPaneRates);
            } else if (scrollPaneRates.getParent() != null) {
                ((javafx.scene.layout.Pane) scrollPaneRates.getParent()).getChildren().remove(scrollPaneRates);
            }
            // Set to null to help garbage collection
            scrollPaneRates = null;
        });
        fadeOut.play();
    }

    private void saveAllChanges() {
        try {
            // Validate license number fields
            if (yrLicensureField.getText().length() != 2 || uniqueIdField.getText().length() != 6) {
                showError("Invalid Input", "Licensure year must be 2 digits and unique ID must be 6 digits.");
                return;
            }
            // Validate contact number if it's not empty
            String contactNumber = contactField.getText().trim();
            if (!contactNumber.isEmpty() && !contactNumber.matches("\\d{11}")) {
                showError("Invalid Input", "Contact number must be exactly 11 digits.");
                return;
            }

            // Ensure all predefined services are in the database
            for (Map.Entry<String, List<String>> entry : serviceCategories.entrySet()) {
                for (String serviceType : entry.getValue()) {
                    if (!rateMap.containsKey(serviceType)) {
                        BillRate newRate = new BillRate(
                            0, doctorId, serviceType, BigDecimal.ZERO, "", true, null, null
                        );
                        billRateDAO.addBillRate(newRate);
                        rateMap.put(serviceType, newRate);
                    }
                }
            }

            // Save rates
            for (BillRate rate : rateMap.values()) {
                billRateDAO.updateBillRate(rate);
            }

            // Save doctor details
            DoctorDAO doctorDAO = new DoctorDAO();
            Doctor currentDoctor = doctorDAO.getDoctorById(String.valueOf(doctorId));
            if (currentDoctor != null) {
                String licenseNumber = yrLicensureField.getText() + uniqueIdField.getText();
                Doctor updatedDoctor = new Doctor(
                    currentDoctor.getDoctorId(),
                    currentDoctor.getFirstName(),
                    currentDoctor.getLastName(),
                    specializationComboBox.getValue(),
                    licenseNumber,
                    contactNumber,
                    currentDoctor.getCreatedAt(),
                    LocalDateTime.now(),
                    currentDoctor.isAvailable()
                );
                boolean success = doctorDAO.updateDoctor(updatedDoctor);
                if (success) {
                    showFloatingMessage("Settings Updated Successfully!");
                    closeForm();
                } else {
                    showError("Error", "Failed to update doctor information");
                }
            } else {
                showError("Error", "Could not find doctor information");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showError("Error", "Failed to save changes: " + e.getMessage());
        }
    }

    private void loadRates() {
        try {
            // 1. Get all rates for this doctor from the database
            List<BillRate> rates = billRateDAO.getBillRatesByDoctorId(doctorId);
            rateMap.clear();
            Set<String> existingServiceTypes = new HashSet<>();
            for (BillRate rate : rates) {
                rateMap.put(rate.getServiceType(), rate);
                existingServiceTypes.add(rate.getServiceType());
            }

            // 2. Ensure all standard services exist in the database
            boolean addedAny = false;
            for (Map.Entry<String, List<String>> entry : serviceCategories.entrySet()) {
                for (String serviceType : entry.getValue()) {
                    if (!existingServiceTypes.contains(serviceType)) {
                        BillRate newRate = new BillRate(
                            0, doctorId, serviceType, BigDecimal.ZERO, "", true, null, null
                        );
                        billRateDAO.addBillRate(newRate);
                        addedAny = true;
                    }
                }
            }

            // 3. If any were added, reload from DB to get their IDs
            if (addedAny) {
                rates = billRateDAO.getBillRatesByDoctorId(doctorId);
                rateMap.clear();
                for (BillRate rate : rates) {
                    rateMap.put(rate.getServiceType(), rate);
                }
            }

            // 4. Group rates by category for UI
            Map<String, List<BillRate>> categorizedRates = new HashMap<>();
            for (BillRate rate : rateMap.values()) {
                String category = getCategoryForService(rate.getServiceType());
                if (category != null) {
                    categorizedRates.computeIfAbsent(category, k -> new ArrayList<>()).add(rate);
                } else {
                    categorizedRates.computeIfAbsent("Custom", k -> new ArrayList<>()).add(rate);
                }
            }

            // 5. Clear containers
            consultationRatesContainer.getChildren().clear();
            procedureRatesContainer.getChildren().clear();
            diagnosticRatesContainer.getChildren().clear();
            customRatesContainer.getChildren().clear();

            // 6. Populate each category
            populateCategory(consultationRatesContainer, "Consultation", categorizedRates.getOrDefault("Consultation", new ArrayList<>()));
            populateCategory(procedureRatesContainer, "Procedures", categorizedRates.getOrDefault("Procedures", new ArrayList<>()));
            populateCategory(diagnosticRatesContainer, "Diagnostics", categorizedRates.getOrDefault("Diagnostics", new ArrayList<>()));
            populateCategory(customRatesContainer, "Custom", categorizedRates.getOrDefault("Custom", new ArrayList<>()));
        } catch (SQLException e) {
            showError("Error", "Failed to load rates: " + e.getMessage());
        }
    }

    private void populateCategory(VBox container, String category, List<BillRate> rates) {
        // Add header
        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);
        header.setStyle("-fx-background-color: #f5f9ff; -fx-border-color: #d1e3f8; -fx-border-width: 0 0 1 0;");
        header.setPadding(new Insets(12));

        Label serviceLabel = new Label("Service Type");
        Label rateLabel = new Label("Rate Amount");
        Label descLabel = new Label("Description");
        Label statusLabel = new Label("Status");

        serviceLabel.setPrefWidth(200);
        rateLabel.setPrefWidth(150);
        descLabel.setPrefWidth(200);
        statusLabel.setPrefWidth(100);

        serviceLabel.setFont(Font.font("☞Gilroy-SemiBold", 12));
        rateLabel.setFont(Font.font("☞Gilroy-SemiBold", 12));
        descLabel.setFont(Font.font("☞Gilroy-SemiBold", 12));
        statusLabel.setFont(Font.font("☞Gilroy-SemiBold", 12));

        header.getChildren().addAll(serviceLabel, rateLabel, descLabel, statusLabel);
        container.getChildren().add(header);

        // Add predefined services if they don't exist
        if (!category.equals("Custom")) {
            for (String serviceType : serviceCategories.get(category)) {
                if (!rateMap.containsKey(serviceType)) {
                    BillRate newRate = new BillRate(
                        0, doctorId, serviceType, 
                        BigDecimal.ZERO, "", true, 
                        null, null
                    );
                    rates.add(newRate);
                    rateMap.put(serviceType, newRate);
                }
            }
        }

        // Add rate rows
        for (BillRate rate : rates) {
            container.getChildren().add(createRateRow(rate));
        }
    }

    private HBox createRateRow(BillRate rate) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(12));
        row.setStyle("-fx-background-color: white; -fx-border-color: #d1e3f8; -fx-border-width: 0 0 1 0;");

        // Service Type
        Label serviceLabel = new Label(rate.getServiceType());
        serviceLabel.setPrefWidth(200);

        // Rate Amount
        TextField rateField = new TextField(rate.getRateAmount().toString());
        rateField.setPrefWidth(150);
        rateField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*\\.?\\d*")) {
                javafx.application.Platform.runLater(() -> rateField.setText(oldVal));
            } else {
                try {
                    rate.setRateAmount(new BigDecimal(newVal));
                    rateMap.put(rate.getServiceType(), rate);
                } catch (NumberFormatException e) {
                    // Handle invalid number format
                }
            }
        });

        // Description
        TextField descField = new TextField(rate.getDescription());
        descField.setPrefWidth(200);
        descField.textProperty().addListener((obs, oldVal, newVal) -> {
            rate.setDescription(newVal);
            rateMap.put(rate.getServiceType(), rate);
        });

        // Status Toggle
        ToggleButton statusToggle = new ToggleButton(rate.isActive() ? "Active" : "Inactive");
        statusToggle.setSelected(rate.isActive());
        statusToggle.setPrefWidth(100);
        statusToggle.setStyle(rate.isActive() ? 
            "-fx-background-color: #4CAF50; -fx-text-fill: white;" : 
            "-fx-background-color: #f44336; -fx-text-fill: white;");
        
        statusToggle.selectedProperty().addListener((obs, oldVal, newVal) -> {
            statusToggle.setText(newVal ? "Active" : "Inactive");
            statusToggle.setStyle(newVal ? 
                "-fx-background-color: #4CAF50; -fx-text-fill: white;" : 
                "-fx-background-color: #f44336; -fx-text-fill: white;");
            rate.setActive(newVal);
            rateMap.put(rate.getServiceType(), rate);
        });

        // Delete Toggle
        ToggleButton deleteToggle = new ToggleButton("🗑");
        deleteToggle.setPrefWidth(40);
        deleteToggle.setTooltip(new Tooltip("Delete this rate"));
        deleteToggle.selectedProperty().addListener((obs, wasSelected, isSelected) -> {
            if (isSelected) {
                // Delete from DB and UI
                try {
                    billRateDAO.deleteBillRate(rate.getRateId());
                    rateMap.remove(rate.getServiceType());
                    ((VBox) row.getParent()).getChildren().remove(row);
                } catch (Exception e) {
                    showError("Error", "Failed to delete rate: " + e.getMessage());
                }
            }
        });

        row.getChildren().addAll(serviceLabel, rateField, descField, statusToggle, deleteToggle);
        return row;
    }

    private void showAddCustomServiceDialog() {
        Dialog<BillRate> dialog = new Dialog<>();
        dialog.setTitle("Add Custom Service");
        dialog.setHeaderText("Enter details for the new service");

        // Create the custom dialog content
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField serviceTypeField = new TextField();
        TextField rateField = new TextField();
        TextField descField = new TextField();

        // Add validation
        rateField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (!newVal.matches("\\d*\\.?\\d*")) {
                javafx.application.Platform.runLater(() -> rateField.setText(oldVal));
            }
        });

        grid.add(new Label("Service Type:"), 0, 0);
        grid.add(serviceTypeField, 1, 0);
        grid.add(new Label("Rate Amount:"), 0, 1);
        grid.add(rateField, 1, 1);
        grid.add(new Label("Description:"), 0, 2);
        grid.add(descField, 1, 2);

        dialog.getDialogPane().setContent(grid);

        ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        // Disable the Add button until valid input is provided
        Button addButton = (Button) dialog.getDialogPane().lookupButton(addButtonType);
        addButton.setDisable(true);

        // Add validation listeners
        serviceTypeField.textProperty().addListener((obs, oldVal, newVal) -> {
            addButton.setDisable(newVal.trim().isEmpty() || rateField.getText().trim().isEmpty());
        });

        rateField.textProperty().addListener((obs, oldVal, newVal) -> {
            addButton.setDisable(serviceTypeField.getText().trim().isEmpty() || newVal.trim().isEmpty());
        });

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                try {
                    String serviceType = serviceTypeField.getText().trim();
                    String rateText = rateField.getText().trim();
                    String description = descField.getText().trim();

                    if (serviceType.isEmpty() || rateText.isEmpty()) {
                        showError("Invalid Input", "Service type and rate amount are required.");
                        return null;
                    }

                    // Check if service type already exists
                    if (rateMap.containsKey(serviceType)) {
                        showError("Duplicate Service", "A service with this name already exists.");
                        return null;
                    }

                    return new BillRate(
                        0, doctorId,
                        serviceType,
                        new BigDecimal(rateText),
                        description,
                        true, null, null
                    );
                } catch (NumberFormatException e) {
                    showError("Invalid Input", "Please enter a valid rate amount.");
                    return null;
                }
            }
            return null;
        });

        Optional<BillRate> result = dialog.showAndWait();
        result.ifPresent(rate -> {
            try {
                billRateDAO.addBillRate(rate);
                rateMap.put(rate.getServiceType(), rate);
                loadRates();
                showSuccess("Success", "Custom service added successfully.");
            } catch (Exception e) {
                showError("Error", "Failed to add custom service: " + e.getMessage());
            }
        });
    }

    private String getCategoryForService(String serviceType) {
        for (Map.Entry<String, List<String>> entry : serviceCategories.entrySet()) {
            if (entry.getValue().contains(serviceType)) {
                return entry.getKey();
            }
        }
        return null;
    }

    private void showError(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showSuccess(String header, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Success");
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void showFloatingMessage(String message) {
        Label floatingLabel = new Label(message);
        floatingLabel.setStyle(
            "-fx-background-color: #4CAF50; -fx-text-fill: white; -fx-padding: 12 24; -fx-background-radius: 8; -fx-font-size: 15px;"
        );
        floatingLabel.setOpacity(0);
        floatingLabel.setMouseTransparent(true);

        // Add to the root StackPane (or main container)
        Scene scene = scrollPaneRates.getScene();
        if (scene != null) {
            StackPane root = (StackPane) scene.lookup("#stackPaneContainer");
            if (root == null) {
                // fallback: add to scene root
                root = (StackPane) scene.getRoot();
            }
            root.getChildren().add(floatingLabel);
            StackPane.setAlignment(floatingLabel, Pos.TOP_CENTER);

            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), floatingLabel);
            fadeIn.setFromValue(0.0);
            fadeIn.setToValue(1.0);

            FadeTransition fadeOut = new FadeTransition(Duration.millis(300), floatingLabel);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            fadeOut.setDelay(Duration.seconds(2));

            fadeIn.setOnFinished(e -> fadeOut.play());
            StackPane finalRoot = root;
            fadeOut.setOnFinished(e -> finalRoot.getChildren().remove(floatingLabel));

            fadeIn.play();
        }
    }
} 