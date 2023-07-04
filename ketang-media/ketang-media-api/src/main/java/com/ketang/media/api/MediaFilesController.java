package com.ketang.media.api;

import com.ketang.base.model.PageParams;
import com.ketang.base.model.PageResult;
import com.ketang.media.model.dto.QueryMediaParamsDto;
import com.ketang.media.model.dto.UploadFileParamsDto;
import com.ketang.media.model.dto.UploadFileResultDto;
import com.ketang.media.model.po.MediaFiles;
import com.ketang.media.service.MediaFileService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
@Api(value = "媒资文件管理接口",tags = "媒资文件管理接口")
@RestController
public class MediaFilesController {


    @Autowired
    MediaFileService mediaFileService;
    Long companyId = 1232141425L;

    @ApiOperation("媒资列表查询")
    @PostMapping("/files")
    public PageResult<MediaFiles> queryFiles(PageParams pageParams,
                                             @RequestBody QueryMediaParamsDto mediaParamsDto){
        return mediaFileService.queryMediaFiles(companyId, pageParams, mediaParamsDto);
    }

    @ApiOperation("上传图片")
    @RequestMapping(value = "/upload/coursefile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UploadFileResultDto uploadFile(@RequestPart("filedata")MultipartFile filedata) throws IOException {
        // 创建临时文件，获取文件属性
        File tempFile = File.createTempFile("minio", ".temp");
        filedata.transferTo(tempFile);
        // 文件路径获取
        String localPath = tempFile.getAbsolutePath();
        // 文件上传信息
        UploadFileParamsDto fileParams = new UploadFileParamsDto();
        fileParams.setFilename(filedata.getOriginalFilename());
        fileParams.setFileSize(filedata.getSize());
        fileParams.setFileType("001001");

        return mediaFileService.uploadFile(companyId, fileParams, localPath);
    }
}
