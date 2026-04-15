package app;

import java.io.IOException;
import java.util.List;

import javafx.event.ActionEvent;
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

    private static User user;

    @FXML
    private Button classListButton;
    @FXML
    private Button questionSetsButton;
    @FXML
    private Button userStruggleButton;
    @FXML
    private Button logoutButton;
    @FXML
    private Label usernameLabel;

    public void setUser(User user) {
        this.user = user;
        this.usernameLabel.setText("Welcome, " + user.getUsername() + "!");
    }

    @FXML
    public void handleLoad() {
        //User teacher = new User(1, "admin", "admin123", true);
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ClassListView.fxml"));
            Parent root = loader.load();
            ClassListController controller = loader.getController();
            controller.setUser(user);

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

    @FXML
    public void handleLogout(ActionEvent actionEvent) {
        try {
            FXMLLoader loader2 = new FXMLLoader(getClass().getResource("/fxml/LoginView.fxml"));
            Parent root2 = loader2.load();

            Stage stage2 = (Stage) logoutButton.getScene().getWindow();
            stage2.setScene(new Scene(root2));
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    @FXML
    public void handleViewStruggles(ActionEvent actionEvent) {
        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/StruggleView.fxml"));
            Parent root = loader.load();
            StruggleViewController ctrl = loader.getController();

            ctrl.setDataStudent(user);

            Stage stage = (Stage) userStruggleButton.getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/styles/styles.css").toExternalForm());
            stage.setScene(scene);
            app.UIUtils.fadeIn(root);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}