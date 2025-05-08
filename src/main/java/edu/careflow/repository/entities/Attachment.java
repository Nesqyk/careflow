package edu.careflow.repository.entities;

import java.time.LocalDateTime;

public class Attachment {
    private Long attachmentId;
    private String attachmentName;
    private String originalName;
    private String fileType;
    private Long fileSize;
    private String description;
    private byte[] content;
    private LocalDateTime uploadDate;
    private Long patientId;
    private Long doctorId;
    private Long recordId;
    private String previewUrl;  // For image attachments

    public Attachment(String originalName, String attachmentName, String fileType,
                      Long fileSize, String description, byte[] content,
                      Long patientId, Long doctorId, Long recordId) {
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
    public Long getAttachmentId() { return attachmentId; }
    public void setAttachmentId(Long id) { this.attachmentId = id; }
    public String getAttachmentName() { return attachmentName; }
    public void setAttachmentName(String name) { this.attachmentName = name; }
    public String getOriginalName() { return originalName; }
    public void setOriginalName(String name) { this.originalName = name; }
    public String getFileType() { return fileType; }
    public void setFileType(String type) { this.fileType = type; }
    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long size) { this.fileSize = size; }
    public String getDescription() { return description; }
    public void setDescription(String desc) { this.description = desc; }
    public byte[] getContent() { return content; }
    public void setContent(byte[] content) { this.content = content; }
    public LocalDateTime getUploadDate() { return uploadDate; }
    public void setUploadDate(LocalDateTime date) { this.uploadDate = date; }
    public Long getPatientId() { return patientId; }
    public void setPatientId(Long id) { this.patientId = id; }
    public Long getDoctorId() { return doctorId; }
    public void setDoctorId(Long id) { this.doctorId = id; }
    public Long getRecordId() { return recordId; }
    public void setRecordId(Long id) { this.recordId = id; }
    public String getPreviewUrl() { return previewUrl; }
    public void setPreviewUrl(String url) { this.previewUrl = url; }
}