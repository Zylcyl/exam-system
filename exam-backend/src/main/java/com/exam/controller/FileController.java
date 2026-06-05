package com.exam.controller;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import com.exam.common.Result;
import com.exam.entity.FileAttachment;
import com.exam.mapper.FileAttachmentMapper;
import com.exam.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/file")
@RequiredArgsConstructor
public class FileController {

    @Value("${app.upload-path}")
    private String uploadPath;

    private final FileAttachmentMapper fileAttachmentMapper;

    @PostMapping("/upload")
    public Result<FileAttachment> upload(@RequestParam("file") MultipartFile file,
                                         @AuthenticationPrincipal UserPrincipal principal) throws IOException {
        if (file.isEmpty()) return Result.error("文件为空");

        String originalName = file.getOriginalFilename();
        String ext = originalName != null && originalName.contains(".") ?
                originalName.substring(originalName.lastIndexOf(".")) : "";
        String newFileName = IdUtil.fastSimpleUUID() + ext;

        File dir = new File(uploadPath);
        if (!dir.exists()) dir.mkdirs();

        File dest = new File(uploadPath + newFileName);
        file.transferTo(dest);

        FileAttachment attachment = new FileAttachment();
        attachment.setOriginalName(originalName);
        attachment.setFileName(newFileName);
        attachment.setFilePath(uploadPath + newFileName);
        attachment.setFileSize(file.getSize());
        attachment.setFileType(ext);
        attachment.setUploadBy(principal.getUserId());
        fileAttachmentMapper.insert(attachment);

        return Result.success("上传成功", attachment);
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> download(@PathVariable Long id) {
        FileAttachment attachment = fileAttachmentMapper.selectById(id);
        if (attachment == null) return ResponseEntity.notFound().build();

        File file = new File(attachment.getFilePath());
        if (!file.exists()) return ResponseEntity.notFound().build();

        Resource resource = new FileSystemResource(file);
        String encodedName = URLEncoder.encode(attachment.getOriginalName(), StandardCharsets.UTF_8)
                .replace("+", "%20");

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename*=UTF-8''" + encodedName)
                .body(resource);
    }

    @GetMapping("/info/{id}")
    public Result<FileAttachment> info(@PathVariable Long id) {
        FileAttachment attachment = fileAttachmentMapper.selectById(id);
        return attachment != null ? Result.success(attachment) : Result.error("文件不存在");
    }
}
