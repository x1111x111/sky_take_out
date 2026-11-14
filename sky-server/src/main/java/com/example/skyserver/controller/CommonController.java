package com.example.skyserver.controller;

import com.example.skyserver.result.Result;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@Tag(name="图片相关接口")
@SecurityRequirement(name = "token")
@RestController
@RequestMapping("/admin/common")
public class CommonController {

    @PostMapping("/upload")
    public Result<String> upload(
            @RequestParam("file") MultipartFile file) {

        if (file == null || file.isEmpty()) {
            return Result.error("上传文件不能为空");
        }

        // 原始文件名
        String originalFilename = file.getOriginalFilename();

        // 获取文件后缀，例如 .jpg
        String suffix = "";

        if (originalFilename != null
                && originalFilename.contains(".")) {

            suffix = originalFilename.substring(
                    originalFilename.lastIndexOf(".")
            );
        }

        // 使用UUID生成新文件名，防止重名
        String filename =
                UUID.randomUUID() + suffix;

        // 项目运行目录/uploads
        String uploadPath =
                System.getProperty("user.dir")
                        + File.separator
                        + "uploads";

        File directory = new File(uploadPath);

        if (!directory.exists()) {
            directory.mkdirs();
        }

        File targetFile =
                new File(directory, filename);

        try {

            file.transferTo(targetFile);

        } catch (IOException e) {

            throw new RuntimeException("图片上传失败", e);
        }

        // 返回图片访问路径
        String imageUrl =
                "/uploads/" + filename;

        return Result.success(imageUrl);
    }
}