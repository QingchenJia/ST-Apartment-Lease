package com.atguigu.lease.web.admin.service.impl;

import com.atguigu.lease.common.minio.MinioProperties;
import com.atguigu.lease.web.admin.service.FileService;
import io.minio.*;
import io.minio.errors.*;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

@Service
public class FileServiceImpl implements FileService {
    @Resource
    private MinioProperties minioProperties;

    @Resource
    private MinioClient minioClient;

    /**
     * 向MinIO服务器上传文件
     * <p>
     * 该方法负责将给定的文件上传到MinIO服务器的指定桶中如果桶不存在，则会创建新桶
     * 并设置桶的策略以公开访问上传的文件URL由endpoint、bucketName和fileName组成
     *
     * @param file 要上传的文件，类型为MultipartFile
     * @return 返回上传文件的URL地址
     * @throws ServerException           如果服务器发生错误
     * @throws InsufficientDataException 如果提供的数据不足
     * @throws ErrorResponseException    如果接收到错误的响应
     * @throws IOException               如果发生I/O错误
     * @throws NoSuchAlgorithmException  如果没有这样的算法
     * @throws InvalidKeyException       如果密钥无效
     * @throws InvalidResponseException  如果响应无效
     * @throws XmlParserException        如果XML解析失败
     * @throws InternalException         如果发生内部错误
     */
    @Override
    public String upload(MultipartFile file) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        // 获取MinIO服务器的端点和桶名称
        String endpoint = minioProperties.getEndpoint();
        String bucketName = minioProperties.getBucketName();

        // 检查桶是否存在
        boolean bucketExists = minioClient.bucketExists(
                BucketExistsArgs.builder()
                        .bucket(bucketName)
                        .build());

        // 如果桶不存在，创建新桶并设置策略
        if (!bucketExists) {
            minioClient.makeBucket(
                    MakeBucketArgs.builder()
                            .bucket(bucketName)
                            .build());

            minioClient.setBucketPolicy(
                    SetBucketPolicyArgs.builder()
                            .bucket(bucketName)
                            .config(createBucketPolicyConfig(bucketName))
                            .build());
        }

        // 生成文件名，包括日期、UUID和原始文件名，以确保唯一性和可组织性
        String fileName = new SimpleDateFormat("yyyy-MM-dd").format(new Date()) + "/"
                + UUID.randomUUID() + "-"
                + file.getOriginalFilename();

        // 将文件上传到MinIO服务器
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucketName)
                        .stream(file.getInputStream(), file.getSize(), -1)
                        .object(fileName)
                        .contentType(file.getContentType())
                        .build());

        // 返回上传文件的URL地址
        return String.join("/", endpoint, bucketName, fileName);
    }

    private String createBucketPolicyConfig(String bucketName) {
        return """
                {
                  "Statement" : [ {
                    "Action" : "s3:GetObject",
                    "Effect" : "Allow",
                    "Principal" : "*",
                    "Resource" : "arn:aws:s3:::%s/*"
                  } ],
                  "Version" : "2012-10-17"
                }
                """.formatted(bucketName);
    }
}
