package com.ketang.media.config;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MinioConfig {

    @Value("${minio.endpoint}")
    private String endPoint;
    @Value("${minio.accessKey}")
    private String accessKey;
    @Value("${minio.secretKey}")
    private String secretKey;

    @Bean
    public MinioClient minioClient(){
        // Create a minioClient with the MinIO server playground, its access key and secret key.
        MinioClient minioClient =
                MinioClient.builder()
                        .endpoint(endPoint)
                        .credentials(accessKey, secretKey)
                        .build();
        return minioClient;
    }
}
