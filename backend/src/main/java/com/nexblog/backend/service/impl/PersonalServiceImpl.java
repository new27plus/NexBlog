package com.nexblog.backend.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nexblog.backend.dto.personal.PersonalItem;
import com.nexblog.backend.dto.personal.PersonalRequest;
import com.nexblog.backend.dto.personal.PersonalResponse;
import com.nexblog.backend.entity.PersonalConfig;
import com.nexblog.backend.repository.PersonalRepository;
import com.nexblog.backend.service.PersonalService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class PersonalServiceImpl implements PersonalService {
    private static final List<String> SECTION_KEYS = List.of("intro", "story", "links", "topics", "tags", "updates");
    private static final TypeReference<Map<String, List<PersonalItem>>> SECTIONS_TYPE =
            new TypeReference<>() {
            };

    private final PersonalRepository personalRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public PersonalServiceImpl(PersonalRepository personalRepository) {
        this.personalRepository = personalRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public PersonalResponse getStudioPersonalConfig() {
        PersonalConfig config = personalRepository.findFirstByOrderByIdAsc().orElseGet(this::createDefaultConfig);
        return toResponse(config);
    }

    @Override
    @Transactional
    public PersonalResponse updatePersonalConfig(PersonalRequest request) {
        PersonalConfig config = personalRepository.findFirstByOrderByIdAsc().orElseGet(PersonalConfig::new);

        LocalDateTime now = LocalDateTime.now();
        if (config.getCreateTime() == null) {
            config.setCreateTime(now);
        }
        config.setUpDateTime(now);

        Map<String, List<PersonalItem>> normalized = normalizeSections(request.sections());
        config.setSectionsJson(serializeSections(normalized));

        PersonalConfig saved = personalRepository.save(config);
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PersonalResponse getPersonalConfig() {
        return getStudioPersonalConfig();
    }

    private PersonalConfig createDefaultConfig() {
        PersonalConfig config = new PersonalConfig();
        LocalDateTime now = LocalDateTime.now();
        config.setCreateTime(now);
        config.setUpDateTime(now);
        config.setSectionsJson(serializeSections(defaultSections()));
        return personalRepository.save(config);
    }

    private PersonalResponse toResponse(PersonalConfig config) {
        return new PersonalResponse(
                config.getId(),
                deserializeSections(config.getSectionsJson()),
                config.getCreateTime(),
                config.getUpDateTime()
        );
    }

    private String serializeSections(Map<String, List<PersonalItem>> sections) {
        try {
            return objectMapper.writeValueAsString(sections);
        } catch (Exception ex) {
            throw new IllegalStateException("个人配置序列化失败", ex);
        }
    }

    private Map<String, List<PersonalItem>> deserializeSections(String sectionsJson) {
        if (sectionsJson == null || sectionsJson.isBlank()) {
            return defaultSections();
        }
        try {
            Map<String, List<PersonalItem>> parsed = objectMapper.readValue(sectionsJson, SECTIONS_TYPE);
            return normalizeSections(parsed);
        } catch (Exception ex) {
            return defaultSections();
        }
    }

    private Map<String, List<PersonalItem>> normalizeSections(Map<String, List<PersonalItem>> source) {
        Map<String, List<PersonalItem>> normalized = new LinkedHashMap<>();
        for (String key : SECTION_KEYS) {
            List<PersonalItem> items = source == null ? null : source.get(key);
            if (items == null) {
                normalized.put(key, List.of());
            } else {
                normalized.put(key, items.stream().map(this::normalizeItem).toList());
            }
        }
        return normalized;
    }

    private PersonalItem normalizeItem(PersonalItem item) {
        return new PersonalItem(
                item.id(),
                item.title() == null ? "" : item.title(),
                item.subtitle() == null ? "" : item.subtitle(),
                item.content() == null ? "" : item.content(),
                item.tag() == null ? "" : item.tag(),
                item.linkIcon() == null ? "" : item.linkIcon(),
                item.linkLabel() == null ? "" : item.linkLabel(),
                item.linkUrl() == null ? "" : item.linkUrl()
        );
    }

    private Map<String, List<PersonalItem>> defaultSections() {
        Map<String, List<PersonalItem>> defaults = new LinkedHashMap<>();
        defaults.put("intro", List.of());
        defaults.put("story", List.of());
        defaults.put("links", List.of());
        defaults.put("topics", List.of());
        defaults.put("tags", List.of());
        defaults.put("updates", List.of());
        return defaults;
    }
}
