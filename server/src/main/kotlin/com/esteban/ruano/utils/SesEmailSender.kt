package com.esteban.ruano.utils

import io.github.cdimascio.dotenv.Dotenv
import io.github.cdimascio.dotenv.dotenv
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.sesv2.SesV2Client
import software.amazon.awssdk.services.sesv2.model.EmailContent
import software.amazon.awssdk.services.sesv2.model.SendEmailRequest

class SesEmailSender : EmailSender {

    val dotenv: Dotenv = dotenv {
        ignoreIfMissing = true
    }

    private val from: String = dotenv["SES_FROM"] ?: "no-reply@example.com"
    val awsAccessKey = dotenv["AWS_ACCESS_KEY_ID"]
    val awsSecretKey = dotenv["AWS_SECRET_ACCESS_KEY"]
    val awsRegion = dotenv["AWS_REGION"] ?: "us-east-1"

    val sesClient = SesV2Client.builder()
        .region(Region.of(awsRegion))
        .credentialsProvider(
            StaticCredentialsProvider.create(
                AwsBasicCredentials.create(awsAccessKey, awsSecretKey)
            )
        )
        .build()

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