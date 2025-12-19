import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.zip.GZIPInputStream;

/**
 * Stack Overflow Data Collector (Yearly Quota Version)
 * 策略：每年只抓取 Top 500 个高分问题
 */
public class StackOverflowDataCollector {

    // ==================== 配置参数 ====================
    private static final String BASE_URL = "https://api.stackexchange.com/2.3";
    private static final String SITE = "stackoverflow";
    private static final String TAG = "java";

    // 抓取年份设置
    private static final int START_YEAR = 2010;
    private static final int END_YEAR = 2025; // 到哪一年结束
    private static final int QUESTIONS_PER_YEAR = 500; // 每年抓多少条

    private static final int PAGE_SIZE = 100;
    private static final int REQUEST_DELAY_MS = 200;
    private static final String COMMON_FILTER = "!nNPvSNdWme";

    private final String apiKey;
    private int quotaRemaining = 10000;

    public StackOverflowDataCollector(String apiKey) {
        this.apiKey = apiKey;
    }

    public static void main(String[] args) {
        String apiKey = "rl_kYzCCEJDh9UVbgq4SHLHEor33";
        String outputFile = "stackoverflow_java_yearly_balanced.json";

        System.out.println("========================================");
        System.out.println("Stack Overflow Collector (Balanced Yearly)");
        System.out.println("Target: " + QUESTIONS_PER_YEAR + " questions per year (" + START_YEAR + "-" + END_YEAR + ")");
        System.out.println("========================================");

        try {
            StackOverflowDataCollector collector = new StackOverflowDataCollector(apiKey);

            // 执行按年抓取策略
            List<Map<String, Object>> allData = collector.collectFixedAmountPerYear(outputFile);

            System.out.println("========================================");
            System.out.println("Final Write...");
            collector.saveToJson(allData, outputFile);
            System.out.println("DONE! Total Questions: " + allData.size());
            System.out.println("========================================");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 核心策略：按年份循环，每年抓够 500 条就停
     */
    public List<Map<String, Object>> collectFixedAmountPerYear(String outputFile) throws Exception {
        List<Map<String, Object>> allQuestions = new ArrayList<>();

        // 循环年份：2010, 2011, ... 2024
        for (int year = START_YEAR; year <= END_YEAR; year++) {
            System.out.println("\n>>> Starting Collection for YEAR: " + year);

            // 计算该年的开始和结束时间戳
            long fromDate = getEpochSecond(year, 1, 1);
            long toDate = getEpochSecond(year + 1, 1, 1);

            // 获取该年的数据
            List<Map<String, Object>> yearlyData = collectQuestionsForSpecificYear(year, fromDate, toDate);

            allQuestions.addAll(yearlyData);

            // 每年存一次档，防止程序崩溃白跑
            saveToJson(allQuestions, outputFile);
            System.out.println("   [Auto-Save] Data saved. Total so far: " + allQuestions.size());

            // 避免配额耗尽
            if (quotaRemaining < 50) {
                System.out.println("!!! Critical Quota Limit. Stopping.");
                break;
            }
        }
        return allQuestions;
    }

    /**
     * 获取特定年份的 500 条数据
     */
    private List<Map<String, Object>> collectQuestionsForSpecificYear(int year, long fromDate, long toDate) throws Exception {
        List<Map<String, Object>> questionsThisYear = new ArrayList<>();
        int page = 1;

        while (questionsThisYear.size() < QUESTIONS_PER_YEAR) {
            System.out.printf("   [Year %d] Fetching Page %d... (Progress: %d/%d)%n",
                    year, page, questionsThisYear.size(), QUESTIONS_PER_YEAR);

            // 构造 URL: 按票数排序(sort=votes)，保证抓取的是当年最好的问题
            String url = String.format(
                    "%s/questions?page=%d&pagesize=%d&order=desc&sort=votes&min=5&tagged=%s&site=%s&filter=%s&fromdate=%d&todate=%d&key=%s",
                    BASE_URL, page, PAGE_SIZE, TAG, SITE, COMMON_FILTER, fromDate, toDate, apiKey);

            Map<String, Object> response = makeApiRequest(url);
            if (response == null) break;

            if (response.containsKey("quota_remaining")) {
                quotaRemaining = ((Number) response.get("quota_remaining")).intValue();
            }

            List<Map<String, Object>> items = (List<Map<String, Object>>) response.get("items");
            if (items == null || items.isEmpty()) {
                System.out.println("   [Year " + year + "] No more items available.");
                break;
            }

            for (Map<String, Object> q : items) {
                // 如果这一年已经凑够了 500 条，就停止添加
                if (questionsThisYear.size() >= QUESTIONS_PER_YEAR) {
                    break;
                }

                long qId = ((Number) q.get("question_id")).longValue();

                // 获取所有回答
                List<Map<String, Object>> answers = fetchAllAnswers(qId);
                q.put("answers_data", answers);
                q.put("fetched_at", System.currentTimeMillis());
                q.put("year_group", year); // 方便你后续分析，打上年份标签

                questionsThisYear.add(q);
                Thread.sleep(50);
            }

            // 检查是否有更多页
            Boolean more = (Boolean) response.get("has_more");
            if (more == null || !more) break;

            page++;
            Thread.sleep(REQUEST_DELAY_MS);
        }

        System.out.println("   >>> Finished Year " + year + ". Collected: " + questionsThisYear.size());
        return questionsThisYear;
    }

    // 辅助方法：生成时间戳
    private long getEpochSecond(int year, int month, int day) {
        return LocalDate.of(year, month, day)
                .atStartOfDay(ZoneId.of("UTC"))
                .toEpochSecond();
    }

    // ================== 以下部分与之前保持一致 (API请求, Answer抓取, JSON保存) ==================

    private List<Map<String, Object>> fetchAllAnswers(long questionId) throws Exception {
        List<Map<String, Object>> allAnswers = new ArrayList<>();
        int page = 1;
        boolean hasMore = true;

        while (hasMore) {
            String url = String.format(
                    "%s/questions/%d/answers?page=%d&pagesize=100&order=asc&sort=creation&site=%s&filter=%s&key=%s",
                    BASE_URL, questionId, page, SITE, COMMON_FILTER, apiKey);

            Map<String, Object> response = makeApiRequest(url);
            if (response == null) break;

            List<Map<String, Object>> items = (List<Map<String, Object>>) response.get("items");
            if (items != null) allAnswers.addAll(items);

            Boolean more = (Boolean) response.get("has_more");
            hasMore = (more != null) && more;
            page++;
            if (hasMore) Thread.sleep(200);
        }
        return allAnswers;
    }

    private Map<String, Object> makeApiRequest(String urlString) {
        int maxRetries = 3;
        int attempt = 0;
        while (attempt < maxRetries) {
            try {
                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept-Encoding", "gzip");
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(30000);

                int code = conn.getResponseCode();
                if (code == 400 || code == 429) {
                    System.err.println("   [!] Rate Limited. Waiting 60s...");
                    Thread.sleep(60000);
                    attempt++; continue;
                }
                if (code >= 500) {
                    System.err.println("   [!] Server Error. Retrying...");
                    Thread.sleep(5000);
                    attempt++; continue;
                }
                if (code == 200) {
                    String json = readStream(conn.getInputStream(), conn.getContentEncoding());
                    return new SimpleJsonParser().parseObject(json);
                }
                return null;
            } catch (Exception e) {
                try { Thread.sleep(3000); } catch (InterruptedException ignored) {}
                attempt++;
            }
        }
        return null;
    }

    private String readStream(InputStream inputStream, String encoding) throws IOException {
        if ("gzip".equalsIgnoreCase(encoding)) inputStream = new GZIPInputStream(inputStream);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) sb.append(line);
        reader.close();
        return sb.toString();
    }

    public void saveToJson(List<Map<String, Object>> questions, String filename) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(Path.of(filename), StandardCharsets.UTF_8)) {
            writer.write(toJsonString(questions));
        }
    }

    private String toJsonString(Object obj) {
        StringBuilder sb = new StringBuilder();
        toJsonString(obj, sb, 0);
        return sb.toString();
    }

    private void toJsonString(Object obj, StringBuilder sb, int indent) {
        if (obj == null) sb.append("null");
        else if (obj instanceof String) sb.append("\"").append(escapeJson((String) obj)).append("\"");
        else if (obj instanceof Number || obj instanceof Boolean) sb.append(obj);
        else if (obj instanceof List) {
            List<?> list = (List<?>) obj;
            sb.append("[\n");
            for (int i = 0; i < list.size(); i++) {
                sb.append("  ".repeat(indent + 1));
                toJsonString(list.get(i), sb, indent + 1);
                if (i < list.size() - 1) sb.append(",");
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
                if (i < map.size() - 1) sb.append(",");
                sb.append("\n");
                i++;
            }
            sb.append("  ".repeat(indent)).append("}");
        }
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
    }

    static class SimpleJsonParser {
        private String json;
        private int pos;
        public Map<String, Object> parseObject(String json) {
            this.json = json.trim(); this.pos = 0; return readObject();
        }
        private Map<String, Object> readObject() {
            Map<String, Object> map = new LinkedHashMap<>();
            skipWhitespace();
            if (pos >= json.length() || json.charAt(pos) != '{') return map;
            pos++;
            while (pos < json.length()) {
                skipWhitespace();
                if (json.charAt(pos) == '}') { pos++; break; }
                String key = readString();
                skipWhitespace();
                if (json.charAt(pos) == ':') pos++;
                Object value = readValue();
                map.put(key, value);
                skipWhitespace();
                if (pos < json.length() && json.charAt(pos) == ',') pos++;
            }
            return map;
        }
        private List<Object> readArray() {
            List<Object> list = new ArrayList<>();
            pos++;
            while (pos < json.length()) {
                skipWhitespace();
                if (json.charAt(pos) == ']') { pos++; break; }
                list.add(readValue());
                skipWhitespace();
                if (pos < json.length() && json.charAt(pos) == ',') pos++;
            }
            return list;
        }
        private Object readValue() {
            skipWhitespace();
            if (pos >= json.length()) return null;
            char c = json.charAt(pos);
            if (c == '"') return readString();
            if (c == '{') return readObject();
            if (c == '[') return readArray();
            if (c == 't' || c == 'f') return readBoolean();
            if (c == 'n') return readNull();
            if (c == '-' || Character.isDigit(c)) return readNumber();
            return null;
        }
        private String readString() {
            if (json.charAt(pos) != '"') return "";
            pos++; StringBuilder sb = new StringBuilder();
            while (pos < json.length()) {
                char c = json.charAt(pos);
                if (c == '"') { pos++; break; }
                if (c == '\\' && pos + 1 < json.length()) {
                    pos++; char next = json.charAt(pos);
                    if(next=='n') sb.append('\n'); else if(next=='r') sb.append('\r'); else if(next=='t') sb.append('\t'); else if(next=='"') sb.append('"'); else if(next=='\\') sb.append('\\'); else if(next=='u') { if(pos+4<json.length()) { sb.append((char)Integer.parseInt(json.substring(pos+1,pos+5),16)); pos+=4; } } else sb.append(next);
                } else sb.append(c);
                pos++;
            }
            return sb.toString();
        }
        private Number readNumber() {
            int start = pos;
            if (json.charAt(pos) == '-') pos++;
            while (pos < json.length() && Character.isDigit(json.charAt(pos))) pos++;
            if (pos < json.length() && json.charAt(pos) == '.') {
                pos++; while (pos < json.length() && Character.isDigit(json.charAt(pos))) pos++;
                return Double.parseDouble(json.substring(start, pos));
            }
            return Long.parseLong(json.substring(start, pos));
        }
        private Boolean readBoolean() {
            if (json.startsWith("true", pos)) { pos += 4; return true; }
            if (json.startsWith("false", pos)) { pos += 5; return false; }
            return false;
        }
        private Object readNull() {
            if (json.startsWith("null", pos)) pos += 4; return null;
        }
        private void skipWhitespace() {
            while (pos < json.length() && Character.isWhitespace(json.charAt(pos))) pos++;
        }
    }
}