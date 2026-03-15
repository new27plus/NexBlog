package com.nexblog.backend.service.publish;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.nexblog.backend.entity.Article;
import com.nexblog.backend.entity.Category;
import com.nexblog.backend.entity.PersonalConfig;
import com.nexblog.backend.exception.BusinessException;
import com.nexblog.backend.repository.ArticleRepository;
import com.nexblog.backend.repository.PersonalRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class StaticExportService {

    private final ArticleRepository articleRepository;
    private final PersonalRepository personalRepository;
    private final ObjectMapper objectMapper;

    public StaticExportService(ArticleRepository articleRepository, PersonalRepository personalRepository) {
        this.articleRepository = articleRepository;
        this.personalRepository = personalRepository;
        this.objectMapper = new ObjectMapper().findAndRegisterModules();
    }

    public ExportResult exportArticles(PublishWorkspaceService.PublishWorkspace workspace) {
        Path exportRoot = Path.of(workspace.exportRoot());
        Path articlesRoot = Path.of(workspace.exportArticlesRoot());
        Path listFile = exportRoot.resolve("articles.json");

        try {
            Files.createDirectories(articlesRoot);
        } catch (IOException e) {
            throw new BusinessException(50011, "创建静态导出目录失败: " + e.getMessage());
        }

        List<Article> articles = articleRepository.findAll(Sort.by(Sort.Direction.DESC, "createTime"));

        List<ArticleListItem> listItems = articles.stream()
            .map(this::toListItem)
            .toList();

        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(listFile.toFile(), listItems);
            for (Article article : articles) {
                Path detailFile = articlesRoot.resolve(article.getId() + ".json");
                objectMapper.writerWithDefaultPrettyPrinter().writeValue(detailFile.toFile(), toDetailItem(article));
            }
            writePersonalConfig(exportRoot);
        } catch (IOException e) {
            throw new BusinessException(50012, "写入静态导出文件失败: " + e.getMessage());
        }

        return new ExportResult(workspace.jobId(), listItems.size(), listFile.toString(), articlesRoot.toString());
    }

    private ArticleListItem toListItem(Article article) {
        Category category = article.getCategory();
        return new ArticleListItem(
            article.getId(),
            article.getTitle(),
            article.getSummary(),
            category == null ? null : category.getId(),
            category == null ? null : category.getName(),
            article.getCreateTime()
        );
    }

    private ArticleDetailItem toDetailItem(Article article) {
        Category category = article.getCategory();
        return new ArticleDetailItem(
            article.getId(),
            article.getTitle(),
            article.getContent(),
            article.getSummary(),
            category == null ? null : category.getId(),
            category == null ? null : category.getName(),
            article.getCreateTime(),
            article.getUpDateTime()
        );
    }

    private void writePersonalConfig(Path exportRoot) throws IOException {
        Path personalFile = exportRoot.resolve("personal-config.json");
        PersonalConfig config = personalRepository.findFirstByOrderByIdAsc().orElse(null);
        objectMapper.writerWithDefaultPrettyPrinter().writeValue(personalFile.toFile(), toPersonalPayload(config));
    }

    private Map<String, Object> toPersonalPayload(PersonalConfig config) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("id", config == null ? null : config.getId());
        payload.put("sections", normalizePersonalSections(readSectionsJson(config == null ? null : config.getSectionsJson())));
        payload.put("createdAt", config == null ? null : config.getCreateTime());
        payload.put("updatedAt", config == null ? null : config.getUpDateTime());
        return payload;
    }

    private Map<String, Object> readSectionsJson(String sectionsJson) {
        if (sectionsJson == null || sectionsJson.isBlank()) {
            return defaultPersonalSections();
        }
        try {
            return objectMapper.readValue(sectionsJson, new TypeReference<Map<String, Object>>() {
            });
        } catch (Exception ignored) {
            return defaultPersonalSections();
        }
    }

    private Map<String, Object> normalizePersonalSections(Map<String, Object> source) {
        Map<String, Object> sections = new LinkedHashMap<>();
        List<String> keys = List.of("intro", "story", "links", "topics", "tags", "updates");
        for (String key : keys) {
            Object value = source == null ? null : source.get(key);
            sections.put(key, value instanceof List<?> ? value : List.of());
        }
        return sections;
    }

    private Map<String, Object> defaultPersonalSections() {
        Map<String, Object> sections = new LinkedHashMap<>();
        sections.put("intro", List.of());
        sections.put("story", List.of());
        sections.put("links", List.of());
        sections.put("topics", List.of());
        sections.put("tags", List.of());
        sections.put("updates", List.of());
        return sections;
    }

    public record ExportResult(
        String jobId,
        int articleCount,
        String listFilePath,
        String detailDirectoryPath
    ) {
    }

    public record ArticleListItem(
        Long id,
        String title,
        String summary,
        Long categoryId,
        String categoryName,
        LocalDateTime createdAt
    ) {
    }

    public record ArticleDetailItem(
        Long id,
        String title,
        String content,
        String summary,
        Long categoryId,
        String categoryName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
    ) {
    }
}
