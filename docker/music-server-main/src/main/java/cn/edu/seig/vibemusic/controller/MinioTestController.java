package cn.edu.seig.vibemusic.controller;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import io.minio.messages.Bucket; // 修改导入路径
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/test")
public class MinioTestController {

    @Value("${minio.endpoint}")
    private String endpoint;

    @Value("${minio.accessKey}")
    private String accessKey;

    @Value("${minio.secretKey}")
    private String secretKey;

    @GetMapping("/minio")
    public ResponseEntity<String> testMinioConnection() {
        try {
            MinioClient minioClient = MinioClient.builder()
                    .endpoint(endpoint)
                    .credentials(accessKey, secretKey)
                    .build();
            List<Bucket> bucketList = minioClient.listBuckets();
            return ResponseEntity.ok("Connected to MinIO successfully! Buckets: " + bucketList.size());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to connect to MinIO: " + e.getMessage());
        }
    }
}
