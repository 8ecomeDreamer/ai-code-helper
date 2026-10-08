package com.example.aicodehelper.service.impl.ai;

import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.example.aicodehelper.domain.AiPromptTemplate;
import com.example.aicodehelper.service.ai.AiPromptTemplateService;
import com.example.aicodehelper.mapper.AiPromptTemplateMapper;
import org.springframework.stereotype.Service;

/**
* @author Jim
* @description 针对表【ai_prompt_template(AI提示词模板表，后台管理，不存储实时用户对话)】的数据库操作Service实现
* @createDate 2026-10-07 15:59:19
*/
@Service
public class AiPromptTemplateServiceImpl extends ServiceImpl<AiPromptTemplateMapper, AiPromptTemplate>
    implements AiPromptTemplateService{

}




