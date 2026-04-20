package app;

import java.io.IOException;
import java.util.List;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.QuestionTracker;
import user.QuestionSet;
import user.User;

public class QuestionSetListController {
    @FXML private VBox setListVBox;
    @FXML private TextField newSetNameField;
    @FXML private Button createSetBtn;

    private User user;

    public void setUser(User user) {
        this.user = user;
        loadSets();
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainView.fxml"));
            Parent root = loader.load();
            
            MainController.setUser(user);
            
            Stage stage = (Stage) setListVBox.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleCreateSet() {
        String name = newSetNameField.getText();
        if (name == null || name.trim().isEmpty()) return;
        QuestionTracker.createQuestionSet(name.trim(), user);
        newSetNameField.setText("");
        loadSets();
    }

    private void loadSets() {
        setListVBox.getChildren().clear();
        List<QuestionSet> sets = java.util.Arrays.asList(QuestionTracker.getQuestionSetsForUser(user));
        for (QuestionSet s : sets) {
            HBox row = new HBox(8);
            Label lbl = new Label(s.getName());
            lbl.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(lbl, javafx.scene.layout.Priority.ALWAYS);
            Button open = new Button("Open");
            open.setOnAction(e -> openSet(s));
            row.getChildren().addAll(lbl, open);
            setListVBox.getChildren().add(row);
        }
    }

    private void openSet(QuestionSet s) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/QuestionSetView.fxml"));
            Parent root = loader.load();
            QuestionSetViewController controller = loader.getController();
            controller.setData(s, user);
            Stage stage = (Stage) setListVBox.getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/styles/styles.css").toExternalForm());
            stage.setScene(scene);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
