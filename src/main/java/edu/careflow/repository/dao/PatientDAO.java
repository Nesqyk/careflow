package edu.careflow.repository.dao;

import edu.careflow.manager.DatabaseManager;
import edu.careflow.repository.entities.Allergy;
import edu.careflow.repository.entities.Patient;
import edu.careflow.utils.DateTimeUtil;
import javafx.scene.image.Image;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;

public class PatientDAO {



    public PatientDAO() {
        DatabaseManager dbManager = DatabaseManager.getInstance();
    }


    /**
     * Generate a random 5-digit unique allergy ID
     * @return A unique 5-digit allergy ID
     * @throws SQLException If a database access error occurs
     */
    private int generateUniqueAllergyId() throws SQLException {
        Random random = new Random();
        int allergyId;
        do {
            allergyId = 10000 + random.nextInt(90000); // Generates a random 5-digit number between 10000 and 99999
        } while (allergyExists(allergyId)); // Ensure the ID is unique
        return allergyId;
    }

    /**
     * Check if an allergy ID exists in the database
     * @param allergyId The allergy ID to check
     * @return true if the allergy exists, false otherwise
     * @throws SQLException If a database access error occurs
     */
    private boolean allergyExists(int allergyId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM allergies WHERE allergy_id = ?";
        try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, allergyId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    public void addAllergy(Allergy allergy) throws SQLException {
        // Generate a unique 5-digit allergy ID
        int allergyId = generateUniqueAllergyId();
        
        // Create a new Allergy object with the generated ID
        Allergy newAllergy = new Allergy(
            allergyId,
            allergy.getPatientId(),
            allergy.getAllergen(),
            allergy.getSeverity(),
            allergy.getComment(),
            allergy.getAppointmentId()
        );
        
        // Copy reactions if any
        if (allergy.getReactions() != null) {
            for (String reaction : allergy.getReactions()) {
                newAllergy.addReaction(reaction);
            }
        }

        String sqlAllergy = "INSERT INTO allergies (allergy_id, patient_id, allergen, severity, comment, appointment_id) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sqlAllergy)) {
            stmt.setInt(1, newAllergy.getAllergyId());
            stmt.setInt(2, newAllergy.getPatientId());
            stmt.setString(3, newAllergy.getAllergen());
            stmt.setString(4, newAllergy.getSeverity());
            stmt.setString(5, newAllergy.getComment());
            stmt.setInt(6, newAllergy.getAppointmentId());
            stmt.executeUpdate();

            if(newAllergy.getReactions() != null) {
                for (String reaction : newAllergy.getReactions()) {
                    String sqlReaction = "INSERT INTO allergy_reactions (allergy_id, reaction_description) VALUES (?, ?)";
                    try (PreparedStatement stmtReaction = DatabaseManager.getConnection().prepareStatement(sqlReaction)) {
                        stmtReaction.setInt(1, newAllergy.getAllergyId());
                        stmtReaction.setString(2, reaction);
                        stmtReaction.executeUpdate();
                    }
                }
            }
        }
    }

    public List<Allergy> getAllergies(int patientId) throws SQLException {
        String sql = "SELECT a.allergy_id, a.patient_id, a.allergen, a.severity, a.comment, a.appointment_id, "
                + "ar.reaction_description FROM allergies a "
                + "LEFT JOIN allergy_reactions ar ON a.allergy_id = ar.allergy_id "
                + "WHERE a.patient_id = ?";

        List<Allergy> allergies = new ArrayList<>();
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, patientId);
            ResultSet rs = stmt.executeQuery();

            Map<Integer, Allergy> allergyMap = new HashMap<>();
            while (rs.next()) {
                int allergyId = rs.getInt("allergy_id");
                Allergy allergy = allergyMap.get(allergyId);
                if (allergy == null) {
                    allergy = new Allergy(
                            allergyId,
                            rs.getInt("patient_id"),
                            rs.getString("allergen"),
                            rs.getString("severity"),
                            rs.getString("comment"),
                            rs.getInt("appointment_id")
                    );
                    allergyMap.put(allergyId, allergy);
                    allergies.add(allergy);
                }
                String reaction = rs.getString("reaction_description");
                if (reaction != null) {
                    allergy.addReaction(reaction);
                }
            }
        }
        return allergies;
    }

    public List<Allergy> getAllergiesByAppointmentId(int appointmentId) throws SQLException {
        String sql = "SELECT a.allergy_id, a.patient_id, a.allergen, a.severity, a.comment, a.appointment_id, "
                + "ar.reaction_description FROM allergies a "
                + "LEFT JOIN allergy_reactions ar ON a.allergy_id = ar.allergy_id "
                + "WHERE a.appointment_id = ?";

        List<Allergy> allergies = new ArrayList<>();
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, appointmentId);
            ResultSet rs = stmt.executeQuery();

            Map<Integer, Allergy> allergyMap = new HashMap<>();
            while (rs.next()) {
                int allergyId = rs.getInt("allergy_id");
                Allergy allergy = allergyMap.get(allergyId);
                if (allergy == null) {
                    allergy = new Allergy(
                            allergyId,
                            rs.getInt("patient_id"),
                            rs.getString("allergen"),
                            rs.getString("severity"),
                            rs.getString("comment"),
                            rs.getInt("appointment_id")
                    );
                    allergyMap.put(allergyId, allergy);
                    allergies.add(allergy);
                }
                String reaction = rs.getString("reaction_description");
                if (reaction != null) {
                    allergy.addReaction(reaction);
                }
            }
        }
        return allergies;
    }

    public void updateAllergy(int allergyId, String allergen, String severity, String comment) throws SQLException {
        String sql = "UPDATE allergies SET allergen = ?, severity = ?, comment = ? WHERE allergy_id = ?";
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            stmt.setString(1, allergen);
            stmt.setString(2, severity);
            stmt.setString(3, comment);
            stmt.setInt(4, allergyId);
            stmt.executeUpdate();
        }
    }

    public void deleteAllergy(int allergyId) throws SQLException {
        // Delete reactions first
        String sqlReactions = "DELETE FROM allergy_reactions WHERE allergy_id = ?";
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sqlReactions)) {
            stmt.setInt(1, allergyId);
            stmt.executeUpdate();
        }

        // Delete allergy
        String sqlAllergy = "DELETE FROM allergies WHERE allergy_id = ?";
        try (PreparedStatement stmt = DatabaseManager.getConnection().prepareStatement(sqlAllergy)) {
            stmt.setInt(1, allergyId);
            stmt.executeUpdate();
        }
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

    /**
     * Insert or update patient avatar
     * @param patientId  The patient ID to load the avatar for
     * @param imageFile  The path of image to load for
     * @throws IOException If an I/O error occurs
     * @throws SQLException If a database access error occurs
     */
    public void insertAvatar(int patientId, File imageFile) throws IOException, SQLException {
        byte[] imageData = Files.readAllBytes(imageFile.toPath());
        String sql = "UPDATE patients SET image_avatar = ? WHERE patient_id = ?";
        try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            pstmt.setBytes(1, imageData);
            pstmt.setInt(2, patientId);
            pstmt.executeUpdate();
        }
    }

    /**
     * Load patient avatar
     * @param patientId The patient ID to load the avatar for
     * @return Patient's Image Avatar
     * @throws SQLException  If a database access error occurs
     */
    public Image loadAvatar(int patientId) throws SQLException {
        String sql = "SELECT image_avatar FROM patients WHERE patient_id = ?";
        try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, patientId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                byte[] imageData = rs.getBytes("image_avatar");
                if (imageData != null) {
                    return new Image(new ByteArrayInputStream(imageData));
                }
            }
        }
        return null;
    }


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
                        DateTimeUtil.fromTimestamp(rs.getTimestamp("created_at")),
                        DateTimeUtil.fromTimestamp(rs.getTimestamp("updated_at"))
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
        String sql = "UPDATE patients SET first_name=?, last_name=?, date_of_birth=?, gender=?, contact_number=?, email=?, address=?, updated_at=CURRENT_TIMESTAMP WHERE patient_id=?";
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
                    DateTimeUtil.fromTimestamp(rs.getTimestamp("created_at")),
                    DateTimeUtil.fromTimestamp(rs.getTimestamp("updated_at"))
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


//    /**
//     * Add a new prescription
//     * @param patientId The ID of the patient
//     * @param doctorId The ID of the doctor
//     * @param issueDate The issue date of the prescription
//     * @param validUntil The validity date of the prescription
//     * @return The generated prescription ID
//     * @throws SQLException If a database access error occurs
//     */
//    public int addPrescription(int patientId, int doctorId, LocalDate issueDate, LocalDate validUntil) throws SQLException {
//        String sql = "INSERT INTO prescriptions (patient_id, doctor_id, issue_date, valid_until) VALUES (?, ?, ?, ?)";
//        try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
//            pstmt.setInt(1, patientId);
//            pstmt.setInt(2, doctorId);
//            pstmt.setDate(3, Date.valueOf(issueDate));
//            pstmt.setDate(4, Date.valueOf(validUntil));
//
//            int affectedRows = pstmt.executeUpdate();
//            if (affectedRows == 0) {
//                throw new SQLException("Creating prescription failed, no rows affected.");
//            }
//
//            try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
//                if (generatedKeys.next()) {
//                    return generatedKeys.getInt(1);
//                }
//                else {
//                    throw new SQLException("Creating prescription failed, no ID obtained.");
//                }
//            }
//        }
//    }

    /**
     * Generate a unique, random Jitsi meeting link
     * @return A unique Jitsi meeting link (e.g., https://meet.jit.si/RoomName)
     * @throws SQLException If a database access error occurs
     */
    public String generateUniqueJitsiMeetingLink() throws SQLException {
        String baseUrl = "https://meet.jit.si/";
        String roomName;
        boolean isUnique = false;
        do {
            roomName = generateRandomRoomName(12);
            // Check if this room name already exists in the database (assuming a 'meeting_link' column in 'appointments')
            String sql = "SELECT COUNT(*) FROM appointments WHERE meeting_link = ?";
            try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql)) {
                pstmt.setString(1, baseUrl + roomName);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        isUnique = rs.getInt(1) == 0;
                    }
                }
            }
        } while (!isUnique);
        return baseUrl + roomName;
    }

    /**
     * Generate a random alphanumeric room name for Jitsi
     * @param length Length of the room name
     * @return Random alphanumeric string
     */
    private String generateRandomRoomName(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

}