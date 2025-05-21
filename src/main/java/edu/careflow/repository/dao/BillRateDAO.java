package edu.careflow.repository.dao;

import edu.careflow.manager.DatabaseManager;
import edu.careflow.repository.entities.BillRate;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class BillRateDAO {
    private final DatabaseManager dbManager;

    public BillRateDAO() {
        this.dbManager = DatabaseManager.getInstance();
    }

    public void addBillRate(BillRate billRate) throws SQLException {
        String sql = "INSERT INTO service_rate (doctor_id, service_type, rate_amount, description, is_active, created_at, updated_at) " +
                     "VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            
            pstmt.setInt(1, billRate.getDoctorId());
            pstmt.setString(2, billRate.getServiceType());
            pstmt.setBigDecimal(3, billRate.getRateAmount());
            pstmt.setString(4, billRate.getDescription());
            pstmt.setBoolean(5, billRate.isActive());
            
            pstmt.executeUpdate();
            
            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) {
                    billRate.setRateId(rs.getInt(1));
                }
            }
        }
    }

    public BillRate getBillRateById(int rateId) throws SQLException {
        String sql = "SELECT * FROM service_rate WHERE rate_id = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, rateId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return createBillRateFromResultSet(rs);
                }
            }
        }
        return null;
    }

    public List<BillRate> getAllBillRates() throws SQLException {
        String sql = "SELECT * FROM service_rate WHERE is_active = true ORDER BY service_type";
        List<BillRate> billRates = new ArrayList<>();
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql);
             ResultSet rs = pstmt.executeQuery()) {
            
            while (rs.next()) {
                billRates.add(createBillRateFromResultSet(rs));
            }
        }
        return billRates;
    }

    public List<BillRate> getBillRatesByDoctorId(int doctorId) throws SQLException {
        String sql = "SELECT * FROM service_rate WHERE doctor_id = ? AND is_active = true ORDER BY service_type";
        List<BillRate> billRates = new ArrayList<>();
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, doctorId);
            
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    billRates.add(createBillRateFromResultSet(rs));
                }
            }
        }
        return billRates;
    }

    public void updateBillRate(BillRate billRate) throws SQLException {
        String sql = "UPDATE service_rate SET service_type = ?, rate_amount = ?, description = ?, " +
                     "is_active = ?, updated_at = CURRENT_TIMESTAMP WHERE rate_id = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setString(1, billRate.getServiceType());
            pstmt.setBigDecimal(2, billRate.getRateAmount());
            pstmt.setString(3, billRate.getDescription());
            pstmt.setBoolean(4, billRate.isActive());
            pstmt.setInt(5, billRate.getRateId());
            
            pstmt.executeUpdate();
        }
    }

    public void deleteBillRate(int rateId) throws SQLException {
        String sql = "UPDATE service_rate SET is_active = false, updated_at = CURRENT_TIMESTAMP WHERE rate_id = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setInt(1, rateId);
            pstmt.executeUpdate();
        }
    }

    public BillRate createBillRateFromResultSet(ResultSet rs) throws SQLException {
        return new BillRate(
            rs.getInt("rate_id"),
            rs.getInt("doctor_id"),
            rs.getString("service_type"),
            rs.getBigDecimal("rate_amount"),
            rs.getString("description"),
            rs.getBoolean("is_active"),
            rs.getTimestamp("created_at").toLocalDateTime(),
            rs.getTimestamp("updated_at").toLocalDateTime()
        );
    }
} 