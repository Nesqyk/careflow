package edu.careflow.repository.dao;

import edu.careflow.manager.DatabaseManager;
import edu.careflow.repository.entities.Prescription;
import edu.careflow.repository.entities.PrescriptionDetails;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PrescriptionDAO {


    /**
     * Update an existing prescription
     * @param prescriptionId The ID of the prescription to update
     * @param validUntil The new validity date
     * @return true if the update was successful, false otherwise
     * @throws SQLException If a database access error occurs
     */
    public boolean updatePrescription(int prescriptionId, LocalDate validUntil) throws SQLException {
        String sql = "UPDATE prescriptions SET valid_until = ? WHERE prescription_id = ?";
        try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            pstmt.setDate(1, Date.valueOf(validUntil));
            pstmt.setInt(2, prescriptionId);

            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * Add prescription details for a prescription
     * @param prescriptionId The ID of the prescription
     * @param medicationName The name of the medication
     * @param dosage The dosage of the medication
     * @param frequency The frequency of taking the medication
     * @param instructions Additional instructions
     * @return true if successful, false otherwise
     * @throws SQLException If a database access error occurs
     */
    public boolean addPrescriptionDetails(int prescriptionId, String medicationName, String dosage, String frequency, String instructions) throws SQLException {
        String sql = "INSERT INTO prescription_details (prescription_id, medication_name, dosage, frequency, instructions) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, prescriptionId);
            pstmt.setString(2, medicationName);
            pstmt.setString(3, dosage);
            pstmt.setString(4, frequency);
            pstmt.setString(5, instructions);

            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * Get prescription details for a specific prescription
     * @param prescriptionId The ID of the prescription
     * @return A single prescription detail
     * @throws SQLException If a database access error occurs
     */
    public PrescriptionDetails getPrescriptionDetails(int prescriptionId) throws SQLException {
        String sql = "SELECT * FROM prescription_details WHERE prescription_id = ?";
        try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, prescriptionId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new PrescriptionDetails(
                            rs.getInt("detail_id"),
                            rs.getString("medication_name"),
                            rs.getString("dosage"),
                            rs.getString("frequency"),
                            rs.getString("instructions")
                    );
                }
            }
        }
        return null; // Return null if no details are found
    }

    /**
     * Add a new prescription using Prescription object
     * @param prescription The prescription to add
     * @return The generated prescription ID
     * @throws SQLException If a database access error occurs
     */
    public int addPrescription(Prescription prescription) throws SQLException {
        String sql = "INSERT INTO prescriptions (patient_id, doctor_id, issue_date, valid_until) VALUES (?, ?, ?, ?)";
        try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, prescription.getPatientId());
            pstmt.setInt(2, prescription.getDoctorId());
            pstmt.setDate(3, Date.valueOf(prescription.getIssueDate()));
            pstmt.setDate(4, Date.valueOf(prescription.getValidUntil()));

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

    /**
     * Delete a prescription
     * @param prescriptionId The ID of the prescription to delete
     * @return true if the deletion was successful, false otherwise
     * @throws SQLException If a database access error occurs
     */
    public boolean deletePrescription(int prescriptionId) throws SQLException {
        String sql = "DELETE FROM prescriptions WHERE prescription_id = ?";
        try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, prescriptionId);
            return pstmt.executeUpdate() > 0;
        }
    }


    /**
     * Get all prescriptions for a specific patient
     * @param patientId The ID of the patient
     * @return List of prescriptions for the patient
     * @throws SQLException If a database access error occurs
     */
    public List<Prescription> getPrescriptionsByPatientId(int patientId) throws SQLException {
        List<Prescription> prescriptions = new ArrayList<>();
        String sql = "SELECT * FROM prescriptions WHERE patient_id = ?";
        try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, patientId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    prescriptions.add(new Prescription(
                            rs.getInt("prescription_id"),
                            rs.getInt("patient_id"),
                            rs.getInt("doctor_id"),
                            rs.getDate("issue_date").toLocalDate(),
                            rs.getDate("valid_until") != null ? rs.getDate("valid_until").toLocalDate() : null,
                            rs.getTimestamp("created_at").toLocalDateTime()
                    ));
                }
            }
        }
        return prescriptions;
    }

    /**
     * Get all active prescriptions for a specific patient
     * @param patientId The ID of the patient
     * @return List of active prescriptions for the patient
     * @throws SQLException If a database access error occurs
     */
    public List<Prescription> getActivePrescriptions(int patientId) throws SQLException {
        List<Prescription> prescriptions = new ArrayList<>();
        String sql = "SELECT * FROM prescriptions WHERE patient_id = ? AND valid_until >= CURRENT_DATE";
        try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, patientId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    prescriptions.add(new Prescription(
                            rs.getInt("prescription_id"),
                            rs.getInt("patient_id"),
                            rs.getInt("doctor_id"),
                            rs.getDate("issue_date").toLocalDate(),
                            rs.getDate("valid_until") != null ? rs.getDate("valid_until").toLocalDate() : null,
                            rs.getTimestamp("created_at").toLocalDateTime()
                    ));
                }
            }
        }
        return prescriptions;
    }

    /**
     * Get all past prescriptions for a specific patient
     * @param patientId The ID of the patient
     * @return List of past prescriptions for the patient
     * @throws SQLException If a database access error occurs
     */
    public List<Prescription> getPastMedication(int patientId) throws SQLException {
        List<Prescription> prescriptions = new ArrayList<>();
        String sql = "SELECT * FROM prescriptions WHERE patient_id = ? AND valid_until < CURRENT_DATE";
        try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, patientId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    prescriptions.add(new Prescription(
                            rs.getInt("prescription_id"),
                            rs.getInt("patient_id"),
                            rs.getInt("doctor_id"),
                            rs.getDate("issue_date").toLocalDate(),
                            rs.getDate("valid_until") != null ? rs.getDate("valid_until").toLocalDate() : null,
                            rs.getTimestamp("created_at").toLocalDateTime()
                    ));
                }
            }
        }
        return prescriptions;
    }
}

