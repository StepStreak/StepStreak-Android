package com.stepstreak.dev.googleFit

import android.icu.util.Calendar
import android.util.Log
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.HistoryClient
import com.google.android.gms.fitness.data.DataSet
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.request.DataReadRequest
import com.google.android.gms.fitness.result.DataReadResponse
import com.google.android.gms.tasks.Tasks
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit

class GoogleFitManager(private val activity: FragmentActivity, private val fitnessOptions: FitnessOptions) {

    private lateinit var googleFitApiManager: GoogleFitApiManager

    suspend fun accessGoogleFit() {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startTime = calendar.timeInMillis
        val endTime = System.currentTimeMillis()

        val account = GoogleSignIn.getAccountForExtension(activity, fitnessOptions)
        val historyClient: HistoryClient = Fitness.getHistoryClient(activity, account)

        val dataTypes = listOf(
            DataType.TYPE_STEP_COUNT_DELTA,
            DataType.TYPE_DISTANCE_DELTA,
            DataType.TYPE_CALORIES_EXPENDED
        )

        val responses = mutableListOf<DataReadResponse>()

        for (dataType in dataTypes) {
            val readRequest = DataReadRequest.Builder()
                .read(dataType)
                .bucketByTime(1, TimeUnit.DAYS)
                .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
                .build()

            try {
                val response = withContext(Dispatchers.IO) {
                    Tasks.await(historyClient.readData(readRequest))
                }
                responses.add(response)
            } catch (e: Exception) {
                Log.e("GoogleFit", "Error accessing Google Fit data: ${e.message} $e ${e.cause}")
            }
        }

        val aggregateRequest = DataReadRequest.Builder()
            .aggregate(DataType.TYPE_HEART_RATE_BPM)
            .bucketByTime(1, TimeUnit.DAYS)
            .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
            .build()

        try {
            val response = withContext(Dispatchers.IO) {
                Tasks.await(historyClient.readData(aggregateRequest))
            }
            responses.add(response)
        } catch (e: Exception) {
            Log.e("GoogleFit", "Error accessing Google Fit data: ${e.message} $e ${e.cause}")
        }

        processFitnessData(responses)
    }

    private suspend fun processFitnessData(responses: List<DataReadResponse>) {
        googleFitApiManager = GoogleFitApiManager(activity)

        val filteredDataSets = mutableListOf<DataSet>()

        for (response in responses) {
            if (response.buckets.isNotEmpty()) {
                for (bucket in response.buckets) {
                    for (dataSet in bucket.dataSets) {
                        val filteredDataPoints = dataSet.dataPoints.filter { dataPoint ->
                            !dataPoint.originalDataSource.streamName.contains("user_input")
                        }
                        if (filteredDataPoints.isNotEmpty()) {
                            val filteredDataSet = DataSet.builder(dataSet.dataSource)
                                .addAll(filteredDataPoints)
                                .build()
                            filteredDataSets.add(filteredDataSet)
                        }
                    }
                }
            }
        }

        withContext(Dispatchers.IO) {
            googleFitApiManager.sendPostRequest(filteredDataSets)
        }
    }
}