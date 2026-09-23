package com.campustrade.controller;

import com.campustrade.common.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.io.File;
import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/api/file")
public class FileController {

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @PostMapping("/upload")
    public Result<String> upload(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) return Result.error(400, "请选择文件");
        try {
            File dir = new File(uploadDir);
            if (!dir.exists()) dir.mkdirs();
            String ext = "";
            String name = file.getOriginalFilename();
            if (name != null && name.contains(".")) ext = name.substring(name.lastIndexOf("."));
            String filename = UUID.randomUUID().toString() + ext;
            file.transferTo(new File(dir, filename));
            return Result.success("/uploads/" + filename);
        } catch (IOException e) { return Result.error(500, "上传失败"); }
    }

    @PostMapping("/uploads")
    public Result<String> uploads(@RequestParam("files") MultipartFile[] files) {
        StringBuilder urls = new StringBuilder();
        try {
            File dir = new File(uploadDir);
            if (!dir.exists()) dir.mkdirs();
            for (MultipartFile file : files) {
                String ext = "";
                String name = file.getOriginalFilename();
                if (name != null && name.contains(".")) ext = name.substring(name.lastIndexOf("."));
                String filename = UUID.randomUUID().toString() + ext;
                file.transferTo(new File(dir, filename));
                if (urls.length() > 0) urls.append(",");
                urls.append("/uploads/").append(filename);
            }
            return Result.success(urls.toString());
        } catch (IOException e) { return Result.error(500, "上传失败"); }
    }
}
