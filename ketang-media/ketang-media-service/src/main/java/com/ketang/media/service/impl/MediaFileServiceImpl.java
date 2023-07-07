package com.ketang.media.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.j256.simplemagic.ContentInfo;
import com.j256.simplemagic.ContentInfoUtil;
import com.ketang.base.exception.KeTangException;
import com.ketang.base.model.PageParams;
import com.ketang.base.model.PageResult;
import com.ketang.base.model.RestResponse;
import com.ketang.media.mapper.MediaFilesMapper;
import com.ketang.media.model.dto.QueryMediaParamsDto;
import com.ketang.media.model.dto.UploadFileParamsDto;
import com.ketang.media.model.dto.UploadFileResultDto;
import com.ketang.media.model.po.MediaFiles;
import com.ketang.media.service.MediaFileService;
import io.minio.*;
import io.minio.errors.*;
import io.minio.messages.DeleteError;
import io.minio.messages.DeleteObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
        String mimeType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
        // 通过扩展名得到媒体资源类型mimeType
        ContentInfo extensionMatch = ContentInfoUtil.findExtensionMatch(extension);
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

    private String getChunkFileFolder(String fileMd5, int isChunk){
        return isChunk == 0 ? fileMd5.charAt(0) + "/" + fileMd5.charAt(1) + "/" + fileMd5 + "/":
                              fileMd5.charAt(0) + "/" + fileMd5.charAt(1) + "/" + fileMd5 + "/chunk/" ;
    }

    private File downLoadFromMinIO(String bucket, String objName){
        File minioMerge = null;
        FileOutputStream fos = null;

        try {
            // 从MinIO中获取流
            InputStream inputStream = minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucket).object(objName).build());
            minioMerge = File.createTempFile("minio", ".merge");
            fos = new FileOutputStream(minioMerge);
            IOUtils.copy(inputStream, fos);
            return minioMerge;
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if (fos!=null){
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    private void clearMinIOChunk(String chunkFilePath, int chunkTotal){
        Iterable<DeleteObject> objs = Stream.iterate(0, i -> ++i).limit(chunkTotal)
                                            .map(i -> new DeleteObject(chunkFilePath + i))
                                            .collect(Collectors.toList());
        RemoveObjectsArgs removeObjs = RemoveObjectsArgs.builder().bucket(bucket_video).objects(objs).build();
        Iterable<Result<DeleteError>> results = minioClient.removeObjects(removeObjs);
        // 遍历删除
        results.forEach(file -> {
            try {
                file.get(); // 删除
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void clearLocalTemp(String localPath){
        File List = new File(localPath);
        File[] files = List.listFiles();
        for (File file : files) {
            if (file.isDirectory()) continue;
            if (file.getAbsolutePath().contains("minio")){
                if (file.getAbsolutePath().endsWith(".temp") || file.getAbsolutePath().endsWith(".merge")){
                    if (file.delete()){
                        System.out.println("[[[[成功删除本地缓存文件："+file.getAbsolutePath()+"]]]]");
                    }
                }
            }
        }
    }
    /**
     * @description 将文件信息添加到文件表
     * @param companyId  机构id
     * @param fileMd5  文件md5值
     * @param fileParamsDto  上传文件的信息
     * @param bucket  桶
     * @param objectName 对象名称
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
    public RestResponse<Boolean> checkFiles(String fileMd5) {
        // 先查询数据库文件是否存在
        MediaFiles mediaFiles = mediaFilesMapper.selectById(fileMd5);
        if (mediaFiles != null){
            // 若存在则查询minio是否存在
            String bucket = mediaFiles.getBucket();
            String objName = mediaFiles.getFilePath();

            GetObjectArgs getObjArgs = GetObjectArgs.builder().bucket(bucket).object(objName).build();

            try {
                FilterInputStream inputStream = minioClient.getObject(getObjArgs);
                if (inputStream != null){
                    return RestResponse.success(true);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        // 文件不存在
        return RestResponse.success(false);
    }

    @Override
    public RestResponse<Boolean> checkChunk(String fileMd5, int chunkIndex) {
        // 根据md5获取minio中的存储路径
        String chunkPath = getChunkFileFolder(fileMd5, 1) + chunkIndex;

        GetObjectArgs getObjArgs = GetObjectArgs.builder()
                                                .bucket(bucket_video)
                                                .object(chunkPath)
                                                .build();
        try {
            FilterInputStream inputStream = minioClient.getObject(getObjArgs);
            System.out.println(inputStream);
            if (inputStream != null){
                return RestResponse.success(true);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 文件不存在
        return RestResponse.success(false);
    }

    @Override
    public RestResponse uploadChunk(String fileMd5, int chunkNum, String chunkFilePath) {
        String objName = getChunkFileFolder(fileMd5, 1) + chunkNum;
        // 将分块文件上传至MinIo
        String mimeType = getMimeType(null);
        boolean minio_res = uploadToMinio(bucket_video, chunkFilePath, mimeType, objName);
        if (!minio_res){
            return RestResponse.validfail(false, "上传分块文件失败");
        }
        return RestResponse.success(true);
    }

    @Override
    public RestResponse mergeChunks(Long companyId, String fileMd5, int chunkTotal, UploadFileParamsDto uploadFileParamsDto) {
        // 找到MinIO中的分块并进行合并
        List<ComposeSource> sources = Stream.iterate(0, i -> ++i).limit(chunkTotal)
                .map(i -> ComposeSource
                            .builder()
                            .bucket(bucket_video)
                            .object(getChunkFileFolder(fileMd5, 1) + i)
                            .build())
                .collect(Collectors.toList());
        String fileName = getChunkFileFolder(fileMd5, 0) + fileMd5 + uploadFileParamsDto.getFilename().split("(?=\\.)")[1];
        // 文件合并
        ComposeObjectArgs objectArgs = ComposeObjectArgs.builder()
                .bucket(bucket_video)
                .object(fileName)
                .sources(sources) // 指定源文件
                .build();
        try {
            minioClient.composeObject(objectArgs);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("合并文件出错, bucket:{}, objName:{}, 错误信息:{}", bucket_video, fileName, e.getMessage());
            return RestResponse.validfail(false, "合并文件出错");
        }
        // 校验合并后的文件的MD5值
        File mergedFile = downLoadFromMinIO(bucket_video, fileName);
        try(FileInputStream inputStream = new FileInputStream(mergedFile)){
            String newFileMd5 = DigestUtils.md5Hex(inputStream);
            // 比较原始与新的md5值
            if (!fileMd5.equals(newFileMd5)){
                log.error("校验合并文件与原始文件不一致,Original: {}, NEW: {}", fileMd5, newFileMd5);
                return RestResponse.validfail(false, "文件校验出错,文件不匹配");
            }
            uploadFileParamsDto.setFileSize(Integer.toUnsignedLong(newFileMd5.length()));
        }catch (Exception e){
            e.printStackTrace();
            return RestResponse.validfail(false, "文件校验出错,文件不匹配");
        }
        // 文件信息保存入库
        MediaFiles mediaFiles = currProxy.addMediaFilesToDb(companyId, fileMd5, uploadFileParamsDto, bucket_video, fileName);
        if (mediaFiles == null) RestResponse.validfail(false, "文件入库失败");
        // 清理MinIO分块文件
        clearMinIOChunk(getChunkFileFolder(fileMd5, 1), chunkTotal);
//        clearLocalTemp("C:\\Users\\40223\\AppData\\Local\\Temp\\");
        return RestResponse.success(true);
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
