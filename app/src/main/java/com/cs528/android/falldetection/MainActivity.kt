package com.cs528.android.falldetection

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cs528.android.falldetection.databinding.ActivityMainBinding
import java.text.DecimalFormat
import java.util.*
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.math.abs


class MainActivity : AppCompatActivity(), SensorEventListener {

    /*val sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
    val sensor: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)*/

    private lateinit var binding : ActivityMainBinding

    private lateinit var sensorManager : SensorManager
    private var accelerometer: Sensor? = null

    private var accelerationReaderPast : Float = SensorManager.GRAVITY_EARTH
    private var accelerationReader : Float = SensorManager.GRAVITY_EARTH
    private var mAccel: Float = 0.0F

    private var movementStart: Long = 0

    private val mTimer = Timer()

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

    }

    override fun onSensorChanged(event: SensorEvent?) {
        when (event?.sensor?.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                movementStart = System.currentTimeMillis()

                val x = event.values[0]
                val y = event.values[1]
                val z = event.values[2]

                accelerationReaderPast = accelerationReader

                accelerationReader = sqrt(
                    x.toDouble().pow(2.0) + y.toDouble().pow(2.0) + z.toDouble().pow(2.0)
                ).toFloat()

                if(accelerationReader<0.5){
                    Log.d("FreeFall", accelerationReader.toString())
                    Toast.makeText(this,"free fall",Toast.LENGTH_SHORT).show()
                    mTimer.schedule(object : TimerTask() {
                        //start after 2 second delay to make acceleration values "rest"
                        override fun run() {
                            firstTimer.start()
                        }
                    }, 2000)
                }

                val precision = DecimalFormat("0.00")
                val ldAccRound = java.lang.Double.parseDouble(precision.format(accelerationReader))

                binding.allTheNumbers.text = getString(R.string.acc_value, ldAccRound.toString())

                //ldAccRound > 0.3 && ldAccRound < 1.2   0.3<ldAccRound<1.2

                /*if(ldAccRound>0.3 && ldAccRound < 1.2 && (movementStart - lastMovementFall) > 8000){
                    lastMovementFall = System.currentTimeMillis()
                    Toast.makeText(this,"Fall Detected",Toast.LENGTH_SHORT).show()
                }
                 */

                /*
                val delta: Float = accelerationReader - accelerationReaderPast
                mAccel = mAccel * 0.9f + delta
                mAccel = abs(mAccel)

                //binding.allTheOtherNumbers.text = getString(R.string.m_accel, mAccel.toString())

                if(mAccel>5.0f){
                    Toast.makeText(this,"Exceeded the acceleration, starting timer of 40s",Toast.LENGTH_SHORT).show()
                    Log.d("exceed",mAccel.toString())
                    mTimer.schedule(object : TimerTask() {
                        //start after 2 second delay to make acceleration values "rest"
                        override fun run() {
                            firstTimer.start()
                        }
                    }, 2000)
                }*/
            }
        }
    }

var firstTimer: CountDownTimer = object : CountDownTimer(30*1000, 1000) {
    //recovery timer
    override fun onTick(millisUntilFinished: Long) {
        //if there is movement before 30 seconds, cancel the timer
        val ms1 = millisUntilFinished/1000
        binding.allTheOtherNumbers.text = getString(R.string.timer , ms1.toString())
        //if (mAccel > 2.0f) {
        if(accelerationReader>11.0f){
            val toast = Toast.makeText(
                applicationContext,
                "You moved.", Toast.LENGTH_SHORT
            )
            toast.show()
            Log.d("Moved", accelerationReader.toString())
            cancel()
        }
    }

    override fun onFinish() {
        val toast = Toast.makeText(
            applicationContext,
            "Fall Detected!!", Toast.LENGTH_SHORT
        )
        toast.show()
        //secondTimer.start()
    }

}

var secondTimer: CountDownTimer = object : CountDownTimer((30 * 1000).toLong(), 1000) {
    //fall confirmation timer
    override fun onTick(millisUntilFinished: Long) {
        val ms = millisUntilFinished/1000
        binding.allTheOtherNumbers.text = getString(R.string.timer , ms.toString())
    }

    override fun onFinish() {
        val toast = Toast.makeText(
            applicationContext,
            "Fall Registered!", Toast.LENGTH_SHORT
        )
        toast.show()
        binding.allTheOtherNumbers.text = getString(R.string.timer , "Registered the fall")
    }
}


    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
        return
    }

    override fun onDestroy(){
        sensorManager.unregisterListener(this)
        super.onDestroy()
    }

}