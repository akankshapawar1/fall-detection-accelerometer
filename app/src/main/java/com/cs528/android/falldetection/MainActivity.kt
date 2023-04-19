package com.cs528.android.falldetection

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.cs528.android.falldetection.databinding.ActivityMainBinding
import java.text.DecimalFormat
import java.util.*
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt


class MainActivity : AppCompatActivity(), SensorEventListener {

    /*val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
    val sensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)*/

    private lateinit var binding : ActivityMainBinding

    private lateinit var sensorManager : SensorManager

    private var accelerometer: Sensor? = null
    private var gyroscope: Sensor? = null
    private var grav : Sensor? = null

    private var lastMovementFall: Long = 0
    private var movementStart: Long = 0
    private var loAccelerationReaderPast : Float = SensorManager.GRAVITY_EARTH
    private var loAccelerationReader : Float = SensorManager.GRAVITY_EARTH
    private var mAccel: Float = 0.0F
    private val mTimer = Timer()
    private val gravity = FloatArray(3)
    private val linear_acceleration = FloatArray(3)

    var fallen : Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupSensor()
    }

    private fun setupSensor() {
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager

        //register accelerometer
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

        accelerometer?.also {
            sensorManager.registerListener(this,
                it,
                SensorManager.SENSOR_DELAY_NORMAL,
                SensorManager.SENSOR_DELAY_NORMAL)
        }

        grav = sensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY)

        //register gyroscope
        /*gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

        gyroscope?.also {
            sensorManager.registerListener(this,
                it,
                SensorManager.SENSOR_DELAY_NORMAL,
                SensorManager.SENSOR_DELAY_NORMAL)
        }
         */

    }

    override fun onSensorChanged(event: SensorEvent?) {
        when (event?.sensor?.type) {
            Sensor.TYPE_ACCELEROMETER -> {

                for (i in 0..2) {
                    gravity[i] = (0.1 * event.values[i] + 0.9 * gravity[i]).toFloat()
                }

                movementStart = System.currentTimeMillis()

                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]

                loAccelerationReaderPast = loAccelerationReader

                /*
                //check if the values are being retrieved
                binding.allTheNumbers.text =
                    getString(R.string.all_the_numbers, x.toString(), y.toString(), z.toString())
                 */

                loAccelerationReader = sqrt(
                    x.toDouble().pow(2.0) + y.toDouble().pow(2.0) + z.toDouble().pow(2.0)
                ).toFloat()

                // This will print a number upto 8 decimal points
                // binding.allTheNumbers.text = getString(R.string.acc_sq_value, loAccelerationReader.toString())

                if(loAccelerationReader<1.0){
                    Log.d("FreeFall", loAccelerationReader.toString())
                }

                val precision = DecimalFormat("0.00")
                val ldAccRound = java.lang.Double.parseDouble(precision.format(loAccelerationReader))

                binding.allTheNumbers.text = getString(R.string.acc_sq_value, ldAccRound.toString())

                //ldAccRound > 0.3 && ldAccRound < 1.2   0.3<ldAccRound<1.2

                /*if(ldAccRound>0.3 && ldAccRound < 1.2 && (movementStart - lastMovementFall) > 8000){
                    lastMovementFall = System.currentTimeMillis()
                    Toast.makeText(this,"Fall Detected",Toast.LENGTH_SHORT).show()
                }
                 */


                //val delta: Float = loAccelerationReader - loAccelerationReaderPast
                //mAccel = mAccel * 0.9f + delta

                val alpha: Float = 0.8f

                // Isolate the force of gravity with the low-pass filter.
                gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0]
                gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1]
                gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2]

                // Remove the gravity contribution with the high-pass filter.
                linear_acceleration[0] = event.values[0] - gravity[0]
                linear_acceleration[1] = event.values[1] - gravity[1]
                linear_acceleration[2] = event.values[2] - gravity[2]

                mAccel += linear_acceleration[2]

                mAccel = java.lang.Double.parseDouble(precision.format(mAccel)).toFloat()
                mAccel = abs(mAccel)

                binding.allTheOtherNumbers.text = getString(R.string.acc_sq_value, mAccel.toString())

                if(mAccel>5.0f){
                    binding.fall.text = "YOU FELL"
                    startCountdown()

                    //Toast.makeText(this,"Exceeded the acceleration, starting timer of 2s",Toast.LENGTH_SHORT).show()
                    mTimer.schedule(object : TimerTask() {
                        //start after 2 second delay to make acceleration values "rest"
                        override fun run() {
                            //firstTimer.start()
                        }
                    }, 2000)
                }
                else{
                    binding.fall.text = "STABLE"
                }
            }

            /*Sensor.TYPE_GYROSCOPE -> {
                val gyro_x = event.values[0]
                val gyro_y = event.values[1]
                val gyro_z = event.values[2]

                binding.allTheOtherNumbers.text =
                    getString(R.string.all_the_other_numbers, gyro_x.toString(), gyro_y.toString(), gyro_z.toString())
            }
             */


        }


    }
    fun startCountdown(): Unit
    {
        val timer = object: CountDownTimer(60000, 1000) {
            override fun onTick(p0: Long) {
                binding.timerText.text = "Time Left: $p0"
            }
            override fun onFinish() {
                fallen = true
            }
        }
        timer.start()
    }
            /*
    var firstTimer: CountDownTimer = object : CountDownTimer(40*1000, 1000) {
        //fall registration timer
        override fun onTick(millisUntilFinished: Long) {
            //if there is movement before 30 seconds pass cancel the timer
            if (mAccel > 4.0f) {
                val toast = Toast.makeText(
                    applicationContext,
                    "Fall not Detected, you moved.", Toast.LENGTH_SHORT
                )
                toast.show()
                cancel()
            }
        }

        override fun onFinish() {
            val toast = Toast.makeText(
                applicationContext,
                "Fall Detected,registering after 30 seconds", Toast.LENGTH_SHORT
            )
            toast.show()
            secondTimer.start()
        }
    }

    var secondTimer: CountDownTimer = object : CountDownTimer((30 * 1000).toLong(), 1000) {
        //fall confirmation timer
        override fun onTick(millisUntilFinished: Long) {
            binding.allTheOtherNumbers.text = getString(R.string.newValue , millisUntilFinished.toString())
        }

        override fun onFinish() {
            val toast = Toast.makeText(
                applicationContext,
                "Fall Registered!", Toast.LENGTH_SHORT
            )
            toast.show()
        }
    }
    */


    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
        return
    }

    override fun onDestroy(){
        sensorManager.unregisterListener(this)
        super.onDestroy()
    }

}