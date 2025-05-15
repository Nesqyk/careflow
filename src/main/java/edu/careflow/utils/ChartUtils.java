package edu.careflow.utils;

import edu.careflow.repository.entities.Vitals;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;

import java.time.ZoneOffset;
import java.util.List;

public class ChartUtils {
    
    public static LineChart<Number, Number> createVitalsLineChart(List<Vitals> vitalsList) {
        NumberAxis xAxis = new NumberAxis();
        NumberAxis yAxis = new NumberAxis();
        
        xAxis.setLabel("Time");
        yAxis.setLabel("Value");
        
        LineChart<Number, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("Patient Vitals Over Time");
        
        // Create series for each vital sign
        XYChart.Series<Number, Number> heartRateSeries = new XYChart.Series<>();
        XYChart.Series<Number, Number> temperatureSeries = new XYChart.Series<>();
        XYChart.Series<Number, Number> oxygenSeries = new XYChart.Series<>();
        
        heartRateSeries.setName("Heart Rate");
        temperatureSeries.setName("Temperature");
        oxygenSeries.setName("Oxygen Saturation");
        
        // Add data points
        for (Vitals vitals : vitalsList) {
            long timeStamp = vitals.getRecordedAt().toEpochSecond(ZoneOffset.UTC);
            
            heartRateSeries.getData().add(new XYChart.Data<>(timeStamp, vitals.getHeartRate()));
            temperatureSeries.getData().add(new XYChart.Data<>(timeStamp, vitals.getTemperature()));
            oxygenSeries.getData().add(new XYChart.Data<>(timeStamp, vitals.getOxygenSaturation()));
        }
        
        lineChart.getData().addAll(heartRateSeries, temperatureSeries, oxygenSeries);
        lineChart.setCreateSymbols(false); // Hide data points
        
        return lineChart;
    }
}
