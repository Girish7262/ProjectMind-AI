package com.acciobuild.ai.memory;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Deterministic summarizing engine creating premium-grade structured logs
 * without making external LLM API requests.
 */
@Component
@Slf4j
public class MemorySummarizer {

    public String summarize(List<String> messages) {
        log.info("Generating deterministic summary for {} messages", messages.size());
        if (messages == null || messages.isEmpty()) {
            return "No content to summarize.";
        }

        List<String> shortPhrases = messages.stream()
                .filter(msg -> msg != null && !msg.strip().isEmpty())
                .map(msg -> {
                    String clean = msg.strip().replaceAll("\\s+", " ");
                    if (clean.length() > 80) {
                        return clean.substring(0, 77) + "...";
                    }
                    return clean;
                })
                .collect(Collectors.toList());

        if (shortPhrases.isEmpty()) {
            return "Empty conversation log.";
        }

        StringBuilder summary = new StringBuilder();
        summary.append("Structured context summary:\n");
        for (int i = 0; i < Math.min(shortPhrases.size(), 8); i++) {
            summary.append(String.format(" - Point %d: %s\n", i + 1, shortPhrases.get(i)));
        }
        if (shortPhrases.size() > 8) {
            summary.append(String.format(" - [Truncated %d more exchanges]", shortPhrases.size() - 8));
        }

        return summary.toString().trim();
    }
}
