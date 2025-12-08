package cs209a.finalproject_demo.service;

import cs209a.finalproject_demo.model.Answer;
import cs209a.finalproject_demo.model.Question;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

/**
 * Generate sample data for testing purposes
 * Use this when you don't have real Stack Overflow data
 */
@Service
public class SampleDataGenerator {

    private static final List<String> JAVA_TOPICS = Arrays.asList(
            "generics", "collections", "io", "lambda", "stream",
            "multithreading", "concurrency", "thread", "socket",
            "reflection", "spring", "spring-boot", "jpa", "hibernate");

    private static final String[] MULTITHREADING_TITLES = {
            "How to avoid race condition in Java?",
            "Deadlock problem with synchronized methods",
            "ConcurrentModificationException when iterating ArrayList",
            "Thread synchronization using volatile keyword",
            "ExecutorService thread pool best practices",
            "Wait and notify causing IllegalMonitorStateException",
            "Memory visibility issues with shared variables",
            "How to prevent thread starvation?",
            "Atomic operations vs synchronized blocks",
            "ReentrantLock vs synchronized performance"
    };

    private static final String[] MULTITHREADING_BODIES = {
            "I have multiple threads accessing a shared variable and getting unexpected results. How can I fix this race condition?",
            "Two threads are blocked and waiting for each other. I think it's a deadlock but not sure how to resolve it.",
            "Getting ConcurrentModificationException when one thread modifies a list while another iterates it.",
            "Should I use volatile or synchronized for thread-safe access to variables?",
            "What's the best way to configure thread pools with ExecutorService?",
            "My wait() and notify() calls are throwing IllegalMonitorStateException. What am I doing wrong?",
            "Changes made by one thread are not visible to other threads. Is this a memory visibility problem?",
            "Some threads in my pool never get executed. How to prevent starvation?",
            "When should I use AtomicInteger vs synchronized methods?",
            "Which is more efficient: ReentrantLock or synchronized blocks?"
    };

    public List<Question> generateSampleData(int count) {
        List<Question> questions = new ArrayList<>();
        Random random = new Random(42); // Fixed seed for reproducibility

        Instant now = Instant.now();

        for (int i = 0; i < count; i++) {
            Question question = new Question();
            question.setQuestionId(1000000L + i);

            // Generate creation date within past 3 years
            long daysAgo = random.nextInt(365 * 3);
            Instant creationDate = now.minus(daysAgo, ChronoUnit.DAYS);
            question.setCreationDate(creationDate.getEpochSecond());

            // Assign topics randomly
            List<String> questionTags = new ArrayList<>();
            questionTags.add("java");

            int numTopics = 1 + random.nextInt(4); // 1-4 topics
            for (int j = 0; j < numTopics; j++) {
                String topic = JAVA_TOPICS.get(random.nextInt(JAVA_TOPICS.size()));
                if (!questionTags.contains(topic)) {
                    questionTags.add(topic);
                }
            }
            question.setTags(questionTags);

            // Check if it's a multithreading question
            boolean isMultithreading = questionTags.contains("multithreading") ||
                    questionTags.contains("concurrency") ||
                    questionTags.contains("thread");

            if (isMultithreading && random.nextDouble() < 0.7) {
                // Use specific multithreading content
                int idx = random.nextInt(MULTITHREADING_TITLES.length);
                question.setTitle(MULTITHREADING_TITLES[idx]);
                question.setBody(MULTITHREADING_BODIES[idx] + " <code>synchronized void method() { ... }</code>");
            } else {
                // Generic question
                question.setTitle("Java " + questionTags.get(1) + " question " + i);
                question.setBody("I'm having trouble with " + questionTags.get(1) +
                        ". Here's my code: <code>public class Test { ...</code>");
            }

            // Set metrics
            question.setScore(random.nextInt(50) - 5); // -5 to 44
            question.setViewCount(random.nextInt(10000) + 100);
            question.setAnswerCount(random.nextInt(8));

            // Owner info
            question.setOwnerId(10000L + random.nextInt(5000));
            question.setOwnerDisplayName("User" + question.getOwnerId());
            question.setOwnerReputation(random.nextInt(50000) + 100);

            // Generate answers
            List<Answer> answers = new ArrayList<>();
            int answerCount = question.getAnswerCount();
            boolean hasAcceptedAnswer = answerCount > 0 && random.nextDouble() < 0.6;

            for (int j = 0; j < answerCount; j++) {
                Answer answer = new Answer();
                answer.setAnswerId(2000000L + i * 10 + j);
                answer.setQuestionId(question.getQuestionId());
                answer.setBody("Here's the solution: " + questionTags.get(0) + " works this way...");
                answer.setScore(random.nextInt(30) - 2);
                answer.setCreationDate(creationDate.plus(random.nextInt(7), ChronoUnit.DAYS).getEpochSecond());
                answer.setOwnerId(10000L + random.nextInt(5000));
                answer.setOwnerDisplayName("User" + answer.getOwnerId());
                answer.setOwnerReputation(random.nextInt(100000) + 500);

                if (hasAcceptedAnswer && j == 0) {
                    answer.setAccepted(true);
                    question.setAcceptedAnswerId(answer.getAnswerId());
                }

                answers.add(answer);
            }

            question.setAnswers(answers);
            question.setAnswered(hasAcceptedAnswer || (answerCount > 0 && random.nextDouble() < 0.3));

            questions.add(question);
        }

        return questions;
    }
}
