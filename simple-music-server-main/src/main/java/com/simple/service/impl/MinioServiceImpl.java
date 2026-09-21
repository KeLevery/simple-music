package com.simple.service.impl;

import com.simple.constant.MessageConstant;
import com.simple.service.MinioService;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;

@Service
public class MinioServiceImpl implements MinioService {
    @Value("${minio.bucket}")
    private String bucketName;

    @Value("${minio.endpoint}")
    private String endpoint;

    private final MinioClient minioClient;

    // 构造函数
    public MinioServiceImpl(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    /**
     * 上传文件
     * @param file   要上传的文件
     * @param folder 存储文件的目录
     * @return
     */
    @Override
    public String uploadFile(MultipartFile file, String folder) {
        try {
            // 生成文件名
            String fileName = folder +"/" + UUID.randomUUID()+"-"+file.getOriginalFilename();
            // 获取文件流
            InputStream inputStream = file.getInputStream();
            // 上传文件
            minioClient.putObject(
                PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(fileName)
                    .stream(inputStream, inputStream.available(), -1)
                    .contentType(file.getContentType())
                    .build()
            );

            return endpoint + "/" + bucketName + "/" + fileName;
        } catch (Exception e){
            throw new RuntimeException(MessageConstant.FILE_UPLOAD + MessageConstant.FAILED + "：" + e.getMessage());
        }
    }

    /**
     * 删除文件
     *
     * @param fileUrl 文件访问地址
     */
    @Override
    public void deleteFile(String fileUrl) {
        try {
            // 获取文件名
            String filePath = fileUrl.replace(endpoint + "/" + bucketName + "/", "");

            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(filePath)
                            .build()
            );
        }catch (Exception e){
            throw new RuntimeException("文件删除失败: " + e.getMessage());
        }
    }
}
