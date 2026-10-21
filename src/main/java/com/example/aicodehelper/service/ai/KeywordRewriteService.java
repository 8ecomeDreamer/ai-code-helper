package com.example.aicodehelper.service.ai;

import com.example.aicodehelper.domain.AiKeywordMapping;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * 问答查询改写：用关键词术语映射表（ai_keyword_mapping）把用户口语词替换为标准术语后再检索/提问，
 * 提升 RAG 召回与专业问答准确度。
 *
 * <p>映射表数据量小、变更低频，故用带 TTL 的进程内缓存（{@value #CACHE_TTL_MS} 毫秒），
 * 避免每次问答都全表查询；后台维护映射最多延迟一个 TTL 周期生效。</p>
 *
 * <p>仅影响发给模型的查询串，不改写落库的历史原文。</p>
 */
@Service
@Slf4j
public class KeywordRewriteService {

    /** 映射缓存有效期（毫秒） */
    private static final long CACHE_TTL_MS = 60_000L;

    @Resource
    private AiKeywordMappingService aiKeywordMappingService;

    /** 启用态映射快照（volatile 保证多线程可见，整体替换无需加锁） */
    private volatile List<AiKeywordMapping> cache = null;
    private volatile long cacheAt = 0L;

    /** 将提问中的口语词按映射表替换为标准术语；无映射或异常时原样返回 */
    public String rewrite(String query) {
        if (query == null || query.isBlank()) {
            return query;
        }
        String result = query;
        for (AiKeywordMapping m : mappings()) {
            String original = m.getOriginal_word();
            String standard = m.getStandard_word();
            if (original == null || original.isBlank() || standard == null || standard.isBlank()
                    || original.equals(standard)) {
                continue;
            }
            if (result.contains(original)) {
                result = result.replace(original, standard);
            }
        }
        return result;
    }

    /** 取启用态映射（enable=1 且未删除），命中缓存直接返回，过期则重载 */
    private List<AiKeywordMapping> mappings() {
        long now = System.currentTimeMillis();
        List<AiKeywordMapping> local = cache;
        if (local != null && now - cacheAt < CACHE_TTL_MS) {
            return local;
        }
        try {
            local = aiKeywordMappingService.lambdaQuery()
                    .eq(AiKeywordMapping::getEnable, 1)
                    .eq(AiKeywordMapping::getDeleted, 0)
                    .list();
        } catch (Exception e) {
            log.warn("加载关键词映射失败，本次跳过查询改写", e);
            local = cache != null ? cache : new ArrayList<>();
        }
        cache = local;
        cacheAt = now;
        return local;
    }
}
