package edu.careflow.repository.dao;

import edu.careflow.manager.DatabaseManager;
import edu.careflow.repository.entities.Vitals;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VitalsDAO {

    private final DatabaseManager dbManager;

    public VitalsDAO() {
        dbManager = DatabaseManager.getInstance();
    }

    public boolean addVitals(Vitals vitals) throws SQLException {
        String sql = "INSERT INTO vitals (patient_id, nurse_id, weight_kg, height_cm, blood_pressure, heart_rate, temperature, oxygen_saturation) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, vitals.getPatientId());
            pstmt.setInt(2, vitals.getNurseId());
            pstmt.setDouble(3, vitals.getWeightKg());
            pstmt.setDouble(4, vitals.getHeightCm());
            pstmt.setString(5, vitals.getBloodPressure());
            pstmt.setInt(6, vitals.getHeartRate());
            pstmt.setDouble(7, vitals.getTemperature());
            pstmt.setDouble(8, vitals.getOxygenSaturation());

            return pstmt.executeUpdate() > 0;
        }
    }

    public boolean updateVitals(Vitals vitals) throws SQLException {
        String sql = "UPDATE vitals SET patient_id=?, nurse_id=?, weight_kg=?, height_cm=?, blood_pressure=?, heart_rate=?, temperature=?, oxygen_saturation=? WHERE vital_id=?";
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, vitals.getPatientId());
            pstmt.setInt(2, vitals.getNurseId());
            pstmt.setDouble(3, vitals.getWeightKg());
            pstmt.setDouble(4, vitals.getHeightCm());
            pstmt.setString(5, vitals.getBloodPressure());
            pstmt.setInt(6, vitals.getHeartRate());
            pstmt.setDouble(7, vitals.getTemperature());
            pstmt.setDouble(8, vitals.getOxygenSaturation());
            pstmt.setInt(9, vitals.getVitalsId());

            return pstmt.executeUpdate() > 0;
        }
    }

    public boolean deleteVitals(int vitalsId) throws SQLException {
        String sql = "DELETE FROM vitals WHERE vital_id=?";
        try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, vitalsId);
            return pstmt.executeUpdate() > 0;
        }
    }

    public Vitals getVitalsById(int vitalsId) throws SQLException {
        String sql = "SELECT * FROM vitals WHERE vital_id=?";
        try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, vitalsId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Vitals(
                            rs.getInt("patient_id"),
                            rs.getInt("vital_id"),
                            rs.getInt("nurse_id"),
                            rs.getString("blood_pressure"),
                            rs.getInt("heart_rate"),
                            0, // respiratoryRate not in table
                            rs.getDouble("weight_kg"),
                            rs.getDouble("height_cm"),
                            rs.getDouble("temperature"),
                            rs.getDouble("oxygen_saturation"),
                            rs.getTimestamp("recorded_at").toLocalDateTime(),
                            null // updatedAt not in table
                    );
                }
            }
        }
        return null;
    }

    public List<Vitals> getAllVitals() throws SQLException {
        List<Vitals> vitalsList = new ArrayList<>();
        String sql = "SELECT * FROM vitals";
        try (Statement stmt = DatabaseManager.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                vitalsList.add(new Vitals(
                        rs.getInt("patient_id"),
                        rs.getInt("vital_id"),
                        rs.getInt("nurse_id"),
                        rs.getString("blood_pressure"),
                        rs.getInt("heart_rate"),
                        0, // respiratoryRate not in table
                        rs.getDouble("weight_kg"),
                        rs.getDouble("height_cm"),
                        rs.getDouble("temperature"),
                        rs.getDouble("oxygen_saturation"),
                        rs.getTimestamp("recorded_at").toLocalDateTime(),
                        null // updatedAt not in table
                ));
            }
        }
        return vitalsList;
    }

    public List<Vitals> getVitalsByPatientId(int patientId) throws SQLException {
        List<Vitals> vitalsList = new ArrayList<>();
        String sql = "SELECT * FROM vitals WHERE patient_id=?";
        try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, patientId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    vitalsList.add(new Vitals(
                            rs.getInt("patient_id"),
                            rs.getInt("vital_id"),
                            rs.getInt("nurse_id"),
                            rs.getString("blood_pressure"),
                            rs.getInt("heart_rate"),
                            0, // respiratoryRate not in table
                            rs.getDouble("weight_kg"),
                            rs.getDouble("height_cm"),
                            rs.getDouble("temperature"),
                            rs.getDouble("oxygen_saturation"),
                            rs.getTimestamp("recorded_at").toLocalDateTime(),
                            null // updatedAt not in table
                    ));
                }
            }
        }
        return vitalsList;
    }

    public List<Vitals> getVitalsByNurseId(int nurseId) throws SQLException {
        List<Vitals> vitalsList = new ArrayList<>();
        String sql = "SELECT * FROM vitals WHERE nurse_id=?";
        try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, nurseId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    vitalsList.add(new Vitals(
                            rs.getInt("patient_id"),
                            rs.getInt("vital_id"),
                            rs.getInt("nurse_id"),
                            rs.getString("blood_pressure"),
                            rs.getInt("heart_rate"),
                            0, // respiratoryRate not in table
                            rs.getDouble("weight_kg"),
                            rs.getDouble("height_cm"),
                            rs.getDouble("temperature"),
                            rs.getDouble("oxygen_saturation"),
                            rs.getTimestamp("recorded_at").toLocalDateTime(),
                            null // updatedAt not in table
                    ));
                }
            }
        }
        return vitalsList;
    }

    public Vitals getLatestVitals(int patientId) throws SQLException {
        String sql = "SELECT * FROM vitals WHERE patient_id=? ORDER BY recorded_at DESC LIMIT 1";
        try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, patientId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Vitals(
                            rs.getInt("patient_id"),
                            rs.getInt("vital_id"),
                            rs.getInt("nurse_id"),
                            rs.getString("blood_pressure"),
                            rs.getInt("heart_rate"),
                            0, // respiratoryRate not in table
                            rs.getDouble("weight_kg"),
                            rs.getDouble("height_cm"),
                            rs.getDouble("temperature"),
                            rs.getDouble("oxygen_saturation"),
                            rs.getTimestamp("recorded_at").toLocalDateTime(),
                            null // updatedAt not in table
                    );
                }
            }
        }
        return null;
    }
}
