package edu.careflow;


import edu.careflow.manager.DatabaseManager;
import edu.careflow.repository.dao.PatientDAO;
import edu.careflow.repository.dao.VitalsDAO;
import edu.careflow.repository.entities.Vitals;
import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDateTime;

public class Careflow extends Application {


    /**
     * What does the user sees first dba is the login page?
     *
     *  Check if user stays login;
     *      if true continue dashboard
     *      else : login page
     */

    public Careflow() {}

    // I have to set up a database too
    // its manager
    // Initializaiton of the stuff
    @FXML
    public void start(Stage stage) throws SQLException {

        PatientDAO patientDAO = new PatientDAO();

        Vitals vitals = new Vitals(12674, 1, 1, "120/80", 80, 16, 100.0, 180.0, 36.0, 98.0, LocalDateTime.now(), LocalDateTime.now());

        VitalsDAO vitalsDAO = new VitalsDAO();

        vitalsDAO.addVitals(vitals);

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

    public static void main(String[] args) {
        launch();

        // initiate date base here
        // check the database's connection

        // database initialization
        // connecting to the database.
        DatabaseManager.getInstance().initializeDatabase();

    }
}