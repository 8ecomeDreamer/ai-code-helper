package com.example.textileMultiModalAgent.service.impl.ai;

import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.example.textileMultiModalAgent.domain.RagDocument;
import com.example.textileMultiModalAgent.mapper.RagDocumentMapper;
import com.example.textileMultiModalAgent.service.ai.RagDocumentService;
import org.springframework.stereotype.Service;

/**
* @author Jim
* @description 针对表【rag_document(RAG知识库文档表)】的数据库操作Service实现
*/
@Service
public class RagDocumentServiceImpl extends ServiceImpl<RagDocumentMapper, RagDocument>
    implements RagDocumentService {

}
