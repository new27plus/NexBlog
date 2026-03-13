package com.nexblog.backend.controller;

import com.nexblog.backend.common.ApiResponse;
import com.nexblog.backend.exception.BusinessException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final ObjectProvider<ChatClient.Builder> chatClientBuilderProvider;

    public AiController(ObjectProvider<ChatClient.Builder> chatClientBuilderProvider) {
        this.chatClientBuilderProvider = chatClientBuilderProvider;
    }

    @PostMapping("/optimize")
    public ApiResponse<Object> optimizeContent(@Valid @RequestBody OptimizeRequest request) {
        String originalContent = request.content();
        String mode = request.mode();
        String systemInstruction;

        if ("compare".equals(mode)) {
            systemInstruction = "你是一个只输出 JSON 格式数据的机器接口。请润色用户的文本。\n" +
                "【最高指令】：绝不允许输出任何解释、问候或Markdown代码块标记(如```json)！\n" +
                "你必须严格返回如下 JSON 格式：\n" +
                "{\n" +
                "  \"original\": \"(原封不动保留原文)\",\n" +
                "  \"optimized\": \"(润色后的完整内容)\"\n" +
                "}\n\n";
        } else {
            systemInstruction = "你是一个没有感情的文本处理工具。请润色用户的文本，修正错别字并提升专业度。\n" +
                "【最高指令】：只输出最终的润色结果文本！绝对不能包含诸如“润色后内容”、“修改说明”等任何多余废话！不要加任何前缀！\n\n";
        }

        String responseText = callAi(systemInstruction, originalContent);
        if (!"compare".equals(mode)) {
            return ApiResponse.success(responseText.trim());
        }

        return ApiResponse.success(parseCompareResponse(originalContent, responseText));
    }

    @PostMapping({"/summary", "/summery"})
    public ApiResponse<String> summary(@Valid @RequestBody SummaryRequest request) {
        String originalContent = request.content();
        String systemInstruction = "你是一个没有感情的文本处理工具。请只提炼总结用户文本的核心观点。\n" +
            "【最高指令】：只输出一条纯文本中文句子，必须在一句话内完成表达，禁止使用 Markdown、换行、列表、前后缀说明。\n\n";
        String responseText = callAi(systemInstruction, originalContent);
        return ApiResponse.success(normalizeSummary(responseText));
    }

    private ChatClient getChatClient() {
        ChatClient.Builder chatClientBuilder = chatClientBuilderProvider.getIfAvailable();
        if (chatClientBuilder == null) {
            throw new BusinessException(40003, "AI 服务未配置，请先检查 Ollama 连接");
        }
        return chatClientBuilder.build();
    }

    private String callAi(String systemInstruction, String originalContent) {
        String prompt = systemInstruction + "用户文本：\n" + originalContent;
        try {
            String responseText = getChatClient().prompt().user(prompt).call().content();
            if (responseText == null) {
                throw new BusinessException(40002, "AI 返回空内容，请稍后重试");
            }
            return responseText;
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException(40001, "AI 服务暂时不可用，请稍后重试");
        }
    }

    private CompareResponse parseCompareResponse(String originalContent, String responseText) {
        String cleanText = responseText.replace("```json", "").replace("```", "").trim();
        String optimized = extractJsonField(cleanText, "optimized");
        if (optimized.isBlank()) {
            optimized = cleanText;
        }
        return new CompareResponse(originalContent, optimized.trim());
    }

    private String extractJsonField(String text, String fieldName) {
        String fieldToken = "\"" + fieldName + "\"";
        int fieldStart = text.indexOf(fieldToken);
        if (fieldStart < 0) {
            return "";
        }
        int colonIndex = text.indexOf(':', fieldStart + fieldToken.length());
        if (colonIndex < 0) {
            return "";
        }
        int firstQuote = text.indexOf('"', colonIndex + 1);
        if (firstQuote < 0) {
            return "";
        }
        StringBuilder value = new StringBuilder();
        boolean escaping = false;
        for (int i = firstQuote + 1; i < text.length(); i++) {
            char current = text.charAt(i);
            if (escaping) {
                value.append(current);
                escaping = false;
                continue;
            }
            if (current == '\\') {
                escaping = true;
                continue;
            }
            if (current == '"') {
                break;
            }
            value.append(current);
        }
        return value.toString();
    }

    private String normalizeSummary(String responseText) {
        String plainText = responseText
            .replace("```", " ")
            .replaceAll("[\\r\\n]+", " ")
            .replaceAll("\\s+", " ")
            .trim();
        if (plainText.isBlank()) {
            throw new BusinessException(40002, "AI 返回空内容，请稍后重试");
        }

        int sentenceEnd = plainText.indexOf('。');
        if (sentenceEnd < 0) {
            sentenceEnd = plainText.indexOf('.');
        }
        if (sentenceEnd >= 0) {
            plainText = plainText.substring(0, sentenceEnd + 1).trim();
        }

        if (plainText.length() > 120) {
            plainText = plainText.substring(0, 120).trim() + "。";
        }

        return plainText;
    }

    public record OptimizeRequest(
        @NotBlank(message = "内容不能为空")
        String content,
        @Pattern(regexp = "replace|compare", message = "mode 仅支持 replace 或 compare")
        String mode
    ) {
        public OptimizeRequest {
            if (mode == null || mode.isBlank()) {
                mode = "replace";
            }
        }
    }

    public record SummaryRequest(
        @NotBlank(message = "内容不能为空")
        String content
    ) { }

    public record CompareResponse(
        String original,
        String optimized
    ) { }
}
