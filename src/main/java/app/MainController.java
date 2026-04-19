package app;

import java.io.IOException;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import user.User;

public class MainController {

    private static User currentUser;

    private static MainController instance;

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

    @FXML
    public void initialize() {
        instance = this;

        if (currentUser != null) {
            usernameLabel.setText("Welcome, " + currentUser.getUsername() + "!");
        }
    }

    private static final String CSS_SHEET = "/styles/styles.css";

    public static void setUser(User user) {
        currentUser = user;
        if (instance != null && instance.usernameLabel != null) {
            instance.usernameLabel.setText("Welcome, " + currentUser.getUsername() + "!");
        }
    }

    @FXML
    public void handleLoad() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ClassListView.fxml"));
            Parent root = loader.load();
            ClassListController controller = loader.getController();
            controller.setUser(currentUser);

            Stage stage = (Stage) classListButton.getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource(CSS_SHEET).toExternalForm());
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
            controller.setUser(currentUser);

            Stage stage = (Stage) questionSetsButton.getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource(CSS_SHEET).toExternalForm());
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

            ctrl.setDataStudent(currentUser);

            Stage stage = (Stage) userStruggleButton.getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource(CSS_SHEET).toExternalForm());
            stage.setScene(scene);
            app.UIUtils.fadeIn(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}