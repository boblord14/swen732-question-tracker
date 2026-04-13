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
    public void handleLoad() {
        User teacher = new User(1, "admin", "admin123", true);
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ClassListView.fxml"));
            Parent root = loader.load();
            ClassListController controller = loader.getController();
            controller.setUser(teacher);

            Stage stage = (Stage) classListButton.getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException exception) {
            exception.printStackTrace();
        }
    }

}