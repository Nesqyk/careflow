package edu.careflow.repository.dao;

import edu.careflow.manager.DatabaseManager;
import edu.careflow.repository.entities.Prescription;
import edu.careflow.repository.entities.PrescriptionDetails;
import edu.careflow.utils.DateTimeUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MedicationDAO {
    /**
     * Save a prescription and its details to the database
     * @param prescription The prescription entity
     * @param details The list of prescription details (medications)
     * @return true if successful, false otherwise
     * @throws SQLException If a database access error occurs
     */
    public boolean savePrescription(Prescription prescription, List<PrescriptionDetails> details) throws SQLException {
        if (prescription == null || details == null || details.isEmpty()) {
            throw new IllegalArgumentException("Prescription and details cannot be null or empty");
        }

        Connection conn = null;
        PreparedStatement pstmtPrescription = null;
        PreparedStatement pstmtDetail = null;
        PreparedStatement pstmtLastId = null;
        ResultSet rs = null;
        try {
            conn = DatabaseManager.getConnection();
            conn.setAutoCommit(false);

            // Validate dates
            if (prescription.getValidUntil().isBefore(prescription.getIssueDate())) {
                throw new IllegalArgumentException("Valid until date must be after issue date");
            }

            // Insert prescription
            String sqlPrescription = "INSERT INTO prescriptions (patient_id, doctor_id, issue_date, valid_until, appointment_id) VALUES (?, ?, ?, ?, ?)";
            pstmtPrescription = conn.prepareStatement(sqlPrescription, Statement.RETURN_GENERATED_KEYS);
            pstmtPrescription.setInt(1, prescription.getPatientId());
            pstmtPrescription.setInt(2, prescription.getDoctorId());
            pstmtPrescription.setDate(3, Date.valueOf(prescription.getIssueDate()));
            pstmtPrescription.setDate(4, Date.valueOf(prescription.getValidUntil()));
            pstmtPrescription.setInt(5, prescription.getAppointmentId());
            pstmtPrescription.executeUpdate();

            // Get the last inserted ID
            String sqlLastId = "SELECT last_insert_rowid()";
            pstmtLastId = conn.prepareStatement(sqlLastId);
            rs = pstmtLastId.executeQuery();
            int prescriptionId;
            if (rs.next()) {
                prescriptionId = rs.getInt(1);
            } else {
                conn.rollback();
                return false;
            }

            // Insert prescription details
            String sqlDetail = "INSERT INTO prescription_details (prescription_id, medication_name, dosage, frequency, duration) VALUES (?, ?, ?, ?, ?)";
            pstmtDetail = conn.prepareStatement(sqlDetail);
            for (PrescriptionDetails detail : details) {
                validatePrescriptionDetail(detail);
                pstmtDetail.setInt(1, prescriptionId);
                pstmtDetail.setString(2, detail.getMedicineName());
                pstmtDetail.setString(3, detail.getDosage());
                pstmtDetail.setString(4, detail.getFrequency());
                pstmtDetail.setString(5, detail.getDuration());
                pstmtDetail.addBatch();
            }
            pstmtDetail.executeBatch();
            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    throw new SQLException("Failed to rollback transaction", ex);
                }
            }
            throw e;
        } finally {
            closeResources(rs, pstmtDetail, pstmtPrescription, pstmtLastId, conn);
        }
    }

    /**
     * Get all prescriptions for a patient
     * @param patientId The patient ID
     * @return List of prescriptions
     * @throws SQLException If a database access error occurs
     */
    public List<Prescription> getPrescriptionsByPatientId(int patientId) throws SQLException {
        List<Prescription> prescriptions = new ArrayList<>();
        String sql = "SELECT * FROM prescriptions WHERE patient_id = ? ORDER BY created_at DESC";
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
                            DateTimeUtil.fromTimestamp(rs.getTimestamp("created_at")),
                            rs.getInt("appointment_id")
                    ));
                }
            }
        }
        return prescriptions;
    }

    /**
     * Get active prescriptions for a patient
     * @param patientId The patient ID
     * @return List of active prescriptions
     * @throws SQLException If a database access error occurs
     */
    public List<Prescription> getActivePrescriptions(int patientId) throws SQLException {
        List<Prescription> prescriptions = new ArrayList<>();
        String sql = "SELECT * FROM prescriptions WHERE patient_id = ? AND valid_until >= CURRENT_DATE ORDER BY created_at DESC";
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
                            DateTimeUtil.fromTimestamp(rs.getTimestamp("created_at")),
                            rs.getInt("appointment_id")
                    ));
                }
            }
        }
        return prescriptions;
    }

    /**
     * Get prescription details for a prescription
     * @param prescriptionId The prescription ID
     * @return List of prescription details
     * @throws SQLException If a database access error occurs
     */
    public List<PrescriptionDetails> getPrescriptionDetailsByPrescriptionId(int prescriptionId) throws SQLException {
        List<PrescriptionDetails> details = new ArrayList<>();
        String sql = "SELECT * FROM prescription_details WHERE prescription_id = ?";
        try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, prescriptionId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    details.add(mapResultSetToPrescriptionDetail(rs));
                }
            }
        }
        return details;
    }

    /**
     * Update a prescription
     * @param prescription The prescription to update
     * @return true if successful
     * @throws SQLException If a database access error occurs
     */
    public boolean updatePrescription(Prescription prescription) throws SQLException {
        if (prescription == null) {
            throw new IllegalArgumentException("Prescription cannot be null");
        }

        validatePrescriptionDates(prescription);

        String sql = "UPDATE prescriptions SET issue_date=?, valid_until=? WHERE prescription_id=?";
        try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            pstmt.setDate(1, Date.valueOf(prescription.getIssueDate()));
            pstmt.setDate(2, Date.valueOf(prescription.getValidUntil()));
            pstmt.setInt(3, prescription.getPrescriptionId());
            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * Update a prescription detail
     * @param detail The detail to update
     * @return true if successful
     * @throws SQLException If a database access error occurs
     */
    public boolean updatePrescriptionDetail(PrescriptionDetails detail) throws SQLException {
        if (detail == null) {
            throw new IllegalArgumentException("Prescription detail cannot be null");
        }

        validatePrescriptionDetail(detail);

        String sql = "UPDATE prescription_details SET medicine_name=?, dosage=?, frequency=?, duration=? WHERE detail_id=?";
        try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, detail.getMedicineName());
            pstmt.setString(2, detail.getDosage());
            pstmt.setString(3, detail.getFrequency());
            pstmt.setString(4, detail.getDuration());
            pstmt.setInt(5, detail.getDetailId());
            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * Delete a prescription and its details
     * @param prescriptionId The prescription ID
     * @return true if successful
     * @throws SQLException If a database access error occurs
     */
    public boolean deletePrescription(int prescriptionId) throws SQLException {
        Connection conn = null;
        try {
            conn = DatabaseManager.getConnection();
            conn.setAutoCommit(false);

            // Delete details first (to maintain referential integrity)
            String sqlDetails = "DELETE FROM prescription_details WHERE prescription_id=?";
            String sqlPrescription = "DELETE FROM prescriptions WHERE prescription_id=?";
            
            try (PreparedStatement pstmtDetails = conn.prepareStatement(sqlDetails);
                 PreparedStatement pstmtPrescription = conn.prepareStatement(sqlPrescription)) {
                
                pstmtDetails.setInt(1, prescriptionId);
                pstmtDetails.executeUpdate();
                
                pstmtPrescription.setInt(1, prescriptionId);
                int affected = pstmtPrescription.executeUpdate();
                
                conn.commit();
                return affected > 0;
            }
        } catch (SQLException e) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException ex) {
                    throw new SQLException("Failed to rollback transaction", ex);
                }
            }
            throw e;
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Delete a prescription detail
     * @param detailId The detail ID
     * @return true if successful
     * @throws SQLException If a database access error occurs
     */
    public boolean deletePrescriptionDetail(int detailId) throws SQLException {
        String sql = "DELETE FROM prescription_details WHERE detail_id=?";
        try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, detailId);
            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * Update a prescription and replace its details
     * @param prescription The prescription to update
     * @param details The new list of prescription details
     * @return true if successful
     * @throws SQLException If a database access error occurs
     */
    public boolean updatePrescriptionWithDetails(Prescription prescription, List<PrescriptionDetails> details) throws SQLException {
        if (prescription == null || details == null || details.isEmpty()) {
            throw new IllegalArgumentException("Prescription and details cannot be null or empty");
        }
        Connection conn = null;
        PreparedStatement pstmtUpdate = null;
        PreparedStatement pstmtDeleteDetails = null;
        PreparedStatement pstmtInsertDetail = null;
        try {
            conn = DatabaseManager.getConnection();
            conn.setAutoCommit(false);

            // Update prescription
            String sqlUpdate = "UPDATE prescriptions SET issue_date=?, valid_until=? WHERE prescription_id=?";
            pstmtUpdate = conn.prepareStatement(sqlUpdate);
            pstmtUpdate.setDate(1, java.sql.Date.valueOf(prescription.getIssueDate()));
            pstmtUpdate.setDate(2, java.sql.Date.valueOf(prescription.getValidUntil()));
            pstmtUpdate.setInt(3, prescription.getPrescriptionId());
            int updated = pstmtUpdate.executeUpdate();
            if (updated == 0) {
                conn.rollback();
                return false;
            }

            // Delete old details
            String sqlDeleteDetails = "DELETE FROM prescription_details WHERE prescription_id=?";
            pstmtDeleteDetails = conn.prepareStatement(sqlDeleteDetails);
            pstmtDeleteDetails.setInt(1, prescription.getPrescriptionId());
            pstmtDeleteDetails.executeUpdate();

            // Insert new details
            String sqlInsertDetail = "INSERT INTO prescription_details (prescription_id, medication_name, dosage, frequency, duration) VALUES (?, ?, ?, ?, ?)";
            pstmtInsertDetail = conn.prepareStatement(sqlInsertDetail);
            for (PrescriptionDetails detail : details) {
                validatePrescriptionDetail(detail);
                pstmtInsertDetail.setInt(1, prescription.getPrescriptionId());
                pstmtInsertDetail.setString(2, detail.getMedicineName());
                pstmtInsertDetail.setString(3, detail.getDosage());
                pstmtInsertDetail.setString(4, detail.getFrequency());
                pstmtInsertDetail.setString(5, detail.getDuration());
                pstmtInsertDetail.addBatch();
            }
            pstmtInsertDetail.executeBatch();
            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { /* ignore */ }
            }
            throw e;
        } finally {
            closeResources(null, pstmtUpdate, pstmtDeleteDetails, pstmtInsertDetail, conn);
        }
    }

    public List<Prescription> getPrescriptionsByAppointmentId(int appointmentId) throws SQLException {
        List<Prescription> prescriptions = new ArrayList<>();
        String sql = "SELECT * FROM prescriptions WHERE appointment_id = ? ORDER BY created_at DESC";
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
                            DateTimeUtil.fromTimestamp(rs.getTimestamp("created_at")),
                            rs.getInt("appointment_id")
                    ));
                }
            }
        }
        return prescriptions;
    }

    // Helper methods
    private Prescription mapResultSetToPrescription(ResultSet rs) throws SQLException {
        return new Prescription(
            rs.getInt("prescription_id"),
            rs.getInt("patient_id"),
            rs.getInt("doctor_id"),
            rs.getDate("issue_date").toLocalDate(),
            rs.getDate("valid_until") != null ? rs.getDate("valid_until").toLocalDate() : null,
            DateTimeUtil.fromTimestamp(rs.getTimestamp("created_at")),
            rs.getInt("appointment_id")
        );
    }

    private PrescriptionDetails mapResultSetToPrescriptionDetail(ResultSet rs) throws SQLException {
        return new PrescriptionDetails(
            rs.getInt("detail_id"),
            rs.getString("medication_name"),
            rs.getString("dosage"),
            rs.getString("frequency"),
            rs.getString("duration")
        );
    }

    private void validatePrescriptionDates(Prescription prescription) {
        if (prescription.getValidUntil().isBefore(prescription.getIssueDate())) {
            throw new IllegalArgumentException("Valid until date must be after issue date");
        }
    }

    private void validatePrescriptionDetail(PrescriptionDetails detail) {
        if (detail.getMedicineName() == null || detail.getMedicineName().trim().isEmpty()) {
            throw new IllegalArgumentException("Medicine name cannot be empty");
        }
        if (detail.getDosage() == null || detail.getDosage().trim().isEmpty()) {
            throw new IllegalArgumentException("Dosage cannot be empty");
        }
        if (detail.getFrequency() == null || detail.getFrequency().trim().isEmpty()) {
            throw new IllegalArgumentException("Frequency cannot be empty");
        }
        if (detail.getDuration() == null || detail.getDuration().trim().isEmpty()) {
            throw new IllegalArgumentException("Duration cannot be empty");
        }
    }

    private void closeResources(ResultSet rs, Object... resources) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        for (Object resource : resources) {
            if (resource != null) {
                try {
                    if (resource instanceof AutoCloseable) {
                        ((AutoCloseable) resource).close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Generate a unique 5-digit prescription ID
     * @return A unique 5-digit prescription ID
     * @throws SQLException If a database access error occurs
     */
    public int generateUniquePrescriptionId() throws SQLException {
        while (true) {
            int randomId = 10000 + (int)(Math.random() * 90000); // 5-digit number
            String sql = "SELECT COUNT(*) FROM prescriptions WHERE prescription_id = ?";
            try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql)) {
                pstmt.setInt(1, randomId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) == 0) {
                        return randomId;
                    }
                }
            }
        }
    }
}
