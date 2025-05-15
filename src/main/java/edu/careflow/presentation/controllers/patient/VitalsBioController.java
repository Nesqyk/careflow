package edu.careflow.presentation.controllers.patient;

import edu.careflow.presentation.controllers.patient.cards.VitalsChartCardController;
import edu.careflow.repository.dao.VitalsDAO;
import edu.careflow.repository.entities.Vitals;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Pagination;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import edu.careflow.presentation.controllers.patient.cards.VitalCardLongController;
import edu.careflow.presentation.controllers.patient.cards.BioCardLongController;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class VitalsBioController {

    @FXML private Button bioChartBtn;
    @FXML private Button bioTableView;
    @FXML private VBox biometricsContainerMain;
    @FXML private VBox biometricsHistoryContainer;
    @FXML private Pagination biometricsPagination;
    @FXML private HBox buttonsAptFilterBio;
    @FXML private HBox buttonsAptFilterVital;
    @FXML private HBox headerBiometrics;
    @FXML private HBox headerVitals;
    @FXML private Button newBioBtn;
    @FXML private Button newVitalBtn;
    @FXML private Button oldBioBtn;
    @FXML private Button oldVitalBtn;
    @FXML private Button tableViewBtnVitals;
    @FXML private Button vitalChartBtnVital;
    @FXML private VBox vitalsContainerMain;
    @FXML private VBox vitalsHistoryContainer;
    @FXML private Pagination vitalsPagination;

    private VitalsDAO vitalsDAO;
    private List<Vitals> vitalsList;
    private int patientId;
    private static final int ITEMS_PER_PAGE = 5;
    private boolean showNewestFirst = true;
    private boolean isVitalsTableView = true;
    private boolean isBiometricsTableView = true;

    public void initializeData(int patientId) {
        this.patientId = patientId;
        vitalsDAO = new VitalsDAO();
        vitalsList = new ArrayList<>();

        setupButtonListeners();
        setupPagination();
        loadPatientData();
    }



    private Node createVitalsPage(int pageIndex) {
        VBox page = new VBox();
        page.setSpacing(4);

        // Handle empty state
        if (vitalsList.isEmpty()) {
            Label emptyLabel = createEmptyStateLabel("No vital records available");
            page.getChildren().add(emptyLabel);
            return page;
        }

        int start = pageIndex * ITEMS_PER_PAGE;
        int end = Math.min(start + ITEMS_PER_PAGE, vitalsList.size());

        if (isVitalsTableView) {
            for (int i = start; i < end; i++) {
                try {
                    page.getChildren().add(createVitalsCard(vitalsList.get(i)));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/edu/careflow/fxml/chart/vitalChart.fxml"));
                Parent chartView = loader.load();
                VitalsChartCardController chartController = loader.getController();
                chartController.setupVitalsChart(vitalsList);
                page.getChildren().add(chartView);
            } catch (IOException e) {
                e.printStackTrace();
                Label errorLabel = createEmptyStateLabel("Error loading chart");
                page.getChildren().add(errorLabel);
            }
        }

        return page;
    }

    private Node createBiometricsPage(int pageIndex) {
        VBox page = new VBox();
        page.setSpacing(4);

        // Handle empty state
        if (vitalsList.isEmpty()) {
            Label emptyLabel = createEmptyStateLabel("No biometric records available");
            page.getChildren().add(emptyLabel);
            return page;
        }

        int start = pageIndex * ITEMS_PER_PAGE;
        int end = Math.min(start + ITEMS_PER_PAGE, vitalsList.size());

        if (isBiometricsTableView) {
            for (int i = start; i < end; i++) {
                try {
                    page.getChildren().add(createBiometricsCard(vitalsList.get(i)));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        } else {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/edu/careflow/fxml/chart/vitalChart.fxml"));
                Parent chartView = loader.load();
                VitalsChartCardController chartController = loader.getController();
                chartController.setupBiometricChart(vitalsList);
                page.getChildren().add(chartView);
            } catch (IOException e) {
                e.printStackTrace();
                Label errorLabel = createEmptyStateLabel("Error loading chart");
                page.getChildren().add(errorLabel);
            }
        }

        return page;
    }

    private Label createEmptyStateLabel(String message) {
        Label emptyLabel = new Label(message);
        emptyLabel.getStyleClass().add("empty-state-label");
        emptyLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #757575; -fx-padding: 20px;");
        return emptyLabel;
    }

    private Node createVitalsCard(Vitals vital) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/edu/careflow/fxml/components/patient/vitalsCardLong.fxml"));
        Parent cardView = loader.load();
        VitalCardLongController controller = loader.getController();
        controller.initializeData(vital);
        return cardView;
    }

    private Node createBiometricsCard(Vitals vital) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/edu/careflow/fxml/components/patient/bioCardLong.fxml"));
        Parent cardView = loader.load();
        BioCardLongController controller = loader.getController();
        controller.initializeData(vital);
        return cardView;
    }





    private void setupButtonListeners() {
        // Vitals view toggle buttons
        tableViewBtnVitals.setOnAction(e -> {
            isVitalsTableView = true;
            updateButtonState(tableViewBtnVitals, vitalChartBtnVital);
            headerVitals.setVisible(true);
            refreshVitalsView();
        });

        vitalChartBtnVital.setOnAction(e -> {
            isVitalsTableView = false;
            updateButtonState(vitalChartBtnVital, tableViewBtnVitals);
            headerVitals.setVisible(false);
            refreshVitalsView();
        });

        // Biometrics view toggle buttons
        bioTableView.setOnAction(e -> {
            isBiometricsTableView = true;
            updateButtonState(bioTableView, bioChartBtn);
            headerBiometrics.setVisible(true);
            refreshBiometricsView();
        });

        bioChartBtn.setOnAction(e -> {
            isBiometricsTableView = false;
            updateButtonState(bioChartBtn, bioTableView);
            headerBiometrics.setVisible(false);
            refreshBiometricsView();
        });

        // Filter buttons for vitals
        newVitalBtn.setOnAction(e -> {
            showNewestFirst = true;
            updateButtonState(newVitalBtn, oldVitalBtn);
            sortVitalsList();
            refreshVitalsView();
        });

        oldVitalBtn.setOnAction(e -> {
            showNewestFirst = false;
            updateButtonState(oldVitalBtn, newVitalBtn);
            sortVitalsList();
            refreshVitalsView();
        });

        // Filter buttons for biometrics
        newBioBtn.setOnAction(e -> {
            showNewestFirst = true;
            updateButtonState(newBioBtn, oldBioBtn);
            sortVitalsList();
            refreshBiometricsView();
        });

        oldBioBtn.setOnAction(e -> {
            showNewestFirst = false;
            updateButtonState(oldBioBtn, newBioBtn);
            sortVitalsList();
            refreshBiometricsView();
        });

        // Set default button states
        updateButtonState(tableViewBtnVitals, vitalChartBtnVital);
        updateButtonState(bioTableView, bioChartBtn);
        updateButtonState(newVitalBtn, oldVitalBtn);
        updateButtonState(newBioBtn, oldBioBtn);
    }

    private void updateButtonState(Button activeButton, Button inactiveButton) {
        activeButton.getStyleClass().add("nav-button-active");
        inactiveButton.getStyleClass().remove("nav-button-active");
    }

    private void setupPagination() {
        vitalsPagination.currentPageIndexProperty().addListener((obs, oldIndex, newIndex) -> {
            if (isVitalsTableView) {
                refreshVitalsView();
            }
        });

        biometricsPagination.currentPageIndexProperty().addListener((obs, oldIndex, newIndex) -> {
            if (isBiometricsTableView) {
                refreshBiometricsView();
            }
        });
    }

    private void refreshVitalsView() {
        vitalsContainerMain.getChildren().clear();

        if (vitalsList.isEmpty()) {
            vitalsContainerMain.getChildren().add(createEmptyStateLabel("No vital records available"));
            return;
        }

        if (isVitalsTableView) {
            displayVitalsTableView();
        } else {
            displayVitalsChartView();
        }
    }

    private void refreshBiometricsView() {
        biometricsContainerMain.getChildren().clear();

        if (vitalsList.isEmpty()) {
            biometricsContainerMain.getChildren().add(createEmptyStateLabel("No biometric records available"));
            return;
        }

        if (isBiometricsTableView) {
            displayBiometricsTableView();
        } else {
            displayBiometricsChartView();
        }
    }

    private void displayVitalsTableView() {
        int start = vitalsPagination.getCurrentPageIndex() * ITEMS_PER_PAGE;
        int end = Math.min(start + ITEMS_PER_PAGE, vitalsList.size());

        for (int i = start; i < end; i++) {
            try {
                vitalsContainerMain.getChildren().add(createVitalsCard(vitalsList.get(i)));
            } catch (IOException e) {
                e.printStackTrace();
                vitalsContainerMain.getChildren().add(createEmptyStateLabel("Error loading vital card"));
            }
        }
    }

    private void displayBiometricsTableView() {
        int start = biometricsPagination.getCurrentPageIndex() * ITEMS_PER_PAGE;
        int end = Math.min(start + ITEMS_PER_PAGE, vitalsList.size());

        for (int i = start; i < end; i++) {
            try {
                biometricsContainerMain.getChildren().add(createBiometricsCard(vitalsList.get(i)));
            } catch (IOException e) {
                e.printStackTrace();
                biometricsContainerMain.getChildren().add(createEmptyStateLabel("Error loading biometric card"));
            }
        }
    }

    private void displayVitalsChartView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/edu/careflow/fxml/chart/vitalChart.fxml"));
            Parent chartView = loader.load();
            VitalsChartCardController chartController = loader.getController();
            chartController.setupVitalsChart(vitalsList);
            vitalsContainerMain.getChildren().add(chartView);
        } catch (IOException e) {
            e.printStackTrace();
            vitalsContainerMain.getChildren().add(createEmptyStateLabel("Error loading vitals chart"));
        }
    }

    private void displayBiometricsChartView() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/edu/careflow/fxml/chart/vitalChart.fxml"));
            Parent chartView = loader.load();
            VitalsChartCardController chartController = loader.getController();
            chartController.setupBiometricChart(vitalsList);
            biometricsContainerMain.getChildren().add(chartView);
        } catch (IOException e) {
            e.printStackTrace();
            biometricsContainerMain.getChildren().add(createEmptyStateLabel("Error loading biometrics chart"));
        }
    }

    private void loadPatientData() {
        Thread dataLoadThread = new Thread(() -> {
            try {
                vitalsList = vitalsDAO.getVitalsByPatientId(patientId);
                sortVitalsList();

                Platform.runLater(() -> {
                    updatePaginationCount();
                    refreshVitalsView();
                    refreshBiometricsView();
                });
            } catch (SQLException e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    vitalsList = new ArrayList<>();
                    updatePaginationCount();
                    refreshVitalsView();
                    refreshBiometricsView();
                });
            }
        });
        dataLoadThread.setDaemon(true);
        dataLoadThread.start();
    }

    private void sortVitalsList() {
        if (showNewestFirst) {
            vitalsList.sort(Comparator.comparing(Vitals::getRecordedAt).reversed());
        } else {
            vitalsList.sort(Comparator.comparing(Vitals::getRecordedAt));
        }
    }

    private void updatePaginationCount() {
        int pageCount = vitalsList.isEmpty() ? 1 : (int) Math.ceil((double) vitalsList.size() / ITEMS_PER_PAGE);
        vitalsPagination.setPageCount(pageCount);
        biometricsPagination.setPageCount(pageCount);
    }
}