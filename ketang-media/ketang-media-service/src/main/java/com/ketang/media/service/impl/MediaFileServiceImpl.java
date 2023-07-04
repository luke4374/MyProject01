package com.ketang.media.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import com.ketang.base.exception.KeTangException;
import com.ketang.base.model.PageParams;
import com.ketang.base.model.PageResult;
import com.ketang.media.mapper.MediaFilesMapper;
import com.ketang.media.model.dto.QueryMediaParamsDto;
import com.ketang.media.model.dto.UploadFileParamsDto;
import com.ketang.media.model.dto.UploadFileResultDto;
import com.ketang.media.model.po.MediaFiles;
import com.ketang.media.service.MediaFileService;
import io.minio.MinioClient;
import io.minio.UploadObjectArgs;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class MediaFileServiceImpl implements MediaFileService {

    @Autowired
    MediaFilesMapper mediaFilesMapper;
    @Autowired
    MinioClient minioClient;
    // 注入代理对象
    @Autowired
    MediaFileService currProxy;

    @Value("${minio.bucket.files}")
    private String bucket_media;
    @Value("${minio.bucket.videofiles}")
    private String bucket_video;

    @Override
    public PageResult<MediaFiles> queryMediaFiles(Long companyId, PageParams pageParams, QueryMediaParamsDto mediaParamsDto) {
        LambdaQueryWrapper<MediaFiles> lqw = new LambdaQueryWrapper<>();

        Page<MediaFiles> page = new Page<>(pageParams.getPageNo(), pageParams.getPageSize());
        Page<MediaFiles> pageResult = mediaFilesMapper.selectPage(page, lqw);
        // 获取数据列表，总数
        List<MediaFiles> fileList = pageResult.getRecords();
        long total = pageResult.getTotal();

        PageResult<MediaFiles> finalResult = new PageResult<>(fileList, total, pageParams.getPageNo(), pageParams.getPageSize());
        return finalResult;
    }

    // 根据文件扩展名获取文件类型
    private String getMimeType(String extension){
        if (extension == null) extension = "";
        // 通过扩展名得到媒体资源类型mimeType
        ContentInfo extensionMatch = ContentInfoUtil.findExtensionMatch(extension);
        String mimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        if (extensionMatch != null){
            mimeType = extensionMatch.getMimeType();
        }
        return mimeType;
    }

    // 上传近Minio中
    private boolean uploadToMinio(String bucketName, String localSavePath, String mimeType, String objName) {
        // Upload '/home/user/Photos/asiaphotos.zip' as object name 'asiaphotos-2015.zip' to bucket
        try {
            UploadObjectArgs uploadArgs = UploadObjectArgs.builder()
                    .bucket(bucketName) // 桶
                    .filename(localSavePath) // 本地文件路径
                    //              .object("ugh.pdf") // 桶中的存放位置与对象名
                    .contentType(mimeType)
                    .object(objName) // 包含子目录的存放形式
                    .build();
            minioClient.uploadObject(uploadArgs);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            log.error("上传文件出错, bucket:{}, objectName:{}, 错误信息:{}", bucketName, objName, e.getMessage());
        }
        return false;
    }

    private String getDefaultFolderPath() {
        //获取文件默认存储目录路径 年/月/日
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.format(new Date()).replace("-", "/")+"/";
    }

    //获取文件的md5
    private String getFileMd5(File file) {
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            String fileMd5 = DigestUtils.md5Hex(fileInputStream);
            return fileMd5;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * @description 将文件信息添加到文件表
     * @param companyId  机构id
     * @param fileMd5  文件md5值
     * @param fileParamsDto  上传文件的信息
     * @param bucket  桶
     * @param objectName 对象名称
     * @return com.ketang.media.model.po.MediaFiles
     */
    @Transactional
    public MediaFiles addMediaFilesToDb(Long companyId, String fileMd5,
                                        UploadFileParamsDto fileParamsDto,
                                        String bucket, String objectName){

        MediaFiles DB_Media = mediaFilesMapper.selectById(fileMd5);
        if (DB_Media == null){
            MediaFiles newMediaFile = new MediaFiles();
            BeanUtils.copyProperties(fileParamsDto, newMediaFile);
            // 设置字段
            newMediaFile.setId(fileMd5);
            newMediaFile.setCompanyId(companyId);
            newMediaFile.setBucket(bucket);
            newMediaFile.setFilePath(objectName);
            newMediaFile.setFileId(fileMd5);
            newMediaFile.setUrl("/" + bucket + "/" + objectName);
            newMediaFile.setCreateDate(LocalDateTime.now());
            newMediaFile.setStatus("1");
            newMediaFile.setAuditStatus("002003");
            // 插入
            int insert = mediaFilesMapper.insert(newMediaFile);
            if (insert <= 0){
                log.error("媒资service模块插入数据库错误, bucket:{}, objectName:{}", bucket, objectName);
                return null;
            }
            return newMediaFile;
        }else {
            return null;
        }
    }

    @Override
    public UploadFileResultDto uploadFile(Long companyId, UploadFileParamsDto fileParamsDto, String localSavePath) {
        //0. 处理文件扩展名等信息
        String filename = fileParamsDto.getFilename();
        String extension = null;
        if (filename.contains(".")){
            extension = filename.split("(?=\\.)")[1];
        }
        String mimeType = getMimeType(extension);
        // pdf/2023/07/04/MD5.pdf
        String fileMd5 = getFileMd5(new File(localSavePath));
        String objName = extension + "/" + getDefaultFolderPath() + fileMd5 + extension;
        //1. 文件上传minio
        boolean uploadResult = uploadToMinio(bucket_media, localSavePath, mimeType, objName);
        if (!uploadResult){
            KeTangException.throwExp("文件上传失败");
        }

        //2. 信息存入数据库
        /*
            事务的控制必须同时满足: 1. 必须由代理对象去执行事务方法
                                 2. 标注Transactional注解，在运行方法前开启事务，结束后提交事务
            !!非事务方法调用同类的事务方法，事务将无法控制!!
            解决方法：将本对象注入进来，把事务方法抽象到接口处
        */
        MediaFiles mediaFilesRes = currProxy.addMediaFilesToDb(companyId, fileMd5, fileParamsDto, bucket_media, objName);

        //3. 返回对象
        UploadFileResultDto returnFileResult = new UploadFileResultDto();
        if (mediaFilesRes == null) KeTangException.throwExp("媒资信息入库失败");
        BeanUtils.copyProperties(mediaFilesRes, returnFileResult);
        return returnFileResult;
    }
}
