package edu.careflow.presentation.controllers.components.table;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

public class TableTemplateController<T> {
    private static final int ROWS_PER_PAGE = 7;

    @FXML
    private VBox tableContainer;
    @FXML private Label titleLabel;
    @FXML private Button newButton;
    @FXML private Button oldButton;
    @FXML private Button tableViewBtn;
    @FXML private Button chartViewBtn;
    @FXML private TableView<T> tableView;
    @FXML private Pagination pagination;

    private List<T> allItems;
    private BiFunction<T, String, Boolean> filterPredicate;

    public void initialize(String title,
                           List<TableColumn<T, ?>> columns,
                           List<T> items,
                           BiFunction<T, String, Boolean> filterPredicate) {
        this.allItems = new ArrayList<>(items);
        this.filterPredicate = filterPredicate;

        titleLabel.setText(title);
        setupTable(columns);
        setupFilters();
        setupTableStyle();

        // Initial display
        handleFilterChange("New");
    }

    private void setupTable(List<TableColumn<T, ?>> columns) {
        tableView.getColumns().addAll(columns);
        tableView.setFixedCellSize(40);

        // Style each column header
        for (TableColumn<T, ?> column : columns) {
            // Match vitals table header style
            column.setStyle("-fx-alignment: CENTER;" +
                    "-fx-font-size: 12px; " +
                    "-fx-font-family: 'Gilroy-SemiBold'; " +
                    "-fx-text-fill: #333333;");

            // Set header padding
            column.getStyleClass().add("table-column-header");
        }

        // Style the table and rows
        tableView.setStyle("-fx-background-color: transparent;");
        tableView.getStyleClass().add("table-view");
    }



    private void setupFilters() {
        newButton.setOnAction(e -> handleFilterChange("New"));
        oldButton.setOnAction(e -> handleFilterChange("Old"));
    }

    private void handleFilterChange(String filter) {
        newButton.getStyleClass().remove("active-filter");
        oldButton.getStyleClass().remove("active-filter");

        if ("New".equals(filter)) {
            newButton.getStyleClass().add("active-filter");
        } else {
            oldButton.getStyleClass().add("active-filter");
        }

        List<T> filteredItems = allItems.stream()
                .filter(item -> filterPredicate.apply(item, filter))
                .collect(Collectors.toList());

        if (filteredItems.isEmpty()) {
            showEmptyState();
            return;
        }

        setupPagination(filteredItems);
    }

    private void setupPagination(List<T> items) {
        int pageCount = (int) Math.ceil((double) items.size() / ROWS_PER_PAGE);
        pagination.setPageCount(pageCount);
        pagination.setCurrentPageIndex(0);

        pagination.setPageFactory(pageIndex -> {
            int fromIndex = pageIndex * ROWS_PER_PAGE;
            int toIndex = Math.min(fromIndex + ROWS_PER_PAGE, items.size());
            tableView.setItems(FXCollections.observableArrayList(
                    items.subList(fromIndex, toIndex)));
            return tableView;
        });
    }

    private void showEmptyState() {
        tableView.setItems(FXCollections.emptyObservableList());
        pagination.setPageCount(1);
    }

    private void setupTableStyle() {
        // Add row styling with 4px spacing but no borders
        tableView.setRowFactory(tv -> {
            TableRow<T> row = new TableRow<>() {
                @Override
                protected void updateItem(T item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty) {
                        setStyle(null);
                    } else {
                        // Remove border, keep spacing
                        setStyle("-fx-background-color: white;" +
                                "-fx-border-width: 0;" +
                                "-fx-padding: 0 0 4 0;");
                    }
                }
            };

            // Hover effect matching vitalsCardLong without borders
            row.setOnMouseEntered(e ->
                    row.setStyle("-fx-background-color: #F1F5FE;" +
                            "-fx-border-width: 0;" +
                            "-fx-padding: 0 0 4 0;"));

            row.setOnMouseExited(e ->
                    row.setStyle("-fx-background-color: white;" +
                            "-fx-border-width: 0;" +
                            "-fx-padding: 0 0 4 0;"));

            return row;
        });

        // Add row spacing to table
        tableView.setFixedCellSize(44); // 40px height + 4px spacing
        tableView.setStyle("-fx-background-color: transparent; -fx-cell-size: 44;");
        tableView.getStyleClass().add("table-view");

        // Update CSS file reference
        tableContainer.getStylesheets().add(
                getClass().getResource("/edu/careflow/css/styles.css").toExternalForm()
        );

        // Add header styling
        Region headerBackground = (Region) tableView.lookup(".column-header-background");
        if (headerBackground != null) {
            headerBackground.setStyle("-fx-background-color: #f5f9ff;" +
                    "-fx-border-color: #d1e3f8;" +
                    "-fx-border-width: 0 0 1 0;");
        }

        // Add padding to match vitals table
        tableView.setPadding(new Insets(0, 12, 0, 12));
    }

    public void refresh() {
        handleFilterChange("New");
    }
}