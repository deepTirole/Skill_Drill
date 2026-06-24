package com.deep.skill_drill.services;

import org.springframework.stereotype.Service;

@Service
public class RatingService {

    private int k_factor;
    private double expected_score;

    public int updateRating(int currRating, double interviewScore, int kFactor, double expectedScore) {

        double differenceScore = interviewScore - expectedScore;
        int newRating = currRating + (int) (kFactor * differenceScore);
        return Math.max(500, Math.min(newRating, 1500));
    }

    public int dynamicRatingUpdate(int currRating, double interviewScore) {

        int kFactor=0;
        double expectedScore=0.0;

        if (currRating <= 800) {
            kFactor = 60;
            expectedScore = 6.5;
        }
        else if (currRating <= 1000) {
            kFactor = 50;
            expectedScore = 7.5;
        } else if (currRating <= 1200) {
            kFactor = 40;
            expectedScore = 8.0;
        }
        else {
            kFactor = 30;
            expectedScore = 8.5;
        }

        return updateRating(currRating, interviewScore, kFactor, expectedScore);
    }

    public String getDifficultyInstructions(int userRating) {
        String difficultyInstructions = "Keep one thing while generating the questions higher the " +
                "rating, harder the questions should be.";
        if (userRating < 800) {
            difficultyInstructions += "DIFFICULTY: BEGINNER. Focus on core fundamentals, definitions, and basic syntax. " +
                    "Ask straightforward 'What is...' or 'How do you...' questions. " +
                    "Avoid complex architecture or edge-case scenarios.";

        } else if (userRating <= 1000) {
            difficultyInstructions += "DIFFICULTY: INTERMEDIATE. Focus on practical application and framework features. " +
                    "Ask scenario-based questions where the candidate must explain how they would implement a common feature. " +
                    "Include standard debugging or basic problem-solving concepts.";

        } else if (userRating <= 1200) {
            difficultyInstructions += "DIFFICULTY: ADVANCED. Focus on complex integrations, performance, and optimization. " +
                    "Ask trade-off evaluations (e.g., 'Why choose X over Y?') and intermediate system design concepts. " +
                    "Include questions on how to handle concurrency or database bottlenecks.";

        } else {
            difficultyInstructions += "DIFFICULTY: EXPERT. Focus on high-scale system design, internal framework workings, and elite architectural patterns. " +
                    "Ask highly complex questions about distributed systems, extreme edge cases, and memory/garbage collection optimization. " +
                    "Challenge the candidate to architect solutions for millions of concurrent users.";
        }

        return difficultyInstructions;
    }

}
