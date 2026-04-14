package app;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.QuestionSetSession;
import model.questionTracker;
import user.Question;
import user.QuestionSet;
import user.User;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class QuestionSetViewController {
    @FXML private Label setTitleLabel;
    @FXML private VBox questionListVBox;
    @FXML private VBox sessionBox;
    @FXML private Label sessionQuestionLabel;
    @FXML private Label sessionAnswerLabel;

    private QuestionSet set;
    private User user;
    private QuestionSetSession session;

    public void setData(QuestionSet set, User user) {
        this.set = set;
        this.user = user;
        refreshView();
    }

    private void refreshView() {
        setTitleLabel.setText(set.getName());
        questionListVBox.getChildren().clear();
        for (Question q : set.getQuestions()) {
            HBox row = new HBox(8);
            Label lbl = new Label(q.getText() + " — " + q.getAnswer());
            lbl.setMaxWidth(Double.MAX_VALUE);
            HBox.setHgrow(lbl, javafx.scene.layout.Priority.ALWAYS);
            row.getChildren().addAll(lbl);
            questionListVBox.getChildren().add(row);
        }
    }

    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/QuestionSetListView.fxml"));
            Parent root = loader.load();
            QuestionSetListController ctrl = loader.getController();
            ctrl.setUser(user);
            Stage stage = (Stage) setTitleLabel.getScene().getWindow();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/styles/styles.css").toExternalForm());
            stage.setScene(scene);
            // fade in new scene content
            app.UIUtils.fadeIn(root);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleAddQuestion() {
        Dialog<List<String>> dialog = new Dialog<>();
        dialog.setGraphic(null);
        dialog.setTitle("Add Question");
        ButtonType addBtn = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addBtn, ButtonType.CANCEL);

        VBox box = new VBox(8);
        TextField qField = new TextField(); qField.setPromptText("Question text");
        TextField aField = new TextField(); aField.setPromptText("Answer text");
        TextField tagsField = new TextField(); tagsField.setPromptText("Tags (comma separated)");
        box.getChildren().addAll(new Label("Question:"), qField, new Label("Answer:"), aField, new Label("Tags:"), tagsField);
        dialog.getDialogPane().setContent(box);

        dialog.setResultConverter(btn -> {
            if (btn == addBtn) {
                String q = qField.getText();
                String a = aField.getText();
                String tags = tagsField.getText();
                return Arrays.asList(q, a, tags == null ? "" : tags);
            }
            return null;
        });

        dialog.showAndWait().ifPresent(list -> {
            String q = list.get(0);
            String a = list.get(1);
            List<String> tags = Arrays.stream(list.get(2).split(",")).map(String::trim).filter(s -> !s.isEmpty()).collect(Collectors.toList());
            questionTracker.addQuestionToSet(set.getId(), q, a, tags);
            // reload the set from storage
            QuestionSet[] sets = questionTracker.getQuestionSets();
            for (QuestionSet s : sets) if (s.getId() == set.getId()) { set = s; break; }
            refreshView();
        });
    }

    @FXML
    private void handleTakeSet() {
        this.session = questionTracker.createQuestionSetSession(set.getId(), user);
        if (session == null) return;
        // hide the full question list while the user takes the set
        if (questionListVBox != null) {
            questionListVBox.setVisible(false);
            questionListVBox.setManaged(false);
        }
        sessionBox.setVisible(true);
        sessionBox.setManaged(true);
        advanceSession();
    }

    private void advanceSession() {
        if (session.hasNext()) {
            Question q = session.nextQuestion();
            sessionQuestionLabel.setText(q.getText());
            sessionAnswerLabel.setText("");
        } else {
            finishSession();
        }
    }

    private void finishSession() {
        // show summary and restore question list visibility
        int total = session.getTotalQuestions();
        int correct = 0;
        for (Boolean b : session.getResults().values()) if (b != null && b) correct++;
        double avg = total > 0 ? ((double) correct) / ((double) total) : 0.0;

        // persist score on user
        try {
            user.addStudySetScore(set.getId(), avg);
            questionTracker.saveUser(user);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // restore question list view
        if (questionListVBox != null) {
            questionListVBox.setVisible(true);
            questionListVBox.setManaged(true);
        }

        sessionBox.setVisible(false);
        sessionBox.setManaged(false);

        sessionQuestionLabel.setText("Session complete.");
        sessionAnswerLabel.setText("");

        Alert fin = new Alert(Alert.AlertType.INFORMATION, "You answered " + correct + " / " + total + " correctly (avg=" + avg + ")", ButtonType.OK);
        fin.setGraphic(null);
        fin.showAndWait();
    }

    @FXML
    private void handleShowAnswer() {
        sessionAnswerLabel.setText(session.revealAnswer());
    }

    @FXML
    private void handleMarkCorrect() {
        if (session == null) return;
        session.markLastAnswer(true);
        // move to next question immediately after marking
        advanceSession();
    }

    @FXML
    private void handleMarkWrong() {
        if (session == null) return;
        session.markLastAnswer(false);
        // move to next question immediately after marking
        advanceSession();
    }

    @FXML
    private void handleNext() {
        advanceSession();
    }
}
