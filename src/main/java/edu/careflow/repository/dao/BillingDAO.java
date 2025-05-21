package edu.careflow.repository.dao;

import edu.careflow.manager.DatabaseManager;
import edu.careflow.repository.entities.BillRate;
import edu.careflow.repository.entities.Billing;
import edu.careflow.utils.DateTimeUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

public class BillingDAO {
    private final DatabaseManager dbManager;
    private final ZoneId zoneId = ZoneId.systemDefault(); // Gets the system's default timezone
    private final BillRateDAO billRateDAO = new BillRateDAO();

    public BillingDAO() {
        this.dbManager = DatabaseManager.getInstance();
    }

    public int generateUniqueBillingId() throws SQLException {
        // Keep trying until we find a unique ID
        while (true) {
            // Generate random 5 digit number between 10000 and 99999
            int randomId = 10000 + (int)(Math.random() * 90000);

            // Check if ID exists
            String sql = "SELECT COUNT(*) FROM billing WHERE billing_id = ?";
            try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql)) {
                pstmt.setInt(1, randomId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    if (rs.next() && rs.getInt(1) == 0) {
                        // ID is unique, return it
                        return randomId;
                    }
                }
            }
        }
    }

    public void addBilling(Billing billing) throws SQLException {
        String sql = "INSERT INTO billing (patient_id, doctor_id, service_rate_id, amount, status, due_date, paid_date, description, created_at, updated_at, appointment_id, billing_date, payment_method, reference_number, tax_amount, discount_amount, subtotal) " +
                    "VALUES (?, ?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, billing.getPatientId());
            pstmt.setInt(2, billing.getDoctorId());
            pstmt.setInt(3, billing.getServiceRateId());
            pstmt.setBigDecimal(4, billing.getAmount());
            pstmt.setString(5, billing.getStatus());
            pstmt.setTimestamp(6, DateTimeUtil.toTimestamp(billing.getDueDate()));
            pstmt.setTimestamp(7, DateTimeUtil.toTimestamp(billing.getPaidDate()));
            pstmt.setString(8, billing.getDescription());
            pstmt.setObject(9, billing.getAppointmentId());
            pstmt.setTimestamp(10, DateTimeUtil.toTimestamp(billing.getBillingDate()));
            pstmt.setString(11, billing.getPaymentMethod());
            pstmt.setString(12, billing.getReferenceNumber());
            pstmt.setBigDecimal(13, billing.getTaxAmount());
            pstmt.setBigDecimal(14, billing.getDiscountAmount());
            pstmt.setBigDecimal(15, billing.getSubtotal());
            pstmt.executeUpdate();
        }
    }

    public Billing getBillingById(int billingId) throws SQLException {
        String sql = "SELECT * FROM billing WHERE billing_id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, billingId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return createBillingFromResultSet(rs);
                }
            }
        }
        return null;
    }

    public List<Billing> getBillingsByPatientId(int patientId) throws SQLException {
        String sql = "SELECT * FROM billing WHERE patient_id = ? ORDER BY created_at DESC";
        List<Billing> billings = new ArrayList<>();
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, patientId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    billings.add(createBillingFromResultSet(rs));
                }
            }
        }
        return billings;
    }

    public List<Billing> getBillingsByDoctorId(int doctorId) throws SQLException {
        String sql = "SELECT b.* FROM billing b " +
                    "JOIN appointments a ON b.appointment_id = a.appointment_id " +
                    "WHERE a.doctor_id = ? ORDER BY b.created_at DESC";
        List<Billing> billings = new ArrayList<>();
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, doctorId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    billings.add(createBillingFromResultSet(rs));
                }
            }
        }
        return billings;
    }

    public void updateBilling(Billing billing) throws SQLException {
        String sql = "UPDATE billing SET patient_id = ?, appointment_id = ?, amount = ?, status = ?, due_date = ?, " +
                    "payment_method = ?, reference_number = ?, tax_amount = ?, discount_amount = ?, subtotal = ? " +
                    "WHERE billing_id = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, billing.getPatientId());
            pstmt.setInt(2, billing.getAppointmentId() != null ? billing.getAppointmentId() : null);
            pstmt.setBigDecimal(3, billing.getAmount());
            pstmt.setString(4, billing.getStatus());
            pstmt.setTimestamp(5, DateTimeUtil.toTimestamp(billing.getDueDate()));
            pstmt.setString(6, billing.getPaymentMethod());
            pstmt.setString(7, billing.getReferenceNumber());
            pstmt.setBigDecimal(8, billing.getTaxAmount());
            pstmt.setBigDecimal(9, billing.getDiscountAmount());
            pstmt.setBigDecimal(10, billing.getSubtotal());
            pstmt.setInt(11, billing.getBillingId());
            
            pstmt.executeUpdate();
        }
    }

    public void updateBillingStatus(int billingId, String status) throws SQLException {
        String sql = "UPDATE billing SET status = ? WHERE bill_id = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, status);
            pstmt.setInt(2, billingId);
            
            pstmt.executeUpdate();
        }
    }

    public void deleteBilling(int billingId) throws SQLException {
        String sql = "DELETE FROM billing WHERE bill_id = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, billingId);
            pstmt.executeUpdate();
        }
    }

    public List<Billing> getOverdueBillings() throws SQLException {
        String sql = "SELECT * FROM billing WHERE status = 'Pending' AND due_date < CURRENT_DATE";
        List<Billing> billings = new ArrayList<>();
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    billings.add(createBillingFromResultSet(rs));
                }
            }
        }
        return billings;
    }

    public List<Billing> getBillingsByAppointmentId(int appointmentId) throws SQLException {
        String sql = "SELECT * FROM billing WHERE appointment_id = ?";
        List<Billing> billings = new ArrayList<>();
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, appointmentId);
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    billings.add(createBillingFromResultSet(rs));
                }
            }
        }
        return billings;
    }

    public Billing getBillingByAppointmentId(int appointmentId) throws SQLException {
        String sql = "SELECT * FROM billing WHERE appointment_id = ?";
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, appointmentId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return createBillingFromResultSet(rs);
                }
            }
        }
        return null;
    }

    public void updateBillingStatusByAppointmentId(int appointmentId, String status) throws SQLException {
        String sql = "UPDATE billing SET status = ? WHERE appointment_id = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, status);
            pstmt.setInt(2, appointmentId);
            
            pstmt.executeUpdate();
        }
    }

    public List<Billing> getAllBillings() throws SQLException {
        String sql = "SELECT * FROM billing ORDER BY created_at DESC";
        List<Billing> billings = new ArrayList<>();
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    billings.add(createBillingFromResultSet(rs));
                }
            }
        }
        return billings;
    }

    public void addBillingService(int billingId, int serviceRateId) throws SQLException {
        String sql = "UPDATE billing SET service_rate_id = ? WHERE billing_id = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, serviceRateId);
            pstmt.setInt(2, billingId);
            
            pstmt.executeUpdate();
        }
    }

    public List<BillRate> getBillingServices(int billingId) throws SQLException {
        String sql = "SELECT sr.* FROM service_rate sr " +
                    "JOIN billing b ON sr.rate_id = b.service_rate_id " +
                    "WHERE b.billing_id = ?";
        
        List<BillRate> services = new ArrayList<>();
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, billingId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    services.add(billRateDAO.createBillRateFromResultSet(rs));
                }
            }
        }
        return services;
    }

    public void deleteBillingServices(int billingId) throws SQLException {
        String sql = "UPDATE billing SET service_rate_id = NULL WHERE billing_id = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, billingId);
            pstmt.executeUpdate();
        }
    }

    private Billing createBillingFromResultSet(ResultSet rs) throws SQLException {
        Billing billing = new Billing();
        billing.setBillingId(rs.getInt("billing_id"));
        billing.setPatientId(rs.getInt("patient_id"));
        billing.setDoctorId(rs.getInt("doctor_id"));
        billing.setServiceRateId(rs.getInt("service_rate_id"));
        billing.setAmount(rs.getBigDecimal("amount"));
        billing.setStatus(rs.getString("status"));
        billing.setDueDate(DateTimeUtil.fromTimestamp(rs.getTimestamp("due_date")));
        billing.setPaidDate(DateTimeUtil.fromTimestamp(rs.getTimestamp("paid_date")));
        billing.setDescription(rs.getString("description"));
        billing.setCreatedAt(DateTimeUtil.fromTimestamp(rs.getTimestamp("created_at")));
        billing.setUpdatedAt(DateTimeUtil.fromTimestamp(rs.getTimestamp("updated_at")));
        billing.setAppointmentId(rs.getObject("appointment_id") != null ? rs.getInt("appointment_id") : null);
        billing.setBillingDate(DateTimeUtil.fromTimestamp(rs.getTimestamp("billing_date")));
        billing.setPaymentMethod(rs.getString("payment_method"));
        billing.setReferenceNumber(rs.getString("reference_number"));
        billing.setTaxAmount(rs.getBigDecimal("tax_amount"));
        billing.setDiscountAmount(rs.getBigDecimal("discount_amount"));
        billing.setSubtotal(rs.getBigDecimal("subtotal"));
        return billing;
    }
} 