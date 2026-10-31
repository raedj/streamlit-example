package com.polyglot.academy.android

import android.app.job.JobInfo
import android.app.job.JobParameters
import android.app.job.JobScheduler
import android.app.job.JobService
import android.content.ComponentName
import android.content.Context
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

/**
 * Background sync through the framework JobScheduler: about every 6 hours, only with a network (an
 * unmetered one unless the learner allows mobile data) and not when the battery is low. The job
 * survives reboots (RECEIVE_BOOT_COMPLETED is already declared for the reminder).
 */
class AutoSyncJob : JobService() {
    private var work: Job? = null

    override fun onStartJob(params: JobParameters): Boolean {
        val app = application as PolyglotApplication
        work = app.appScope.launch {
            runCatching { app.services.sync.autoSync() }
            jobFinished(params, false)
        }
        return true
    }

    /** The system withdrew the conditions (network lost, battery low): stop and try at the next period. */
    override fun onStopJob(params: JobParameters): Boolean {
        work?.cancel()
        return false
    }

    companion object {
        private const val ID = 4201
        private val PERIOD = TimeUnit.HOURS.toMillis(6)
        private val FLEX = TimeUnit.HOURS.toMillis(1)

        fun schedule(context: Context, enabled: Boolean, unmeteredOnly: Boolean) {
            val scheduler = context.getSystemService(JobScheduler::class.java) ?: return
            if (!enabled) { scheduler.cancel(ID); return }
            val network = if (unmeteredOnly) JobInfo.NETWORK_TYPE_UNMETERED else JobInfo.NETWORK_TYPE_ANY
            // Rescheduling an identical job would restart its period on every app start.
            scheduler.getPendingJob(ID)?.let { if (it.networkType == network && it.intervalMillis == PERIOD) return }
            scheduler.schedule(
                JobInfo.Builder(ID, ComponentName(context, AutoSyncJob::class.java))
                    .setRequiredNetworkType(network)
                    .setRequiresBatteryNotLow(true)
                    .setPeriodic(PERIOD, FLEX)
                    .setPersisted(true)
                    .build(),
            )
        }
    }
}
