package com.example.textileMultiModalAgent.domain;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * RAG知识库文档表（MinIO 对象 + 向量切片索引）
 * @TableName rag_document
 */
@TableName(value ="rag_document")
@Data
public class RagDocument implements Serializable {
    /**
     * 主键
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 文档名称
     */
    @TableField(value = "doc_name")
    private String doc_name;

    /**
     * MinIO对象key
     */
    @TableField(value = "oss_object_key")
    private String oss_object_key;

    /**
     * 访问链接
     */
    @TableField(value = "oss_url")
    private String oss_url;

    /**
     * 知识库分类：process/business/competitor
     */
    @TableField(value = "kb_type")
    private String kb_type;

    /**
     * 向量化状态：0待解析，1解析完成，2解析失败
     */
    @TableField(value = "status")
    private Integer status;

    /**
     * 启用：0-停用 1-启用（独立于 status，控制是否参与检索）
     */
    @TableField(value = "enabled")
    private Integer enabled;

    /**
     * 向量切片ID列表（JSON 数组字符串，停用/删除时按此清理向量库）
     */
    @TableField(value = "chunk_ids")
    private String chunk_ids;

    /**
     * 上传人用户id
     */
    @TableField(value = "create_by")
    private Long create_by;

    /**
     * 创建时间
     */
    @TableField(value = "create_time")
    private Date create_time;

    /**
     * 更新时间
     */
    @TableField(value = "update_time")
    private Date update_time;

    /**
     * 逻辑删除：0-否 1-是
     */
    @TableField(value = "deleted")
    private Integer deleted;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}
