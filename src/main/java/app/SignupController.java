package app;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import user.User;
import model.QuestionTracker;

import java.io.IOException;
import java.util.Arrays;

public class SignupController {

    @FXML private CheckBox teacherBox;
    @FXML private Button loginButton;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    private User[] users;

    @FXML
    public void handleSignup() {
        String username = usernameField.getText();
        String password = passwordField.getText();
        
        users = QuestionTracker.getUsers();
        User found = Arrays.stream(users)
                .filter(u -> u.getUsername().equals(username))
                .findFirst()
                .orElse(null);

        if (found != null) {
            errorLabel.setText("This username already exists");
            return;
        }
        User realUser = QuestionTracker.signUp(username, password, teacherBox.isSelected());

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainView.fxml"));
            Parent root = loader.load();

            assert realUser != null;
            MainController.setUser(realUser);

            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void handleLogin() {
        try{
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/LoginView.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) loginButton.getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/styles/styles.css").toExternalForm());
            stage.setScene(scene);
            app.UIUtils.fadeIn(root);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

}
