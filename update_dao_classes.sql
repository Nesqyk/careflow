-- Java code for VitalsDAO.java
/*
package edu.careflow.repository.dao;

import edu.careflow.manager.DatabaseManager;
import edu.careflow.repository.entities.Vitals;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class VitalsDAO {
    // ... existing code ...
    
    public boolean addVitals(Vitals vitals) throws SQLException {
        int uniqueId = generateUniqueVitalsId();

        String sql = "INSERT INTO vitals (vital_id, patient_id, nurse_id, weight_kg, height_cm, " +
                    "blood_pressure, heart_rate, temperature, oxygen_saturation, appointment_id) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
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
        String sql = "UPDATE vitals SET patient_id=?, nurse_id=?, weight_kg=?, height_cm=?, " +
                    "blood_pressure=?, heart_rate=?, temperature=?, oxygen_saturation=?, appointment_id=? " +
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
    
    // Update createVitalsFromResultSet method
    private Vitals createVitalsFromResultSet(ResultSet rs) throws SQLException {
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
                null, // updatedAt not in table
                rs.getInt("appointment_id")
        );
    }
    
    // Add method to get vitals by appointment ID
    public List<Vitals> getVitalsByAppointmentId(int appointmentId) throws SQLException {
        List<Vitals> vitalsList = new ArrayList<>();
        String sql = "SELECT * FROM vitals WHERE appointment_id=?";
        try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, appointmentId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    vitalsList.add(createVitalsFromResultSet(rs));
                }
            }
        }
        return vitalsList;
    }
}
*/

-- Java code for AllergyDAO.java
/*
package edu.careflow.repository.dao;

import edu.careflow.manager.DatabaseManager;
import edu.careflow.repository.entities.Allergy;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AllergyDAO {
    // ... existing code ...
    
    public boolean addAllergy(Allergy allergy) throws SQLException {
        String sql = "INSERT INTO allergies (allergy_id, patient_id, allergen, severity, comment, appointment_id) " +
                    "VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, allergy.getAllergyId());
            pstmt.setInt(2, allergy.getPatientId());
            pstmt.setString(3, allergy.getAllergen());
            pstmt.setString(4, allergy.getSeverity());
            pstmt.setString(5, allergy.getComment());
            pstmt.setInt(6, allergy.getAppointmentId());

            return pstmt.executeUpdate() > 0;
        }
    }
    
    // Add method to get allergies by appointment ID
    public List<Allergy> getAllergiesByAppointmentId(int appointmentId) throws SQLException {
        List<Allergy> allergies = new ArrayList<>();
        String sql = "SELECT * FROM allergies WHERE appointment_id = ?";
        try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, appointmentId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    Allergy allergy = new Allergy(
                            rs.getInt("allergy_id"),
                            rs.getInt("patient_id"),
                            rs.getString("allergen"),
                            rs.getString("severity"),
                            rs.getString("comment"),
                            rs.getInt("appointment_id")
                    );
                    
                    // Load reactions
                    String reactionsSql = "SELECT reaction FROM allergy_reactions WHERE allergy_id = ?";
                    try (PreparedStatement reactionsStmt = DatabaseManager.getConnection().prepareStatement(reactionsSql)) {
                        reactionsStmt.setInt(1, allergy.getAllergyId());
                        try (ResultSet reactionsRs = reactionsStmt.executeQuery()) {
                            while (reactionsRs.next()) {
                                allergy.addReaction(reactionsRs.getString("reaction"));
                            }
                        }
                    }
                    
                    allergies.add(allergy);
                }
            }
        }
        return allergies;
    }
}
*/

-- Java code for ConditionDAO.java
/*
package edu.careflow.repository.dao;

import edu.careflow.manager.DatabaseManager;
import edu.careflow.repository.entities.Condition;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ConditionDAO {
    // ... existing code ...
    
    public boolean addCondition(Condition condition) throws SQLException {
        String sql = "INSERT INTO conditions (patient_id, condition_id, condition_name, description, " +
                    "onset_date, status, appointment_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, condition.getPatientId());
            pstmt.setInt(2, condition.getConditionId());
            pstmt.setString(3, condition.getConditionName());
            pstmt.setString(4, condition.getDescription());
            pstmt.setDate(5, Date.valueOf(condition.getOnSetDate()));
            pstmt.setString(6, condition.getStatus());
            pstmt.setInt(7, condition.getAppointmentId());

            return pstmt.executeUpdate() > 0;
        }
    }
    
    public boolean updateCondition(Condition condition) throws SQLException {
        String sql = "UPDATE conditions SET condition_name=?, description=?, onset_date=?, status=?, " +
                    "appointment_id=? WHERE condition_id=?";
        try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, condition.getConditionName());
            pstmt.setString(2, condition.getDescription());
            pstmt.setDate(3, Date.valueOf(condition.getOnSetDate()));
            pstmt.setString(4, condition.getStatus());
            pstmt.setInt(5, condition.getAppointmentId());
            pstmt.setInt(6, condition.getConditionId());

            return pstmt.executeUpdate() > 0;
        }
    }
    
    // Add method to get conditions by appointment ID
    public List<Condition> getConditionsByAppointmentId(int appointmentId) throws SQLException {
        List<Condition> conditions = new ArrayList<>();
        String sql = "SELECT * FROM conditions WHERE appointment_id = ?";
        try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, appointmentId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    conditions.add(new Condition(
                            rs.getInt("condition_id"),
                            rs.getInt("patient_id"),
                            rs.getString("condition_name"),
                            rs.getString("description"),
                            rs.getDate("onset_date").toLocalDate(),
                            rs.getString("status"),
                            rs.getInt("appointment_id")
                    ));
                }
            }
        }
        return conditions;
    }
}
*/

-- Java code for PrescriptionDAO.java
/*
package edu.careflow.repository.dao;

import edu.careflow.manager.DatabaseManager;
import edu.careflow.repository.entities.Prescription;
import edu.careflow.repository.entities.PrescriptionDetails;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PrescriptionDAO {
    // ... existing code ...
    
    public int addPrescription(Prescription prescription) throws SQLException {
        String sql = "INSERT INTO prescriptions (patient_id, doctor_id, issue_date, valid_until, appointment_id) " +
                    "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, prescription.getPatientId());
            pstmt.setInt(2, prescription.getDoctorId());
            pstmt.setDate(3, Date.valueOf(prescription.getIssueDate()));
            pstmt.setDate(4, Date.valueOf(prescription.getValidUntil()));
            pstmt.setInt(5, prescription.getAppointmentId());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating prescription failed, no rows affected.");
            }

            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
                else {
                    throw new SQLException("Creating prescription failed, no ID obtained.");
                }
            }
        }
    }
    
    // Add method to get prescriptions by appointment ID
    public List<Prescription> getPrescriptionsByAppointmentId(int appointmentId) throws SQLException {
        List<Prescription> prescriptions = new ArrayList<>();
        String sql = "SELECT * FROM prescriptions WHERE appointment_id = ?";
        try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, appointmentId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    prescriptions.add(new Prescription(
                            rs.getInt("prescription_id"),
                            rs.getInt("patient_id"),
                            rs.getInt("doctor_id"),
                            rs.getDate("issue_date").toLocalDate(),
                            rs.getDate("valid_until") != null ? rs.getDate("valid_until").toLocalDate() : null,
                            rs.getTimestamp("created_at").toLocalDateTime(),
                            rs.getInt("appointment_id")
                    ));
                }
            }
        }
        return prescriptions;
    }
}
*/ 