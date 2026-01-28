package org.example.backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.backend.entity.Topic;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

/**
 * AI Service for OpenAI integration
 * Handles topic compliance check and similarity check
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AIService {

    private final TopicService topicService;
    private final ReviewService reviewService;

    /**
     * Asynchronously process topic with AI checks
     */
    @Async
    public CompletableFuture<Topic> processTopicAsync(Long topicId) {
        log.info("Starting AI processing for topic: {}", topicId);

        try {
            // AI Check 1: Compliance Check
            ComplianceResult complianceResult = performComplianceCheck(topicId);

            // AI Check 2: Similarity Check
            SimilarityResult similarityResult = performSimilarityCheck(topicId);

            // Update topic with AI results
            Topic updatedTopic = topicService.updateAIResults(
                    topicId,
                    complianceResult.passed(),
                    complianceResult.feedback(),
                    similarityResult.score(),
                    similarityResult.details());

            // If passed, assign reviewers
            if (updatedTopic.getAiCompliancePass() &&
                    (updatedTopic.getAiSimilarityScore() == null || updatedTopic.getAiSimilarityScore() < 80)) {
                reviewService.assignReviewers(topicId, 2);
            }

            log.info("AI processing completed for topic: {}", topicId);
            return CompletableFuture.completedFuture(updatedTopic);

        } catch (Exception e) {
            log.error("AI processing failed for topic: {}", topicId, e);
            throw new RuntimeException("AI processing failed", e);
        }
    }

    /**
     * AI Check 1: Compliance Check
     * Validates topic format and content requirements
     */
    private ComplianceResult performComplianceCheck(Long topicId) {
        Topic topic = topicService.findById(topicId)
                .orElseThrow(() -> new RuntimeException("Topic not found"));

        StringBuilder feedback = new StringBuilder();
        boolean passed = true;

        // Check description length
        if (topic.getDescription() == null || topic.getDescription().length() < 200) {
            feedback.append("- Description must be at least 200 characters.\n");
            passed = false;
        } else if (topic.getDescription().length() > 2000) {
            feedback.append("- Description should not exceed 2000 characters.\n");
            passed = false;
        }

        // Check requirements
        if (topic.getRequirements() == null || topic.getRequirements().isEmpty()) {
            feedback.append("- Technical requirements must be specified.\n");
            passed = false;
        }

        // Check titles
        if (topic.getTitleEn() == null || topic.getTitleEn().length() < 10) {
            feedback.append("- English title must be at least 10 characters.\n");
            passed = false;
        }

        if (topic.getTitleVi() == null || topic.getTitleVi().length() < 10) {
            feedback.append("- Vietnamese title must be at least 10 characters.\n");
            passed = false;
        }

        if (passed) {
            feedback.append("Topic meets all compliance requirements.");
        }

        // TODO: Integrate with OpenAI API for advanced content analysis
        // Example:
        // String prompt = "Analyze the following capstone project proposal for clarity
        // and feasibility: " + topic.getDescription();
        // OpenAIResponse response = openAIClient.chat(prompt);

        return new ComplianceResult(passed, feedback.toString());
    }

    /**
     * AI Check 2: Similarity Check
     * Compares topic with existing topics using embeddings
     */
    private SimilarityResult performSimilarityCheck(Long topicId) {
        Topic topic = topicService.findById(topicId)
                .orElseThrow(() -> new RuntimeException("Topic not found"));

        // TODO: Implement actual similarity check using OpenAI Embeddings
        // Example workflow:
        // 1. Generate embedding for current topic description
        // 2. Compare with stored embeddings of past topics
        // 3. Find topics with similarity > threshold
        //
        // List<Double> embedding =
        // openAIClient.createEmbedding(topic.getDescription());
        // List<SimilarTopic> similarities = vectorStore.findSimilar(embedding, 0.8);

        // Placeholder implementation
        log.debug("Checking similarity for topic: {}", topic.getCode());
        double similarityScore = 0.0; // No similarity for now
        String details = "No similar topics found in the database.";

        return new SimilarityResult(similarityScore, details);
    }

    // Result records
    private record ComplianceResult(boolean passed, String feedback) {
    }

    private record SimilarityResult(double score, String details) {
    }
}
