package edu.careflow.presentation.controllers.patient.cards;

import edu.careflow.repository.entities.Vitals;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.util.Duration;

import java.util.List;

public class VitalsChartCardController {

    @FXML
    private Button bpBtn;

    @FXML
    private Button heartRateBtn;

    @FXML
    private Button oxygenBtn;

    @FXML
    private Button tempBtn;

    @FXML
    private LineChart<String, Number> vitalsLineChart;

    private List<Vitals> vitalsList;

    public void initializeData(List<Vitals> vitalsList) {
        this.vitalsList = vitalsList;

        // Style the chart
        vitalsLineChart.getStyleClass().add("chart");
        vitalsLineChart.setCreateSymbols(false);
        vitalsLineChart.setAnimated(true);
        vitalsLineChart.getStyleClass().add("chart");

        vitalsLineChart.getXAxis().setTickLabelRotation(0);
        ((NumberAxis) vitalsLineChart.getYAxis()).setTickUnit(5);

        updateChart("hr");

        bpBtn.setOnAction(e -> updateChart("bp"));
        heartRateBtn.setOnAction(e -> updateChart("hr"));
        oxygenBtn.setOnAction(e -> updateChart("o2"));
        tempBtn.setOnAction(e -> updateChart("temp"));
    }

    private void updateChart(String vitalType) {
        resetButtonStates();
        vitalsLineChart.getData().clear();

        NumberAxis yAxis = (NumberAxis) vitalsLineChart.getYAxis();
        XYChart.Series<String, Number> series = new XYChart.Series<>();

        switch (vitalType) {
            case "bp":
                bpBtn.getStyleClass().addAll("nav-button-active", "nav-button-animating");
                plotBloodPressure(series);
                yAxis.setLabel("Blood Pressure (mmHg)");
                break;
            case "hr":
                heartRateBtn.getStyleClass().addAll("nav-button-active", "nav-button-animating");
                plotSimpleVital(series, v -> v.getHeartRate(), "Heart Rate");
                yAxis.setLabel("Heart Rate (bpm)");
                break;
            case "o2":
                oxygenBtn.getStyleClass().addAll("nav-button-active", "nav-button-animating");
                plotSimpleVital(series, v -> v.getOxygenSaturation(), "Oxygen Saturation");
                yAxis.setLabel("Oxygen Saturation (%)");
                break;
            case "temp":
                tempBtn.getStyleClass().addAll("nav-button-active", "nav-button-animating");
                plotSimpleVital(series, v -> v.getTemperature(), "Temperature");
                yAxis.setLabel("Temperature (°C)");
                break;
        }


        vitalsLineChart.setOpacity(0);
        FadeTransition fadeIn = new FadeTransition(Duration.millis(500), vitalsLineChart);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();
    }

    private void resetButtonStates() {
        bpBtn.getStyleClass().removeAll("nav-button-active", "nav-button-animating");
        heartRateBtn.getStyleClass().removeAll("nav-button-active", "nav-button-animating");
        oxygenBtn.getStyleClass().removeAll("nav-button-active", "nav-button-animating");
        tempBtn.getStyleClass().removeAll("nav-button-active", "nav-button-animating");
    }

    private void plotSimpleVital(XYChart.Series<String, Number> series, VitalValueExtractor extractor, String name) {
        series.setName(name);

        for (int i = 0; i < vitalsList.size(); i++) {
            XYChart.Data<String, Number> data = new XYChart.Data<>(String.valueOf(i), extractor.extract(vitalsList.get(i)));
            series.getData().add(data);
        }

        vitalsLineChart.getData().add(series);

        if (series.getNode() != null) {
            series.getNode().getStyleClass().add("chart-series-line");
        }

        for (XYChart.Data<String, Number> data : series.getData()) {
            if (data.getNode() != null) {
                data.getNode().getStyleClass().add("chart-line-symbol");
            }
        }
    }

    private void plotBloodPressure(XYChart.Series<String, Number> series) {
        series.setName("Blood Pressure");

        for (int i = 0; i < vitalsList.size(); i++) {
            String[] bpParts = vitalsList.get(i).getBloodPressure().split("/");
            if (bpParts.length == 2) {
                int systolic = Integer.parseInt(bpParts[0]);
                XYChart.Data<String, Number> data = new XYChart.Data<>(String.valueOf(i), systolic);
                series.getData().add(data);
            }
        }

        vitalsLineChart.getData().add(series);

        if (series.getNode() != null) {
            series.getNode().getStyleClass().add("chart-series-line");
        }

        for (XYChart.Data<String, Number> data : series.getData()) {
            if (data.getNode() != null) {
                data.getNode().getStyleClass().add("chart-line-symbol");
            }
        }
    }

    @FunctionalInterface
    private interface VitalValueExtractor {
        double extract(Vitals vital);
    }
}
