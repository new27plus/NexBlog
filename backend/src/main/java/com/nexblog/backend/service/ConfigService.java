package com.nexblog.backend.service;

import com.nexblog.backend.dto.config.ConfigCreateRequest;
import com.nexblog.backend.dto.config.ConfigDetailResponse;

public interface ConfigService {

    ConfigDetailResponse getConfig();

    ConfigDetailResponse upsertConfig(ConfigCreateRequest request);
}
