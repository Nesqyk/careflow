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


    /**
     * Get all conditions for a specific patient
     * @param patientId The ID of the patient
     * @return List of conditions for the patient
     * @throws SQLException If a database access error occurs
     */
    public static List<Condition> getConditionsByPatientId(int patientId) throws SQLException {
        List<Condition> conditions = new ArrayList<>();
        String sql = "SELECT * FROM conditions WHERE patient_id = ?";
        try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, patientId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    conditions.add(new Condition(
                            rs.getInt("condition_id"),
                            rs.getInt("patient_id"),
                            rs.getString("condition_name"),
                            rs.getString("description"),
                            rs.getDate("onset_date").toLocalDate(),
                            rs.getString("status")
                    ));
                }
            }
        }
        return conditions;
    }

    /**
     * Add a new condition for a patient
     * @param condition The condition to add
     * @return true if the condition was added successfully, false otherwise
     * @throws SQLException If a database access error occurs
     */
    public boolean addCondition(Condition condition) throws SQLException {
        String sql = "INSERT INTO conditions (patient_id, condition_id, condition_name, description, onset_date, status) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, condition.getPatientId());
            pstmt.setInt(2, condition.getConditionId());
            pstmt.setString(3, condition.getConditionName());
            pstmt.setString(4, condition.getDescription());
            pstmt.setDate(5, Date.valueOf(condition.getOnSetDate()));
            pstmt.setString(6, condition.getStatus());

            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * Remove a condition by ID
     * @param conditionId The ID of the condition to remove
     * @return true if the condition was removed successfully, false otherwise
     * @throws SQLException If a database access error occurs
     */
    public boolean removeCondition(int conditionId) throws SQLException {
        String sql = "DELETE FROM conditions WHERE condition_id = ?";
        try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, conditionId);
            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * Update an existing condition
     * @param condition The updated condition data
     * @return true if the condition was updated successfully, false otherwise
     * @throws SQLException If a database access error occurs
     */
    public boolean updateCondition(Condition condition) throws SQLException {
        String sql = "UPDATE conditions SET condition_name=?, description=?, onset_date=?, status=? WHERE condition_id=?";
        try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, condition.getConditionName());
            pstmt.setString(2, condition.getDescription());
            pstmt.setDate(3, Date.valueOf(condition.getOnSetDate()));
            pstmt.setString(4, condition.getStatus());
            pstmt.setInt(5, condition.getConditionId());

            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * Generate a unique 5-digit condition ID (not present in the conditions table)
     * @return unique 5-digit integer ID
     * @throws SQLException if a database access error occurs
     */
    public int generateUniqueConditionId() throws SQLException {
        while (true) {
            int randomId = 10000 + (int)(Math.random() * 90000); // 5-digit number
            String sql = "SELECT COUNT(*) FROM conditions WHERE condition_id = ?";
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
