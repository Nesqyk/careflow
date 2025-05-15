package edu.careflow.presentation.controllers.patient.cards;

import edu.careflow.repository.entities.Vitals;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;

import java.time.format.DateTimeFormatter;
import java.util.List;

public class VitalsChartCardController {

    @FXML private LineChart<String, Number> vitalsLineChart;
    @FXML private Button heartRateBtn;
    @FXML private Button oxygenBtn;
    @FXML private Button tempBtn;
    @FXML private Button bpBtn;

    private List<Vitals> vitalsList;
    private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("MM/dd");

    @FXML
    public void initialize() {
        // Clear any existing data
        vitalsLineChart.getData().clear();
        vitalsLineChart.setAnimated(false); // Disable animations for better performance

        // Setup button style classes
        setupButtonStyles();

        // Setup button actions
        setupButtonActions();
    }

    private void setupButtonStyles() {
        // Add default styling
        heartRateBtn.getStyleClass().add("nav-button-active");
        oxygenBtn.getStyleClass().remove("nav-button-active");
        tempBtn.getStyleClass().remove("nav-button-active");
        bpBtn.getStyleClass().remove("nav-button-active");
    }

    private void setupButtonActions() {
        heartRateBtn.setOnAction(e -> {
            updateActiveButton(heartRateBtn);
            displayHeartRateChart();
        });

        oxygenBtn.setOnAction(e -> {
            updateActiveButton(oxygenBtn);
            displayOxygenChart();
        });

        tempBtn.setOnAction(e -> {
            updateActiveButton(tempBtn);
            displayTemperatureChart();
        });

        bpBtn.setOnAction(e -> {
            updateActiveButton(bpBtn);
            displayBloodPressureChart();
        });
    }

    private void updateActiveButton(Button activeButton) {
        // Remove active class from all buttons
        heartRateBtn.getStyleClass().remove("nav-button-active");
        oxygenBtn.getStyleClass().remove("nav-button-active");
        tempBtn.getStyleClass().remove("nav-button-active");
        bpBtn.getStyleClass().remove("nav-button-active");

        // Add active class to selected button
        activeButton.getStyleClass().add("nav-button-active");
    }

    public void setupVitalsChart(List<Vitals> vitalsList) {
        this.vitalsList = vitalsList;

        // Default to heart rate view
        displayHeartRateChart();
    }

    public void setupBiometricChart(List<Vitals> vitalsList) {
        this.vitalsList = vitalsList;

        // For biometrics, different buttons will be used
        heartRateBtn.setText("Weight");
        oxygenBtn.setText("Height");
        tempBtn.setText("BMI");
        bpBtn.setVisible(false);

        // Default to weight view
        displayWeightChart();
    }

    private void displayHeartRateChart() {
        vitalsLineChart.getData().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Heart Rate (bpm)");

        for (Vitals vital : vitalsList) {
            if (vital.getHeartRate() > 0) {
                String date = vital.getRecordedAt().format(dateFormatter);
                series.getData().add(new XYChart.Data<>(date, vital.getHeartRate()));
            }
        }

        vitalsLineChart.getData().add(series);
        applySeriesStyle(series);
    }

    private void displayOxygenChart() {
        vitalsLineChart.getData().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Oxygen Level (%)");

        for (Vitals vital : vitalsList) {
            if (vital.getOxygenSaturation() > 0) {
                String date = vital.getRecordedAt().format(dateFormatter);
                series.getData().add(new XYChart.Data<>(date, vital.getOxygenSaturation()));
            }
        }

        vitalsLineChart.getData().add(series);
        applySeriesStyle(series);
    }

    private void displayTemperatureChart() {
        vitalsLineChart.getData().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Temperature (°C)");

        for (Vitals vital : vitalsList) {
            if (vital.getTemperature() > 0) {
                String date = vital.getRecordedAt().format(dateFormatter);
                series.getData().add(new XYChart.Data<>(date, vital.getTemperature()));
            }
        }

        vitalsLineChart.getData().add(series);
        applySeriesStyle(series);
    }

    private void displayBloodPressureChart() {
        vitalsLineChart.getData().clear();

        XYChart.Series<String, Number> systolicSeries = new XYChart.Series<>();
        systolicSeries.setName("Systolic");

        XYChart.Series<String, Number> diastolicSeries = new XYChart.Series<>();
        diastolicSeries.setName("Diastolic");

        for (Vitals vital : vitalsList) {
            if (vital.getBloodPressure() != null && !vital.getBloodPressure().isEmpty()) {
                String date = vital.getRecordedAt().format(dateFormatter);
                try {
                    String[] bpParts = vital.getBloodPressure().split("/");
                    if (bpParts.length == 2) {
                        int systolic = Integer.parseInt(bpParts[0].trim());
                        int diastolic = Integer.parseInt(bpParts[1].trim());

                        systolicSeries.getData().add(new XYChart.Data<>(date, systolic));
                        diastolicSeries.getData().add(new XYChart.Data<>(date, diastolic));
                    }
                } catch (Exception e) {
                    // Skip invalid blood pressure readings
                }
            }
        }

        vitalsLineChart.getData().addAll(systolicSeries, diastolicSeries);
        applySeriesStyle(systolicSeries);
        applySeriesStyle(diastolicSeries);
    }

    private void displayWeightChart() {
        vitalsLineChart.getData().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Weight (kg)");

        for (Vitals vital : vitalsList) {
            if (vital.getWeightKg() > 0) {
                String date = vital.getRecordedAt().format(dateFormatter);
                series.getData().add(new XYChart.Data<>(date, vital.getWeightKg()));
            }
        }

        vitalsLineChart.getData().add(series);
        applySeriesStyle(series);
    }

    private void applySeriesStyle(XYChart.Series<String, Number> series) {
        // Apply custom styling to data points
        for (XYChart.Data<String, Number> data : series.getData()) {
            if (data.getNode() != null) {
                data.getNode().setStyle(
                        "-fx-background-color: #0762F2, white; " +
                                "-fx-background-insets: 0, 2; " +
                                "-fx-background-radius: 5px; " +
                                "-fx-padding: 5px;"
                );
            }
        }
    }
}