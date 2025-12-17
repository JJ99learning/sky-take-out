package com.sky.controller.admin;


import com.sky.constant.FileUploadConstant;
import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.UUID;

/**
 * 通用接口
 */

@RestController
@RequestMapping("/admin/common")
@Api(tags = "通用接口")
@Slf4j
public class CommonController {

    @PostMapping("/upload")
    @ApiOperation("文件上传")
    public Result<String> upload(MultipartFile file) throws IOException {
        log.info("文件上传：{}", file);
        Set<String> extensions = Set.of("jpg", "png", "jpeg");
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            log.error("上传文件名不能为空");
            return Result.error("Uploaded filename is null");
        }
        String extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1);

        if (!extensions.contains(extension)) {
            log.error("上传文件类型不支持");
            return Result.error("Uploaded file type is not supported");
        }

        String newFilename = UUID.randomUUID() + "." + extension;
        String storePath = FileUploadConstant.FILE_UPLOAD_PATH + newFilename;

        // 储存文件
        file.transferTo(new File(storePath));



        return Result.success(FileUploadConstant.FILE_VISIT_PATH + newFilename);
    }

}
