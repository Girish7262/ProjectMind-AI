package com.acciobuild.ai.engine;

import com.acciobuild.ai.dto.ContextDto.SourceDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Pipeline component that ranks assembled context sources using relevance score metrics.
 */
@Component
@Slf4j
public class ConversationContextRanker {

    /**
     * Sorts context sources descending by relevance score.
     */
    public List<SourceDto> rank(List<SourceDto> sources) {
        log.info("Ranking {} context sources using relevancy heuristics", sources.size());
        
        List<SourceDto> sorted = new ArrayList<>(sources);
        sorted.sort(Comparator.comparingDouble((SourceDto s) -> s.getScore() != null ? s.getScore() : 0.0).reversed());
        return sorted;
    }
}
