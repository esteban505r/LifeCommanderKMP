package com.esteban.ruano.service

import io.github.cdimascio.dotenv.Dotenv
import io.github.cdimascio.dotenv.dotenv
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.regions.providers.DefaultAwsRegionProviderChain
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import java.io.File
import java.util.*

class StudyImageService : BaseService() {

    // Load .env if present (dev). In prod this file typically won't exist.
    private val dotenv = dotenv {
        ignoreIfMissing = true
    }

    // ---- Credentials strategy ------------------------------------------------
    private val credentialsProvider = run {
        val envKey = dotenv["AWS_ACCESS_KEY_ID"]
        val envSecret = dotenv["AWS_SECRET_ACCESS_KEY"]

        if (!envKey.isNullOrBlank() && !envSecret.isNullOrBlank()) {
            StaticCredentialsProvider.create(
                AwsBasicCredentials.create(envKey, envSecret)
            )
        } else {
            DefaultCredentialsProvider.builder().build()
        }
    }

    // ---- Region resolution ---------------------------------------------------
    private val resolvedRegion: Region = run {
        val fromDotenv = dotenv["AWS_REGION"]
        if (!fromDotenv.isNullOrBlank()) {
            Region.of(fromDotenv)
        } else {
            DefaultAwsRegionProviderChain.builder().build().region ?: Region.US_EAST_1
        }
    }

    // ---- Bucket --------------------------------------------------------------
    private val bucketName: String = run {
        dotenv["S3_BUCKET_NAME"]
            ?: System.getenv("S3_BUCKET_NAME")
            ?: "estebanruanoposts"
    }

    // ---- S3 client (single instance) ----------------------------------------
    private val s3: S3Client by lazy {
        S3Client.builder()
            .credentialsProvider(credentialsProvider)
            .region(resolvedRegion)
            .build()
    }

    /**
     * Upload an icon image to S3 and return the public URL
     * @param file The image file to upload
     * @param userId The user ID for organizing files
     * @param fileName Optional custom file name, otherwise uses UUID
     * @return The S3 URL of the uploaded image
     */
    fun uploadIcon(file: File, userId: Int, fileName: String? = null): String {
        val fileExtension = file.extension.ifBlank { "png" }
        val s3Key = fileName ?: "study-icons/${userId}/${UUID.randomUUID()}.${fileExtension}"
        
        val putObjectRequest = PutObjectRequest.builder()
            .bucket(bucketName)
            .key(s3Key)
            .contentType("image/${fileExtension}")
            .build()
        
        s3.putObject(putObjectRequest, RequestBody.fromFile(file))
        
        // Return the S3 URL (adjust based on your S3 bucket configuration)
        // If using CloudFront or custom domain, adjust accordingly
        return "https://${bucketName}.s3.${resolvedRegion.id()}.amazonaws.com/${s3Key}"
    }
}

