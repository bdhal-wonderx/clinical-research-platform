package com.wonderx.rwe.validation;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.wonderx.rwe.entity.StudyProtocolRule;
import com.wonderx.rwe.enums.ValidationStatus;
import com.wonderx.rwe.repository.StudyProtocolRepository;
import com.wonderx.rwe.repository.StudyProtocolRuleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ProtocolValidationEngine {

    private final StudyProtocolRepository studyProtocolRepository;
    private final StudyProtocolRuleRepository ruleRepository;
    private final ObjectMapper objectMapper;

    public ValidationOutcome validate(UUID studyId, JsonNode ecrfData) {
        var protocol = studyProtocolRepository.findByStudyIdAndStatus(studyId, "ACTIVE")
                .stream().findFirst()
                .orElse(null);

        if (protocol == null) {
            return new ValidationOutcome(ValidationStatus.PASS, objectMapper.createArrayNode());
        }

        List<StudyProtocolRule> rules = ruleRepository
                .findByProtocolIdAndIsActiveTrueOrderByDisplayOrder(protocol.getId());

        ArrayNode ruleResults = objectMapper.createArrayNode();
        ValidationStatus overall = ValidationStatus.PASS;

        for (StudyProtocolRule rule : rules) {
            ObjectNode result = evaluateRule(rule, ecrfData);
            ruleResults.add(result);
            String status = result.get("status").asText();
            if ("FAIL".equals(status)) {
                overall = ValidationStatus.FAIL;
            } else if ("WARNING".equals(status) && overall != ValidationStatus.FAIL) {
                overall = ValidationStatus.WARNING;
            }
        }

        return new ValidationOutcome(overall, ruleResults);
    }

    private ObjectNode evaluateRule(StudyProtocolRule rule, JsonNode data) {
        ObjectNode result = objectMapper.createObjectNode();
        result.put("ruleName", rule.getRuleName());
        result.put("fieldName", rule.getFieldName());
        result.put("expected", rule.getExpectedValue() != null ? rule.getExpectedValue() : "");

        JsonNode actualNode = data.get(rule.getFieldName());
        String actual = actualNode == null || actualNode.isNull() ? null : actualNode.asText();

        result.put("actual", actual != null ? actual : "null");

        boolean passed = switch (rule.getOperator()) {
            case "NOT_NULL" -> actual != null && !actual.isBlank();
            case "EQ" -> rule.getExpectedValue() != null && rule.getExpectedValue().equalsIgnoreCase(actual);
            case "GTE" -> compareNumeric(actual, rule.getExpectedValue()) >= 0;
            case "LTE" -> compareNumeric(actual, rule.getExpectedValue()) <= 0;
            case "LT" -> compareNumeric(actual, rule.getExpectedValue()) < 0;
            case "GT" -> compareNumeric(actual, rule.getExpectedValue()) > 0;
            default -> true;
        };

        result.put("status", passed ? "PASS" : rule.getSeverity());
        return result;
    }

    private int compareNumeric(String actual, String expected) {
        if (actual == null || expected == null) return -1;
        try {
            return Double.compare(Double.parseDouble(actual), Double.parseDouble(expected));
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    public record ValidationOutcome(ValidationStatus overallStatus, ArrayNode ruleResults) {}
}
