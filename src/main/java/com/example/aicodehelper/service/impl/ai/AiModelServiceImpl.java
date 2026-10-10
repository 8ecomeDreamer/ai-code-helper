package com.example.aicodehelper.service.impl.ai;

import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.example.aicodehelper.domain.AiModel;
import com.example.aicodehelper.mapper.AiModelMapper;
import com.example.aicodehelper.service.ai.AiModelService;
import org.springframework.stereotype.Service;

/**
 * 针对表【ai_model(AI大模型配置表)】的数据库操作Service实现
 */
@Service
public class AiModelServiceImpl extends ServiceImpl<AiModelMapper, AiModel>
    implements AiModelService {

}
