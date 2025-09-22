package com.esteban.ruano.utils

import io.github.cdimascio.dotenv.Dotenv
import io.github.cdimascio.dotenv.dotenv
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.sesv2.SesV2Client
import software.amazon.awssdk.services.sesv2.model.EmailContent
import software.amazon.awssdk.services.sesv2.model.SendEmailRequest


class SesEmailSender : EmailSender {

    // Load environment variables from .env for local development, otherwise use IAM role in production
    private val dotenv: Dotenv = dotenv { ignoreIfMissing = true }

    private val from: String = dotenv["SES_FROM"] ?: "no-reply@example.com"

    // Detect if running in production (EC2 with IAM)
    private val isProduction = System.getenv("AWS_REGION") != null

    // Use IAM credentials when in production, else fall back to .env for local
    private val sesClient = if (isProduction) {
        SesV2Client.builder()
            .region(Region.of(dotenv["AWS_REGION"] ?: "us-east-1"))
            .credentialsProvider(DefaultCredentialsProvider.create()) // IAM role credentials
            .build()
    } else {
        // Fallback to .env credentials for local development
        val awsAccessKey = dotenv["AWS_ACCESS_KEY_ID"]
        val awsSecretKey = dotenv["AWS_SECRET_ACCESS_KEY"]

        if (awsAccessKey == null || awsSecretKey == null) {
            throw IllegalArgumentException("AWS Access and Secret keys are required for local development.")
        }

        SesV2Client.builder()
            .region(Region.of(dotenv["AWS_REGION"] ?: "us-east-1"))
            .credentialsProvider(
                StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(awsAccessKey, awsSecretKey)
                )
            )
            .build()
    }

    override fun send(to: String, subject: String, htmlBody: String, textBody: String?) {
        val content = EmailContent.builder()
            .simple {
                it.subject { s -> s.data(subject) }
                it.body { b ->
                    if (!textBody.isNullOrBlank()) b.text { t -> t.data(textBody) }
                    b.html { h -> h.data(htmlBody) }
                }
            }.build()

        val request = SendEmailRequest.builder()
            .fromEmailAddress(from)
            .destination { d -> d.toAddresses(to) }
            .content(content)
            .build()

        sesClient.sendEmail(request)
    }
}
