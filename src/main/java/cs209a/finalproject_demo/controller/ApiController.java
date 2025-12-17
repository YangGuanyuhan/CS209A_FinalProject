package cs209a.finalproject_demo.controller;

import cs209a.finalproject_demo.model.Question;
import cs209a.finalproject_demo.service.DataAnalysisService;
import cs209a.finalproject_demo.service.DataCollectionService;
import cs209a.finalproject_demo.service.SampleDataGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class ApiController {

    @Autowired
    private DataCollectionService dataCollectionService;

    @Autowired
    private DataAnalysisService dataAnalysisService;

    @Autowired
    private SampleDataGenerator sampleDataGenerator;

    private List<Question> cachedQuestions = null;

    /**
     * Initialize data - loads from file, generates sample data, or collects from
     * API
     * 
     * mode 参数:
     * - "auto" (默认): 优先加载已有的 JSON 文件，如果没有则生成示例数据
     * - "sample": 强制生成示例数据
     * - "api": 从 Stack Overflow API 收集数据（需要在单独的收集器中完成）
     */
    @PostMapping("/init")
    public ResponseEntity<Map<String, Object>> initializeData(
            @RequestParam(defaultValue = "false") boolean forceCollect,
            @RequestParam(defaultValue = "auto") String mode,
            @RequestParam(defaultValue = "5000") int maxQuestions) {

        Map<String, Object> response = new HashMap<>();

        try {
            // 模式 "auto" - 优先加载已有文件
            if (mode.equals("auto") && !forceCollect) {
                try {
                    cachedQuestions = dataCollectionService.loadData("stackoverflow_data.json");
                    if (cachedQuestions != null && !cachedQuestions.isEmpty()) {
                        response.put("status", "loaded");
                        response.put("message", "Real data loaded from stackoverflow_data.json");
                        response.put("dataType", "real");
                        response.put("collected", cachedQuestions.size());
                        return ResponseEntity.ok(response);
                    }
                } catch (Exception e) {
                    System.out.println("No existing data file found, will generate sample data.");
                    cachedQuestions = null;
                }
            }

            // 模式 "sample" 或没有找到已有数据
            if (mode.equals("sample")
                    || (mode.equals("auto") && (cachedQuestions == null || cachedQuestions.isEmpty()))) {
                // Generate sample data for testing
                response.put("status", "generating");
                response.put("message", "Generating sample data for demonstration...");

                cachedQuestions = sampleDataGenerator.generateSampleData(maxQuestions);
                // 不覆盖已有的真实数据文件，使用不同的文件名
                if (mode.equals("sample")) {
                    dataCollectionService.saveData(cachedQuestions, "sample_data.json");
                } else {
                    dataCollectionService.saveData(cachedQuestions, "stackoverflow_data.json");
                }

                response.put("dataType", "sample");
                response.put("collected", cachedQuestions.size());
            } else if (mode.equals("api")) {
                // 提示用户使用独立的数据收集器
                response.put("status", "info");
                response.put("message",
                        "Please use the standalone data collector in data-collector/ folder to collect real data from Stack Overflow API. This avoids API rate limits during demo.");
                response.put("instruction",
                        "cd data-collector && java StackOverflowDataCollector YOUR_API_KEY 1000 ..\\stackoverflow_data.json");
                return ResponseEntity.ok(response);
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Get dataset statistics
     */
    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        if (cachedQuestions == null || cachedQuestions.isEmpty()) {
            try {
                cachedQuestions = dataCollectionService.loadData("stackoverflow_data.json");
            } catch (Exception e) {
                return ResponseEntity.status(404).body(Map.of("error", "No data available. Please initialize first."));
            }
        }

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalQuestions", cachedQuestions.size());
        stats.put("totalAnswers", cachedQuestions.stream()
                .mapToInt(q -> q.getAnswers().size())
                .sum());
        stats.put("avgScore", cachedQuestions.stream()
                .mapToInt(Question::getScore)
                .average()
                .orElse(0));
        stats.put("answeredQuestions", cachedQuestions.stream()
                .filter(Question::isAnswered)
                .count());

        return ResponseEntity.ok(stats);
    }

    /**
     * Analysis 1: Topic Trends
     */
    @GetMapping("/trends")
    public ResponseEntity<Map<String, Object>> getTopicTrends(
            @RequestParam(defaultValue = "3") int years) {

        if (cachedQuestions == null || cachedQuestions.isEmpty()) {
            try {
                cachedQuestions = dataCollectionService.loadData("stackoverflow_data.json");
            } catch (Exception e) {
                return ResponseEntity.status(404).body(Map.of("error", "No data available"));
            }
        }

        Map<String, Object> result = dataAnalysisService.analyzeTopicTrends(cachedQuestions, years);
        return ResponseEntity.ok(result);
    }

    /**
     * Analysis 2: Topic Co-occurrence
     */
    @GetMapping("/cooccurrence")
    public ResponseEntity<Map<String, Object>> getTopicCooccurrence(
            @RequestParam(defaultValue = "10") int topN) {

        if (cachedQuestions == null || cachedQuestions.isEmpty()) {
            try {
                cachedQuestions = dataCollectionService.loadData("stackoverflow_data.json");
            } catch (Exception e) {
                return ResponseEntity.status(404).body(Map.of("error", "No data available"));
            }
        }

        Map<String, Object> result = dataAnalysisService.analyzeTopicCooccurrence(cachedQuestions, topN);
        return ResponseEntity.ok(result);
    }

    /**
     * Analysis 3: Multithreading Pitfalls
     */
    @GetMapping("/pitfalls")
    public ResponseEntity<Map<String, Object>> getMultithreadingPitfalls(
            @RequestParam(defaultValue = "8") int topN) {

        if (cachedQuestions == null || cachedQuestions.isEmpty()) {
            try {
                cachedQuestions = dataCollectionService.loadData("stackoverflow_data.json");
            } catch (Exception e) {
                return ResponseEntity.status(404).body(Map.of("error", "No data available"));
            }
        }

        Map<String, Object> result = dataAnalysisService.analyzeMultithreadingPitfalls(cachedQuestions, topN);
        return ResponseEntity.ok(result);
    }

    /**
     * Analysis 4: Solvability Analysis
     */
    @GetMapping("/solvability")
    public ResponseEntity<Map<String, Object>> getSolvabilityAnalysis() {

        if (cachedQuestions == null || cachedQuestions.isEmpty()) {
            try {
                cachedQuestions = dataCollectionService.loadData("stackoverflow_data.json");
            } catch (Exception e) {
                return ResponseEntity.status(404).body(Map.of("error", "No data available"));
            }
        }

        Map<String, Object> result = dataAnalysisService.analyzeSolvability(cachedQuestions);
        return ResponseEntity.ok(result);
    }

    /**
     * Get all questions (for debugging)
     */
    @GetMapping("/questions")
    public ResponseEntity<List<Question>> getAllQuestions(
            @RequestParam(defaultValue = "10") int limit) {

        if (cachedQuestions == null || cachedQuestions.isEmpty()) {
            try {
                cachedQuestions = dataCollectionService.loadData("stackoverflow_data.json");
            } catch (Exception e) {
                return ResponseEntity.status(404).body(null);
            }
        }

        List<Question> limitedQuestions = cachedQuestions.stream()
                .limit(limit)
                .toList();

        return ResponseEntity.ok(limitedQuestions);
    }
}
