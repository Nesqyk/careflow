package edu.careflow.repository.dao;

import edu.careflow.manager.DatabaseManager;
import edu.careflow.repository.entities.Attachment;
import edu.careflow.utils.DateTimeUtil;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AttachmentDAO {
    private final DatabaseManager dbManager;

    public AttachmentDAO() {
        this.dbManager = DatabaseManager.getInstance();
    }

    public void save(Attachment attachment) throws SQLException {
        String sql = "INSERT INTO attachments (file_name, original_name, file_type, file_size, "
                + "description, content, patient_id, doctor_id, record_id) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement stmt = dbManager.getConnection().prepareStatement(sql)) {
            stmt.setString(1, attachment.getAttachmentName());
            stmt.setString(2, attachment.getOriginalName());
            stmt.setString(3, attachment.getFileType());
            stmt.setInt(4, attachment.getFileSize());
            stmt.setString(5, attachment.getDescription());
            stmt.setBytes(6, attachment.getContent());
            stmt.setInt(7, attachment.getPatientId());
            stmt.setInt(8, attachment.getDoctorId());
            stmt.setInt(9, attachment.getRecordId());

            stmt.executeUpdate();

            try (Statement idStmt = DatabaseManager.getConnection().createStatement()) {
                ResultSet rs = idStmt.executeQuery("SELECT last_insert_rowid()");
                if (rs.next()) {
                    attachment.setAttachmentId(rs.getInt(1));
                }
            }
        }
    }

    public void update(Attachment attachment) throws SQLException {
        String sql = """
            UPDATE attachments
            SET file_name = ?, description = ?
            WHERE attachment_id = ?
        """;

        try (PreparedStatement stmt = dbManager.getConnection().prepareStatement(sql)) {
            stmt.setString(1, attachment.getAttachmentName());
            stmt.setString(2, attachment.getDescription());
            stmt.setLong(3, attachment.getAttachmentId());
            stmt.executeUpdate();
        }
    }

    public void delete(Long attachmentId) throws SQLException {
        String sql = "DELETE FROM attachments WHERE attachment_id = ?";

        try (PreparedStatement stmt = dbManager.getConnection().prepareStatement(sql)) {
            stmt.setLong(1, attachmentId);
            stmt.executeUpdate();
        }
    }

    public Optional<Attachment> findById(Long attachmentId) throws SQLException {
        String sql = "SELECT * FROM attachments WHERE attachment_id = ?";

        try (PreparedStatement stmt = dbManager.getConnection().prepareStatement(sql)) {
            stmt.setLong(1, attachmentId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                return Optional.of(mapResultSetToAttachment(rs));
            }
        }
        return Optional.empty();
    }

    public List<Attachment> findByPatientId(Long patientId) throws SQLException {
        String sql = "SELECT * FROM attachments WHERE patient_id = ? ORDER BY upload_date DESC";
        List<Attachment> attachments = new ArrayList<>();

        try (PreparedStatement stmt = dbManager.getConnection().prepareStatement(sql)) {
            stmt.setLong(1, patientId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                attachments.add(mapResultSetToAttachment(rs));
            }
        }
        return attachments;
    }

    public List<Attachment> findByDoctorId(Long doctorId) throws SQLException {
        String sql = "SELECT * FROM attachments WHERE doctor_id = ? ORDER BY upload_date DESC";
        List<Attachment> attachments = new ArrayList<>();

        try (PreparedStatement stmt = dbManager.getConnection().prepareStatement(sql)) {
            stmt.setLong(1, doctorId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                attachments.add(mapResultSetToAttachment(rs));
            }
        }
        return attachments;
    }

    public List<Attachment> findByRecordId(Long recordId) throws SQLException {
        String sql = "SELECT * FROM attachments WHERE record_id = ? ORDER BY upload_date DESC";
        List<Attachment> attachments = new ArrayList<>();

        try (PreparedStatement stmt = dbManager.getConnection().prepareStatement(sql)) {
            stmt.setLong(1, recordId);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                attachments.add(mapResultSetToAttachment(rs));
            }
        }
        return attachments;
    }

    public List<Attachment> findByFileType(String fileType) throws SQLException {
        String sql = "SELECT * FROM attachments WHERE file_type = ? ORDER BY upload_date DESC";
        List<Attachment> attachments = new ArrayList<>();

        try (PreparedStatement stmt = dbManager.getConnection().prepareStatement(sql)) {
            stmt.setString(1, fileType);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                attachments.add(mapResultSetToAttachment(rs));
            }
        }
        return attachments;
    }

    public boolean exists(Long attachmentId) throws SQLException {
        String sql = "SELECT 1 FROM attachments WHERE attachment_id = ?";

        try (PreparedStatement stmt = dbManager.getConnection().prepareStatement(sql)) {
            stmt.setLong(1, attachmentId);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        }
    }

    private Attachment mapResultSetToAttachment(ResultSet rs) throws SQLException {
        Attachment attachment = new Attachment(
                rs.getString("original_name"),
                rs.getString("file_name"),
                rs.getString("file_type"),
                rs.getInt("file_size"),
                rs.getString("description"),
                rs.getBytes("content"),
                rs.getInt("patient_id"),
                rs.getInt("doctor_id"),
                rs.getInt("record_id")
        );
        attachment.setAttachmentId(rs.getInt("attachment_id"));
        attachment.setUploadDate(DateTimeUtil.fromTimestamp(rs.getTimestamp("upload_date")));
        return attachment;
    }
}