package cs209a.finalproject_demo.model;

import java.util.ArrayList;
import java.util.List;

public class Answer {
    private long answerId;
    private long questionId;
    private String body;
    private long creationDate;
    private int score;
    private boolean isAccepted;
    private long ownerId;
    private String ownerDisplayName;
    private int ownerReputation;
    private List<Comment> comments;

    public Answer() {
        this.comments = new ArrayList<>();
    }

    // Getters and Setters
    public long getAnswerId() {
        return answerId;
    }

    public void setAnswerId(long answerId) {
        this.answerId = answerId;
    }

    public long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(long questionId) {
        this.questionId = questionId;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public long getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(long creationDate) {
        this.creationDate = creationDate;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public boolean isAccepted() {
        return isAccepted;
    }

    public void setAccepted(boolean accepted) {
        isAccepted = accepted;
    }

    public long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(long ownerId) {
        this.ownerId = ownerId;
    }

    public String getOwnerDisplayName() {
        return ownerDisplayName;
    }

    public void setOwnerDisplayName(String ownerDisplayName) {
        this.ownerDisplayName = ownerDisplayName;
    }

    public int getOwnerReputation() {
        return ownerReputation;
    }

    public void setOwnerReputation(int ownerReputation) {
        this.ownerReputation = ownerReputation;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }
}
