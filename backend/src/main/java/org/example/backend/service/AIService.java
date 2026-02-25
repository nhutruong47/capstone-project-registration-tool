package org.example.backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.entity.Topic;
import org.example.backend.repository.TopicRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * AI Service - Kiểm tra trùng lặp đề tài
 * So sánh tiêu đề và mô tả với các đề tài đã có trong hệ thống
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AIService {

    private final TopicService topicService;
    private final TopicRepository topicRepository;

    /**
     * Kiểm tra trùng lặp đề tài bằng AI (async)
     * Nếu độ tương đồng vượt ngưỡng 80% → cảnh báo giảng viên
     */
    @Async
    public CompletableFuture<Topic> checkSimilarityAsync(Long topicId) {
        log.info("Starting AI similarity check for topic: {}", topicId);

        try {
            Topic topic = topicService.findById(topicId)
                    .orElseThrow(() -> new RuntimeException("Topic not found"));

            SimilarityResult result = performSimilarityCheck(topic);

            // Cập nhật kết quả AI
            Topic updatedTopic = topicService.updateAISimilarity(
                    topicId,
                    result.score(),
                    result.details());

            log.info("AI similarity check completed for topic: {}. Score: {}", topicId, result.score());
            return CompletableFuture.completedFuture(updatedTopic);

        } catch (Exception e) {
            log.error("AI similarity check failed for topic: {}", topicId, e);
            throw new RuntimeException("AI similarity check failed", e);
        }
    }

    /**
     * So sánh tiêu đề và mô tả với các đề tài đã có
     * TODO: Tích hợp OpenAI Embeddings để so sánh nâng cao
     */
    private SimilarityResult performSimilarityCheck(Topic topic) {
        List<Topic> allTopics = topicRepository.findBySemester(topic.getSemester());

        double maxSimilarity = 0.0;
        StringBuilder details = new StringBuilder();

        for (Topic existingTopic : allTopics) {
            if (existingTopic.getId().equals(topic.getId()))
                continue;

            // Simple similarity check dựa trên tiêu đề
            double titleSimilarity = calculateSimpleSimilarity(
                    topic.getTitle().toLowerCase(),
                    existingTopic.getTitle().toLowerCase());

            if (titleSimilarity > maxSimilarity) {
                maxSimilarity = titleSimilarity;
            }

            if (titleSimilarity > 0.5) {
                details.append(String.format("- Tương đồng %.0f%% với đề tài %s: %s\n",
                        titleSimilarity * 100,
                        existingTopic.getCode(),
                        existingTopic.getTitle()));
            }
        }

        if (details.isEmpty()) {
            details.append("Không tìm thấy đề tài tương đồng trong hệ thống.");
        }

        // TODO: Integrate with OpenAI Embeddings for advanced similarity
        // Example:
        // List<Double> embedding =
        // openAIClient.createEmbedding(topic.getDescription());
        // List<SimilarTopic> similarities = vectorStore.findSimilar(embedding, 0.8);

        return new SimilarityResult(maxSimilarity * 100, details.toString());
    }

    /**
     * Simple Jaccard similarity cho tiêu đề (placeholder)
     */
    private double calculateSimpleSimilarity(String text1, String text2) {
        String[] words1 = text1.split("\\s+");
        String[] words2 = text2.split("\\s+");

        java.util.Set<String> set1 = new java.util.HashSet<>(java.util.Arrays.asList(words1));
        java.util.Set<String> set2 = new java.util.HashSet<>(java.util.Arrays.asList(words2));

        java.util.Set<String> intersection = new java.util.HashSet<>(set1);
        intersection.retainAll(set2);

        java.util.Set<String> union = new java.util.HashSet<>(set1);
        union.addAll(set2);

        if (union.isEmpty())
            return 0.0;
        return (double) intersection.size() / union.size();
    }

    private record SimilarityResult(double score, String details) {
    }
}
