package cs209a.finalproject_demo.service;

import cs209a.finalproject_demo.model.Question;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class DataAnalysisService {

        // Java topics to track
        private static final List<String> JAVA_TOPICS = Arrays.asList(
                        "generics", "collections", "io", "lambda", "stream",
                        "multithreading", "concurrency", "thread", "socket",
                        "reflection", "spring", "spring-boot", "jpa", "hibernate",
                        "exception", "testing", "junit", "annotation","android");

    /**
     * Part I.1: Topic Trends Analysis
     * <p>
     * Analyzes the trend of different Java-related topics over a specified
     * time range and aggregates question counts by time period.
     * @return a {@code Map<String, Object>} containing topic trend analysis results:
     *         <ul>
     *           <li>{@code "topicTrends"} – a {@code Map<String, Map<String, Integer>>}
     *               where each key is a Java topic, and the value maps a time period
     *               (year or year-month, represented as {@link String}) to the number
     *               of related questions</li>
     *           <li>{@code "aggregateByYear"} – a {@link Boolean} indicating whether
     *               the data is aggregated by year instead of by month</li>
     *           <li>{@code "yearsPast"} – an {@link Integer} representing the number
     *               of years included in the analysis</li>
     *           <li>{@code "totalQuestions"} – an {@link Integer} indicating the total
     *               number of questions within the selected time range</li>
     *         </ul>
     */
    public Map<String, Object> analyzeTopicTrends(List<Question> questions, int yearsPast) {
                Map<String, Object> result = new HashMap<>();

                // Calculate date range - support full range when yearsPast >= 15
                long currentTime = System.currentTimeMillis() / 1000;
                long startTime;
                if (yearsPast >= 15) {
                        // For 15+ years, use the earliest data (2010-01-01 = 1262304000)
                        startTime = 1262304000L;
                } else {
                        startTime = currentTime - (yearsPast * 365L * 24 * 60 * 60);
                }

                // Filter questions within time range
                List<Question> filteredQuestions = questions.stream()
                                .filter(q -> q.getCreationDate() >= startTime && q.getCreationDate() <= currentTime)
                                .collect(Collectors.toList());

                // For long time ranges (>5 years), aggregate by year instead of year-month
                boolean aggregateByYear = yearsPast > 5;

                // Group questions by topic and time period
                Map<String, Map<String, Integer>> topicTrends = new HashMap<>();

                for (String topic : JAVA_TOPICS) {
                        Map<String, Integer> periodCount = new TreeMap<>();

                        for (Question question : filteredQuestions) {
                                // Check if question is related to this topic
                                if (isQuestionRelatedToTopic(question, topic)) {
                                        String period = aggregateByYear
                                                        ? getYear(question.getCreationDate())
                                                        : getYearMonth(question.getCreationDate());
                                        periodCount.put(period, periodCount.getOrDefault(period, 0) + 1);
                                }
                        }

                        if (!periodCount.isEmpty()) {
                                topicTrends.put(topic, periodCount);
                        }
                }

                result.put("topicTrends", topicTrends);
                result.put("aggregateByYear", aggregateByYear);
                result.put("yearsPast", yearsPast);
                result.put("totalQuestions", filteredQuestions.size());

                return result;
        }

        /**
         * Part I.2: Topic Co-occurrence Analysis
         * <p>
         * Analyzes the co-occurrence of Java-related topics within questions and
         * identifies the top N most frequently co-occurring topic pairs.
         * @return a {@code Map<String, Object>} containing topic co-occurrence analysis results:
         *         <ul>
         *           <li>{@code "topPairs"} – a {@code List<Map<String, Object>>} where each map contains:
         *               <ul>
         *                 <li>{@code "topics"} – a {@link String} representing the co-occurring topic pair</li>
         *                 <li>{@code "count"} – an {@link Integer} indicating the number of co-occurrences</li>
         *               </ul></li>
         *           <li>{@code "topN"} – an {@link Integer} representing the number of top pairs returned</li>
         *         </ul>
         *
         */
        public Map<String, Object> analyzeTopicCooccurrence(List<Question> questions, int topN) {
                Map<String, Object> result = new HashMap<>();

                // Count co-occurrences
                Map<String, Integer> cooccurrenceCount = new HashMap<>();//string is "topic1 & topic2", integer is count

                for (Question question : questions) {
                        List<String> relatedTopics = new ArrayList<>();

                        // Find all topics related to this question
                        for (String topic : JAVA_TOPICS) {
                                if (isQuestionRelatedToTopic(question, topic)) {
                                        relatedTopics.add(topic);
                                }
                        }

                        // Count pairs
                        for (int i = 0; i < relatedTopics.size(); i++) {
                                for (int j = i + 1; j < relatedTopics.size(); j++) {
                                        String topic1 = relatedTopics.get(i);
                                        String topic2 = relatedTopics.get(j);

                                        // Create a sorted pair key
                                        String pairKey = topic1.compareTo(topic2) < 0
                                                        ? topic1 + " & " + topic2
                                                        : topic2 + " & " + topic1;

                                        cooccurrenceCount.put(pairKey, cooccurrenceCount.getOrDefault(pairKey, 0) + 1);
                                }
                        }
                }

                // Get top N pairs
                List<Map.Entry<String, Integer>> topPairs = cooccurrenceCount.entrySet().stream()
                                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                                .limit(topN)
                                .collect(Collectors.toList());

                // Format result
                List<Map<String, Object>> formattedPairs = new ArrayList<>();
                for (Map.Entry<String, Integer> entry : topPairs) {
                        Map<String, Object> pair = new HashMap<>();
                        pair.put("topics", entry.getKey());
                        pair.put("count", entry.getValue());
                        formattedPairs.add(pair);
                }

                result.put("topPairs", formattedPairs);
                result.put("topN", topN);

                return result;
        }

        /**
         * Part I.3: Multithreading Pitfalls Analysis
         * <p>
         * Identifies and counts common multithreading pitfalls in Java-related questions,
         * and provides examples for the top N pitfalls.
         * @return a {@code Map<String, Object>} containing multithreading pitfalls analysis results:
         *         <ul>
         *           <li>{@code "topPitfalls"} – a {@code List<Map<String, Object>>} where each map contains:
         *               <ul>
         *                 <li>{@code "name"} – a {@link String} representing the pitfall name</li>
         *                 <li>{@code "count"} – an {@link Integer} indicating the number of occurrences</li>
         *                 <li>{@code "percentage"} – a {@link String} representing the percentage of
         *                     questions affected by this pitfall</li>
         *                 <li>{@code "examples"} – a {@code List<String>} of example question titles</li>
         *               </ul></li>
         *           <li>{@code "totalMultithreadingQuestions"} – an {@link Integer} indicating the total
         *               number of multithreading-related questions analyzed</li>
         *           <li>{@code "topN"} – an {@link Integer} representing the number of top pitfalls returned</li>
         *         </ul>
         */
        public Map<String, Object> analyzeMultithreadingPitfalls(List<Question> questions, int topN) {
                Map<String, Object> result = new HashMap<>();

                // Filter multithreading related questions
                List<Question> mtQuestions = questions.stream()
                                .filter(q -> isQuestionRelatedToTopic(q, "multithreading")
                                                || isQuestionRelatedToTopic(q, "concurrency")
                                                || isQuestionRelatedToTopic(q, "thread"))
                                .collect(Collectors.toList());

                // Define common pitfalls patterns
                Map<String, List<String>> pitfallPatterns = new HashMap<>();
                pitfallPatterns.put("Race Condition", Arrays.asList(
                                "race condition", "race-condition", "concurrent modification",
                                "shared variable", "thread safety", "thread-safety"));
                pitfallPatterns.put("Deadlock", Arrays.asList(
                                "deadlock", "dead lock", "circular wait", "thread blocked"));
                pitfallPatterns.put("Thread Synchronization", Arrays.asList(
                                "synchroniz", "volatile", "atomic", "lock", "mutex"));
                pitfallPatterns.put("Thread Pool Issues", Arrays.asList(
                                "thread pool", "executor", "threadpool", "executorservice"));
                pitfallPatterns.put("Wait/Notify Problems", Arrays.asList(
                                "wait\\(\\)", "notify", "notifyall", "IllegalMonitorStateException"));
                pitfallPatterns.put("ConcurrentModificationException", Arrays.asList(
                                "ConcurrentModificationException", "concurrent modification exception"));
                pitfallPatterns.put("Memory Visibility", Arrays.asList(
                                "memory visibility", "happens-before", "volatile keyword", "cache coherence"));
                pitfallPatterns.put("Livelock", Arrays.asList(
                                "livelock", "live lock", "thread starvation", "starvation"));

                // Count pitfalls
                Map<String, Integer> pitfallCount = new HashMap<>();
                Map<String, List<String>> pitfallExamples = new HashMap<>();

                for (Map.Entry<String, List<String>> entry : pitfallPatterns.entrySet()) {
                        String pitfallName = entry.getKey();
                        List<String> patterns = entry.getValue();
                        int count = 0;
                        List<String> examples = new ArrayList<>();

                        for (Question question : mtQuestions) {
                                String title = question.getTitle() != null ? question.getTitle() : "";
                                String body = question.getBody() != null ? question.getBody() : "";
                                String searchText = (title + " " + body).toLowerCase();

                                for (String pattern : patterns) {
                                        if (searchText.matches(".*\\b" + pattern.toLowerCase() + ".*")) {
                                                count++;
                                                if (examples.size() < 3) {
                                                        examples.add(question.getTitle());
                                                }
                                                break;
                                        }
                                }
                        }

                        if (count > 0) {
                                pitfallCount.put(pitfallName, count);
                                pitfallExamples.put(pitfallName, examples);
                        }
                }

                // Get top N pitfalls
                List<Map.Entry<String, Integer>> topPitfalls = pitfallCount.entrySet().stream()
                                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                                .limit(topN)
                                .collect(Collectors.toList());

                // Format result
                List<Map<String, Object>> formattedPitfalls = new ArrayList<>();
                for (Map.Entry<String, Integer> entry : topPitfalls) {
                        Map<String, Object> pitfall = new HashMap<>();
                        pitfall.put("name", entry.getKey());
                        pitfall.put("count", entry.getValue());
                        pitfall.put("percentage", String.format("%.2f%%",
                                        (entry.getValue() * 100.0) / mtQuestions.size()));
                        pitfall.put("examples", pitfallExamples.get(entry.getKey()));
                        formattedPitfalls.add(pitfall);
                }

                result.put("topPitfalls", formattedPitfalls);
                result.put("totalMultithreadingQuestions", mtQuestions.size());
                result.put("topN", topN);

                return result;
        }

        /**
         * Part I.4: Solvability Analysis
         * <p>
         * Analyzes factors that influence the solvability of Java-related questions
         * by comparing questions that were successfully solved versus those that
         * remained unsolved.
         * @return a {@code Map<String, Object>} containing solvability analysis results:
         *         <ul>
         *           <li>{@code "solvableCount"} – an {@link Integer} indicating the number of
         *               solvable questions</li>
         *           <li>{@code "hardToSolveCount"} – an {@link Integer} indicating the number of
         *               hard-to-solve questions</li>
         *           <li>{@code "factors"} – a {@code Map<String, Map<String, Object>>} where each key is a
         *               factor name, and the value contains:
         *               <ul>
         *                 <li>{@code "solvable"} – a {@link String} representing the metric for solvable questions</li>
         *                 <li>{@code "hardToSolve"} – a {@link String} representing the metric for hard-to-solve questions</li>
         *                 <li>{@code "insight"} – a {@link String} providing insights about the factor</li>
         *               </ul></li>
         *         </ul>
         */
        public Map<String, Object> analyzeSolvability(List<Question> questions) {
                Map<String, Object> result = new HashMap<>();

                // Classify questions - use more relaxed criteria
                // Solvable: has accepted answer OR is marked as answered OR has answers with
                // positive score
                List<Question> solvableQuestions = questions.stream()
                                .filter(q -> q.getAcceptedAnswerId() != null ||
                                                q.isAnswered() ||
                                                (q.getAnswerCount() > 0 && q.getAnswers() != null &&
                                                                !q.getAnswers().isEmpty() &&
                                                                q.getAnswers().stream()
                                                                                .anyMatch(a -> a.getScore() >= 1)))
                                .collect(Collectors.toList());

                // Hard-to-solve: no accepted answer, not marked as answered, and either no
                // answers or all answers have score < 1
                List<Question> hardQuestions = questions.stream()
                                .filter(q -> q.getAcceptedAnswerId() == null &&
                                                !q.isAnswered() &&
                                                (q.getAnswerCount() == 0 ||
                                                                q.getAnswers() == null ||
                                                                q.getAnswers().isEmpty() ||
                                                                q.getAnswers().stream()
                                                                                .allMatch(a -> a.getScore() < 1)))
                                .collect(Collectors.toList());

                // Handle case where solvableQuestions is empty to avoid NaN
                int solvableSize = Math.max(1, solvableQuestions.size());
                int hardSize = Math.max(1, hardQuestions.size());

                // Factor 1: Code Snippet Presence (check in answers since question body may not
                // be available)
                double solvableWithCode = solvableQuestions.stream()
                                .filter(this::hasCodeSnippetInAnswers)
                                .count() * 100.0 / solvableSize;

                double hardWithCode = hardQuestions.stream()
                                .filter(this::hasCodeSnippetInAnswers)
                                .count() * 100.0 / hardSize;

                // Factor 2: Title Length (as proxy for clarity, since body may not be
                // available)
                double avgSolvableLength = solvableQuestions.stream()
                                .filter(q -> q.getTitle() != null)
                                .mapToInt(q -> q.getTitle().length())
                                .average()
                                .orElse(0);

                double avgHardLength = hardQuestions.stream()
                                .filter(q -> q.getTitle() != null)
                                .mapToInt(q -> q.getTitle().length())
                                .average()
                                .orElse(0);

                // Factor 3: Owner Reputation
                double avgSolvableReputation = solvableQuestions.stream()
                                .mapToInt(Question::getOwnerReputation)
                                .average()
                                .orElse(0);

                double avgHardReputation = hardQuestions.stream()
                                .mapToInt(Question::getOwnerReputation)
                                .average()
                                .orElse(0);

                // Factor 4: Number of Tags
                double avgSolvableTags = solvableQuestions.stream()
                                .mapToInt(q -> q.getTags().size())
                                .average()
                                .orElse(0);

                double avgHardTags = hardQuestions.stream()
                                .mapToInt(q -> q.getTags().size())
                                .average()
                                .orElse(0);

                // Factor 5: View Count (popularity)
                double avgSolvableViews = solvableQuestions.stream()
                                .mapToInt(Question::getViewCount)
                                .average()
                                .orElse(0);

                double avgHardViews = hardQuestions.stream()
                                .mapToInt(Question::getViewCount)
                                .average()
                                .orElse(0);

                // Prepare results
                Map<String, Map<String, Object>> factors = new LinkedHashMap<>();

                factors.put("Answer Has Code Snippet", Map.of(
                                "solvable", String.format("%.2f%%", solvableWithCode),
                                "hardToSolve", String.format("%.2f%%", hardWithCode),
                                "insight", "Questions with code snippets in answers indicate better explanations"));

                factors.put("Title Length", Map.of(
                                "solvable", String.format("%.0f chars", avgSolvableLength),
                                "hardToSolve", String.format("%.0f chars", avgHardLength),
                                "insight",
                                "Clear and descriptive titles help questions get better answers"));

                factors.put("Owner Reputation", Map.of(
                                "solvable", String.format("%.0f", avgSolvableReputation),
                                "hardToSolve", String.format("%.0f", avgHardReputation),
                                "insight", "User reputation affects question visibility and response quality"));

                factors.put("Number of Tags", Map.of(
                                "solvable", String.format("%.2f", avgSolvableTags),
                                "hardToSolve", String.format("%.2f", avgHardTags),
                                "insight", "Appropriate tagging helps questions reach the right audience"));

                factors.put("View Count", Map.of(
                                "solvable", String.format("%.0f", avgSolvableViews),
                                "hardToSolve", String.format("%.0f", avgHardViews),
                                "insight", "Higher visibility correlates with better chances of getting answers"));

                result.put("solvableCount", solvableQuestions.size());
                result.put("hardToSolveCount", hardQuestions.size());
                result.put("factors", factors);

                return result;
        }

        // Helper methods

        private boolean isQuestionRelatedToTopic(Question question, String topic) {
                // Check in tags
                if (question.getTags() != null) {
                        for (String tag : question.getTags()) {
                                if (tag != null && tag.toLowerCase().contains(topic.toLowerCase())) {
                                        return true;
                                }
                        }
                }

                // Check in title and body
                String title = question.getTitle() != null ? question.getTitle() : "";
                String body = question.getBody() != null ? question.getBody() : "";
                String searchText = (title + " " + body).toLowerCase();
                return searchText.contains(topic.toLowerCase());
        }

        private String getYearMonth(long epochSeconds) {
                LocalDate date = Instant.ofEpochSecond(epochSeconds)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate();
                return String.format("%d-%02d", date.getYear(), date.getMonthValue());
        }

        private String getYear(long epochSeconds) {
                LocalDate date = Instant.ofEpochSecond(epochSeconds)
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate();
                return String.valueOf(date.getYear());
        }

        private boolean hasCodeSnippet(Question question) {
                String body = question.getBody();
                if (body == null || body.isEmpty()) {
                        return false;
                }
                // Check for common code markers
                return body.contains("<code>") || body.contains("```") ||
                                body.contains("<pre>") || body.matches("(?s).*\\{.*\\}.*");
        }

        /**
         * Check if any answer to the question contains code snippets
         */
        private boolean hasCodeSnippetInAnswers(Question question) {
                if (question.getAnswers() == null || question.getAnswers().isEmpty()) {
                        return false;
                }
                return question.getAnswers().stream()
                                .anyMatch(answer -> {
                                        String body = answer.getBody();
                                        if (body == null || body.isEmpty()) {
                                                return false;
                                        }
                                        return body.contains("<code>") || body.contains("```") ||
                                                        body.contains("<pre>");
                                });
        }
}
