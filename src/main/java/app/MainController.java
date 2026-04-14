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

    private User user;

    public void setUser(User user) {
        this.user = user;
    }

    @FXML
    private Button classListButton;
    @FXML
    private Button questionSetsButton;

    @FXML
    private Button loginButton;

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

    public void handleLogin() {
        try {
            FXMLLoader loader2 = new FXMLLoader(getClass().getResource("/fxml/LoginView.fxml"));
            Parent root2 = loader2.load();
            LoginController controller2 = loader2.getController();
            controller2.loadLoginScreen(user);

            Stage stage2 = (Stage) loginButton.getScene().getWindow();
            stage2.setScene(new Scene(root2));
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

    public void handleSignup(){
        try{
            FXMLLoader loader3 = new FXMLLoader(getClass().getResource("/fxml/SignupView.fxml"));
            Parent root3 = loader3.load();
            SignupController controller3 = loader3.getController();
            controller3.loadSignupScreen(user);

            Stage stage3 = (Stage) loginButton.getScene().getWindow();
            stage3.setScene(new Scene(root3));
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

}