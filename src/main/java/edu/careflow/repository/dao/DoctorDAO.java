package edu.careflow.repository.dao;

import edu.careflow.manager.DatabaseManager;
import edu.careflow.repository.entities.Doctor;
import edu.careflow.repository.entities.Appointment;
import edu.careflow.repository.entities.Patient;
import edu.careflow.utils.DateTimeUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.time.LocalDateTime;
import java.time.LocalDate;

public class DoctorDAO {


    public List<Doctor> getAllDoctors() throws SQLException {
        List<Doctor> doctors = new ArrayList<>();
        String sql = "SELECT * FROM doctors";
        try (Statement stmt = DatabaseManager.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                java.sql.Timestamp createdTs = rs.getTimestamp("created_at");
                java.time.LocalDateTime createdAt = createdTs != null ? DateTimeUtil.fromTimestamp(createdTs) : null;
                java.sql.Timestamp updatedTs = rs.getTimestamp("updated_at");
                java.time.LocalDateTime updatedAt = updatedTs != null ? DateTimeUtil.fromTimestamp(updatedTs) : null;
                doctors.add(new Doctor(
                        rs.getInt("doctor_id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("specialization"),
                        rs.getString("license_number"),
                        rs.getString("contact_number"),
                        createdAt,
                        updatedAt,
                        rs.getInt("availability") == 1
                ));
            }
        }
        return doctors;
    }
    /**
     * Generate a unique random 5-digit doctor ID
     * @return A unique 5-digit doctor ID
     * @throws SQLException If a database access error occurs
     */
    private String generateUniqueDoctorId() throws SQLException {
        Random random = new Random();
        String doctorId;
        do {
            // Generate a random 5-digit number between 10000 and 99999
            int randomNum = 10000 + random.nextInt(90000);
            doctorId = String.valueOf(randomNum);
        } while (doctorExists(doctorId)); // Ensure the ID is unique
        
        return doctorId;
    }

    /**
     * Create a new doctor in the database
     * @param doctor The doctor to create
     * @return true if the doctor was created successfully, false otherwise
     * @throws SQLException If a database access error occurs
     */
    public boolean createDoctor(Doctor doctor) throws SQLException {
        String sql = "INSERT INTO doctors (doctor_id, first_name, last_name, specialization, license_number, contact_number, created_at, updated_at, availability) VALUES (?, ?, ?, ?, ?, ?, datetime('now'), datetime('now'), ?)";
        try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, doctor.getDoctorId());
            pstmt.setString(2, doctor.getFirstName());
            pstmt.setString(3, doctor.getLastName());
            pstmt.setString(4, doctor.getSpecialization());
            pstmt.setString(5, doctor.getLicenseNumber());
            pstmt.setString(6, doctor.getContactNumber());
            pstmt.setInt(7, doctor.isAvailable() ? 1 : 0);

            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * Delete a doctor from the database
     * @param doctorId The ID of the doctor to delete
     * @return true if the doctor was deleted successfully, false otherwise
     * @throws SQLException If a database access error occurs
     */
    public boolean deleteDoctor(String doctorId) throws SQLException {
        String sql = "DELETE FROM doctors WHERE doctor_id = ?";
        try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, doctorId);
            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * Get a doctor by ID
     * @param doctorId The ID of the doctor to retrieve
     * @return Doctor object if found, null otherwise
     * @throws SQLException If a database access error occurs
     */
    public Doctor getDoctorById(String doctorId) throws SQLException {
        String sql = "SELECT * FROM doctors WHERE doctor_id = ?";
        try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, doctorId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    // Safely handle timestamps
                    LocalDateTime createdAt = rs.getTimestamp("created_at") != null ? 
                        DateTimeUtil.fromTimestamp(rs.getTimestamp("created_at")) : 
                        DateTimeUtil.now();
                    
                    LocalDateTime updatedAt = rs.getTimestamp("updated_at") != null ? 
                        DateTimeUtil.fromTimestamp(rs.getTimestamp("updated_at")) : 
                        DateTimeUtil.now();

                    return new Doctor(
                        rs.getInt("doctor_id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("specialization"),
                        rs.getString("license_number"),
                        rs.getString("contact_number"),
                        createdAt,
                        updatedAt,
                        rs.getInt("availability") == 1
                    );
                }
            }
        }
        return null;
    }

    /**
     * Get all doctors by specialization
     * @param specialization The specialization to filter by
     * @return List of doctors with the specified specialization
     * @throws SQLException If a database access error occurs
     */
    public List<Doctor> getDoctorsBySpecialization(String specialization) throws SQLException {
        List<Doctor> doctors = new ArrayList<>();
        String sql = "SELECT * FROM doctors WHERE specialization = ?";
        try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, specialization);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    doctors.add(new Doctor(
                        rs.getInt("doctor_id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("specialization"),
                        rs.getString("license_number"),
                        rs.getString("contact_number"),
                        DateTimeUtil.fromTimestamp(rs.getTimestamp("created_at")),
                        DateTimeUtil.fromTimestamp(rs.getTimestamp("updated_at")),
                        rs.getInt("availability") == 1
                    ));
                }
            }
        }
        return doctors;
    }

    /**
     * Get all doctors by specialization and availability
     * @param specialization The specialization to filter by
     * @param availability The availability to filter by
     * @return List of doctors with the specified specialization and availability
     * @throws SQLException If a database access error occurs
     */
    public List<Doctor> getDoctorsBySpecializationAndAvailability(String specialization, boolean availability) throws SQLException {
        List<Doctor> doctors = new ArrayList<>();
        String sql = "SELECT * FROM doctors WHERE specialization = ? AND availability = ?";
        try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, specialization);
            pstmt.setInt(2, availability ? 1 : 0);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    doctors.add(new Doctor(
                        rs.getInt("doctor_id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("specialization"),
                        rs.getString("license_number"),
                        rs.getString("contact_number"),
                        DateTimeUtil.fromTimestamp(rs.getTimestamp("created_at")),
                        DateTimeUtil.fromTimestamp(rs.getTimestamp("updated_at")),
                        rs.getInt("availability") == 1
                    ));
                }
            }
        }
        return doctors;
    }

    /**
     * Get all doctors by availability
     * @param availability The availability to filter by
     * @return List of doctors with the specified availability
     * @throws SQLException If a database access error occurs
     */
    public List<Doctor> getDoctorsByAvailability(boolean availability) throws SQLException {
        List<Doctor> doctors = new ArrayList<>();
        String sql = "SELECT * FROM doctors WHERE availability = ?";
        try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, availability ? 1 : 0);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    doctors.add(new Doctor(
                        rs.getInt("doctor_id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("specialization"),
                        rs.getString("license_number"),
                        rs.getString("contact_number"),
                        DateTimeUtil.fromTimestamp(rs.getTimestamp("created_at")),
                        DateTimeUtil.fromTimestamp(rs.getTimestamp("updated_at")),
                        rs.getInt("availability") == 1
                    ));
                }
            }
        }
        return doctors;
    }

    /**
     * Update an existing doctor
     * @param doctor The updated doctor data
     * @return true if the doctor was updated successfully, false otherwise
     * @throws SQLException If a database access error occurs
     */
    public boolean updateDoctor(Doctor doctor) throws SQLException {
        String sql = "UPDATE doctors SET first_name=?, last_name=?, specialization=?, license_number=?, contact_number=?, updated_at=datetime('now') WHERE doctor_id=?";
        try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, doctor.getFirstName());
            pstmt.setString(2, doctor.getLastName());
            pstmt.setString(3, doctor.getSpecialization());
            pstmt.setString(4, doctor.getLicenseNumber());
            pstmt.setString(5, doctor.getContactNumber());
            pstmt.setInt(6, doctor.getDoctorId());

            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * Update doctor's availability status
     * @param doctorId The ID of the doctor
     * @param available The new availability status
     * @return true if update was successful, false otherwise
     * @throws SQLException If a database access error occurs
     */
    public boolean updateAvailability(String doctorId, boolean available) throws SQLException {
        String sql = "UPDATE doctors SET availability = ?, updated_at = datetime('now') WHERE doctor_id = ?";
        try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, available ? 1 : 0);
            pstmt.setString(2, doctorId);
            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * Get all appointments for a specific doctor
     * @param doctorId The ID of the doctor
     * @return List of appointments for the doctor
     * @throws SQLException If a database access error occurs
     */
    public List<Appointment> getDoctorAppointments(String doctorId) throws SQLException {
        List<Appointment> appointments = new ArrayList<>();
        String sql = "SELECT * FROM appointments WHERE doctor_id = ? ORDER BY appointment_date";
        try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, doctorId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    appointments.add(new Appointment(
                        rs.getInt("appointment_id"),
                        rs.getInt("patient_id"),
                        rs.getInt("doctor_id"),
                        rs.getInt("nurse_id"),
                        DateTimeUtil.fromTimestamp(rs.getTimestamp("appointment_date")),
                        rs.getString("status"),
                        rs.getString("notes"),
                        DateTimeUtil.fromTimestamp(rs.getTimestamp("created_at")),
                        rs.getString("room"),
                        rs.getString("symptoms"),
                        rs.getString("diagnosis"),
                        rs.getString("service_type"),
                        rs.getString("appointment_type"),
                        rs.getString("meeting_link"),
                        rs.getString("booked_by"),
                        rs.getString("preferred_contact"),
                        DateTimeUtil.fromTimestamp(rs.getTimestamp("booking_time"))
                    ));
                }
            }
        }
        return appointments;
    }

    /**
     * Get all upcoming appointments for a specific doctor
     * @param doctorId The ID of the doctor
     * @return List of upcoming appointments for the doctor
     * @throws SQLException If a database access error occurs
     */
    public List<Appointment> getUpcomingAppointments(String doctorId) throws SQLException {
        List<Appointment> appointments = new ArrayList<>();
        String sql = "SELECT * FROM appointments WHERE doctor_id = ? AND appointment_date > datetime('now') ORDER BY appointment_date";
        try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, doctorId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    appointments.add(new Appointment(
                        rs.getInt("appointment_id"),
                        rs.getInt("patient_id"),
                        rs.getInt("doctor_id"),
                        rs.getInt("nurse_id"),
                        DateTimeUtil.fromTimestamp(rs.getTimestamp("appointment_date")),
                        rs.getString("status"),
                        rs.getString("notes"),
                        DateTimeUtil.fromTimestamp(rs.getTimestamp("created_at")),
                        rs.getString("room"),
                        rs.getString("symptoms"),
                        rs.getString("diagnosis"),
                        rs.getString("service_type"),
                        rs.getString("appointment_type"),
                        rs.getString("meeting_link"),
                        rs.getString("booked_by"),
                        rs.getString("preferred_contact"),
                        DateTimeUtil.fromTimestamp(rs.getTimestamp("booking_time"))
                    ));
                }
            }
        }
        return appointments;
    }

    /**
     * Check if a doctor exists by ID
     * @param doctorId The ID of the doctor
     * @return true if the doctor exists, false otherwise
     * @throws SQLException If a database access error occurs
     */
    public boolean doctorExists(String doctorId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM doctors WHERE doctor_id = ?";
        try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, doctorId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    /**
     * Count the total number of doctors in the database
     * @return The total number of doctors
     * @throws SQLException If a database access error occurs
     */
    public int countDoctors() throws SQLException {
        String sql = "SELECT COUNT(*) FROM doctors";
        try (Statement stmt = DatabaseManager.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    /**
     * Get all patients associated with a specific doctor through their appointments
     * @param doctorId The ID of the doctor
     * @return List of patients who have had appointments with the doctor
     * @throws SQLException If a database access error occurs
     */
    public List<Patient> getPatientsByDoctorId(int doctorId) throws SQLException {
        List<Patient> patients = new ArrayList<>();
        String sql = "SELECT DISTINCT p.* FROM patients p " +
                    "JOIN appointments a ON p.patient_id = a.patient_id " +
                    "WHERE a.doctor_id = ?";
        
        try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, doctorId);
            try (ResultSet rs = pstmt.executeQuery()) {
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
                        rs.getTimestamp("created_at") != null ? rs.getTimestamp("created_at").toLocalDateTime() : null,
                        rs.getTimestamp("updated_at") != null ? rs.getTimestamp("updated_at").toLocalDateTime() : null
                    ));
                }
            }
        }
        return patients;
    }
}
