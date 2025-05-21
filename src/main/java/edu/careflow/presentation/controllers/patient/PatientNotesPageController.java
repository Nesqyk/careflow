package edu.careflow.presentation.controllers.patient;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Pagination;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import edu.careflow.repository.dao.VisitNoteDAO;
import edu.careflow.repository.entities.VisitNote;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PatientNotesPageController {

    @FXML
    private HBox buttonsAptFilter;

    @FXML
    private VBox containerMain;

    @FXML
    private Button newNotesBtn;

    @FXML
    private Button oldNotesBtn;

    @FXML
    private Pagination notesPagination;

    private int patientId;
    private final VisitNoteDAO visitNoteDAO = new VisitNoteDAO();
    private List<VisitNote> allNotes = List.of();
    private List<VisitNote> filteredNotes = List.of();
    private static final int NOTES_PER_PAGE = 3;
    private static final int NEW_NOTES_DAYS = 30;

    public void setPatientId(int patientId) {
        this.patientId = patientId;
        loadVisitNotes();
    }

    @FXML
    private void initialize() {
        if (newNotesBtn != null) newNotesBtn.setOnAction(e -> showNewNotes());
        if (oldNotesBtn != null) oldNotesBtn.setOnAction(e -> showOldNotes());
    }

    private void loadVisitNotes() {
        containerMain.getChildren().clear();
        try {
            allNotes = visitNoteDAO.getVisitNotesByPatientId(patientId);
            filteredNotes = allNotes;
            int pageCount = (int) Math.ceil((double) filteredNotes.size() / NOTES_PER_PAGE);
            notesPagination.setPageCount(Math.max(pageCount, 1));
            notesPagination.setCurrentPageIndex(0);
            notesPagination.setPageFactory(this::pageFactoryWithMainContainer);
        } catch (SQLException e) {
            Label errorLabel = new Label("Failed to load visit notes: " + e.getMessage());
            errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 14px;");
            containerMain.getChildren().add(errorLabel);
        }
    }

    private void showNewNotes() {
        LocalDate threshold = LocalDate.now().minusDays(NEW_NOTES_DAYS);
        filteredNotes = allNotes.stream()
            .filter(note -> note.getVisitDate().isAfter(threshold) || note.getVisitDate().isEqual(threshold))
            .toList();
        updatePaginationAndDisplay();
    }

    private void showOldNotes() {
        LocalDate threshold = LocalDate.now().minusDays(NEW_NOTES_DAYS);
        filteredNotes = allNotes.stream()
            .filter(note -> note.getVisitDate().isBefore(threshold))
            .toList();
        updatePaginationAndDisplay();
    }

    private void updatePaginationAndDisplay() {
        int pageCount = (int) Math.ceil((double) filteredNotes.size() / NOTES_PER_PAGE);
        notesPagination.setPageCount(Math.max(pageCount, 1));
        notesPagination.setCurrentPageIndex(0);
        notesPagination.setPageFactory(this::pageFactoryWithMainContainer);
    }

    private VBox pageFactoryWithMainContainer(int pageIndex) {
        VBox pageBox = createPage(pageIndex);
        containerMain.getChildren().clear();
        containerMain.getChildren().add(pageBox);
        return pageBox;
    }

    private VBox createPage(int pageIndex) {
        VBox pageBox = new VBox(16);
        int start = pageIndex * NOTES_PER_PAGE;
        int end = Math.min(start + NOTES_PER_PAGE, filteredNotes.size());
        for (int i = start; i < end; i++) {
            VisitNote note = filteredNotes.get(i);
            pageBox.getChildren().add(createVisitNoteCard(note));
        }
        return pageBox;
    }

    private Node createVisitNoteCard(VisitNote note) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/edu/careflow/fxml/components/patient/visitNoteCard.fxml"));
            Node card = loader.load();
            // Set fields
            Label visitDateLabel = (Label) card.lookup("#visitDateLabel");
            Label doctorNameLabel = (Label) card.lookup("#doctorNameLabel");
            Label primaryDiagnosisLabel = (Label) card.lookup("#primaryDiagnosisLabel");
            Label secondaryDiagnosisLabel = (Label) card.lookup("#secondaryDiagnosisLabel");
            Label notesLabel = (Label) card.lookup("#notesLabel");
            ImageView noteImageView = (ImageView) card.lookup("#noteImageView");

            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            visitDateLabel.setText(note.getVisitDate().format(dateFormatter));
            doctorNameLabel.setText("Doctor ID: " + note.getDoctorId());
            primaryDiagnosisLabel.setText(note.getPrimaryDiagnosis());
            secondaryDiagnosisLabel.setText(note.getSecondaryDiagnosis() != null ? note.getSecondaryDiagnosis() : "-");
            notesLabel.setText(note.getNotes());

            if (note.getImageData() != null && note.getImageData().length > 0) {
                noteImageView.setImage(new Image(new ByteArrayInputStream(note.getImageData())));
                noteImageView.setVisible(true);
            } else {
                noteImageView.setImage(new Image(getClass().getResourceAsStream("/edu/careflow/images/defaultNoteImage.png")));
            }
            return card;
        } catch (IOException e) {
            Label errorLabel = new Label("Error loading note card: " + e.getMessage());
            errorLabel.setStyle("-fx-text-fill: red; -fx-font-size: 14px;");
            return errorLabel;
        }
    }
}
