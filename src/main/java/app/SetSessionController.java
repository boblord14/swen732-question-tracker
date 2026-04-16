package app;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import model.SetSession;
import model.questionTracker;
import user.Question;
import user.User;

public class SetSessionController {

    @FXML private Label questionLabel;
    @FXML private Label answerLabel;     //relevant for question set mode
    @FXML private TextField answerField; //relevant for study set mode
    @FXML private Label resultLabel;
    @FXML private Button nextButton;
    @FXML private Button correctButton;
    @FXML private Button wrongButton;
    @FXML private Button submitButton;
    @FXML private Button showAnswerButton;
    @FXML private Label progressLabel;


    private SetSession session;
    private Question currentQuestion;
    private boolean answered = false;
    private User user;
    private Runnable onBack;


    public void setSession(SetSession session, User user, Runnable onBack) {
        this.session = session;
        this.user = user;
        this.onBack = onBack;
        loadNext();
    }

    private void loadNext() {
        if (!session.hasNext()) {
            finishSession();
            return;
        }

        currentQuestion = session.nextQuestion();
        questionLabel.setText(currentQuestion.getText());
        progressLabel.setText("Question " + session.getCurrentIndex() + "/" + session.getTotalQuestions());

        answerLabel.setText("");
        resultLabel.setText("");
        answerField.clear();

        answered = false;

        boolean study = session.getIsStudySet();


        //what's being used changes depending on study or question set mode
        answerField.setVisible(study);
        resultLabel.setVisible(study);
        submitButton.setVisible(study);

        answerLabel.setVisible(!study);

        correctButton.setVisible(!study);
        wrongButton.setVisible(!study);
        nextButton.setVisible(!study);
        showAnswerButton.setVisible(!study);

        //relevant for question set mode
        correctButton.setDisable(false);
        wrongButton.setDisable(false);
        nextButton.setDisable(true);
    }

    @FXML
    private void handleShowAnswer() {
        answerLabel.setText(currentQuestion.getAnswer());
    }

    @FXML
    private void handleSubmit() {
        String userAnswer = answerField.getText();

        if (userAnswer == null || userAnswer.trim().isEmpty()) {
            resultLabel.setText("Enter an answer");
            return;
        }

        boolean correct = session.submitAnswer(userAnswer);

        resultLabel.setText(correct ? "Correct" : "Incorrect");
        loadNext();
    }

    @FXML
    private void handleCorrect() {
        if (answered) return;

        session.submitAnswer("FLASHCARD_CORRECT"); // dummy string
        answered = true;

        correctButton.setDisable(true);
        wrongButton.setDisable(true);
        nextButton.setDisable(false);

        resultLabel.setText("Marked Correct");
    }

    @FXML
    private void handleWrong() {
        if (answered) return;

        session.submitAnswer("FLASHCARD_WRONG"); // dummy string
        answered = true;

        correctButton.setDisable(true);
        wrongButton.setDisable(true);
        nextButton.setDisable(false);

        resultLabel.setText("Marked Wrong");
    }

    @FXML
    private void handleNext() {
        loadNext();
    }

    private void finishSession() {
        int total = session.getTotalQuestions();
        int correct = 0;

        for (Boolean b : session.getResults().values()) {
            if (b != null && b) correct++;
        }

        double avg = (double) correct / total;

        Alert alert = new Alert(Alert.AlertType.INFORMATION,
                "Score: " + correct + "/" + total + " (" + avg + ")",
                ButtonType.OK);

        try {
            if(session.getIsStudySet()){
                user.addStudySetScore(session.getSetId(), avg);
                questionTracker.saveUser(user);
            }
        } catch (Exception e) { e.printStackTrace(); }

        alert.showAndWait();
        handleBack();
    }

    private void handleBack() {
        if (onBack != null) onBack.run();
    }
}
