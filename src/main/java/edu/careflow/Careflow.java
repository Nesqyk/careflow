package edu.careflow;


import edu.careflow.manager.DatabaseManager;
import edu.careflow.repository.dao.AppointmentDAO;
import edu.careflow.repository.dao.DoctorDAO;
import edu.careflow.repository.dao.PatientDAO;
import edu.careflow.repository.dao.UserDAO;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;

public class Careflow extends Application {


    /**
     * What does the user sees first dba is the login page?
     *
     *  Check if user stays login;
     *      if true continue dashboard
     *      else : login page
     */

    // 10099
    // 1975/8/5

    private AppointmentDAO appointmentDAO = new AppointmentDAO();
    private PatientDAO patientDAO = new PatientDAO();
    private UserDAO userDAO = new UserDAO();
    private DoctorDAO doctorDAO = new DoctorDAO();


    public Careflow() {}

    // I have to set up a database too
    // its manager
    // Initializaiton of the stuff
    @FXML
    public void start(Stage stage) throws SQLException, IOException {

//        byte[] imageContent = getClass().getResourceAsStream("images/logo/desktopIcon.png").readAllBytes();
//
//        Attachment attachment = new Attachment(
//                "desktopIcon.png",             // originalName
//                "sample_attachment_1",         // attachmentName
//                "image/png",                         // fileType
//                imageContent.length,    // fileSize
//                "Sample image attachment",     // description
//                imageContent,                  // content
//                10099,                       // patientId (using sample patient)
//                13430,                       // doctorId (using sample doctor)
//                3                            // recordId
//        );
//
//        // Save attachment using AttachmentDAO
//        AttachmentDAO attachmentDAO = new AttachmentDAO();
//        attachmentDAO.save(attachment);

//        Allergy allergy = new Allergy(1, 10099, "Severe", "mild", "Almost died dont feed this mf nuts!");
//
//        PatientDAO patientDAO = new PatientDAO();
//        patientDAO.addAllergy(allergy);

//         Vitals vitals = new Vitals(10099, 1, 1, "120/80", 80, 16, 100.0, 180.0, 36.0, 98.0, LocalDateTime.now(), LocalDateTime.now());
//
//         VitalsDAO vitalsDAO = new VitalsDAO();
//
//        vitalsDAO.addVitals(vitals);

        // insertSamplePatients();
//        Appointment appointment = new Appointment(9, 10099, 13430, 1, LocalDateTime.now(), "Scheduled", "none", LocalDateTime.now(), "", "", "", "");
//        appointmentDAO.addAppointment(appointment);
//
//        Doctor doctor = new Doctor(id, "Kim", "Kardashian", "General Medicine", "123", "123");
//        doctorDAO.createDoctor(doctor);
//
//        User user = new User(id, "test_123", "doctora_kim", 0, 0, "Kim", "Kardashian");
//
//        userDAO.insertUser(user);

        try {
            FXMLLoader fxmlLoader = new FXMLLoader(Careflow.class.getResource("fxml/loginPageNew.fxml"));
            Scene scene = new Scene(fxmlLoader.load(), 1024, 768);
            Image icon = new Image(Careflow.class.getResourceAsStream("images/logo/desktopIcon.png"));

            // we check Udao validate diba

            scene.getStylesheets().add(getClass().getResource("css/styles.css").toExternalForm());
            stage.getIcons().add(icon);
            stage.setTitle("Careflow");
            stage.setScene(scene);
            stage.setResizable(false);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//    public static void insertSamplePatients() {
//        PatientDAO patientDAO = new PatientDAO();
//
//        // Sample patient data with distinct names
//        List<Object[]> patientData = Arrays.asList(
//                new Object[]{"John", "Smith", LocalDate.of(1985, 3, 15), "Male", "0712345678", "john.smith@gmail.com", "123 Main St, Nairobi"},
//                new Object[]{"Sarah", "Johnson", LocalDate.of(1990, 7, 22), "Female", "0723456789", "sarah.j@gmail.com", "456 Oak Ave, Mombasa"},
//                new Object[]{"Michael", "Williams", LocalDate.of(1978, 11, 30), "Male", "0734567890", "michael.w@gmail.com", "789 Pine Rd, Kisumu"},
//                new Object[]{"Emily", "Brown", LocalDate.of(1992, 5, 18), "Female", "0745678901", "emily.brown@gmail.com", "101 Cedar Ln, Nakuru"},
//                new Object[]{"David", "Jones", LocalDate.of(1982, 9, 12), "Male", "0756789012", "david.jones@gmail.com", "202 Elm St, Eldoret"},
//                new Object[]{"Lisa", "Garcia", LocalDate.of(1995, 1, 25), "Female", "0767890123", "lisa.g@gmail.com", "303 Birch Dr, Thika"},
//                new Object[]{"Robert", "Miller", LocalDate.of(1975, 8, 5), "Male", "0778901234", "robert.m@gmail.com", "404 Maple Ave, Nyeri"},
//                new Object[]{"Jennifer", "Davis", LocalDate.of(1988, 12, 10), "Female", "0789012345", "jennifer.d@gmail.com", "505 Walnut St, Machakos"},
//                new Object[]{"Thomas", "Rodriguez", LocalDate.of(1980, 4, 20), "Male", "0790123456", "thomas.r@gmail.com", "606 Spruce Rd, Malindi"},
//                new Object[]{"Amanda", "Wilson", LocalDate.of(1993, 6, 17), "Female", "0701234567", "amanda.w@gmail.com", "707 Poplar Ln, Kitale"}
//        );
//
//        for (Object[] data : patientData) {
//            try {
//                int patientId = patientDAO.generateRandomPatientId(); // This generates a unique 5-digit ID
//                Patient patient = new Patient(
//                        patientId,
//                        (String) data[0],  // firstName
//                        (String) data[1],  // lastName
//                        (LocalDate) data[2],  // dateOfBirth
//                        (String) data[3],  // gender
//                        (String) data[4],  // contact
//                        (String) data[5],  // email
//                        (String) data[6],  // address
//                        null,  // createdAt will be set by DB default
//                        null   // updatedAt not used
//                );
//                patientDAO.insertPatient(patient);
//                System.out.println("Inserted patient: " + patient.getFirstName() + " " + patient.getLastName() + " with ID: " + patientId);
//            } catch (SQLException e) {
//                System.err.println("Error inserting patient: " + e.getMessage());
//                e.printStackTrace();
//            }
//        }
//        System.out.println("Sample patients inserted successfully");
//    }

    public static void main(String[] args) throws SQLException {
        launch();

        // initiate date base here
        // check the database's connection

        // database initialization
        // connecting to the database.
        DatabaseManager.getInstance().initializeDatabase();

    }
}