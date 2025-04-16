package edu.careflow.repository.dao;

import edu.careflow.manager.DatabaseManager;
import edu.careflow.repository.entities.Patient;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PatientDAO {

    public PatientDAO() {
        DatabaseManager dbManager = DatabaseManager.getInstance();
    }

    /**
     * Check if a patient exists in the database
     * @param patientId The patient ID to check
     * @return true if the patient exists, false otherwise
     * @throws SQLException If a database access error occurs
     */
    public boolean patientExists(int patientId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM patients WHERE patient_id = ?";
        try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, patientId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    /** Atay tura diay sayop piste
     * Validate patient login using patient ID and date of birth
     * @param patientId The patient ID to validate
     * @param dateOfBirth The date of birth to validate
     * @return true if validation is successful, false otherwise
     * @throws SQLException If a database access error occurs
     */
//    public boolean validatePatientLogin(int patientId, LocalDate dateOfBirth) throws SQLException {
//        String sql = "SELECT COUNT(*) FROM patients WHERE patient_id = ? AND date_of_birth = ?";
//        try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql)) {
//            pstmt.setInt(1, patientId);
//            pstmt.setString(2, dateOfBirth.toString());
//
//            try (ResultSet rs = pstmt.executeQuery()) {
//                return rs.next() && rs.getInt(1) > 0;
//            }
//        }
//    }

    /**
     * Get a patient by ID
     * @param patientId The patient ID to retrieve
     * @return Patient object if found, null otherwise
     * @throws SQLException If a database access error occurs
     */
    public Patient getPatientById(int patientId) throws SQLException {
        String sql = "SELECT * FROM patients WHERE patient_id = ?";
        try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, patientId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Patient(
                        rs.getInt("patient_id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getDate("date_of_birth").toLocalDate(),
                        rs.getString("gender"),
                        rs.getString("contact_number"),
                        rs.getString("email"),
                        rs.getString("address"),
                        rs.getTimestamp("created_at").toLocalDateTime(),
                        null // updatedAt is not in the database schema
                    );
                }
            }
        }
        return null;
    }

    /**
     * Generate a random 5-digit unique patient ID
     * @return A unique 5-digit patient ID
     * @throws SQLException If a database access error occurs
     */
    public int generateRandomPatientId() throws SQLException {
        Random random = new Random();
        int patientId;
        do {
            patientId = 10000 + random.nextInt(90000); // Generates a random 5-digit number between 10000 and 99999
        } while (patientExists(patientId)); // Ensure the ID is unique
        return patientId;
    }

    /**
     * Insert a new patient into the database
     * @param patient The patient to insert
     * @return The generated patient ID
     * @throws SQLException If a database access error occurs
     */
    public int insertPatient(Patient patient) throws SQLException {
        int patientId = generateRandomPatientId();
        String sql = "INSERT INTO patients (patient_id, first_name, last_name, date_of_birth, gender, contact_number, email, address) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, patientId);
            pstmt.setString(2, patient.getFirstName());
            pstmt.setString(3, patient.getLastName());
            pstmt.setDate(4, Date.valueOf(patient.getDateOfBirth()));
            pstmt.setString(5, patient.getGender());
            pstmt.setString(6, patient.getContact());
            pstmt.setString(7, patient.getEmail());
            pstmt.setString(8, patient.getAddress());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating patient failed, no rows affected.");
            }

            return patientId;
        }
    }

    /**
     * Update an existing patient
     * @param patientId The ID of the patient to update
     * @param updatedPatient The updated patient data
     * @return true if the update was successful, false otherwise
     * @throws SQLException If a database access error occurs
     */
    public boolean updatePatient(int patientId, Patient updatedPatient) throws SQLException {
        String sql = "UPDATE patients SET first_name=?, last_name=?, date_of_birth=?, gender=?, contact_number=?, email=?, address=? WHERE patient_id=?";
        try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, updatedPatient.getFirstName());
            pstmt.setString(2, updatedPatient.getLastName());
            pstmt.setDate(3, Date.valueOf(updatedPatient.getDateOfBirth()));
            pstmt.setString(4, updatedPatient.getGender());
            pstmt.setString(5, updatedPatient.getContact());
            pstmt.setString(6, updatedPatient.getEmail());
            pstmt.setString(7, updatedPatient.getAddress());
            pstmt.setInt(8, patientId);

            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * Delete a patient from the database
     * @param patientId The ID of the patient to delete
     * @return true if the deletion was successful, false otherwise
     * @throws SQLException If a database access error occurs
     */
    public boolean deletePatient(int patientId) throws SQLException {
        String sql = "DELETE FROM patients WHERE patient_id=?";
        try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, patientId);
            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * Retrieve all patients from the database
     * @return A list of all patients
     * @throws SQLException If a database access error occurs
     */
    public List<Patient> getAllPatients() throws SQLException {
        List<Patient> patients = new ArrayList<>();
        String sql = "SELECT * FROM patients";

        try (Statement stmt = DatabaseManager.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                patients.add(new Patient(
                    rs.getInt("patient_id"),
                    rs.getString("first_name"),
                    rs.getString("last_name"),
                    rs.getDate("date_of_birth").toLocalDate(),
                    rs.getString("gender"),
                    rs.getString("contact_number"),
                    rs.getString("email"),
                    rs.getString("address"),
                    rs.getTimestamp("created_at").toLocalDateTime(),
                    null // updatedAt is not in the database schema
                ));
            }
        }
        return patients;
    }

    /**
     * Validate patient credentials using patient ID and date of birth
     * @param patientId The patient ID to validate
     * @param dateOfBirth The date of birth to validate
     * @return true if validation is successful, false otherwise
     * @throws SQLException If a database access error occurs
     */
    public boolean validatePatient(int patientId, LocalDate dateOfBirth) throws SQLException {
        String sql = "SELECT COUNT(*) FROM patients WHERE patient_id = ? AND date_of_birth = ?";
        try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, patientId);
            pstmt.setDate(2, Date.valueOf(dateOfBirth));
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }
}