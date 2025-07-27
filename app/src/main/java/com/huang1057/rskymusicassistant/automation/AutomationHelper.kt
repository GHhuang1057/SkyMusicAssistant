package com.huang1057.rskymusicassistant.automation

import android.content.Context
import android.graphics.Point
import android.os.SystemClock
import android.view.InputDevice
import android.view.MotionEvent
import androidx.compose.runtime.mutableStateMapOf
import com.huang1057.rskymusicassistant.calibration.KeyCalibrationManager
import rikka.shizuku.Shizuku
import rikka.shizuku.ShizukuBinderWrapper
import rikka.shizuku.SystemServiceHelper
import kotlinx.coroutines.*

class AutomationHelper(
    private val context: Context,
    private val calibrationManager: KeyCalibrationManager
) {
    
    private var isPlaying = false
    private var currentJob: Job? = null
    private val pressedKeys = mutableStateMapOf<Int, Boolean>()
    
    data class PlayNote(
        val note: Int,
        val velocity: Int = 100,
        val duration: Long = 500
    )
    
    fun startPlayback(notes: List<PlayNote>, onProgress: (Int) -> Unit = {}) {
        if (isPlaying) stopPlayback()
        
        if (!hasShizukuPermission()) {
            throw SecurityException("Shizuku权限未授予")
        }
        
        isPlaying = true
        currentJob = CoroutineScope(Dispatchers.Default).launch {
            try {
                notes.forEachIndexed { index, playNote ->
                    if (!isActive) return@forEachIndexed
                    
                    val keyPosition = calibrationManager.getAllPositions()
                        .find { it.note == playNote.note }
                    
                    keyPosition?.let { pos ->
                        tapKey(pos)
                        delay(playNote.duration)
                        releaseKey(pos)
                    }
                    
                    onProgress((index + 1) * 100 / notes.size)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isPlaying = false
                pressedKeys.clear()
            }
        }
    }
    
    fun stopPlayback() {
        isPlaying = false
        currentJob?.cancel()
        currentJob = null
        pressedKeys.clear()
    }
    
    fun isPlaying(): Boolean = isPlaying
    
    private suspend fun tapKey(keyPosition: KeyCalibrationManager.KeyPosition) {
        withContext(Dispatchers.IO) {
            val x = keyPosition.x + keyPosition.width / 2
            val y = keyPosition.y + keyPosition.height / 2
            
            performClick(x, y)
            pressedKeys[keyPosition.note] = true
        }
    }
    
    private suspend fun releaseKey(keyPosition: KeyCalibrationManager.KeyPosition) {
        withContext(Dispatchers.IO) {
            pressedKeys.remove(keyPosition.note)
        }
    }
    
    private fun performClick(x: Int, y: Int) {
        try {
            val downTime = SystemClock.uptimeMillis()
            val eventTime = SystemClock.uptimeMillis()
            
            val downEvent = MotionEvent.obtain(
                downTime, eventTime,
                MotionEvent.ACTION_DOWN,
                x.toFloat(), y.toFloat(),
                1.0f, 1.0f, 0, 1.0f, 1.0f,
                InputDevice.SOURCE_TOUCHSCREEN, 0
            )
            
            val upEvent = MotionEvent.obtain(
                downTime, eventTime + 50,
                MotionEvent.ACTION_UP,
                x.toFloat(), y.toFloat(),
                1.0f, 1.0f, 0, 1.0f, 1.0f,
                InputDevice.SOURCE_TOUCHSCREEN, 0
            )
            
            // 使用Shizuku执行点击
            val inputManager = SystemServiceHelper.getSystemService("input")
            val inputManagerClass = Class.forName("android.hardware.input.IInputManager")
            val injectInputMethod = inputManagerClass.getMethod(
                "injectInputEvent",
                MotionEvent::class.java,
                Int::class.java
            )
            
            injectInputMethod.invoke(inputManager, downEvent, 0)
            injectInputMethod.invoke(inputManager, upEvent, 0)
            
            downEvent.recycle()
            upEvent.recycle()
            
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    fun hasShizukuPermission(): Boolean {
        return try {
            Shizuku.checkSelfPermission() == android.content.pm.PackageManager.PERMISSION_GRANTED
        } catch (e: Exception) {
            false
        }
    }
    
    fun getPressedKeys(): Map<Int, Boolean> = pressedKeys.toMap()
    
    fun testKey(keyName: String) {
        val note = calibrationManager.getNoteByKeyName(keyName) ?: return
        val position = calibrationManager.getKeyPosition(keyName) ?: return
        
        CoroutineScope(Dispatchers.IO).launch {
            tapKey(position)
            delay(200)
            releaseKey(position)
        }
    }
    
    fun getCalibrationStatus(): Map<String, Boolean> {
        val keyMappings = listOf("C4", "D4", "E4", "F4", "G4", "A4", "B4", "C5")
        return keyMappings.associateWith { calibrationManager.isCalibrated(it) }
    }
}