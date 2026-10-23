package com.example.textileMultiModalAgent.service.impl.ai;

import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.example.textileMultiModalAgent.domain.AiBusinessLog;
import com.example.textileMultiModalAgent.mapper.AiBusinessLogMapper;
import com.example.textileMultiModalAgent.service.ai.AiBusinessLogService;
import org.springframework.stereotype.Service;

/**
 * 针对表【ai_business_log(AI Agent业务执行日志)】的数据库操作Service实现
 */
@Service
public class AiBusinessLogServiceImpl extends ServiceImpl<AiBusinessLogMapper, AiBusinessLog>
    implements AiBusinessLogService {

}
