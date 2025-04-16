package edu.careflow.manager;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.PreparedStatement;

public class DatabaseManager {
    private static Connection connection;
    private static DatabaseManager instance;

    // Singleton pattern: Ensure only one instance of DatabaseManager exists
    private DatabaseManager() {
        this.initializeDatabase();
        // Private constructor to prevent instantiation
    }

    public static Connection getConnection() {
        return connection;
    }

    public static DatabaseManager getInstance() {
        if (instance == null) {
            instance = new DatabaseManager();
        }
        return instance;
    }

    public void initializeDatabase() {
        if (connection == null) {
            try {
                // Database URL for SQLite connection
                String DB_URL = "jdbc:sqlite:src/main/resources/database.db";
                connection = DriverManager.getConnection(DB_URL);
                System.out.println("Database connection established.");
            } catch (SQLException e) {
                System.out.println("Error connecting to the database: " + e.getMessage());
            }
        }
    }

    public void initializeTable() {
        if (connection != null) {
            try (Statement stmt = connection.createStatement()) {
                // PATIENTS table
                stmt.execute("CREATE TABLE IF NOT EXISTS patients (" +
                    "patient_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "first_name VARCHAR(50) NOT NULL, " +
                    "last_name VARCHAR(50) NOT NULL, " +
                    "date_of_birth DATE NOT NULL, " +
                    "gender TEXT CHECK(gender IN ('Male', 'Female', 'Other')), " +
                    "contact_number VARCHAR(15), " +
                    "email VARCHAR(100), " +
                    "address TEXT, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ");");

                // VITALS & BIOMETRICS table
                stmt.execute("CREATE TABLE IF NOT EXISTS vitals (" +
                    "vital_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "patient_id INT NOT NULL, " +
                    "nurse_id INT, " +
                    "weight_kg DECIMAL(5,2), " +
                    "height_cm DECIMAL(5,2), " +
                    "blood_pressure VARCHAR(10), " +
                    "heart_rate INT, " +
                    "temperature DECIMAL(4,2), " +
                    "oxygen_saturation DECIMAL(4,2), " +
                    "recorded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "FOREIGN KEY (patient_id) REFERENCES patients(patient_id)" +
                    ");");

                // DOCTORS table
                stmt.execute("CREATE TABLE IF NOT EXISTS doctors (" +
                    "doctor_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "first_name VARCHAR(50) NOT NULL, " +
                    "last_name VARCHAR(50) NOT NULL, " +
                    "specialization VARCHAR(100), " +
                    "license_number VARCHAR(50), " +
                    "contact_number VARCHAR(15), " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ");");

                // NURSES table
                stmt.execute("CREATE TABLE IF NOT EXISTS nurses (" +
                    "nurse_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "first_name VARCHAR(50) NOT NULL, " +
                    "last_name VARCHAR(50) NOT NULL, " +
                    "license_number VARCHAR(50), " +
                    "contact_number VARCHAR(15), " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ");");

                // APPOINTMENTS table
                stmt.execute("CREATE TABLE IF NOT EXISTS appointments (" +
                    "appointment_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "patient_id INT NOT NULL, " +
                    "doctor_id INT, " +
                    "nurse_id INT, " +
                    "appointment_date DATETIME NOT NULL, " +
                    "status TEXT CHECK(status IN ('Scheduled', 'Completed', 'Cancelled', 'No-Show')) DEFAULT 'Scheduled', " +
                    "notes TEXT, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "FOREIGN KEY (patient_id) REFERENCES patients(patient_id), " +
                    "FOREIGN KEY (doctor_id) REFERENCES doctors(doctor_id), " +
                    "FOREIGN KEY (nurse_id) REFERENCES nurses(nurse_id)" +
                    ");");

                // PRESCRIPTIONS table
                stmt.execute("CREATE TABLE IF NOT EXISTS prescriptions (" +
                    "prescription_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "patient_id INT NOT NULL, " +
                    "doctor_id INT NOT NULL, " +
                    "issue_date DATE NOT NULL, " +
                    "valid_until DATE, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "FOREIGN KEY (patient_id) REFERENCES patients(patient_id), " +
                    "FOREIGN KEY (doctor_id) REFERENCES doctors(doctor_id)" +
                    ");");

                // PRESCRIPTION DETAILS table
                stmt.execute("CREATE TABLE IF NOT EXISTS prescription_details (" +
                    "detail_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "prescription_id INT NOT NULL, " +
                    "medication_name VARCHAR(100) NOT NULL, " +
                    "dosage VARCHAR(50), " +
                    "frequency VARCHAR(50), " +
                    "instructions TEXT, " +
                    "FOREIGN KEY (prescription_id) REFERENCES prescriptions(prescription_id)" +
                    ");");

                // BILLING table
                stmt.execute("CREATE TABLE IF NOT EXISTS billing (" +
                    "bill_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "patient_id INT NOT NULL, " +
                    "appointment_id INT, " +
                    "total_amount DECIMAL(10,2) NOT NULL, " +
                    "amount_paid DECIMAL(10,2) DEFAULT 0.00, " +
                    "balance DECIMAL(10,2) GENERATED ALWAYS AS (total_amount - amount_paid), " +
                    "status TEXT CHECK(status IN ('Pending', 'Partial', 'Paid')) DEFAULT 'Pending', " +
                    "due_date DATE, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "FOREIGN KEY (patient_id) REFERENCES patients(patient_id), " +
                    "FOREIGN KEY (appointment_id) REFERENCES appointments(appointment_id)" +
                    ");");

                // BILLING DETAILS table
                stmt.execute("CREATE TABLE IF NOT EXISTS billing_details (" +
                    "detail_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "bill_id INT NOT NULL, " +
                    "description VARCHAR(255) NOT NULL, " +
                    "amount DECIMAL(10,2) NOT NULL, " +
                    "FOREIGN KEY (bill_id) REFERENCES billing(bill_id)" +
                    ");");

                // ROLES table
                stmt.execute("CREATE TABLE IF NOT EXISTS roles (" +
                    "role_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "role_name VARCHAR(50) UNIQUE NOT NULL" +
                    ");");

                // PERMISSIONS table
                stmt.execute("CREATE TABLE IF NOT EXISTS permissions (" +
                    "permission_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "permission_name VARCHAR(100) UNIQUE NOT NULL" +
                    ");");

                // ROLE_PERMISSIONS table
                stmt.execute("CREATE TABLE IF NOT EXISTS role_permissions (" +
                    "role_id INT, " +
                    "permission_id INT, " +
                    "PRIMARY KEY (role_id, permission_id), " +
                    "FOREIGN KEY (role_id) REFERENCES roles(role_id), " +
                    "FOREIGN KEY (permission_id) REFERENCES permissions(permission_id)" +
                    ");");

                // USERS table
                stmt.execute("CREATE TABLE IF NOT EXISTS users (" +
                    "user_id INT UNIQUE NOT NULL, " +
                    "username VARCHAR(50) UNIQUE NOT NULL, " +
                    "password_hash VARCHAR(255) NOT NULL, " +
                    "role_id INT NOT NULL, " +
                    "staff_id INT, " +
                    "first_name VARCHAR(50), " +
                    "last_name VARCHAR(50), " +
                    "last_login TIMESTAMP NULL, " +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                    "FOREIGN KEY (role_id) REFERENCES roles(role_id)" +
                    ");");

                // INDEXES
                stmt.execute("CREATE INDEX IF NOT EXISTS idx_appointments_patient ON appointments(patient_id);");
                stmt.execute("CREATE INDEX IF NOT EXISTS idx_prescriptions_patient ON prescriptions(patient_id);");
                stmt.execute("CREATE INDEX IF NOT EXISTS idx_billing_patient ON billing(patient_id);");

                System.out.println("Tables initialized successfully.");
            } catch (SQLException e) {
                System.out.println("Error initializing tables: " + e.getMessage());
            }
        }
    }

    public void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("Database connection closed.");
            } catch (SQLException e) {
                System.out.println("Error closing the database connection: " + e.getMessage());
            }
        }
    }

    public ResultSet executeQuery(String query) {
        ResultSet resultSet = null;
        if (connection != null) {
            try (Statement stmt = connection.createStatement()) {
                resultSet = stmt.executeQuery(query);
            } catch (SQLException e) {
                System.out.println("Error executing query: " + e.getMessage());
            }
        }
        return resultSet;
    }

    public int executeUpdate(String query) {
        int affectedRows = 0;
        if (connection != null) {
            try (Statement stmt = connection.createStatement()) {
                affectedRows = stmt.executeUpdate(query);
            } catch (SQLException e) {
                System.out.println("Error executing update: " + e.getMessage());
            }
        }
        return affectedRows;
    }

    public void beginTransaction() {
        if (connection != null) {
            try {
                connection.setAutoCommit(false);
            } catch (SQLException e) {
                System.out.println("Error beginning transaction: " + e.getMessage());
            }
        }
    }

    public void commitTransaction() {
        if (connection != null) {
            try {
                connection.commit();
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                System.out.println("Error committing transaction: " + e.getMessage());
            }
        }
    }

    public void rollbackTransaction() {
        if (connection != null) {
            try {
                connection.rollback();
                connection.setAutoCommit(true);
            } catch (SQLException e) {
                System.out.println("Error rolling back transaction: " + e.getMessage());
            }
        }
    }
}

