package edu.careflow.repository.dao;

import edu.careflow.manager.DatabaseManager;
import edu.careflow.repository.entities.VisitNote;
import edu.careflow.utils.DateTimeUtil;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.sql.Timestamp;

public class VisitNoteDAO {
    
    /**
     * Save a new visit note to the database
     * @param visitNote The visit note to save
     * @return true if successful, false otherwise
     * @throws SQLException If a database access error occurs
     */
    public boolean saveVisitNote(VisitNote visitNote) throws SQLException {
        int uniqueId = generateUniqueVisitNoteId();
        visitNote.setVisitNoteId(uniqueId);

        String sql = "INSERT INTO visit_notes (visit_note_id, patient_id, doctor_id, visit_date, primary_diagnosis, " +
                    "secondary_diagnosis, notes, image_data, created_at, updated_at, status, appointment_id, created_at) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, datetime('now', 'localtime'))";

        try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, visitNote.getVisitNoteId());
            pstmt.setInt(2, visitNote.getPatientId());
            pstmt.setInt(3, visitNote.getDoctorId());
            pstmt.setDate(4, java.sql.Date.valueOf(visitNote.getVisitDate()));
            pstmt.setString(5, visitNote.getPrimaryDiagnosis());
            pstmt.setString(6, visitNote.getSecondaryDiagnosis());
            pstmt.setString(7, visitNote.getNotes());
            pstmt.setBytes(8, visitNote.getImageData());
            pstmt.setTimestamp(9, Timestamp.valueOf(visitNote.getCreatedAt()));
            pstmt.setTimestamp(10, Timestamp.valueOf(visitNote.getUpdatedAt()));
            pstmt.setString(11, visitNote.getStatus());
            pstmt.setInt(12, visitNote.getAppointmentId());

            return pstmt.executeUpdate() > 0;
        }
    }
    
    /**
     * Get all visit notes for a patient
     * @param patientId The patient ID
     * @return List of visit notes
     * @throws SQLException If a database access error occurs
     */
    public List<VisitNote> getVisitNotesByPatientId(int patientId) throws SQLException {
        List<VisitNote> visitNotes = new ArrayList<>();
        String sql = "SELECT * FROM visit_notes WHERE patient_id = ? ORDER BY visit_date DESC";
        
        try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, patientId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    visitNotes.add(new VisitNote(
                        rs.getInt("visit_note_id"),
                        rs.getInt("patient_id"),
                        rs.getInt("doctor_id"),
                        rs.getDate("visit_date").toLocalDate(),
                        rs.getString("primary_diagnosis"),
                        rs.getString("secondary_diagnosis"),
                        rs.getString("notes"),
                        rs.getBytes("image_data"),
                        DateTimeUtil.fromTimestamp(rs.getTimestamp("created_at")),
                        DateTimeUtil.fromTimestamp(rs.getTimestamp("updated_at")),
                        rs.getString("status"),
                        rs.getInt("appointment_id")
                    ));
                }
            }
        }
        return visitNotes;
    }
    
    /**
     * Get a specific visit note by ID
     * @param visitNoteId The visit note ID
     * @return The visit note if found, null otherwise
     * @throws SQLException If a database access error occurs
     */
    public VisitNote getVisitNoteById(int visitNoteId) throws SQLException {
        String sql = "SELECT * FROM visit_notes WHERE visit_note_id = ?";
        
        try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, visitNoteId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new VisitNote(
                        rs.getInt("visit_note_id"),
                        rs.getInt("patient_id"),
                        rs.getInt("doctor_id"),
                        rs.getDate("visit_date").toLocalDate(),
                        rs.getString("primary_diagnosis"),
                        rs.getString("secondary_diagnosis"),
                        rs.getString("notes"),
                        rs.getBytes("image_data"),
                        DateTimeUtil.fromTimestamp(rs.getTimestamp("created_at")),
                        DateTimeUtil.fromTimestamp(rs.getTimestamp("updated_at")),
                        rs.getString("status"),
                        rs.getInt("appointment_id")
                    );
                }
            }
        }
        return null;
    }
    
    /**
     * Update an existing visit note
     * @param visitNote The updated visit note
     * @return true if successful, false otherwise
     * @throws SQLException If a database access error occurs
     */
    public boolean updateVisitNote(VisitNote visitNote) throws SQLException {
        String sql = "UPDATE visit_notes SET patient_id = ?, doctor_id = ?, primary_diagnosis = ?, " +
                    "secondary_diagnosis = ?, notes = ?, image_data = ?, updated_at = ?, status = ?, updated_at = datetime('now', 'localtime')" +
                    "WHERE visit_note_id = ?";
        
        try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, visitNote.getPatientId());
            pstmt.setInt(2, visitNote.getDoctorId());
            pstmt.setString(3, visitNote.getPrimaryDiagnosis());
            pstmt.setString(4, visitNote.getSecondaryDiagnosis());
            pstmt.setString(5, visitNote.getNotes());
            pstmt.setBytes(6, visitNote.getImageData());
            pstmt.setTimestamp(7, Timestamp.valueOf(visitNote.getUpdatedAt()));
            pstmt.setString(8, visitNote.getStatus());
            pstmt.setInt(9, visitNote.getVisitNoteId());
            
            return pstmt.executeUpdate() > 0;
        }
    }
    
    /**
     * Update the status of a visit note
     * @param visitNoteId The visit note ID
     * @param status The new status
     * @return true if successful, false otherwise
     * @throws SQLException If a database access error occurs
     */
    public boolean updateVisitNoteStatus(int visitNoteId, String status) throws SQLException {
        String sql = "UPDATE visit_notes SET status = ?, updated_at = ? WHERE visit_note_id = ?";
        
        try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, status);
            pstmt.setTimestamp(2, Timestamp.valueOf(DateTimeUtil.now()));
            pstmt.setInt(3, visitNoteId);
            
            return pstmt.executeUpdate() > 0;
        }
    }
    
    /**
     * Delete a visit note
     * @param visitNoteId The visit note ID to delete
     * @return true if successful, false otherwise
     * @throws SQLException If a database access error occurs
     */
    public boolean deleteVisitNote(int visitNoteId) throws SQLException {
        String sql = "DELETE FROM visit_notes WHERE visit_note_id = ?";
        
        try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, visitNoteId);
            return pstmt.executeUpdate() > 0;
        }
    }

    public int generateUniqueVisitNoteId() throws SQLException {
        while (true) {
            int randomId = 10000 + (int)(Math.random() * 90000);
            String sql = "SELECT COUNT(*) FROM visit_notes WHERE visit_note_id = ?";
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

    public List<VisitNote> getVisitNotesByAppointmentId(int appointmentId) throws SQLException {
        List<VisitNote> visitNotes = new ArrayList<>();
        String sql = "SELECT * FROM visit_notes WHERE appointment_id = ? ORDER BY visit_date DESC";
        
        try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, appointmentId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    visitNotes.add(new VisitNote(
                        rs.getInt("visit_note_id"),
                        rs.getInt("patient_id"),
                        rs.getInt("doctor_id"),
                        rs.getDate("visit_date").toLocalDate(),
                        rs.getString("primary_diagnosis"),
                        rs.getString("secondary_diagnosis"),
                        rs.getString("notes"),
                        rs.getBytes("image_data"),
                        DateTimeUtil.fromTimestamp(rs.getTimestamp("created_at")),
                        DateTimeUtil.fromTimestamp(rs.getTimestamp("updated_at")),
                        rs.getString("status"),
                        rs.getInt("appointment_id")
                    ));
                }
            }
        }
        return visitNotes;
    }
} 