package com.nexblog.backend.service.impl;

import com.nexblog.backend.dto.config.ConfigCreateRequest;
import com.nexblog.backend.dto.config.ConfigDetailResponse;
import com.nexblog.backend.entity.SystemConfig;
import com.nexblog.backend.repository.ConfigRepository;
import com.nexblog.backend.service.ConfigService;
import org.springframework.stereotype.Service;

@Service
public class ConfigServiceImpl implements ConfigService {

    private final ConfigRepository configRepository;

    public ConfigServiceImpl(ConfigRepository configRepository) {
        this.configRepository = configRepository;
    }

    @Override
    public ConfigDetailResponse getConfig() {
        SystemConfig config = configRepository.findFirstByOrderByIdAsc().orElseGet(SystemConfig::new);

        return toDetailResponse(config);
    }

    @Override
    public ConfigDetailResponse upsertConfig(ConfigCreateRequest request) {

        SystemConfig config = configRepository.findFirstByOrderByIdAsc().orElseGet(SystemConfig::new);

        config.setGithubOwner(request.githubOwner());
        config.setGithubRepo(request.githubRepo());
        config.setPublishBranch(request.publishBranch());
        config.setSiteBasePath(request.siteBasePath());
        /*
         * token 更新策略（核心）：
         * - 当本次请求明确传了 token：覆盖旧值
         * - 当本次请求没传 token（null/空串）：保留数据库里的旧 token
         * 这样前端改其他配置时，不需要每次重新填写敏感信息。
         */
        if (request.githubToken() != null && !request.githubToken().isBlank()) {
            config.setGithubToken(request.githubToken().trim());
        }

        SystemConfig saved = configRepository.save(config);

        return toDetailResponse(saved);
    }


    private ConfigDetailResponse toDetailResponse(SystemConfig config) {
        /*
         * 安全边界：
         * - 这里不能把 githubToken 明文返回给前端
         * - 只返回一个布尔值，告诉前端“当前是否已经配置过 token”
         */
        boolean tokenConfigured = config.getGithubToken() != null && !config.getGithubToken().isBlank();
        return new ConfigDetailResponse(
                config.getId(),
                config.getGithubOwner(),
                config.getGithubRepo(),
                config.getPublishBranch(),
                tokenConfigured,
                config.getSiteBasePath()
        );
    }
}
