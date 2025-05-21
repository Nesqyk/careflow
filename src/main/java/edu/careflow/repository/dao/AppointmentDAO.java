package edu.careflow.repository.dao;

import edu.careflow.manager.DatabaseManager;
import edu.careflow.repository.entities.Appointment;
import edu.careflow.repository.entities.Patient;
import edu.careflow.utils.DateTimeUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AppointmentDAO {

    /**
     *
     * @return
     * @throws SQLException
     */
    public int generateUniqueAppointmentId() throws SQLException {
        int appointmentId;
        boolean isUnique = false;

        do {
            // Generate a random 5-digit number (10000 to 99999)
            appointmentId = 10000 + (int)(Math.random() * 90000);

            // Check if the ID already exists
            String sql = "SELECT COUNT(*) FROM appointments WHERE appointment_id = ?";
            try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql)) {
                pstmt.setInt(1, appointmentId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next()) {
                        isUnique = rs.getInt(1) == 0;
                    }
                }
            }
        } while (!isUnique);

        return appointmentId;
    }


    /**
     * Add a new appointment
     * @param appointment The appointment to add
     * @return true if the appointment was added successfully, false otherwise
     * @throws SQLException If a database access error occurs
     */
    public boolean addAppointment(Appointment appointment) throws SQLException {
        String sql = "INSERT INTO appointments (patient_id, doctor_id, nurse_id, appointment_date, status, notes, created_at, room, symptoms, diagnosis, service_type, appointment_type, meeting_link, booked_by, preferred_contact, booking_time, appointment_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?)";
        try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, appointment.getPatientId());
            pstmt.setInt(2, appointment.getDoctorId());
            pstmt.setInt(3, appointment.getNurseId());
            pstmt.setTimestamp(4, DateTimeUtil.toTimestamp(appointment.getAppointmentDate()));
            pstmt.setString(5, appointment.getStatus());
            pstmt.setString(6, appointment.getNotes());
            pstmt.setTimestamp(7, DateTimeUtil.toTimestamp(appointment.getCreatedAt()));
            pstmt.setString(8, appointment.getRoom());
            pstmt.setString(9, appointment.getSymptoms());
            pstmt.setString(10, appointment.getDiagnosis());
            pstmt.setString(11, appointment.getServiceType());
            pstmt.setString(12, appointment.getAppointmentType());
            pstmt.setString(13, appointment.getMeetingLink());
            pstmt.setString(14, appointment.getBookedBy());
            pstmt.setString(15, appointment.getPreferredContact());
            pstmt.setTimestamp(16, DateTimeUtil.toTimestamp(appointment.getBookingTime()));
            pstmt.setInt(17,appointment.getAppointmentId());

            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * Delete an appointment by ID
     * @param appointmentId The ID of the appointment to delete
     * @return true if the appointment was deleted successfully, false otherwise
     * @throws SQLException If a database access error occurs
     */
    public boolean deleteAppointment(int appointmentId) throws SQLException {
        String sql = "DELETE FROM appointments WHERE appointment_id = ?";
        try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, appointmentId);
            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * Update an existing appointment
     * @param appointment The updated appointment data
     * @return true if the appointment was updated successfully, false otherwise
     * @throws SQLException If a database access error occurs
     */
    public boolean updateAppointment(Appointment appointment) throws SQLException {
        String sql = "UPDATE appointments SET patient_id=?, doctor_id=?, nurse_id=?, appointment_date=?, status=?, notes=?, room=?, symptoms=?, diagnosis=?, service_type=?, appointment_type=?, meeting_link=?, booked_by=?, preferred_contact=?, booking_time=? WHERE appointment_id=?";
        try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, appointment.getPatientId());
            pstmt.setInt(2, appointment.getDoctorId());
            pstmt.setInt(3, appointment.getNurseId());
            pstmt.setTimestamp(4, DateTimeUtil.toTimestamp(appointment.getAppointmentDate()));
            pstmt.setString(5, appointment.getStatus());
            pstmt.setString(6, appointment.getNotes());
            pstmt.setString(7, appointment.getRoom());
            pstmt.setString(8, appointment.getSymptoms());
            pstmt.setString(9, appointment.getDiagnosis());
            pstmt.setString(10, appointment.getServiceType());
            pstmt.setString(11, appointment.getAppointmentType());
            pstmt.setString(12, appointment.getMeetingLink());
            pstmt.setString(13, appointment.getBookedBy());
            pstmt.setString(14, appointment.getPreferredContact());
            pstmt.setTimestamp(15, DateTimeUtil.toTimestamp(appointment.getBookingTime()));
            pstmt.setInt(16, appointment.getAppointmentId());

            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * Get an appointment by ID
     * @param appointmentId The ID of the appointment to retrieve
     * @return Appointment object if found, null otherwise
     * @throws SQLException If a database access error occurs
     */
    public Appointment getAppointmentById(int appointmentId) throws SQLException {
        String sql = "SELECT * FROM appointments WHERE appointment_id = ?";
        try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, appointmentId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return createAppointmentFromResultSet(rs);
                }
            }
        }
        return null;
    }

    /**
     * Get all appointments for a specific patient
     * @param patientId The ID of the patient
     * @return List of appointments for the patient
     * @throws SQLException If a database access error occurs
     */
    public List<Appointment> getAppointmentsByPatientId(int patientId) throws SQLException {
        List<Appointment> appointments = new ArrayList<>();
        String sql = "SELECT * FROM appointments WHERE patient_id = ?";
        try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, patientId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    appointments.add(createAppointmentFromResultSet(rs));
                }
            }
        }
        return appointments;
    }

    /**
     * Get all appointments for a specific doctor
     * @param doctorId The ID of the doctor
     * @return List of appointments for the doctor
     * @throws SQLException If a database access error occurs
     */
    public List<Appointment> getAppointmentsByDoctorId(int doctorId) throws SQLException {
        List<Appointment> appointments = new ArrayList<>();
        String sql = "SELECT * FROM appointments WHERE doctor_id = ?";
        try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, doctorId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    appointments.add(createAppointmentFromResultSet(rs));
                }
            }
        }
        return appointments;
    }

    /**
     * Get all appointments for a specific nurse
     * @param nurseId The ID of the nurse
     * @return List of appointments for the nurse
     * @throws SQLException If a database access error occurs
     */
    public List<Appointment> getAppointmentsByNurseId(int nurseId) throws SQLException {
        List<Appointment> appointments = new ArrayList<>();
        String sql = "SELECT * FROM appointments WHERE nurse_id = ?";
        try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, nurseId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    appointments.add(createAppointmentFromResultSet(rs));
                }
            }
        }
        return appointments;
    }

    /**
     * Get all appointments with a specific status
     * @param status The status to filter by
     * @return List of appointments with the specified status
     * @throws SQLException If a database access error occurs
     */
    public List<Appointment> getAppointmentsByStatus(String status) throws SQLException {
        List<Appointment> appointments = new ArrayList<>();
        String sql = "SELECT * FROM appointments WHERE status = ?";
        try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, status);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    appointments.add(createAppointmentFromResultSet(rs));
                }
            }
        }
        return appointments;
    }

    /**
     * Get all appointments sorted by status
     * @return List of all appointments sorted by status
     * @throws SQLException If a database access error occurs
     */
    public List<Appointment> getAllAppointmentsSortedByStatus() throws SQLException {
        List<Appointment> appointments = new ArrayList<>();
        String sql = "SELECT * FROM appointments ORDER BY status";
        try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    appointments.add(createAppointmentFromResultSet(rs));
                }
            }
        }
        return appointments;
    }

    /**
     * Get all appointments
     * @return List of all appointments
     * @throws SQLException If a database access error occurs
     */
    public List<Appointment> getAllAppointments() throws SQLException {
        List<Appointment> appointments = new ArrayList<>();
        String sql = "SELECT * FROM appointments";
        try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    appointments.add(createAppointmentFromResultSet(rs));
                }
            }
        }
        return appointments;
    }

    private Appointment createAppointmentFromResultSet(ResultSet rs) throws SQLException {
        return new Appointment(
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
        );
    }

    public List<Patient> getPatientsByDoctorId(int doctorId) throws SQLException {
        List<Patient> patients = new ArrayList<>();
        String sql = "SELECT DISTINCT p.* FROM patients p " +
                    "JOIN appointments a ON p.patient_id = a.patient_id " +
                    "WHERE a.doctor_id = ?";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, doctorId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Patient patient = new Patient(
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
                patients.add(patient);
            }
        }
        return patients;
    }

    public List<Patient> getRecentPatientsByDoctorId(int doctorId, int days) throws SQLException {
        List<Patient> patients = new ArrayList<>();
        String sql = "SELECT DISTINCT p.* FROM patients p " +
                    "JOIN appointments a ON p.patient_id = a.patient_id " +
                    "WHERE a.doctor_id = ? " +
                    "AND a.appointment_date >= datetime('now', ?) " +
                    "ORDER BY a.appointment_date DESC";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, doctorId);
            pstmt.setString(2, "-" + days + " days");
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Patient patient = new Patient(
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
                patients.add(patient);
            }
        }
        return patients;
    }

    public List<Patient> getActivePatientsByDoctorId(int doctorId) throws SQLException {
        List<Patient> patients = new ArrayList<>();
        String sql = "SELECT DISTINCT p.* FROM patients p " +
                    "JOIN appointments a ON p.patient_id = a.patient_id " +
                    "WHERE a.doctor_id = ? " +
                    "AND a.status = 'Scheduled' " +
                    "ORDER BY a.appointment_date DESC";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, doctorId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Patient patient = new Patient(
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
                patients.add(patient);
            }
        }
        return patients;
    }

    public List<Patient> getScheduledPatientsByDoctorId(int doctorId) throws SQLException {
        List<Patient> patients = new ArrayList<>();
        String sql = "SELECT DISTINCT p.* FROM patients p " +
                    "JOIN appointments a ON p.patient_id = a.patient_id " +
                    "WHERE a.doctor_id = ? " +
                    "AND a.status = 'Scheduled' " +
                    "AND a.appointment_date >= datetime('now') " +
                    "ORDER BY a.appointment_date ASC";
        
        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, doctorId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Patient patient = new Patient(
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
                patients.add(patient);
            }
        }
        return patients;
    }
}
