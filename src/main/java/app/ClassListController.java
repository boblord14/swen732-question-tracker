package app;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import javafx.geometry.Insets;
import javafx.util.Pair;
import java.util.Optional;
import model.questionTracker;
import user.Classroom;
import user.User;

import java.io.IOException;
import java.util.List;

public class ClassListController {
    private User user;

    public void setUser(User user) {
        this.user = user;
        configureUI();
    }

    @FXML
    private VBox classListBox;
    @FXML
    private Button actionButton;

    @FXML
    public void handleAction() {
        if (user.getIsTeacher()) {
            handleCreateClass();
        } else {
            handleJoinClass();
        }
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainView.fxml"));
            Parent root = loader.load();

            MainController controller = loader.getController();
            controller.setUser(user);

            Stage stage = (Stage) classListBox.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private void loadClasses(){
        classListBox.getChildren().clear();
        List<String> classes = user.getClassrooms();

        for (String c : classes) {
            Button btn = new Button(c);

            btn.setMaxWidth(Double.MAX_VALUE); // fill VBox width

            btn.setOnAction(e -> {
                System.out.println("Clicked: " + c);
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ClassView.fxml"));
                    Parent root = loader.load();
                    ClassViewController controller = loader.getController();
                    controller.setData(c, user);

                    Stage stage = (Stage) classListBox.getScene().getWindow();
                    Scene scene = new Scene(root);
                    scene.getStylesheets().add(getClass().getResource("/styles/styles.css").toExternalForm());
                    stage.setScene(scene);
                } catch (IOException exception) {
                    exception.printStackTrace();
                }
            });

            classListBox.getChildren().add(btn);
        }
    }

    private void configureUI() {
        loadClasses();
        if (user.getIsTeacher()) {
            actionButton.setText("Create Class");
        } else {
            actionButton.setText("Join Class");
        }
    }
    
    private void handleCreateClass() {
        Dialog<Pair<String, String>> dialog = new Dialog<>();
        dialog.setGraphic(null);
        dialog.setTitle("Create Class");
        dialog.setHeaderText("Enter Class Details");
    
        ButtonType createButtonType = new ButtonType("Create", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);
    
        // Layout
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));
    
        TextField nameField = new TextField();
        nameField.setPromptText("Class Name");
    
        TextField codeField = new TextField();
        codeField.setPromptText("Class Code");
    
        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Code:"), 0, 1);
        grid.add(codeField, 1, 1);
    
        dialog.getDialogPane().setContent(grid);
    
        // Convert result
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == createButtonType) {
                return new Pair<>(nameField.getText(), codeField.getText());
            }
            return null;
        });
    
        Optional<Pair<String, String>> result = dialog.showAndWait();
    
        result.ifPresent(data -> {
            String name = data.getKey();
            String code = data.getValue();
    
            System.out.println("Creating class: " + name + " (" + code + ")");
    
            questionTracker.createClass(name, code, user);
        });
        loadClasses();
    }

    private void handleJoinClass() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setGraphic(null);
        dialog.setTitle("Join Class");
        dialog.setHeaderText("Enter Class Code");
        dialog.setContentText("Code:");

        Optional<String> result = dialog.showAndWait();

        result.ifPresent(code -> {
            System.out.println("Joining class with code: " + code);
            questionTracker.joinClass(user, code);
        });
        loadClasses();
    }
}