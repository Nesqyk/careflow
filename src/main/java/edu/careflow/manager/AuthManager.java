package edu.careflow.manager;

import edu.careflow.repository.dao.PatientDAO;
import edu.careflow.repository.dao.UserDAO;
import edu.careflow.repository.entities.Patient;
import edu.careflow.repository.entities.User;

public class AuthManager {

    private final UserDAO userDAO = new UserDAO();
    private  final PatientDAO patientDAO = new PatientDAO();

    /**
     * Validates user login credentials
     * @param username The username to validate
     * @param password The password to validate
     * @return true if credentials are valid, false otherwise
     */
    public boolean validateLogin(String username, String password) {
        try {
            User user = userDAO.validateUser(username, password);
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
            return userDAO.insertUser(user);
        } catch (Exception e) {
            System.err.println("User registration error: " + e.getMessage());
            return -1;
        }
    }

    /**
     * Handles user logout
     */
    public void logout() {

    }

    /**
     * Resets the user's password directly without email or token
     * @param username The username of the user
     * @param newPassword The new password to set
     * @return true if password reset was successful, false otherwise
     */
    public boolean resetPassword(String username, String newPassword) {
        try {
            User user = userDAO.getUserByUsername(username);
            if (user != null) {
                user.setPassword(newPassword);
                return userDAO.updateUserPassword(user.getId(), newPassword);
            }
            return false;
        } catch (Exception e) {
            System.err.println("Password reset error for User: " + username + " : " + e.getMessage());
            return false;
        }
    }

//    /**
//     * Special login interface for patients
//     * @param patientId The patient's ID
//     * @param password The patient's password
//     * @return true if login successful, false otherwise
//     */
//    public boolean patientLogin(int patientId, String dateOfBirth) {
//        try {
//           Patient patient = patientDAO.getPatientById(patientId);
//
//           if(patientDAO.validatePatientLogin(patientId, LocalDate.parse(dateOfBirth)))
//        } catch (Exception e) {
//            System.err.println("Patient login error: " + e.getMessage());
//            return false;
//        }
//    }
}