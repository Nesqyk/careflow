package edu.careflow.repository.dao;

import edu.careflow.manager.DatabaseManager;
import edu.careflow.repository.entities.User;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class UserDAO {

    public UserDAO() {
        // Database manager instance
        DatabaseManager dbManager = DatabaseManager.getInstance();
    }

    /**
     * Check if a username already exists in the database
     * @param username The username to check
     * @return true if the username exists, false otherwise
     * @throws SQLException If a database access error occurs
     */
    public boolean usernameExists(String username) throws SQLException {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ?";
        try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    /**
     * Check if a user ID already exists in the database
     * @param userId The user ID to check
     * @return true if the user ID exists, false otherwise
     * @throws SQLException If a database access error occurs
     */
    public boolean userIdExists(int userId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM users WHERE user_id = ?";
        try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    /**
     * Generate a random 5-digit unique user ID
     * @return A unique 5-digit user ID
     * @throws SQLException If a database access error occurs
     */
    public int generateRandomUserId() throws SQLException {
        Random random = new Random();
        int userId;
        do {
            userId = 10000 + random.nextInt(90000); // Generates a random 5-digit number between 10000 and 99999
        } while (userIdExists(userId)); // Ensure the ID is unique
        return userId;
    }

    /**
     * Create a new user
     * @param user User object to be inserted
     * @return The generated user ID
     * @throws SQLException If a database access error occurs
     */
    public int insertUser(User user) throws SQLException {
        if (usernameExists(user.getUsername())) {
            throw new SQLException("Username already exists.");
        }

        int userId = user.getUser_id();

        String sql = "INSERT INTO users (user_id, username, password_hash, role_id, staff_id, " +
                     "first_name, last_name) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            pstmt.setString(2, user.getUsername());
            pstmt.setString(3, user.getPassword());
            pstmt.setInt(4, user.getRoleId());
            pstmt.setInt(5, user.getStaffId());
            pstmt.setString(6, user.getFirstName());
            pstmt.setString(7, user.getLastName());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Creating user failed, no rows affected.");
            }

            return userId;
        }
    }

    /**
     * Update an existing user
     * @param userId The ID of the user to be updated
     * @param updatedUser The updated User object
     * @return True if the update was successful, false otherwise
     * @throws SQLException If a database access error occurs
     */
    public boolean updateUser(int userId, User updatedUser) throws SQLException {
        if (usernameExists(updatedUser.getUsername())) {
            throw new SQLException("Username already exists.");
        }

        String sql = "UPDATE users SET username=?, password_hash=?, role_id=?, " +
                     "staff_id=?, first_name=?, last_name=? WHERE user_id=?";

        try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, updatedUser.getUsername());
            pstmt.setString(2, updatedUser.getPassword());
            pstmt.setInt(3, updatedUser.getRoleId());
            pstmt.setInt(4, updatedUser.getStaffId());
            pstmt.setString(5, updatedUser.getFirstName());
            pstmt.setString(6, updatedUser.getLastName());
            pstmt.setInt(7, userId);

            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * Delete a user
     * @param userId The ID of the user to be deleted
     * @return True if the deletion was successful, false otherwise
     * @throws SQLException If a database access error occurs
     */
    public boolean deleteUser(int userId) throws SQLException {
        String sql = "DELETE FROM users WHERE user_id=?";
        try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * Retrieve all users from the database
     * @return A list of all users
     * @throws SQLException If a database access error occurs
     */
    public List<User> getAllUsers() throws SQLException {
        List<User> users = new ArrayList<>();
        String sql = "SELECT user_id, username, password_hash, role_id, staff_id, " +
                     "first_name, last_name, created_at FROM users";

        try (Statement stmt = DatabaseManager.getConnection().createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                users.add(new User(
                    rs.getInt("user_id"),
                    rs.getString("password_hash"),
                    rs.getString("username"),
                    rs.getInt("role_id"),
                    rs.getInt("staff_id"),
                    rs.getString("first_name"),
                    rs.getString("last_name")
                ));
            }
        }
        return users;
    }

    /**
     *
     * @param userId user id
     * @return User user object
     * @throws SQLException exception
     */
    public User getUserById(int userId) throws SQLException {
        String sql = "SELECT * FROM users WHERE user_id=?";
        try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() ? new User(
                    rs.getInt("user_id"),
                    rs.getString("password_hash"),
                    rs.getString("username"),
                    rs.getInt("role_id"),
                    rs.getInt("staff_id"),
                    rs.getString("first_name"),
                    rs.getString("last_name")
                ) : null;
            }
        }
    }

    /**
     * Get a user by username
     * @param username The username to search for
     * @return The User object if found, null otherwise
     * @throws SQLException If a database access error occurs
     */
    public User getUserByUsername(String username) throws SQLException {
        String sql = "SELECT * FROM users WHERE username=?";
        try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, username);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() ? new User(
                    rs.getInt("user_id"),
                    rs.getString("password_hash"),
                    rs.getString("username"),
                    rs.getInt("role_id"),
                    rs.getInt("staff_id"),
                    rs.getString("first_name"),
                    rs.getString("last_name")
                ) : null;
            }
        }
    }

    /**
     * Update a user's password
     * @param userId The ID of the user to update
     * @param newPassword The new password to set
     * @return true if the update was successful, false otherwise
     * @throws SQLException If a database access error occurs
     */
    public boolean updateUserPassword(int userId, String newPassword) throws SQLException {
        String sql = "UPDATE users SET password_hash=? WHERE user_id=?";
        try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, newPassword);
            pstmt.setInt(2, userId);
            return pstmt.executeUpdate() > 0;
        }
    }

    /**
     * Validate user credentials
     * @param username The username (email) to validate
     * @param password The password hash to validate
     * @return The validated User object, or null if validation fails
     * @throws SQLException If a database access error occurs
     */
    public User validateUser(String username, String password) throws SQLException {
        String sql = "SELECT * FROM users WHERE username=? AND password_hash=?";
        try (PreparedStatement pstmt = DatabaseManager.getConnection().prepareStatement(sql)) {
            pstmt.setString(1, username);
            pstmt.setString(2, password);
            try (ResultSet rs = pstmt.executeQuery()) {
                return rs.next() ? new User(
                    rs.getInt("user_id"),
                    rs.getString("password_hash"),
                    rs.getString("username"),
                    rs.getInt("role_id"),
                    rs.getInt("staff_id"),
                    rs.getString("first_name"),
                    rs.getString("last_name")
                ) : null;
            }
        }
    }

    /**
     * Validates user login credentials
     * @param username The username to validate
     * @param password The password to validate
     * @return true if credentials are valid, false otherwise
     */
    public boolean validateLogin(String username, String password) {
        try {
            User user = validateUser(username, password);
            return user != null;
        } catch (Exception e) {
            System.err.println("Login validation error for User :" + username + " : " + e.getMessage());
            return false;
        }
    }

    /**
     * Registers a new user
     * @param user The user to register
     * @return The generated user ID if successful, -1 otherwise
     */
    public int registerUser(User user) {
        try {
            return insertUser(user);
        } catch (Exception e) {
            System.err.println("User registration error: " + e.getMessage());
            return -1;
        }
    }

    /**
     * Resets the user's password directly without email or token
     * @param username The username of the user
     * @param newPassword The new password to set
     * @return true if password reset was successful, false otherwise
     */
    public boolean resetPassword(String username, String newPassword) {
        try {
            User user = getUserByUsername(username);
            if (user != null) {
                user.setPassword(newPassword);
                return updateUserPassword(user.getUser_id(), newPassword);
            }
            return false;
        } catch (Exception e) {
            System.err.println("Password reset error for User: " + username + " : " + e.getMessage());
            return false;
        }
    }
}

