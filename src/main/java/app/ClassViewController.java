package app;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.Node;
import javafx.event.ActionEvent;

import model.questionTracker;
import model.studySetMaker;
import teacher.StudySet;
import user.Classroom;
import user.User;
import user.Question;
import user.QuestionSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ClassViewController {

    @FXML private Label classNameLabel;
    @FXML private Label classCodeLabel;
    @FXML private VBox studentListVBox;
    @FXML private VBox teacherSection;
    @FXML private VBox studySetListVBox;

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
            loadStudySets();
        } else {
            teacherSection.setVisible(false);
            loadStudySets();
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

    private void loadStudySets() {
        studySetListVBox.getChildren().clear();

        // Prefer persisted IDs
        List<Integer> ids = classroom.getAssignedStudySetIds();
        if (ids != null && !ids.isEmpty()) {
            for (Integer id : ids) {
                StudySet tset = studySetMaker.getSetById(id);
                if (tset != null) {
                    HBox row = new HBox(10);
                    String subj = tset.getSubject();
                    String titleText = tset.getTitle();
                    if (subj != null && !subj.trim().isEmpty()) titleText += " (" + subj + ")";
                    Label lbl = new Label(titleText);
                    Button action = new Button(user != null && user.getIsTeacher() ? "View" : "Take");
                    action.setOnAction(e -> takeStudySet(tset));
                    row.getChildren().addAll(lbl, action);
                    studySetListVBox.getChildren().add(row);
                    continue;
                }

                // try centralized QuestionSet (user.QuestionSet)
                QuestionSet qset = questionTracker.getQuestionSetById(id);
                if (qset != null) {
                    HBox row = new HBox(10);
                    Label lbl = new Label(qset.getName() + " (question set)");
                    Button action = new Button(user != null && user.getIsTeacher() ? "View" : "Take");
                    action.setOnAction(e -> takeStudySet(qset));
                    row.getChildren().addAll(lbl, action);
                    studySetListVBox.getChildren().add(row);
                    continue;
                }
            }
            return;
        }

        // Fallback: runtime StudySet objects
        if (classroom.getAssignedStudySets() != null) {
            for (StudySet set : classroom.getAssignedStudySets()) {
                if (set == null) continue;
                HBox row = new HBox(10);
                String subj = set.getSubject();
                String titleText = set.getTitle();
                if (subj != null && !subj.trim().isEmpty()) titleText += " (" + subj + ")";
                Label lbl = new Label(titleText);
                Button action = new Button(user != null && user.getIsTeacher() ? "View" : "Take");
                action.setOnAction(e -> takeStudySet(set));
                row.getChildren().addAll(lbl, action);
                studySetListVBox.getChildren().add(row);
            }
        }
    }

    @FXML
    private void handleCreateStudySet(ActionEvent event) {
        if (user == null || !user.getIsTeacher()) return;

        TextInputDialog titleDlg = new TextInputDialog();
        titleDlg.setGraphic(null);
        titleDlg.setHeaderText("Enter study set title");
        Optional<String> titleOpt = titleDlg.showAndWait();
        if (!titleOpt.isPresent()) return;
        String title = titleOpt.get().trim();
        if (title.isEmpty()) return;

        TextInputDialog subjDlg = new TextInputDialog();
    subjDlg.setGraphic(null);
        subjDlg.setHeaderText("Enter subject (optional)");
        Optional<String> subjOpt = subjDlg.showAndWait();
        String subject = subjOpt.map(String::trim).orElse("");

        TextInputDialog numDlg = new TextInputDialog("3");
    numDlg.setGraphic(null);
        numDlg.setHeaderText("How many questions?");
        Optional<String> numOpt = numDlg.showAndWait();
        int count = 0;
        try { count = Integer.parseInt(numOpt.orElse("0").trim()); } catch (Exception ex) { count = 0; }
        if (count <= 0) count = 1;

        ArrayList<Question> questions = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            TextInputDialog qDlg = new TextInputDialog();
            qDlg.setGraphic(null);
            qDlg.setHeaderText("Question " + i);
            Optional<String> qOpt = qDlg.showAndWait();
            if (!qOpt.isPresent()) break;
            String qText = qOpt.get().trim();
            if (qText.isEmpty()) break;

            TextInputDialog aDlg = new TextInputDialog();
            aDlg.setGraphic(null);
            aDlg.setHeaderText("Answer for question " + i);
            Optional<String> aOpt = aDlg.showAndWait();
            String ans = aOpt.map(String::trim).orElse("");

            questions.add(new Question(i, qText, ans));
        }

        teacher.StudySet set = studySetMaker.createSet(questions, user, title, subject);

        // assign to class and persist
        boolean assigned = questionTracker.assignStudySetToClass(classroom.getName(), set.getId());
        if (assigned) {
            // also add to user's owned sets and persist user
            try {
                user.addQuestionSetId(set.getId());
                questionTracker.saveUser(user);
            } catch (Exception ex) { ex.printStackTrace(); }
            // update local classroom object
            classroom.addStudySet(set);
            classroom.addAssignedStudySetId(set.getId());
            loadStudySets();
            Alert a = new Alert(Alert.AlertType.INFORMATION, "Study set created and assigned to class.", ButtonType.OK);
            a.setGraphic(null);
            a.showAndWait();
        } else {
            Alert a = new Alert(Alert.AlertType.WARNING, "Failed to assign study set to class.", ButtonType.OK);
            a.setGraphic(null);
            a.showAndWait();
        }
    }

    private void takeStudySet(StudySet set) {
        if (set == null || user == null) return;
        takeQuestions(set.getQuestionSet(), set.getId());
    }

    private void takeStudySet(QuestionSet qset) {
        if (qset == null || user == null) return;
        takeQuestions(qset.getQuestions(), qset.getId());
    }

    private void takeQuestions(List<Question> qlist, int setId) {
        if (qlist == null || qlist.isEmpty()) {
            Alert a = new Alert(Alert.AlertType.INFORMATION, "This study set has no questions.", ButtonType.OK);
            a.showAndWait();
            return;
        }

        int correct = 0;
        for (Question q : qlist) {
            TextInputDialog dlg = new TextInputDialog();
            dlg.setGraphic(null);
            dlg.setHeaderText(q.getText());
            Optional<String> ansOpt = dlg.showAndWait();
            String ans = ansOpt.map(String::trim).orElse("");
            boolean ok = ans.equalsIgnoreCase(q.getAnswer());
            Alert res = new Alert(Alert.AlertType.INFORMATION, ok ? "Correct" : "Incorrect. Correct: " + q.getAnswer(), ButtonType.OK);
            res.setGraphic(null);
            res.showAndWait();
            if (ok) correct++;
        }

        double avg = (double) correct / (double) qlist.size();
        user.addStudySetScore(setId, avg);
        questionTracker.saveUser(user);

        Alert fin = new Alert(Alert.AlertType.INFORMATION, "You got " + correct + " out of " + qlist.size() + " (avg=" + avg + ")", ButtonType.OK);
        fin.setGraphic(null);
        fin.showAndWait();
    }

    // Study sets (Question Sets) are now managed from the main app screen; class view keeps class-only controls.

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
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/styles/styles.css").toExternalForm());
            stage.setScene(scene);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}