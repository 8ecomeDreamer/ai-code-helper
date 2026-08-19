package com.example.aicodehelper.ai.rag;

import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import dev.langchain4j.data.document.splitter.DocumentByParagraphSplitter;
import dev.langchain4j.data.segment.TextSegment;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.rag.content.retriever.ContentRetriever;
import dev.langchain4j.rag.content.retriever.EmbeddingStoreContentRetriever;
import dev.langchain4j.store.embedding.EmbeddingStore;
import dev.langchain4j.store.embedding.EmbeddingStoreIngestor;
import dev.langchain4j.store.embedding.inmemory.InMemoryEmbeddingStore;
import jakarta.annotation.Resource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;

@Configuration
public class RagConfig {

    @Resource
    private EmbeddingModel embeddingModel;

//    @Resource
//    private EmbeddingStore<TextSegment> embeddingStore;

    @Bean
    public ContentRetriever contentRetriever() throws IOException {
        // 需求：切割文档

        // 引入EmbeddingModel和EmbeddingStore
        // EmbeddingModel embeddingModel = new BgeSmallEnV15QuantizedEmbedddingModel();

        // EmbeddingStore无法使用Resource注入，好像是报找不到bean? 从官网上找到创建实例代替
        EmbeddingStore<TextSegment> embeddingStore = new InMemoryEmbeddingStore<>();

//        // 1. 加载文档
//        ClassPathResource resource = new ClassPathResource("docs/Java_AI应用开发学习路线.md");
//        Document document;
//        try (var inputStream = resource.getInputStream()) {
//            document = new TextDocumentParser().parse(inputStream);
//        }

//        Document document = FileSystemDocumentLoader.loadDocument("D:\\study\\github\\ai-code-helper\\ai-code-helper\\src\\main\\resources\\docs\\Java_AI应用开发学习路线.md");
//        List<Document> documents = FileSystemDocumentLoader.loadDocuments("src\\main\\resources\\docs");
        // 2. 切割文档,每个文档按照段落进行切割。每次最多切割1000字符，最多可重叠200字符。
        DocumentByParagraphSplitter documentByParagraphSplitter = new DocumentByParagraphSplitter(1000, 200);
        // 3. 转换成向量模型并存储到向量数据中
        EmbeddingStoreIngestor ingestor = EmbeddingStoreIngestor.builder()
                .documentSplitter(documentByParagraphSplitter)
                // 为了提升文档的质量，为每个切割后的文档添加metadata元信息
                .textSegmentTransformer(textSegment -> TextSegment.from(textSegment.metadata().getString("file_name") + "\n" + textSegment.text(), textSegment.metadata()))
                // 使用向量模型
                .embeddingModel(embeddingModel)
                .embeddingStore(embeddingStore)
                .build();

        // 加载文档
//        ingestor.ingest(document);
        // 4.自定义内容加载器
        EmbeddingStoreContentRetriever contentRetriver = EmbeddingStoreContentRetriever.builder()
                .embeddingStore(embeddingStore)
                .embeddingModel(embeddingModel)
                .maxResults(5) // 最多5条结果
                .minScore(0.75) // 过滤掉分数小于0.75的结果
                .build();

        return contentRetriver;

    }
}
