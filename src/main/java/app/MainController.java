package app;

import java.io.IOException;
import java.util.List;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import user.Classroom;
import user.User;

public class MainController {

    @FXML
    private Button classListButton;
    @FXML
    private Button questionSetsButton;

    @FXML
    public void handleLoad() {
        User teacher = model.questionTracker.getUserById(1);
        if (teacher == null) {
            // fallback to default transient user if persisted user not found
            teacher = new User(1, "admin", "admin123", true);
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ClassListView.fxml"));
            Parent root = loader.load();
            ClassListController controller = loader.getController();
            controller.setUser(teacher);

            Stage stage = (Stage) classListButton.getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/styles/styles.css").toExternalForm());
            stage.setScene(scene);
            // apply a small fade animation on scene load
            app.UIUtils.fadeIn(root);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    @FXML
    public void handleQuestionSets() {
        User user = model.questionTracker.getUserById(1);
        if (user == null) {
            user = new User(1, "admin", "admin123", true);
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/QuestionSetListView.fxml"));
            Parent root = loader.load();
            QuestionSetListController controller = loader.getController();
            controller.setUser(user);

            Stage stage = (Stage) questionSetsButton.getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/styles/styles.css").toExternalForm());
            stage.setScene(scene);
            // subtle fade in
            app.UIUtils.fadeIn(root);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

}