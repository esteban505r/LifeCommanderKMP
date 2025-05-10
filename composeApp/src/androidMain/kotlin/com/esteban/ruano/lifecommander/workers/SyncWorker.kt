package com.esteban.ruano.lifecommander.workers

import androidx.work.ListenableWorker

interface SyncWorker {
    fun doWork(): ListenableWorker.Result
    fun sync(): ListenableWorker.Result
}