package com.ketang.media.service;

import com.ketang.base.model.PageParams;
import com.ketang.base.model.PageResult;
import com.ketang.base.model.RestResponse;
import com.ketang.media.model.dto.QueryMediaParamsDto;
import com.ketang.media.model.dto.UploadFileParamsDto;
import com.ketang.media.model.dto.UploadFileResultDto;
import com.ketang.media.model.po.MediaFiles;

public interface MediaFileService {
    public PageResult<MediaFiles> queryMediaFiles(Long companyId, PageParams pageParams, QueryMediaParamsDto mediaParamsDto);

    /**
     * 上传文件
     * @param companyId 公司ID
     * @param fileParamsDto 文件基本信息 存入数据库
     * @param localSavePath 本地存放路径
     * @return
     */
    public UploadFileResultDto uploadFile(Long companyId, UploadFileParamsDto fileParamsDto, String localSavePath);

    public MediaFiles addMediaFilesToDb(Long companyId, String fileMd5, UploadFileParamsDto fileParamsDto,
                                        String bucket, String objectName);
    // 查询文件是否存在
    public RestResponse<Boolean> checkFiles(String fileMd5);
    // 查询分块是否存在
    public RestResponse<Boolean> checkChunk(String fileMd5, int chunkIndex);
    // 上传分块
    public RestResponse uploadChunk(String fileMd5, int chunkNum, String chunkFilePath);
    /**
     * @description 合并分块
     * @param companyId  机构id
     * @param fileMd5  文件md5
     * @param chunkTotal 分块总和
     * @param uploadFileParamsDto 文件信息
     */
    public RestResponse mergeChunks(Long companyId,String fileMd5,int chunkTotal,UploadFileParamsDto uploadFileParamsDto);

}
