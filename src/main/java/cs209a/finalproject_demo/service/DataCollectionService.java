package cs209a.finalproject_demo.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import cs209a.finalproject_demo.model.Answer;
import cs209a.finalproject_demo.model.Comment;
import cs209a.finalproject_demo.model.Question;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.GZIPInputStream;

@Service
public class DataCollectionService {

    private static final String BASE_URL = "https://api.stackexchange.com/2.3";
    private static final String SITE = "stackoverflow";
    private static final String DATA_FILE = "stackoverflow_data.json";
    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Collect questions with 'java' tag from Stack Overflow
     * 
     * @param maxQuestions Maximum number of questions to collect
     * @return List of questions with answers and comments
     */
    public List<Question> collectData(int maxQuestions) throws Exception {
        List<Question> allQuestions = new ArrayList<>();
        int page = 1;
        int pageSize = 100;

        while (allQuestions.size() < maxQuestions) {
            System.out.println("Fetching page " + page + "...");

            String url = String.format(
                    "%s/questions?page=%d&pagesize=%d&order=desc&sort=votes&tagged=java&site=%s&filter=withbody",
                    BASE_URL, page, pageSize, SITE);

            JsonNode response = makeApiRequest(url);
            JsonNode items = response.get("items");

            if (items == null || items.size() == 0) {
                break;
            }

            for (JsonNode item : items) {
                if (allQuestions.size() >= maxQuestions) {
                    break;
                }

                Question question = parseQuestion(item);

                // Fetch answers for this question
                question.setAnswers(fetchAnswers(question.getQuestionId()));

                // Small delay to avoid rate limiting
                Thread.sleep(100);

                allQuestions.add(question);
            }

            // Check if there are more pages
            if (!response.get("has_more").asBoolean()) {
                break;
            }

            page++;

            // Respect rate limits
            Thread.sleep(1000);
        }

        return allQuestions;
    }

    /**
     * Fetch answers for a specific question
     */
    private List<Answer> fetchAnswers(long questionId) throws Exception {
        String url = String.format(
                "%s/questions/%d/answers?order=desc&sort=votes&site=%s&filter=withbody",
                BASE_URL, questionId, SITE);

        JsonNode response = makeApiRequest(url);
        JsonNode items = response.get("items");

        List<Answer> answers = new ArrayList<>();
        if (items != null) {
            for (JsonNode item : items) {
                answers.add(parseAnswer(item, questionId));
            }
        }

        return answers;
    }

    /**
     * Parse Question from JSON
     */
    private Question parseQuestion(JsonNode node) {
        Question question = new Question();
        question.setQuestionId(node.get("question_id").asLong());
        question.setTitle(node.has("title") ? node.get("title").asText() : "");
        question.setBody(node.has("body") ? node.get("body").asText() : "");
        question.setCreationDate(node.get("creation_date").asLong());
        question.setScore(node.get("score").asInt());
        question.setViewCount(node.has("view_count") ? node.get("view_count").asInt() : 0);
        question.setAnswerCount(node.has("answer_count") ? node.get("answer_count").asInt() : 0);
        question.setAnswered(node.has("is_answered") && node.get("is_answered").asBoolean());

        if (node.has("accepted_answer_id")) {
            question.setAcceptedAnswerId(node.get("accepted_answer_id").asLong());
        }

        // Parse tags
        if (node.has("tags")) {
            List<String> tags = new ArrayList<>();
            for (JsonNode tag : node.get("tags")) {
                tags.add(tag.asText());
            }
            question.setTags(tags);
        }

        // Parse owner
        if (node.has("owner")) {
            JsonNode owner = node.get("owner");
            if (owner.has("user_id")) {
                question.setOwnerId(owner.get("user_id").asLong());
            }
            if (owner.has("display_name")) {
                question.setOwnerDisplayName(owner.get("display_name").asText());
            }
            if (owner.has("reputation")) {
                question.setOwnerReputation(owner.get("reputation").asInt());
            }
        }

        return question;
    }

    /**
     * Parse Answer from JSON
     */
    private Answer parseAnswer(JsonNode node, long questionId) {
        Answer answer = new Answer();
        answer.setAnswerId(node.get("answer_id").asLong());
        answer.setQuestionId(questionId);
        answer.setBody(node.has("body") ? node.get("body").asText() : "");
        answer.setCreationDate(node.get("creation_date").asLong());
        answer.setScore(node.get("score").asInt());
        answer.setAccepted(node.has("is_accepted") && node.get("is_accepted").asBoolean());

        // Parse owner
        if (node.has("owner")) {
            JsonNode owner = node.get("owner");
            if (owner.has("user_id")) {
                answer.setOwnerId(owner.get("user_id").asLong());
            }
            if (owner.has("display_name")) {
                answer.setOwnerDisplayName(owner.get("display_name").asText());
            }
            if (owner.has("reputation")) {
                answer.setOwnerReputation(owner.get("reputation").asInt());
            }
        }

        return answer;
    }

    /**
     * Make API request and handle compression
     */
    private JsonNode makeApiRequest(String urlString) throws Exception {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept-Encoding", "gzip");

        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            throw new IOException("HTTP error code: " + responseCode);
        }

        InputStream inputStream = conn.getInputStream();

        // Check if response is gzipped
        String encoding = conn.getContentEncoding();
        if ("gzip".equalsIgnoreCase(encoding)) {
            inputStream = new GZIPInputStream(inputStream);
        }

        return objectMapper.readTree(inputStream);
    }

    /**
     * Save collected data to JSON file
     */
    public void saveData(List<Question> questions, String filename) throws IOException {
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(new File(filename), questions);
        System.out.println("Data saved to " + filename);
    }

    /**
     * Load data from JSON file
     */
    public List<Question> loadData(String filename) throws IOException {
        File file = new File(filename);
        if (!file.exists()) {
            return new ArrayList<>();
        }

        Question[] questions = objectMapper.readValue(file, Question[].class);
        return List.of(questions);
    }
}
