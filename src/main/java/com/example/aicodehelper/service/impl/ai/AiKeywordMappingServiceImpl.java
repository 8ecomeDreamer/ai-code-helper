package com.example.aicodehelper.service.impl.ai;

import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.example.aicodehelper.domain.AiKeywordMapping;
import com.example.aicodehelper.mapper.AiKeywordMappingMapper;
import com.example.aicodehelper.service.ai.AiKeywordMappingService;
import org.springframework.stereotype.Service;

/**
 * 针对表【ai_keyword_mapping(关键词术语映射表)】的数据库操作Service实现
 */
@Service
public class AiKeywordMappingServiceImpl extends ServiceImpl<AiKeywordMappingMapper, AiKeywordMapping>
    implements AiKeywordMappingService {

}
