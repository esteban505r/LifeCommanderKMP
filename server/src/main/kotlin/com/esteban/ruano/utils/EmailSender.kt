package com.esteban.ruano.utils

interface EmailSender {
    fun send(to: String, subject: String, htmlBody: String,textBody: String?)
}
