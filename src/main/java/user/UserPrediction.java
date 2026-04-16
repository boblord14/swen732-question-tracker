package user;

import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.SingularValueDecomposition;

public class UserPrediction {
    double[][] userQuestionDataArray;
    String[] tags;

    public UserPrediction(User user){
        List<List<String>> questionData = user.getWrongQuestionData();

        //list of all tags in the user's "wrong" section
        String[] questions = questionData.stream()
                .flatMap(List::stream)
                .distinct()
                .toArray(String[]::new);

        double[][] tagMatrix = new double[questions.length][questionData.size()];

        for (int i = 0; i < questions.length; i++) {
            for (int j = 0; j < questionData.size(); j++) {
                tagMatrix[i][j] = questionData.get(j).contains(questions[i]) ? 1 : 0;
            }
        }
        userQuestionDataArray = tagMatrix;
        tags = questions;
    }

    /**
     * User specific helper method designed to show the user their struggles and recommend questions to help target those
     * areas. Calls the main logic, all this does is call it from an individual UserPrediction class.
     * @return a map of (Tag, struggleScore) where the higher a score is for a tag, the more struggle they have with it
     */
    public Map<String, Double> generateUserStruggleVector() {
        RealMatrix userQuestionMatrix = new Array2DRowRealMatrix(userQuestionDataArray);
        return computeStruggleVector(userQuestionMatrix, tags);
    }

    /**
     * Composite version of the struggle vector generator that can perform it from a list of users(looking at classes)
     * rather than a single user. Computes for each user individually, then combines them all and computes the final
     * result from the composite user array.
     *
     * What makes this version interesting is that in the final output vector, each user's struggles is weighted at a
     * total weight of 1. So if a user has a lot more wrong answers in one subject, it doesn't disproportionately throw
     * off the final result like a normal average might. That value would just be closer to 1 for the user in the input.
     *
     * If users dont have overlapping wrong question tag sets, this fizzles a bit and provides a slightly suspect result.
     * With that said, that should never be the case as a question set should by default, share tags across the board,
     * even if it's just a generic tag like "math" rather than a subject specific topic like "addition".
     *
     * In general, this will score higher tags that multiple users got wrong, which is pretty much what we want to see
     * on a class wide basis. Some users getting various things wrong here and there sure, but multiple users getting
     * one thing wrong speaks to the class fundamentally not understanding the topic(which is the point)
     * @param users user input list
     * @return a map of (Tag, struggleScore) where the higher a score is for a tag, the more struggle the class has with it
     */
    public static Map<String, Double> generateUserStruggleVector(List<User> users) {
        List<UserPrediction> userData = users.stream()
                .map(UserPrediction::new)
                .collect(Collectors.toList());

        String[] fullTagList = userData.stream()
                .flatMap(user -> Arrays.stream(user.tags))
                .distinct()
                .toArray(String[]::new);

        double[][] fullPredictionMatrix = new double[fullTagList.length][userData.size()];
        for (int i = 0; i < userData.size(); i++) {
            Map<String, Double> userVector = userData.get(i).generateUserStruggleVector();
            for  (int j = 0; j < fullTagList.length; j++) {
                fullPredictionMatrix[j][i] = userVector.getOrDefault(fullTagList[j], 0.0);
            }
        }

        RealMatrix compositeUserMatrix = new Array2DRowRealMatrix(fullPredictionMatrix);
        return computeStruggleVector(compositeUserMatrix, fullTagList);
    }

    /**
     * Core computing of struggle vector logic. Uses singular value decomposition(complete overkill, but I like it)
     * to take in a list of data from the user of what questions they struggled with, crunch it, and return a list of the
     * tags they struggle the most with. Results are normalized from 0 to 1 by the sum of all the struggle values.
     * @param struggleMatrix RealMatrix object of questions and their tags, 0 if question(column) didnt have the tag denoted by the
     *                       row, 1 if it did. Rows map to the tag strings list
     * @param tagStrings    List of tags as strings. Corresponds to the rows of the StruggleMatrix
     * @return Map in (Tag, struggle score) format of tags the user struggles with.
     */
    private static Map<String, Double> computeStruggleVector(RealMatrix struggleMatrix, String[] tagStrings){
        SingularValueDecomposition svd = new SingularValueDecomposition(struggleMatrix);

        double[] tagData = svd.getU().getColumn(0);

        double tagDataSum = 0;
        for (int i = 0; i < tagData.length; i++) {
            tagData[i] = Math.abs(tagData[i]); //svd can do negatives sometimes, dont want
            if (tagData[i] < 0.1) tagData[i] = 0.0; //clean up weirdly small values
            tagDataSum += tagData[i];
        }

        Map<String, Double> userStruggleVector = new HashMap<>();
        for (int i = 0; i < tagData.length; i++) {
            userStruggleVector.put(tagStrings[i], tagDataSum > 0 ? tagData[i] / tagDataSum : 0.0); //add but prevent divide by 0
        }
        return userStruggleVector;
    }

    /**
     * Quick scoring for a question to see how it lines up with a user's score vector. More specialized questions on
     * subjects the user struggles with will score higher than questions that tackle a number of tags that the user
     * has middling issues with.
     *
     * @param question question object to score
     * @param struggleVector user's struggle score vector
     * @return score of the question to see how well it aligns with the user's struggle score
     */
    public static double scoreQuestion(Question question, Map<String, Double> struggleVector) {
        List<String> questionTags = question.getTags();
        if (questionTags.isEmpty()) return 0.0;

        double score = 0.0;
        for (String tag : questionTags) {
            score = score + struggleVector.getOrDefault(tag, 0.0);
        }
        return score/questionTags.size();
    }
}
