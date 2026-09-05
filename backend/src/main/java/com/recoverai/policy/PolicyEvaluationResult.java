package com.recoverai.policy;
import lombok.Data;
import lombok.AllArgsConstructor;
import java.util.List;

@Data
@AllArgsConstructor
public class PolicyEvaluationResult {
    private List<ActionEvaluation> evaluations;
    private List<String> permittedActions;
    private boolean escalationRequired;

    @Data
    @AllArgsConstructor
    public static class ActionEvaluation {
        private String action;
        private boolean allowed;
        private String reason;
    }
}
