package edu.careflow.repository.dao;

import edu.careflow.manager.DatabaseManager;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MeetingDAO {
    public boolean addMeeting(int meetingId, int patientId, int doctorId, String meetingUrl, Timestamp scheduledTime, String status) throws SQLException {
        String sql = "INSERT INTO meetings (meeting_id, patient_id, doctor_id, meeting_url, scheduled_time, status, created_at) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, meetingId);
            pstmt.setInt(2, patientId);
            pstmt.setInt(3, doctorId);
            pstmt.setString(4, meetingUrl);
            pstmt.setTimestamp(5, scheduledTime);
            pstmt.setString(6, status);
            pstmt.setTimestamp(7, new Timestamp(System.currentTimeMillis()));
            return pstmt.executeUpdate() > 0;
        }
    }

    public Meeting getMeetingById(int meetingId) throws SQLException {
        String sql = "SELECT * FROM meetings WHERE meeting_id = ?";
        try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, meetingId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Meeting(
                        rs.getInt("meeting_id"),
                        rs.getInt("patient_id"),
                        rs.getInt("doctor_id"),
                        rs.getString("meeting_url"),
                        rs.getTimestamp("scheduled_time"),
                        rs.getString("status"),
                        rs.getTimestamp("created_at")
                    );
                }
            }
        }
        return null;
    }

    public List<Meeting> getMeetingsByPatientId(int patientId) throws SQLException {
        List<Meeting> meetings = new ArrayList<>();
        String sql = "SELECT * FROM meetings WHERE patient_id = ?";
        try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, patientId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    meetings.add(new Meeting(
                        rs.getInt("meeting_id"),
                        rs.getInt("patient_id"),
                        rs.getInt("doctor_id"),
                        rs.getString("meeting_url"),
                        rs.getTimestamp("scheduled_time"),
                        rs.getString("status"),
                        rs.getTimestamp("created_at")
                    ));
                }
            }
        }
        return meetings;
    }

    public boolean updateMeetingStatus(int meetingId, String status) throws SQLException {
        String sql = "UPDATE meetings SET status = ? WHERE meeting_id = ?";
        try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, status);
            pstmt.setInt(2, meetingId);
            return pstmt.executeUpdate() > 0;
        }
    }

    public boolean deleteMeeting(int meetingId) throws SQLException {
        String sql = "DELETE FROM meetings WHERE meeting_id = ?";
        try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, meetingId);
            return pstmt.executeUpdate() > 0;
        }
    }

    // Meeting entity class (inner static for convenience)
    public static class Meeting {
        private final int meetingId;
        private final int patientId;
        private final int doctorId;
        private final String meetingUrl;
        private final Timestamp scheduledTime;
        private final String status;
        private final Timestamp createdAt;

        public Meeting(int meetingId, int patientId, int doctorId, String meetingUrl, Timestamp scheduledTime, String status, Timestamp createdAt) {
            this.meetingId = meetingId;
            this.patientId = patientId;
            this.doctorId = doctorId;
            this.meetingUrl = meetingUrl;
            this.scheduledTime = scheduledTime;
            this.status = status;
            this.createdAt = createdAt;
        }

        public int getMeetingId() { return meetingId; }
        public int getPatientId() { return patientId; }
        public int getDoctorId() { return doctorId; }
        public String getMeetingUrl() { return meetingUrl; }
        public Timestamp getScheduledTime() { return scheduledTime; }
        public String getStatus() { return status; }
        public Timestamp getCreatedAt() { return createdAt; }
    }
} 