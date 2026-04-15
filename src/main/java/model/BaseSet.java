package model;

import user.Question;

import java.util.List;

public interface BaseSet {
    int getId();
    String getName();
    List<Question> getQuestions();
    List<String> getTags();
    String getCreator();
}
