package edu.careflow.presentation.controllers.patient;

import edu.careflow.repository.dao.AttachmentDAO;
import edu.careflow.repository.entities.Attachment;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.sql.SQLException;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class AttachmentPageController {

    @FXML private VBox attachmentPageContainer;
    @FXML private VBox attachmentsCard;
    @FXML private VBox attachmentsContainer;
    @FXML private Button gridViewBtn;
    @FXML private Button pageLeft;
    @FXML private Button pageRight;
    @FXML private HBox paginationContainer;
    @FXML private Button tableViewBtn;

    private AttachmentDAO attachmentDAO;
    private List<Attachment> attachments;
    private int currentPage = 1;
    private final int ITEMS_PER_PAGE = 5;
    private boolean isGridView = true;
    private int patientId;

    public AttachmentPageController() {
        this.attachmentDAO = new AttachmentDAO();
    }

    public void initializeData(int patientId) {
        this.patientId = patientId;
        try {
            attachments = attachmentDAO.findByPatientId((long) patientId);
            setupButtons();
            updateView();
        } catch (SQLException e) {
            e.printStackTrace();
            // Handle error
        }
    }

    private void setupButtons() {
        gridViewBtn.setOnAction(e -> {
            isGridView = true;
            updateView();
            updateButtonStyles();
        });

        tableViewBtn.setOnAction(e -> {
            isGridView = false;
            updateView();
            updateButtonStyles();
        });

        pageLeft.setOnAction(e -> {
            if (currentPage > 1) {
                currentPage--;
                updateView();
            }
        });

        pageRight.setOnAction(e -> {
            if (currentPage < getTotalPages()) {
                currentPage++;
                updateView();
            }
        });

        updateButtonStyles();
    }

    private void updateButtonStyles() {
        gridViewBtn.getStyleClass().remove("active-view");
        tableViewBtn.getStyleClass().remove("active-view");

        if (isGridView) {
            gridViewBtn.getStyleClass().add("active-view");
        } else {
            tableViewBtn.getStyleClass().add("active-view");
        }
    }

    private void updateView() {
        attachmentsContainer.getChildren().clear();

        if (attachments == null || attachments.isEmpty()) {
            showEmptyState();
            return;
        }

        updatePaginationButtons();

        int startIndex = (currentPage - 1) * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, attachments.size());

        HBox currentRow = null;
        for (int i = startIndex; i < endIndex; i++) {
            try {
                if (i % 2 == 0) {
                    // Create new row for every even index
                    currentRow = new HBox(10);
                    currentRow.getStyleClass().add("attachment-row");
                    attachmentsContainer.getChildren().add(currentRow);
                }

                Node card = createAttachmentNode(attachments.get(i));
                if (currentRow != null) {
                    currentRow.getChildren().add(card);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void showEmptyState() {
        try {

            URL fxmlResource = getClass().getResource("/edu/careflow/fxml/components/states/emptyRecordstCard.fxml");
            Parent cardContent = FXMLLoader.load(fxmlResource);

            Label emptyLabel = (Label) cardContent.lookup("#pageTitle");
            emptyLabel.setText("No Conditions Found");


            attachmentsContainer.getChildren().add(cardContent);

            paginationContainer.setVisible(false);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    private Node createAttachmentNode(Attachment attachment) throws IOException {
        if (isGridView) {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/edu/careflow/fxml/components/patient/attachmentImageCard.fxml"));
            VBox card = loader.load();

            ImageView preview = (ImageView) card.lookup("#preview");
            Label fileName = (Label) card.lookup("#fileName");
            Label uploadDate = (Label) card.lookup("#uploadDate");

            if (attachment.getFileType().equalsIgnoreCase("pdf")) {
                // Show PDF icon for PDF files
                preview.setImage(new Image(getClass().getResourceAsStream("/edu/careflow/images/icons/file-icon.png")));
            } else {
                // Show actual image content for images
                preview.setImage(new Image(new ByteArrayInputStream(attachment.getContent())));
            }

            fileName.setText(attachment.getOriginalName());
            uploadDate.setText("Uploaded on: " + formatDate(attachment));

            // Add click handler for preview
            preview.setOnMouseClicked(e -> showPreviewDialog(attachment));

            return card;
        } else {
            return createTableViewNode(attachment);
        }
    }



    private Node createGridViewNode(Attachment attachment) throws IOException {
        HBox cardRow = new HBox(10); // 10px spacing between cards
        cardRow.getStyleClass().add("attachment-card-row");

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/edu/careflow/fxml/components/patient/attachmentImageCard.fxml"));
        VBox card = loader.load();

        ImageView preview = (ImageView) card.lookup("#preview");
        Label fileName = (Label) card.lookup("#fileName");
        Label uploadDate = (Label) card.lookup("#uploadDate");

        if (isImageFile(attachment.getFileType())) {
            preview.setImage(new Image(new ByteArrayInputStream(attachment.getContent())));
        } else {
            preview.setImage(new Image(getClass().getResourceAsStream("/edu/careflow/images/icons/file-icon.png")));
        }

        fileName.setText(attachment.getOriginalName());
        uploadDate.setText("Uploaded on: " + formatDate(attachment));

        // Set preferred width for the card
        card.setPrefWidth(300); // Adjust this value as needed
        card.setMaxWidth(300);

        attachmentPageContainer.getChildren().add(card);
        return cardRow;
    }



    private void showPreviewDialog(Attachment attachment) {
        try {

            // Load the preview dialog FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/edu/careflow/fxml/components/patient/attachmentPreviewDialog.fxml"));
            VBox previewDialog =  loader.load();

            // Get dialog components
            ImageView previewImage = (ImageView) previewDialog.lookup("#previewImage");
            Label titleLabel = (Label) previewDialog.lookup("#titleLabel");
            Label fileNameLabel = (Label) previewDialog.lookup("#fileNameLabel");
            Label descriptionLabel = (Label) previewDialog.lookup("#descriptionLabel");
            Label uploadDateLabel = (Label) previewDialog.lookup("#uploadDateLabel");
            Button closeButton = (Button) previewDialog.lookup("#closeButton");

            // Set content
            titleLabel.setText("View Attachment");
            String fileName = attachment.getOriginalName();
            fileNameLabel.setText(fileName);


            previewImage.setImage(new Image(new ByteArrayInputStream(attachment.getContent())));
            // print the file type
//            System.out.println("File Type: " + attachment.getFileType());
//            if (isImageFile(attachment.getFileType())) {
//                previewImage.setI5mage(new Image(new ByteArrayInputStream(attachment.getContent())));
//            } else {
//                previewImage.setImage(new Image(getClass().getResourceAsStream("/edu/careflow/images/icons/file-icon.png")));
//            }

            descriptionLabel.setText(attachment.getDescription());
            uploadDateLabel.setText(formatDate(attachment));

            // Find stackPaneContainer from scene root
            Scene scene = attachmentPageContainer.getScene();
            if (scene != null) {
                StackPane container = (StackPane) scene.lookup("#stackPaneContainer");
                if (container != null) {
                    // Add fade-in animation
                    FadeTransition fadeIn = new FadeTransition(Duration.millis(300), previewDialog);
                    fadeIn.setFromValue(0.0);
                    fadeIn.setToValue(1.0);

                    container.getChildren().add(previewDialog);
                    StackPane.setAlignment(previewDialog, Pos.CENTER);
                    fadeIn.play();

                    // Setup close button handler
                    closeButton.setOnAction(event -> {
                        FadeTransition fadeOut = new FadeTransition(Duration.millis(300), previewDialog);
                        fadeOut.setFromValue(1.0);
                        fadeOut.setToValue(0.0);
                        fadeOut.setOnFinished(e -> container.getChildren().remove(previewDialog));
                        fadeOut.play();
                    });
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Node createTableViewNode(Attachment attachment) throws IOException {
        VBox rowContainer = new VBox(10); // Container for the table row
        rowContainer.getStyleClass().add("table-row-container");

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/edu/careflow/fxml/components/patient/attachmentTableRow.fxml"));
        HBox row = loader.load();

        FontIcon fileIcon = (FontIcon) row.lookup("#fileIcon");
        Hyperlink fileName = (Hyperlink) row.lookup("#fileName");
        Label fileType = (Label) row.lookup("#fileType");
        Label uploadDate = (Label) row.lookup("#uploadDate");

        setFileIcon(fileIcon, attachment.getFileType());
        fileName.setText(attachment.getOriginalName());
        fileType.setText(attachment.getFileType().toUpperCase());
        uploadDate.setText(formatDate(attachment));
        fileName.setOnAction(e -> showPreviewDialog(attachment));
        attachmentsContainer.getChildren().add(row);
        return rowContainer;
    }

    private void setFileIcon(FontIcon icon, String fileType) {
        String iconLiteral = switch (fileType.toLowerCase()) {
            case "pdf" -> "fas-file-pdf";
            case "doc", "docx" -> "fas-file-word";
            case "xls", "xlsx" -> "fas-file-excel";
            case "jpg", "jpeg", "png" -> "fas-file-image";
            default -> "fas-file";
        };
        icon.setIconLiteral(iconLiteral);
    }

    private boolean isImageFile(String fileType) {
        return fileType.toLowerCase().matches("jpg|jpeg|png|gif");
    }

    private String formatDate(Attachment attachment) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy");
        return attachment.getUploadDate().format(formatter);
    }

    private void updatePaginationButtons() {
        pageLeft.setDisable(currentPage == 1);
        pageRight.setDisable(currentPage == getTotalPages());
    }

    private int getTotalPages() {
        return (int) Math.ceil((double) attachments.size() / ITEMS_PER_PAGE);
    }
}