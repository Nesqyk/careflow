package edu.careflow.repository.dao;

import edu.careflow.manager.DatabaseManager;
import edu.careflow.repository.entities.Nurse;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class NurseDAO {

    public List<Nurse> getAllNurses() throws SQLException {
        List<Nurse> nurses = new ArrayList<>();
        String sql = "SELECT * FROM nurses";
        try (Statement stmt = DatabaseManager.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                nurses.add(mapResultSetToNurse(rs));
            }
        }
        return nurses;
    }

    public Nurse getNurseById(int nurseId) throws SQLException {
        String sql = "SELECT * FROM nurses WHERE nurse_id = ?";
        try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, nurseId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToNurse(rs);
                }
            }
        }
        return null;
    }

    public boolean updateNurse(Nurse nurse) throws SQLException {
        String sql = "UPDATE nurses SET first_name=?, last_name=?, license_number=?, contact_number=? WHERE nurse_id=?";
        
        try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, nurse.getFirstName());
            pstmt.setString(2, nurse.getLastName());
            pstmt.setString(3, nurse.getLicenseNumber());
            pstmt.setString(4, nurse.getPhone());
            pstmt.setInt(5, nurse.getNurseId());

            return pstmt.executeUpdate() > 0;
        }
    }

    private Nurse mapResultSetToNurse(ResultSet rs) throws SQLException {
        return new Nurse(
            rs.getInt("nurse_id"),
            rs.getString("first_name"),
            rs.getString("last_name"),
            rs.getString("license_number"),
            rs.getString("contact_number"),
            rs.getTimestamp("created_at").toLocalDateTime().toLocalDate()
        );
    }

    // Add more CRUD methods as needed
} 