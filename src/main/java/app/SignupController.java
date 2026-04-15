package app;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import user.User;
import model.questionTracker;

import java.io.IOException;
import java.util.Arrays;

public class SignupController {

    @FXML private Button loginButton;
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;

    private User[] users;

    @FXML
    public void handleSignup() {
        String username = usernameField.getText();
        String password = passwordField.getText();
        
        users = questionTracker.getUsers();
        User found = Arrays.stream(users)
                .filter(u -> u.getUsername().equals(username))
                .findFirst()
                .orElse(null);

        if (found != null) {
            errorLabel.setText("This username already exists");
            return;
        } else{
            questionTracker.signUp(username, password, false);
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/MainView.fxml"));
            Parent root = loader.load();

            MainController controller = loader.getController();
            controller.setUser(found);

            Stage stage = (Stage) usernameField.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void handleLogin(ActionEvent actionEvent) {
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
