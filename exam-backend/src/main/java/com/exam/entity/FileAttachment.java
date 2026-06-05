package com.exam.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

@Data
@TableName("file_attachment")
public class FileAttachment implements Serializable {
    @TableId(type = IdType.AUTO)
    private Long id;
    private String originalName;
    private String fileName;
    private String filePath;
    private Long fileSize;
    private String fileType;
    private Long uploadBy;
    private LocalDateTime createTime;
}
