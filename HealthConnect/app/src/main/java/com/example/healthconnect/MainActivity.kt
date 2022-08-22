package com.example.healthconnect

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.RemoteException
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.health.connect.client.permission.Permission
import androidx.health.connect.client.records.ExerciseEventRecord
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.DistanceRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.SpeedRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.healthconnect.lib.ExerciseSessionViewModel
import com.example.healthconnect.lib.ExerciseSessionViewModelFactory
import com.example.healthconnect.lib.HealthConnectAvailability
import com.example.healthconnect.lib.HealthConnectManager
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.io.IOException
import java.lang.Exception
import java.time.Duration
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import kotlin.random.Random

class MainActivity : AppCompatActivity() , View.OnClickListener{
    private var healthConnectAvailability:  MutableState<HealthConnectAvailability>? = null//MutableState<HealthConnectAvailability>
    private var isAvailable = false
    private var healthConnectManager: HealthConnectManager? = null
    private val permissions =
        setOf(
            Permission.createReadPermission(StepsRecord::class),
            Permission.createWritePermission(StepsRecord::class)
        )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        textRecord.setOnClickListener(this)
        textRecord.visibility = View.GONE
        healthConnectManager = (application as MyApplication).healthConnectManager
        if(healthConnectManager != null) {
            healthConnectAvailability = healthConnectManager!!.availability
            checkAvailability()
        }

    }

    var permissionsGranted = mutableStateOf(false)
        private set
//    @Composable
    private fun checkAvailability() {

         if(healthConnectAvailability != null ) {
            when (healthConnectAvailability?.value) {
                HealthConnectAvailability.INSTALLED -> {
                    textRecord.visibility = View.VISIBLE
                    isAvailable = true
                    textStatusMsg.text = " Health Connect is installed on this device. Use the menu to explore the sample, with each screen demonstrating a different aspect of Health Connect functionality."

//                    val viewModel: ExerciseSessionViewModel = viewModel(
//                        factory = ExerciseSessionViewModelFactory(
//                            healthConnectManager = healthConnectManager!!
//                        )
//                    )
                }
                HealthConnectAvailability.NOT_INSTALLED -> {
                    textStatusMsg.text = "Health Connect is supported on this device, but is not currently installed."
                }
                HealthConnectAvailability.NOT_SUPPORTED -> {
                    textStatusMsg.text = "Health Connect is only supported on Android devices running API level 26 or above."
                }
            }
        }

    }

    override fun onClick(p0: View?) {
        when(p0?.id) {
            R.id.textRecord -> {
//                Handler().post {

                GlobalScope.launch(Dispatchers.Main) {
                    try {
                        permissionsGranted.value =
                            healthConnectManager!!.hasAllPermissions(permissions)
                        if (permissionsGranted.value) recordData()
                        else{

                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }


                }

//                }

            }
        }
    }

    private suspend fun recordData(): Unit? {
        return GlobalScope.async(Dispatchers.IO) {
            // make network call
            // return user
            val startOfDay = ZonedDateTime.now().truncatedTo(ChronoUnit.DAYS)
            val latestStartOfSession = ZonedDateTime.now().minusMinutes(30)
            val offset = Random.nextDouble()

            // Generate random start time between the start of the day and (now - 30mins).
            val startOfSession = startOfDay.plusSeconds(
                (Duration.between(startOfDay, latestStartOfSession).seconds * offset).toLong()
            )
            val endOfSession = startOfSession.plusMinutes(30)
try{
    healthConnectManager?.writeExerciseSession(startOfSession, endOfSession)
} catch (e: Exception) {
    e.printStackTrace()
}

        }.await()

    }

//    private suspend fun tryWithPermissionsCheck(block: suspend () -> Unit) {
//        permissionsGranted.value = healthConnectManager.hasAllPermissions(permissions)
//        uiState = try {
//            if (permissionsGranted.value) {
//                block()
//            }
//            UiState.Done
//        } catch (remoteException: RemoteException) {
//            UiState.Error(remoteException)
//        } catch (securityException: SecurityException) {
//            UiState.Error(securityException)
//        } catch (ioException: IOException) {
//            UiState.Error(ioException)
//        } catch (illegalStateException: IllegalStateException) {
//            UiState.Error(illegalStateException)
//        }
//    }
}