package edu.careflow.repository.dao;

import edu.careflow.manager.DatabaseManager;
import edu.careflow.repository.entities.Doctor;
import edu.careflow.repository.entities.Appointment;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DoctorDAO {

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
        // Generate a unique 5-digit ID for the doctor
        
        String sql = "INSERT INTO doctors (doctor_id, first_name, last_name, specialization, license_number, contact_number) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, doctor.getDoctorId());
            pstmt.setString(2, doctor.getFirstName());
            pstmt.setString(3, doctor.getLastName());
            pstmt.setString(4, doctor.getSpecialization());
            pstmt.setString(5, doctor.getLicenseNumber());
            pstmt.setString(6, doctor.getContactNumber());

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
                    return new Doctor(
                        rs.getInt("doctor_id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("specialization"),
                        rs.getString("license_number"),
                        rs.getString("contact_number")
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
                        rs.getString("contact_number")
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
            pstmt.setBoolean(2, availability);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    doctors.add(new Doctor(
                        rs.getInt("doctor_id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("specialization"),
                        rs.getString("license_number"),
                        rs.getString("contact_number")
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
            pstmt.setBoolean(1, availability);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    doctors.add(new Doctor(
                        rs.getInt("doctor_id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("specialization"),
                        rs.getString("license_number"),
                        rs.getString("contact_number")
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
        String sql = "UPDATE doctors SET first_name=?, last_name=?, specialization=?, license_number=?, contact_number=? WHERE doctor_id=?";
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
        String sql = "UPDATE doctors SET availability = ? WHERE doctor_id = ?";
        try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            pstmt.setBoolean(1, available);
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
        String sql = "SELECT * FROM appointments WHERE doctor_id = ?";
        try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, doctorId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    appointments.add(new Appointment(
                        rs.getInt("appointment_id"),
                        rs.getInt("patient_id"),
                        rs.getInt("doctor_id"),
                        rs.getInt("nurse_id"),
                        rs.getTimestamp("appointment_date").toLocalDateTime(),
                        rs.getString("status"),
                        rs.getString("notes"),
                        rs.getTimestamp("created_at").toLocalDateTime(),
                        rs.getString("room"),
                        rs.getString("symptoms"),
                        rs.getString("diagnosis"),
                        rs.getString("service_type")
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
        String sql = "SELECT * FROM appointments WHERE doctor_id = ? AND appointment_date > NOW()";
        try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, doctorId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    appointments.add(new Appointment(
                        rs.getInt("appointment_id"),
                        rs.getInt("patient_id"),
                        rs.getInt("doctor_id"),
                        rs.getInt("nurse_id"),
                        rs.getTimestamp("appointment_date").toLocalDateTime(),
                        rs.getString("status"),
                        rs.getString("notes"),
                        rs.getTimestamp("created_at").toLocalDateTime(),
                        rs.getString("room"),
                        rs.getString("symptoms"),
                        rs.getString("diagnosis"),
                        rs.getString("service_type")
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
}
