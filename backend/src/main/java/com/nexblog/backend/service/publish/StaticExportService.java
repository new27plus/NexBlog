package com.nexblog.backend.service.publish;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexblog.backend.entity.Article;
import com.nexblog.backend.entity.Category;
import com.nexblog.backend.exception.BusinessException;
import com.nexblog.backend.repository.ArticleRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class StaticExportService {

    private final ArticleRepository articleRepository;
    private final ObjectMapper objectMapper;

    public StaticExportService(ArticleRepository articleRepository) {
        this.articleRepository = articleRepository;
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
