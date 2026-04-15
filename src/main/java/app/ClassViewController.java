package app;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.event.ActionEvent;

import model.questionTracker;
import user.Classroom;
import user.User;

public class ClassViewController {

    @FXML private Label classNameLabel;
    @FXML private Label classCodeLabel;
    @FXML private VBox studentListVBox;
    @FXML private VBox teacherSection;

    private Classroom classroom;
    private User user;

    public void setData(String classroomName, User user) {
        this.classroom = questionTracker.getClassByName(classroomName);
        this.user = user;

        loadClassData();
    }

    private void loadClassData() {
        if (classroom == null) return;

        classNameLabel.setText(classroom.getName());
        classCodeLabel.setText("Code: " + classroom.getCode());

        if (user != null && user.getIsTeacher()) {
            teacherSection.setVisible(true);
            loadStudents();
        } else {
            teacherSection.setVisible(false);
        }
    }

    private void loadStudents() {
        studentListVBox.getChildren().clear();

        for (User student : classroom.getStudents()) {
            Label studentLabel = new Label("• " + student.getUsername());
            studentLabel.setStyle("-fx-font-size: 14px;");
            studentListVBox.getChildren().add(studentLabel);
        }
    }

    @FXML
    private void handleViewStudySets(ActionEvent event) {
        System.out.println("Navigate to Study Sets screen (not implemented)");
    }

    @FXML
    private void handleViewStruggles(ActionEvent event) {
        System.out.println("Navigate to Student Struggles screen (not implemented)");
    }

    @FXML
    private void handleBack(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ClassListView.fxml"));
            Parent root = loader.load();

            ClassListController controller = loader.getController();
            controller.setUser(user);

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}