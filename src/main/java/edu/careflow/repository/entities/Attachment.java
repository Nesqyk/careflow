package edu.careflow.repository.entities;

import java.time.LocalDateTime;

public class Attachment {
    private int attachmentId;
    private String attachmentName;
    private String originalName;
    private String fileType;
    private int fileSize;
    private String description;
    private byte[] content;
    private LocalDateTime uploadDate;
    private int patientId;
    private int doctorId;
    private int recordId;
    private String previewUrl;  // For image attachments

    public Attachment(String originalName, String attachmentName, String fileType,
                      int fileSize, String description, byte[] content,
                      int patientId, int doctorId, int recordId) {
        this.originalName = originalName;
        this.attachmentName = attachmentName;
        this.fileType = fileType;
        this.fileSize = fileSize;
        this.description = description;
        this.content = content;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.recordId = recordId;
    }

    // Getters and setters
    public int getAttachmentId() { return attachmentId; }
    public void setAttachmentId(int id) { this.attachmentId = id; }
    public String getAttachmentName() { return attachmentName; }
    public void setAttachmentName(String name) { this.attachmentName = name; }
    public String getOriginalName() { return originalName; }
    public void setOriginalName(String name) { this.originalName = name; }
    public String getFileType() { return fileType; }
    public void setFileType(String type) { this.fileType = type; }
    public int getFileSize() { return fileSize; }
    public void setFileSize(int size) { this.fileSize = size; }
    public String getDescription() { return description; }
    public void setDescription(String desc) { this.description = desc; }
    public byte[] getContent() { return content; }
    public void setContent(byte[] content) { this.content = content; }
    public LocalDateTime getUploadDate() { return uploadDate; }
    public void setUploadDate(LocalDateTime date) { this.uploadDate = date; }
    public int getPatientId() { return patientId; }
    public void setPatientId(int id) { this.patientId = id; }
    public int getDoctorId() { return doctorId; }
    public void setDoctorId(int id) { this.doctorId = id; }
    public int getRecordId() { return recordId; }
    public void setRecordId(int id) { this.recordId = id; }
    public String getPreviewUrl() { return previewUrl; }
    public void setPreviewUrl(String url) { this.previewUrl = url; }
}