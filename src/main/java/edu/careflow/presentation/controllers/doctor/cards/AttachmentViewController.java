package edu.careflow.presentation.controllers.doctor.cards;

import edu.careflow.repository.dao.AttachmentDAO;
import edu.careflow.repository.entities.Attachment;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.videoio.VideoCapture;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.sql.SQLException;
import java.util.List;

public class AttachmentViewController {
    @FXML private Button closeButton;
    @FXML private ToggleButton fileUploadToggle;
    @FXML private ToggleButton webcamToggle;
    @FXML private StackPane uploadContainer;
    @FXML private StackPane webcamContainer;
    @FXML private VBox dropZone;
    @FXML private Button browseButton;
    @FXML private Button captureButton;
    @FXML private Button retakeButton;
    @FXML private VBox previewContainer;
    @FXML private FontIcon fileTypeIcon;
    @FXML private Label fileName;
    @FXML private Label fileSize;
    @FXML private Button removeButton;
    @FXML private ImageView previewImage;
    @FXML private ImageView webcamView;
    @FXML private TextField attachmentName;
    @FXML private TextArea description;
    @FXML private Label charCount;
    @FXML private Button cancelButton;
    @FXML private Button addButton;
    @FXML private ScrollPane scrollPaneAttachment;

    static {
        nu.pattern.OpenCV.loadLocally();
    }

    private final AttachmentDAO attachmentDAO;
    private File selectedFile;
    private byte[] fileContent;
    private int currentPatientId;
    private int currentDoctorId;
    private int currentRecordId;
    private Stage stage;
    private static final long MAX_FILE_SIZE = 1048576; // 1MB
    private static final List<String> SUPPORTED_TYPES = List.of(".jpg", ".jpeg", ".png", ".pdf");

    private VideoCapture webcam;
    private boolean isWebcamActive = false;
    private Thread webcamThread;

    public AttachmentViewController() {
        this.attachmentDAO = new AttachmentDAO();
    }


    private void setupToggleButtons() {
        ToggleGroup group = new ToggleGroup();
        fileUploadToggle.setToggleGroup(group);
        webcamToggle.setToggleGroup(group);

        group.selectedToggleProperty().addListener((obs, old, newVal) -> {
            if (newVal == null) {
                old.setSelected(true);
                return;
            }
            uploadContainer.setVisible(newVal == fileUploadToggle);
            uploadContainer.setManaged(newVal == fileUploadToggle);
            webcamContainer.setVisible(newVal == webcamToggle);
            webcamContainer.setManaged(newVal == webcamToggle);
        });
    }

    private void setupDragAndDrop() {
        dropZone.setOnDragOver(this::handleDragOver);
        dropZone.setOnDragDropped(this::handleDragDropped);
        dropZone.setOnDragExited(e -> dropZone.getStyleClass().remove("drag-over"));
    }

    private void handleDragOver(DragEvent event) {
        if (event.getDragboard().hasFiles()) {
            File file = event.getDragboard().getFiles().get(0);
            if (isValidFile(file)) {
                event.acceptTransferModes(TransferMode.COPY);
                dropZone.getStyleClass().add("drag-over");
            }
        }
        event.consume();
    }

    private void handleDragDropped(DragEvent event) {
        boolean success = false;
        if (event.getDragboard().hasFiles()) {
            File file = event.getDragboard().getFiles().get(0);
            if (isValidFile(file)) {
                handleFileSelection(file);
                success = true;
            }
        }
        event.setDropCompleted(success);
        event.consume();
    }

    private void setupButtons() {
        browseButton.setOnAction(e -> handleBrowseButton());
        removeButton.setOnAction(e -> clearFileSelection());
        closeButton.setOnAction(e -> closeDialog());
        cancelButton.setOnAction(e -> closeDialog());
        addButton.setOnAction(e -> handleSave());
        captureButton.setOnAction(e -> handleCapture());
        retakeButton.setOnAction(e -> handleRetake());
    }

    private void handleBrowseButton() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Supported Files", "*.jpg", "*.jpeg", "*.png", "*.pdf")
        );

        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            handleFileSelection(file);
        }
    }

    private void handleFileSelection(File file) {
        if (!isValidFile(file)) {
            showError("Invalid file type or size. Please select a supported file under 1MB.");
            return;
        }

        try {
            selectedFile = file;
            fileContent = Files.readAllBytes(file.toPath());
            fileName.setText(file.getName());
            fileSize.setText(String.format("%.2f KB", file.length() / 1024.0));
            previewContainer.setVisible(true);
            previewContainer.setManaged(true);

            if (file.getName().toLowerCase().endsWith(".pdf")) {
                fileTypeIcon.setIconLiteral("fas-file-pdf");
                previewImage.setVisible(false);
            } else {
                fileTypeIcon.setIconLiteral("fas-file-image");
                previewImage.setImage(new Image(file.toURI().toString()));
                previewImage.setVisible(true);
            }
        } catch (IOException ex) {
            showError("Error reading file: " + ex.getMessage());
        }
    }

    private void setupDescriptionCounter() {
        description.textProperty().addListener((obs, old, newVal) -> {
            int count = newVal.length();
            charCount.setText(count + "/500");
            if (count > 500) {
                description.setText(old);
            }
        });
    }




    private void clearFileSelection() {
        selectedFile = null;
        fileContent = null;
        previewContainer.setVisible(false);
        previewContainer.setManaged(false);
        previewImage.setImage(null);
        attachmentName.clear();
        description.clear();
    }

    private void closeDialog() {
        cleanupWebcam();
        FadeTransition fadeOut = new FadeTransition(Duration.millis(150), scrollPaneAttachment);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> {
            // Remove the scrollPane from its parent after fade
            if (scrollPaneAttachment.getParent() instanceof VBox parentBox) {
                parentBox.getChildren().remove(scrollPaneAttachment);
            } else if (scrollPaneAttachment.getParent() != null) {
                ((Pane) scrollPaneAttachment.getParent()).getChildren().remove(scrollPaneAttachment);
            }
            // Set to null to help garbage collection
            scrollPaneAttachment = null;
        });
        fadeOut.play();
    }

    private boolean isValidFile(File file) {
        return file.length() <= MAX_FILE_SIZE &&
                SUPPORTED_TYPES.stream().anyMatch(type ->
                        file.getName().toLowerCase().endsWith(type));
    }

    private String getFileExtension(File file) {
        String name = file.getName();
        int lastIndexOf = name.lastIndexOf(".");
        return lastIndexOf == -1 ? "" : name.substring(lastIndexOf);
    }


    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }



    @FXML
    private void initialize() {
        setupToggleButtons();
        setupDragAndDrop();
        setupButtons();
        setupDescriptionCounter();
        setupWebcam();

        // Add cleanup on window closing
        // stage.setOnCloseRequest(event -> cleanupWebcam());
    }

    private void setupWebcam() {
        webcam = new VideoCapture();
        webcamToggle.selectedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                startWebcam();
            } else {
                stopWebcam();
            }
        });
    }

    private void startWebcam() {
        if (!webcam.isOpened()) {
            webcam.open(0); // Open default camera
            if (!webcam.isOpened()) {
                showError("Failed to open webcam");
                webcamToggle.setSelected(false);
                return;
            }
        }

        isWebcamActive = true;
        webcamThread = new Thread(() -> {
            Mat frame = new Mat();
            while (isWebcamActive) {
                if (webcam.read(frame)) {
                    MatOfByte buffer = new MatOfByte();
                    Imgcodecs.imencode(".png", frame, buffer);
                    Image image = new Image(new ByteArrayInputStream(buffer.toArray()));
                    Platform.runLater(() -> webcamView.setImage(image));
                }
                try {
                    Thread.sleep(33); // ~30 FPS
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });
        webcamThread.setDaemon(true);
        webcamThread.start();

        captureButton.setVisible(true);
        retakeButton.setVisible(false);
    }

    private void stopWebcam() {
        isWebcamActive = false;
        if (webcamThread != null) {
            webcamThread.interrupt();
            webcamThread = null;
        }
        if (webcam.isOpened()) {
            webcam.release();
        }
        Platform.runLater(() -> webcamView.setImage(null));
    }

    private void handleCapture() {
        if (!webcam.isOpened()) {
            showError("Webcam is not active");
            return;
        }

        Mat frame = new Mat();
        if (webcam.read(frame)) {
            MatOfByte buffer = new MatOfByte();
            Imgcodecs.imencode(".png", frame, buffer);
            fileContent = buffer.toArray();

            try {
                selectedFile = File.createTempFile("webcam_capture_", ".png");
                Files.write(selectedFile.toPath(), fileContent);

                fileName.setText("Webcam Capture");
                fileSize.setText(String.format("%.2f KB", selectedFile.length() / 1024.0));

                Image capturedImage = new Image(new ByteArrayInputStream(fileContent));
                previewImage.setImage(capturedImage);
                previewImage.setVisible(true);
                previewContainer.setVisible(true);
                previewContainer.setManaged(true);

                fileTypeIcon.setIconLiteral("fas-file-image");

                // Stop webcam preview after capture
                stopWebcam();
                webcamToggle.setSelected(false);

                captureButton.setVisible(false);
                retakeButton.setVisible(true);
            } catch (IOException ex) {
                showError("Error saving captured image: " + ex.getMessage());
            }
        }
    }

    private void handleRetake() {
        clearFileSelection();
        webcamToggle.setSelected(true);
        startWebcam();
        captureButton.setVisible(true);
        retakeButton.setVisible(false);
    }

    private void handleSave() {
        if (attachmentName.getText().trim().isEmpty()) {
            showError("Please enter an attachment name");
            return;
        }

        if (selectedFile == null) {
            showError("Please select a file or capture an image");
            return;
        }

        try {
            Attachment attachment = new Attachment(
                    selectedFile.getName(),
                    attachmentName.getText().trim(),
                    getFileExtension(selectedFile),
                    (int) selectedFile.length(),
                    description.getText().trim(),
                    fileContent,
                    currentPatientId,
                    currentDoctorId,
                    currentRecordId
            );

            attachmentDAO.save(attachment);
            cleanupWebcam();
            closeDialog();
        } catch (SQLException ex) {
            showError("Error saving attachment: " + ex.getMessage());
        }
    }

    private void cleanupWebcam() {
        stopWebcam();
        if (selectedFile != null && selectedFile.getName().startsWith("webcam_capture_")) {
            selectedFile.delete();
        }
    }


    // Setters for external data
    public void setPatientId(int patientId) {
        this.currentPatientId = patientId;
    }

    public void setDoctorId(int doctorId) {
        this.currentDoctorId = doctorId;
    }

    public void setRecordId(int recordId) {
        this.currentRecordId = recordId;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }
}