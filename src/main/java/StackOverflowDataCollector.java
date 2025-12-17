import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.zip.GZIPInputStream;

/**
 * 独立的 Stack Overflow 数据收集器
 * 
 * 使用方法:
 * 1. 编译: javac StackOverflowDataCollector.java
 * 2. 运行: java StackOverflowDataCollector [API_KEY] [问题数量]
 * 
 * 示例:
 * java StackOverflowDataCollector your_api_key 1000
 * java StackOverflowDataCollector your_api_key 1500 output.json
 */
public class StackOverflowDataCollector {

    // ==================== 配置参数 ====================
    private static final String BASE_URL = "https://api.stackexchange.com/2.3";
    private static final String SITE = "stackoverflow";
    private static final String TAG = "java";
    private static final int PAGE_SIZE = 100; // API 最大允许 100
    private static final int REQUEST_DELAY_MS = 1000; // 请求间隔（毫秒）
    private static final int ANSWER_DELAY_MS = 200; // 获取答案的间隔

    // 自定义 Filter - 包含 body 字段
    // 你可以在 https://api.stackexchange.com/docs/create-filter 创建自定义 filter
    private static final String QUESTION_FILTER = "!nNPvSNdWme"; // 包含 question.body
    private static final String ANSWER_FILTER = "!nNPvSNdWme"; // 包含 answer.body

    private final String apiKey;
    private int requestCount = 0;
    private int quotaRemaining = 10000;

    public StackOverflowDataCollector(String apiKey) {
        this.apiKey = apiKey;
    }

    public static void main(String[] args) {
        // 解析命令行参数
        String apiKey = "rl_kYzCCEJDh9UVbgq4SHLHEor33";
        int targetCount = 4000;
        String outputFile = "stackoverflow_data.json";

        if (apiKey == null || apiKey.isEmpty()) {
            System.out.println("========================================");
            System.out.println("Stack Overflow Data Collector");
            System.out.println("========================================");
            System.out.println();
            System.out.println("Usage: java StackOverflowDataCollector <API_KEY> [count] [output_file]");
            System.out.println();
            System.out.println("Parameters:");
            System.out.println("  API_KEY     - Your Stack Exchange API key (required)");
            System.out.println("  count       - Number of questions to collect (default: 1000)");
            System.out.println("  output_file - Output JSON file path (default: stackoverflow_data.json)");
            System.out.println();
            System.out.println("Example:");
            System.out.println("  java StackOverflowDataCollector abc123key 1500 data.json");
            System.out.println();
            System.out.println("Get your API key at: https://stackapps.com/apps/oauth/register");
            return;
        }

        System.out.println("========================================");
        System.out.println("Stack Overflow Data Collector");
        System.out.println("========================================");
        System.out.println("Target questions: " + targetCount);
        System.out.println("Output file: " + outputFile);
        System.out.println("========================================");
        System.out.println();

        try {
            StackOverflowDataCollector collector = new StackOverflowDataCollector(apiKey);
            List<Map<String, Object>> questions = collector.collectQuestions(targetCount);
            collector.saveToJson(questions, outputFile);

            System.out.println();
            System.out.println("========================================");
            System.out.println("Collection completed!");
            System.out.println("Total questions: " + questions.size());
            System.out.println("Output file: " + outputFile);
            System.out.println("========================================");

        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * 收集指定数量的问题
     */
    public List<Map<String, Object>> collectQuestions(int targetCount) throws Exception {
        List<Map<String, Object>> allQuestions = new ArrayList<>();
        int page = 1;

        System.out.println("Starting data collection...");
        System.out.println();

        while (allQuestions.size() < targetCount) {
            System.out.printf("[Page %d] Fetching questions... (collected: %d/%d)%n",
                    page, allQuestions.size(), targetCount);

            // 获取问题列表
            String url = buildQuestionsUrl(page);
            Map<String, Object> response = makeApiRequest(url);

            if (response == null) {
                System.out.println("Failed to get response, stopping...");
                break;
            }

            // 检查配额
            if (response.containsKey("quota_remaining")) {
                quotaRemaining = ((Number) response.get("quota_remaining")).intValue();
                System.out.println("  Quota remaining: " + quotaRemaining);
            }

            if (quotaRemaining < 100) {
                System.out.println("WARNING: Low quota remaining! Stopping to preserve quota.");
                break;
            }

            // 处理问题
            List<Map<String, Object>> items = (List<Map<String, Object>>) response.get("items");
            if (items == null || items.isEmpty()) {
                System.out.println("No more questions available.");
                break;
            }

            for (Map<String, Object> question : items) {
                if (allQuestions.size() >= targetCount) {
                    break;
                }

                // 获取问题的答案
                long questionId = ((Number) question.get("question_id")).longValue();
                List<Map<String, Object>> answers = fetchAnswers(questionId);
                question.put("answers_data", answers);

                allQuestions.add(question);

                // 显示进度
                if (allQuestions.size() % 50 == 0) {
                    System.out.printf("  Progress: %d/%d questions collected%n",
                            allQuestions.size(), targetCount);
                }

                // 短暂延迟避免触发速率限制
                Thread.sleep(ANSWER_DELAY_MS);
            }

            // 检查是否还有更多页
            Boolean hasMore = (Boolean) response.get("has_more");
            if (hasMore == null || !hasMore) {
                System.out.println("No more pages available.");
                break;
            }

            page++;

            // 页面间延迟
            Thread.sleep(REQUEST_DELAY_MS);
        }

        return allQuestions;
    }

    /**
     * 获取问题的答案
     */
    private List<Map<String, Object>> fetchAnswers(long questionId) throws Exception {
        String url = String.format(
                "%s/questions/%d/answers?order=desc&sort=votes&site=%s&filter=%s&key=%s",
                BASE_URL, questionId, SITE, ANSWER_FILTER, apiKey);

        Map<String, Object> response = makeApiRequest(url);
        if (response == null) {
            return new ArrayList<>();
        }

        List<Map<String, Object>> items = (List<Map<String, Object>>) response.get("items");
        return items != null ? items : new ArrayList<>();
    }

    /**
     * 构建问题列表 URL
     */
    private String buildQuestionsUrl(int page) {
        // 获取最近3年的问题
        long threeYearsAgo = System.currentTimeMillis() / 1000 - (3L * 365 * 24 * 60 * 60);

        return String.format(
                "%s/questions?page=%d&pagesize=%d&order=desc&sort=votes&tagged=%s&site=%s&filter=%s&fromdate=%d&key=%s",
                BASE_URL, page, PAGE_SIZE, TAG, SITE, QUESTION_FILTER, threeYearsAgo, apiKey);
    }

    /**
     * 发送 API 请求
     */
    private Map<String, Object> makeApiRequest(String urlString) throws Exception {
        requestCount++;

        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept-Encoding", "gzip");
        conn.setConnectTimeout(30000);
        conn.setReadTimeout(30000);

        int responseCode = conn.getResponseCode();

        if (responseCode == 429) {
            System.out.println("Rate limited! Waiting 60 seconds...");
            Thread.sleep(60000);
            return makeApiRequest(urlString); // 重试
        }

        if (responseCode != 200) {
            System.err.println("HTTP Error: " + responseCode);
            InputStream errorStream = conn.getErrorStream();
            if (errorStream != null) {
                String error = readStream(errorStream, conn.getContentEncoding());
                System.err.println("Error response: " + error);
            }
            return null;
        }

        String responseBody = readStream(conn.getInputStream(), conn.getContentEncoding());
        return parseJson(responseBody);
    }

    /**
     * 读取输入流
     */
    private String readStream(InputStream inputStream, String encoding) throws Exception {
        if ("gzip".equalsIgnoreCase(encoding)) {
            inputStream = new GZIPInputStream(inputStream);
        }

        BufferedReader reader = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        reader.close();
        return sb.toString();
    }

    /**
     * 简单的 JSON 解析器 (不依赖外部库)
     */
    private Map<String, Object> parseJson(String json) {
        return new SimpleJsonParser().parseObject(json);
    }

    /**
     * 保存数据到 JSON 文件
     */
    public void saveToJson(List<Map<String, Object>> questions, String filename) throws Exception {
        System.out.println("Saving data to " + filename + "...");

        // 转换为项目需要的格式
        List<Map<String, Object>> formattedQuestions = new ArrayList<>();

        for (Map<String, Object> q : questions) {
            Map<String, Object> formatted = new LinkedHashMap<>();

            // 基本信息
            formatted.put("questionId", q.get("question_id"));
            formatted.put("title", q.get("title"));
            formatted.put("body", q.get("body"));
            formatted.put("creationDate", q.get("creation_date"));
            formatted.put("score", q.get("score"));
            formatted.put("viewCount", q.get("view_count"));
            formatted.put("answerCount", q.get("answer_count"));
            formatted.put("isAnswered", q.get("is_answered"));
            formatted.put("acceptedAnswerId", q.get("accepted_answer_id"));

            // 标签
            formatted.put("tags", q.get("tags"));

            // 所有者信息
            Map<String, Object> owner = (Map<String, Object>) q.get("owner");
            if (owner != null) {
                formatted.put("ownerId", owner.get("user_id"));
                formatted.put("ownerDisplayName", owner.get("display_name"));
                formatted.put("ownerReputation", owner.get("reputation"));
            }

            // 答案
            List<Map<String, Object>> answersData = (List<Map<String, Object>>) q.get("answers_data");
            List<Map<String, Object>> formattedAnswers = new ArrayList<>();

            if (answersData != null) {
                for (Map<String, Object> a : answersData) {
                    Map<String, Object> fa = new LinkedHashMap<>();
                    fa.put("answerId", a.get("answer_id"));
                    fa.put("questionId", q.get("question_id"));
                    fa.put("body", a.get("body"));
                    fa.put("creationDate", a.get("creation_date"));
                    fa.put("score", a.get("score"));
                    fa.put("isAccepted", a.get("is_accepted"));

                    Map<String, Object> answerOwner = (Map<String, Object>) a.get("owner");
                    if (answerOwner != null) {
                        fa.put("ownerId", answerOwner.get("user_id"));
                        fa.put("ownerDisplayName", answerOwner.get("display_name"));
                        fa.put("ownerReputation", answerOwner.get("reputation"));
                    }

                    fa.put("comments", new ArrayList<>()); // 可选：获取评论
                    formattedAnswers.add(fa);
                }
            }

            formatted.put("answers", formattedAnswers);
            formatted.put("comments", new ArrayList<>()); // 可选：获取评论

            formattedQuestions.add(formatted);
        }

        // 写入文件
        String jsonOutput = toJsonString(formattedQuestions);
        Files.writeString(Path.of(filename), jsonOutput, StandardCharsets.UTF_8);

        System.out.println("Data saved successfully!");
    }

    /**
     * 将对象转换为 JSON 字符串
     */
    private String toJsonString(Object obj) {
        StringBuilder sb = new StringBuilder();
        toJsonString(obj, sb, 0);
        return sb.toString();
    }

    private void toJsonString(Object obj, StringBuilder sb, int indent) {
        if (obj == null) {
            sb.append("null");
        } else if (obj instanceof String) {
            sb.append("\"").append(escapeJson((String) obj)).append("\"");
        } else if (obj instanceof Number || obj instanceof Boolean) {
            sb.append(obj);
        } else if (obj instanceof List) {
            List<?> list = (List<?>) obj;
            sb.append("[\n");
            for (int i = 0; i < list.size(); i++) {
                sb.append("  ".repeat(indent + 1));
                toJsonString(list.get(i), sb, indent + 1);
                if (i < list.size() - 1)
                    sb.append(",");
                sb.append("\n");
            }
            sb.append("  ".repeat(indent)).append("]");
        } else if (obj instanceof Map) {
            Map<?, ?> map = (Map<?, ?>) obj;
            sb.append("{\n");
            int i = 0;
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                sb.append("  ".repeat(indent + 1));
                sb.append("\"").append(entry.getKey()).append("\": ");
                toJsonString(entry.getValue(), sb, indent + 1);
                if (i < map.size() - 1)
                    sb.append(",");
                sb.append("\n");
                i++;
            }
            sb.append("  ".repeat(indent)).append("}");
        }
    }

    private String escapeJson(String s) {
        if (s == null)
            return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    /**
     * 简单的 JSON 解析器
     */
    static class SimpleJsonParser {
        private String json;
        private int pos;

        public Map<String, Object> parseObject(String json) {
            this.json = json.trim();
            this.pos = 0;
            return readObject();
        }

        private Map<String, Object> readObject() {
            Map<String, Object> map = new LinkedHashMap<>();
            skipWhitespace();
            if (pos >= json.length() || json.charAt(pos) != '{')
                return map;
            pos++; // skip {

            while (pos < json.length()) {
                skipWhitespace();
                if (json.charAt(pos) == '}') {
                    pos++;
                    break;
                }

                String key = readString();
                skipWhitespace();
                if (json.charAt(pos) == ':')
                    pos++;
                skipWhitespace();
                Object value = readValue();
                map.put(key, value);

                skipWhitespace();
                if (pos < json.length() && json.charAt(pos) == ',')
                    pos++;
            }
            return map;
        }

        private List<Object> readArray() {
            List<Object> list = new ArrayList<>();
            pos++; // skip [

            while (pos < json.length()) {
                skipWhitespace();
                if (json.charAt(pos) == ']') {
                    pos++;
                    break;
                }

                list.add(readValue());
                skipWhitespace();
                if (pos < json.length() && json.charAt(pos) == ',')
                    pos++;
            }
            return list;
        }

        private Object readValue() {
            skipWhitespace();
            if (pos >= json.length())
                return null;

            char c = json.charAt(pos);
            if (c == '"')
                return readString();
            if (c == '{')
                return readObject();
            if (c == '[')
                return readArray();
            if (c == 't' || c == 'f')
                return readBoolean();
            if (c == 'n')
                return readNull();
            if (c == '-' || Character.isDigit(c))
                return readNumber();
            return null;
        }

        private String readString() {
            if (json.charAt(pos) != '"')
                return "";
            pos++; // skip opening "

            StringBuilder sb = new StringBuilder();
            while (pos < json.length()) {
                char c = json.charAt(pos);
                if (c == '"') {
                    pos++;
                    break;
                }
                if (c == '\\' && pos + 1 < json.length()) {
                    pos++;
                    char next = json.charAt(pos);
                    switch (next) {
                        case 'n':
                            sb.append('\n');
                            break;
                        case 'r':
                            sb.append('\r');
                            break;
                        case 't':
                            sb.append('\t');
                            break;
                        case '"':
                            sb.append('"');
                            break;
                        case '\\':
                            sb.append('\\');
                            break;
                        case 'u':
                            if (pos + 4 < json.length()) {
                                String hex = json.substring(pos + 1, pos + 5);
                                sb.append((char) Integer.parseInt(hex, 16));
                                pos += 4;
                            }
                            break;
                        default:
                            sb.append(next);
                    }
                } else {
                    sb.append(c);
                }
                pos++;
            }
            return sb.toString();
        }

        private Number readNumber() {
            int start = pos;
            if (json.charAt(pos) == '-')
                pos++;
            while (pos < json.length() && Character.isDigit(json.charAt(pos)))
                pos++;
            if (pos < json.length() && json.charAt(pos) == '.') {
                pos++;
                while (pos < json.length() && Character.isDigit(json.charAt(pos)))
                    pos++;
                return Double.parseDouble(json.substring(start, pos));
            }
            if (pos < json.length() && (json.charAt(pos) == 'e' || json.charAt(pos) == 'E')) {
                pos++;
                if (pos < json.length() && (json.charAt(pos) == '+' || json.charAt(pos) == '-'))
                    pos++;
                while (pos < json.length() && Character.isDigit(json.charAt(pos)))
                    pos++;
                return Double.parseDouble(json.substring(start, pos));
            }
            return Long.parseLong(json.substring(start, pos));
        }

        private Boolean readBoolean() {
            if (json.substring(pos).startsWith("true")) {
                pos += 4;
                return true;
            }
            if (json.substring(pos).startsWith("false")) {
                pos += 5;
                return false;
            }
            return false;
        }

        private Object readNull() {
            if (json.substring(pos).startsWith("null")) {
                pos += 4;
            }
            return null;
        }

        private void skipWhitespace() {
            while (pos < json.length() && Character.isWhitespace(json.charAt(pos))) {
                pos++;
            }
        }
    }
}
