package com.example.aicodehelper.service.impl.ai;

import com.baomidou.mybatisplus.spring.service.impl.ServiceImpl;
import com.example.aicodehelper.domain.RagDocument;
import com.example.aicodehelper.mapper.RagDocumentMapper;
import com.example.aicodehelper.service.ai.RagDocumentService;
import org.springframework.stereotype.Service;

/**
* @author Jim
* @description 针对表【rag_document(RAG知识库文档表)】的数据库操作Service实现
*/
@Service
public class RagDocumentServiceImpl extends ServiceImpl<RagDocumentMapper, RagDocument>
    implements RagDocumentService {

}
