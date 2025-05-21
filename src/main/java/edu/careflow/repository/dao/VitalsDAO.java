package edu.careflow.repository.dao;

import edu.careflow.manager.DatabaseManager;
import edu.careflow.repository.entities.Vitals;
import edu.careflow.utils.DateTimeUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VitalsDAO {

    private final DatabaseManager dbManager;

    public VitalsDAO() {
        dbManager = DatabaseManager.getInstance();
    }

    public int generateUniqueVitalsId() throws SQLException {
        // Keep trying until we find a unique ID
        while (true) {
            // Generate random 5 digit number between 10000 and 99999
            int randomId = 10000 + (int)(Math.random() * 90000);

            // Check if ID exists
            String sql = "SELECT COUNT(*) FROM vitals WHERE vital_id = ?";
            try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql)) {
                pstmt.setInt(1, randomId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) == 0) {
                        // ID is unique, return it
                        return randomId;
                    }
                }
            }
        }
    }

    public boolean addVitals(Vitals vitals) throws SQLException {
        int uniqueId = generateUniqueVitalsId();

        String sql = "INSERT INTO vitals (vital_id, patient_id, nurse_id, weight_kg, height_cm, " +
                    "blood_pressure, heart_rate, temperature, oxygen_saturation, appointment_id, recorded_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, datetime('now', '+8 hours'))";
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, uniqueId);
            pstmt.setInt(2, vitals.getPatientId());
            pstmt.setInt(3, vitals.getNurseId());
            pstmt.setDouble(4, vitals.getWeightKg());
            pstmt.setDouble(5, vitals.getHeightCm());
            pstmt.setString(6, vitals.getBloodPressure());
            pstmt.setInt(7, vitals.getHeartRate());
            pstmt.setDouble(8, vitals.getTemperature());
            pstmt.setDouble(9, vitals.getOxygenSaturation());
            pstmt.setInt(10, vitals.getAppointmentId());

            return pstmt.executeUpdate() > 0;
        }
    }

    public boolean updateVitals(Vitals vitals) throws SQLException {
        String sql = "UPDATE vitals SET patient_id=?, nurse_id=?, weight_kg=?, height_cm=?, blood_pressure=?, " +
                    "heart_rate=?, temperature=?, oxygen_saturation=?, appointment_id=?, recorded_at=date('now') " +
                    "WHERE vital_id=?";
        try (PreparedStatement pstmt = dbManager.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, vitals.getPatientId());
            pstmt.setInt(2, vitals.getNurseId());
            pstmt.setDouble(3, vitals.getWeightKg());
            pstmt.setDouble(4, vitals.getHeightCm());
            pstmt.setString(5, vitals.getBloodPressure());
            pstmt.setInt(6, vitals.getHeartRate());
            pstmt.setDouble(7, vitals.getTemperature());
            pstmt.setDouble(8, vitals.getOxygenSaturation());
            pstmt.setInt(9, vitals.getAppointmentId());
            pstmt.setInt(10, vitals.getVitalsId());

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
                            DateTimeUtil.fromTimestamp(rs.getTimestamp("recorded_at")),
                            null, // updatedAt not in table
                            rs.getInt("appointment_id")
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
                        DateTimeUtil.fromTimestamp(rs.getTimestamp("recorded_at")),
                        null, // updatedAt not in table
                        rs.getInt("appointment_id")
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
                            DateTimeUtil.fromTimestamp(rs.getTimestamp("recorded_at")),
                            null, // updatedAt not in table
                            rs.getInt("appointment_id")
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
                            DateTimeUtil.fromTimestamp(rs.getTimestamp("recorded_at")),
                            null, // updatedAt not in table
                            rs.getInt("appointment_id")
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
                            DateTimeUtil.fromTimestamp(rs.getTimestamp("recorded_at")),
                            null, // updatedAt not in table
                            rs.getInt("appointment_id")
                    );
                }
            }
        }
        return null;
    }

    public List<Vitals> getVitalsByAppointmentId(int appointmentId) throws SQLException {
        List<Vitals> vitalsList = new ArrayList<>();
        String sql = "SELECT * FROM vitals WHERE appointment_id=?";
        try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, appointmentId);
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
                            DateTimeUtil.fromTimestamp(rs.getTimestamp("recorded_at")),
                            null, // updatedAt not in table
                            rs.getInt("appointment_id")
                    ));
                }
            }
        }
        return vitalsList;
    }
}
