package com.example.focusplatform.dto;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Accepts either a JSON object (question id → option) or an array of
 * { "questionId", "answer" | "selectedOption" | "value" } objects.
 */
public class QuizSubmitRequestDeserializer extends JsonDeserializer<QuizSubmitRequest> {

    @Override
    public QuizSubmitRequest deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonNode root = p.getCodec().readTree(p);
        QuizSubmitRequest out = new QuizSubmitRequest();

        if (root.hasNonNull("courseId")) {
            out.setCourseId(root.get("courseId").asLong());
        }

        JsonNode answersNode = root.get("answers");
        if (answersNode == null || answersNode.isNull()) {
            out.setAnswers(Map.of());
            return out;
        }

        Map<Long, String> answers = new LinkedHashMap<>();

        if (answersNode.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> fields = answersNode.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> e = fields.next();
                answers.put(Long.parseLong(e.getKey()), textValue(e.getValue()));
            }
        } else if (answersNode.isArray()) {
            for (JsonNode item : answersNode) {
                if (!item.hasNonNull("questionId")) {
                    continue;
                }
                long qid = item.get("questionId").asLong();
                String opt = null;
                if (item.hasNonNull("selectedOption")) {
                    opt = textValue(item.get("selectedOption"));
                } else if (item.hasNonNull("answer")) {
                    opt = textValue(item.get("answer"));
                } else if (item.hasNonNull("value")) {
                    opt = textValue(item.get("value"));
                }
                if (opt != null) {
                    answers.put(qid, opt);
                }
            }
        }

        out.setAnswers(answers);
        return out;
    }

    private static String textValue(JsonNode n) {
        if (n == null || n.isNull()) {
            return null;
        }
        if (n.isTextual()) {
            return n.asText();
        }
        return n.toString();
    }
}
