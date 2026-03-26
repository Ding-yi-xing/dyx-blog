package com.dyx.blog.common.util;

import com.dyx.blog.entity.Profile;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 首页横幅配置工具。
 */
public final class HeroConfigUtil {

    private static final String DEFAULT_SITE_TITLE = "HELLO THERE!";
    private static final String DEFAULT_HERO_TITLE = "写代码的人，也写点文字。";
    private static final String DEFAULT_HERO_SUBTITLE = "这里有后端开发、安全研究、折腾小工具的记录，也有一些不那么严肃的碎碎念。这个博客更像是一个公开的笔记本，欢迎随便翻翻。";
    private static final String DEFAULT_IMAGE_ALT = "avatar";
    private static final List<String> DEFAULT_TAGS = List.of("后端 · Java", "安全 / 基础设施", "随笔与长文");

    private HeroConfigUtil() {
    }

    /**
     * 当横幅配置为空时，基于旧字段生成默认配置。
     *
     * @param profile      个人资料。
     * @param objectMapper JSON 工具。
     */
    public static void ensureHeroConfig(Profile profile, ObjectMapper objectMapper) {
        if (profile == null || objectMapper == null || !isBlank(profile.getHeroConfig())) {
            return;
        }
        profile.setHeroConfig(buildDefaultHeroConfig(profile, objectMapper));
    }

    /**
     * 基于横幅配置回填旧字段，兼容仍依赖固定字段的页面。
     *
     * @param profile      个人资料。
     * @param objectMapper JSON 工具。
     */
    public static void syncLegacyFields(Profile profile, ObjectMapper objectMapper) {
        if (profile == null || objectMapper == null || isBlank(profile.getHeroConfig())) {
            return;
        }
        try {
            Map<String, Object> config = objectMapper.readValue(profile.getHeroConfig(), new TypeReference<>() {
            });
            Object blocksValue = config.get("blocks");
            if (!(blocksValue instanceof List<?> blocks)) {
                return;
            }
            profile.setSiteTitle(firstNonBlank(extractText(blocks, "eyebrow"), profile.getSiteTitle(), DEFAULT_SITE_TITLE));
            profile.setHeroTitle(firstNonBlank(extractText(blocks, "title"), profile.getHeroTitle(), DEFAULT_HERO_TITLE));
            profile.setHeroSubtitle(firstNonBlank(extractText(blocks, "subtitle"), profile.getHeroSubtitle(), DEFAULT_HERO_SUBTITLE));
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("首页横幅配置格式不正确", exception);
        }
    }

    private static String buildDefaultHeroConfig(Profile profile, ObjectMapper objectMapper) {
        Map<String, Object> config = new LinkedHashMap<>();
        config.put("version", 1);

        List<Map<String, Object>> blocks = new ArrayList<>();
        blocks.add(createTextBlock("eyebrow-default", "eyebrow", "left", firstNonBlank(profile == null ? null : profile.getSiteTitle(), DEFAULT_SITE_TITLE)));
        blocks.add(createTextBlock("title-default", "title", "left", firstNonBlank(profile == null ? null : profile.getHeroTitle(), DEFAULT_HERO_TITLE)));
        blocks.add(createTextBlock("subtitle-default", "subtitle", "left", firstNonBlank(profile == null ? null : profile.getHeroSubtitle(), DEFAULT_HERO_SUBTITLE)));
        blocks.add(createTagsBlock("tags-default", "left", DEFAULT_TAGS));
        blocks.add(createImageBlock("image-default", "right", profile == null ? null : profile.getAvatarUrl(), DEFAULT_IMAGE_ALT));
        config.put("blocks", blocks);

        try {
            return objectMapper.writeValueAsString(config);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("默认首页横幅配置生成失败", exception);
        }
    }

    private static Map<String, Object> createTextBlock(String id, String type, String column, String text) {
        Map<String, Object> block = new LinkedHashMap<>();
        block.put("id", id);
        block.put("type", type);
        block.put("column", column);
        block.put("text", text);
        return block;
    }

    private static Map<String, Object> createTagsBlock(String id, String column, List<String> items) {
        Map<String, Object> block = new LinkedHashMap<>();
        block.put("id", id);
        block.put("type", "tags");
        block.put("column", column);
        block.put("items", items);
        return block;
    }

    private static Map<String, Object> createImageBlock(String id, String column, String imageUrl, String alt) {
        Map<String, Object> block = new LinkedHashMap<>();
        block.put("id", id);
        block.put("type", "image");
        block.put("column", column);
        block.put("imageUrl", imageUrl);
        block.put("alt", alt);
        return block;
    }

    private static String extractText(List<?> blocks, String type) {
        for (Object item : blocks) {
            if (!(item instanceof Map<?, ?> block)) {
                continue;
            }
            if (!type.equals(String.valueOf(block.get("type")))) {
                continue;
            }
            return toTrimmedString(block.get("text"));
        }
        return null;
    }

    private static String extractImageUrl(List<?> blocks) {
        for (Object item : blocks) {
            if (!(item instanceof Map<?, ?> block)) {
                continue;
            }
            if (!"image".equals(String.valueOf(block.get("type")))) {
                continue;
            }
            return toTrimmedString(block.get("imageUrl"));
        }
        return null;
    }

    private static String toTrimmedString(Object value) {
        if (value == null) {
            return null;
        }
        String text = String.valueOf(value).trim();
        return text.isEmpty() ? null : text;
    }

    private static String firstNonBlank(String... values) {
        if (values == null) {
            return null;
        }
        for (String value : values) {
            if (!isBlank(value)) {
                return value.trim();
            }
        }
        return null;
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
