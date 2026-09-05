package com.recoverai.ai;

import lombok.Data;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@Data
public class AiDiagnosisResponse {
    private String diagnosis;
    
    @JsonProperty("candidate_actions")
    private List<String> candidateActions;
    
    private String rationale;
    private String aiProvider;
}
