package com.stepstreak.dev.googleFit

import android.app.Activity
import android.util.Log
import android.widget.Toast
import com.google.android.gms.fitness.data.DataSet
import com.google.android.gms.fitness.data.Field
import com.stepstreak.dev.BuildConfig
import com.stepstreak.dev.util.DataStoreManager
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import org.json.JSONArray
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class GoogleFitApiManager(private val activity: Activity) {

    private val dataStoreManager = DataStoreManager(activity)

    private fun createJsonData(dataSets: List<DataSet>): String {
        val activityMap = mutableMapOf<String, JSONObject>()
        val allowedFields = listOf(Field.FIELD_STEPS.name, Field.FIELD_DISTANCE.name, Field.FIELD_AVERAGE.name, Field.FIELD_MAX.name, Field.FIELD_CALORIES.name)
        for (dataSet in dataSets) {
            for (dataPoint in dataSet.dataPoints) {
                val startDate = dataPoint.getStartTime(TimeUnit.MILLISECONDS)
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
                val date = dateFormat.format(Date(startDate))
                val activityObject = activityMap.getOrPut(date) { JSONObject().put("date", date) }
                for (field in dataPoint.dataType.fields) {
                    if (field.name in allowedFields) {
                        val value = when (field.name) {
                            Field.FIELD_STEPS.name -> dataPoint.getValue(field).asInt()
                            Field.FIELD_DISTANCE.name -> dataPoint.getValue(field).asFloat()
                            Field.FIELD_AVERAGE.name -> dataPoint.getValue(field).asFloat()
                            Field.FIELD_MAX.name -> dataPoint.getValue(field).asFloat()
                            Field.FIELD_CALORIES.name -> dataPoint.getValue(field).asFloat()
                            else -> 0f
                        }
                        val key = when (field.name) {
                            Field.FIELD_STEPS.name -> "steps"
                            Field.FIELD_DISTANCE.name -> "distance"
                            Field.FIELD_AVERAGE.name -> "heart_rate"
                            Field.FIELD_MAX.name -> "max_heart_rate"
                            Field.FIELD_CALORIES.name -> "calories"
                            else -> field.name
                        }
                        if (activityObject.has(key)) {
                            val existingValue = activityObject.getDouble(key)
                            activityObject.put(key, existingValue + value.toFloat())
                        } else {
                            activityObject.put(key, value)
                        }
                    }
                }
            }
        }
        val activityArray = JSONArray()
        for ((_, value) in activityMap) {
            activityArray.put(value)
        }
        val root = JSONObject()
        root.put("activity", activityArray)
        return root.toString()
    }

    fun sendPostRequest(dataSets: List<DataSet>) {
        val jsonData = createJsonData(dataSets)
        val client = OkHttpClient()
        val url = BuildConfig.BASE_URL + "api/activities"
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val body = jsonData.toRequestBody(mediaType)
        val headers = okhttp3.Headers.Builder()
            .add("Content-Type", "application/json")
            .add("Authorization", "Bearer ${dataStoreManager.getToken()}")
            .build()

        val request = Request.Builder()
            .url(url)
            .post(body)
            .headers(headers)
            .build()

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful){
                when(response.code) {
                    401 -> activity.runOnUiThread {
                        Toast.makeText(activity, "Token expired. Please refresh the page.", Toast.LENGTH_LONG).show()
                    }
                    500 -> activity.runOnUiThread {
                        Toast.makeText(activity, "Could not connect to server", Toast.LENGTH_LONG).show()
                    }
                    else -> activity.runOnUiThread {
                        Toast.makeText(activity, "Error: ${response.code}", Toast.LENGTH_LONG).show()
                    }
                }

            }
            Log.i("GoogleFit", "Data sent to API: ${response.body?.string()}")
        }
    }
}