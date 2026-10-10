package com.example.aicodehelper.service.file;

import org.springframework.web.multipart.MultipartFile;
import java.io.InputStream;

public interface FileStorageService {
    /**
     * 上传文件，返回minio objectKey
     * @param file 文件
     * @param prefix 目录前缀，如 knowledge/pdf、fabric/image
     * @return objectKey
     */
    String upload(MultipartFile file, String prefix) throws Exception;

    /**
     * 获取文件流，用于RAG读取PDF、多模态读取面料图片
     */
    InputStream getFileStream(String objectKey) throws Exception;

    /**
     * 删除对象存储文件
     */
    void deleteFile(String objectKey) throws Exception;

    /**
     * 生成临时预览URL，有效期30分钟
     */
    String getPreviewUrl(String objectKey) throws Exception;
}
