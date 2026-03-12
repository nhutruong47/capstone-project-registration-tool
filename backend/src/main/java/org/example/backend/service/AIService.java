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
     * Kiểm tra nội dung đề tài bằng AI (async)
     * Chạy đồng thời 2 bài test: Compliance (chất lượng) và Similarity (trùng lắp)
     */
    @Async
    public CompletableFuture<Topic> checkTopicAIAsync(Long topicId) {
        log.info("Starting AI comprehensive check for topic: {}", topicId);

        try {
            Topic topic = topicService.findById(topicId)
                    .orElseThrow(() -> new RuntimeException("Topic not found"));

            // 1. Compliance Check (Kiểm tra chất lượng mô tả)
            boolean isCompliant = performComplianceCheck(topic);
            
            // 2. Similarity Check (Kiểm tra trùng lắp)
            SimilarityResult similarityResult = performSimilarityCheck(topic);

            // Cập nhật kết quả tổng hợp
            Topic updatedTopic = topicService.updateAIResults(
                    topicId,
                    isCompliant,
                    similarityResult.score(),
                    similarityResult.details());

            log.info("AI check completed for topic: {}. Compliant: {}, Similarity: {}", 
                    topicId, isCompliant, similarityResult.score());
            return CompletableFuture.completedFuture(updatedTopic);

        } catch (Exception e) {
            log.error("AI check failed for topic: {}", topicId, e);
            throw new RuntimeException("AI check failed", e);
        }
    }

    /**
     * Kiểm tra chất lượng mô tả (Step 3: Compliance Check)
     * Tiêu chí: Độ dài > 50 ký tự, không chứa từ khóa rác, nội dung nghiêm túc
     */
    private boolean performComplianceCheck(Topic topic) {
        String desc = topic.getDescription();
        if (desc == null || desc.trim().length() < 50) {
            return false; // Quá ngắn
        }
        
        String[] junkWords = {"test", "abc", "123", "placeholder", "nothing", "demo"};
        for (String junk : junkWords) {
            if (desc.toLowerCase().contains(junk)) {
                return false; // Chứa từ khóa rác
            }
        }
        
        return true;
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
                    topic.getTitleEn().toLowerCase(),
                    existingTopic.getTitleEn().toLowerCase());

            if (titleSimilarity > maxSimilarity) {
                maxSimilarity = titleSimilarity;
            }

            if (titleSimilarity > 0.5) {
                details.append(String.format("- Tương đồng %.0f%% với đề tài %s: %s\n",
                        titleSimilarity * 100,
                        existingTopic.getCode(),
                        existingTopic.getTitleEn()));
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
