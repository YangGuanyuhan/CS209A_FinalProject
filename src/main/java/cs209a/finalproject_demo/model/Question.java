package cs209a.finalproject_demo.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.JsonNode;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Question {
    @JsonProperty("question_id")
    private long questionId;

    private String title;
    private String body;
    private List<String> tags;

    @JsonProperty("creation_date")
    private long creationDate;

    private int score;

    @JsonProperty("view_count")
    private int viewCount;

    @JsonProperty("answer_count")
    private int answerCount;

    @JsonProperty("is_answered")
    private boolean answered;

    @JsonProperty("accepted_answer_id")
    private Long acceptedAnswerId;

    private long ownerId;
    private String ownerDisplayName;
    private int ownerReputation;

    @JsonProperty("answers_data")
    private List<Answer> answers;

    private List<Comment> comments;

    public Question() {
        this.tags = new ArrayList<>();
        this.answers = new ArrayList<>();
        this.comments = new ArrayList<>();
    }

    // Parse nested owner object from JSON
    @JsonSetter("owner")
    public void setOwner(JsonNode ownerNode) {
        if (ownerNode != null) {
            if (ownerNode.has("user_id")) {
                this.ownerId = ownerNode.get("user_id").asLong();
            }
            if (ownerNode.has("display_name")) {
                this.ownerDisplayName = ownerNode.get("display_name").asText();
            }
            if (ownerNode.has("reputation")) {
                this.ownerReputation = ownerNode.get("reputation").asInt();
            }
        }
    }

    // Getters and Setters
    public long getQuestionId() {
        return questionId;
    }

    public void setQuestionId(long questionId) {
        this.questionId = questionId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
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

    public int getViewCount() {
        return viewCount;
    }

    public void setViewCount(int viewCount) {
        this.viewCount = viewCount;
    }

    public int getAnswerCount() {
        return answerCount;
    }

    public void setAnswerCount(int answerCount) {
        this.answerCount = answerCount;
    }

    public boolean isAnswered() {
        return answered;
    }

    public void setAnswered(boolean answered) {
        this.answered = answered;
    }

    public Long getAcceptedAnswerId() {
        return acceptedAnswerId;
    }

    public void setAcceptedAnswerId(Long acceptedAnswerId) {
        this.acceptedAnswerId = acceptedAnswerId;
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

    public List<Answer> getAnswers() {
        return answers;
    }

    public void setAnswers(List<Answer> answers) {
        this.answers = answers;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }
}
