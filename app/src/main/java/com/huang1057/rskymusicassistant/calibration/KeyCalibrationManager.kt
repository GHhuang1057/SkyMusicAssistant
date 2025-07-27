package com.huang1057.rskymusicassistant.calibration

import android.content.Context
import android.content.SharedPreferences
import android.graphics.Point
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.ArrayList
class KeyCalibrationManager(private val context: Context) {
    
    private val sharedPreferences: SharedPreferences = 
        context.getSharedPreferences("key_calibration", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val keyPositionListType = object : TypeToken<ArrayList<KeyPosition>>() {}.type
    
    data class KeyPosition(
        val note: Int,
        val x: Int,
        val y: Int,
        val width: Int = 80,
        val height: Int = 200
    )
    
    private val keyMappings = mapOf(
        "C4" to 60, "C#4" to 61, "D4" to 62, "D#4" to 63,
        "E4" to 64, "F4" to 65, "F#4" to 66, "G4" to 67,
        "G#4" to 68, "A4" to 69, "A#4" to 70, "B4" to 71,
        "C5" to 72, "C#5" to 73, "D5" to 74, "D#5" to 75,
        "E5" to 76, "F5" to 77, "F#5" to 78, "G5" to 79,
        "G#5" to 80, "A5" to 81, "A#5" to 82, "B5" to 83
    )
    
    fun saveKeyPosition(keyName: String, position: Point) {
        val note = keyMappings[keyName] ?: return
        val keyPosition = KeyPosition(note, position.x, position.y)
        
        val positions = getAllPositions().toMutableList()
        positions.removeAll { it.note == note }
        positions.add(keyPosition)
        
        savePositions(positions)
    }
    
    fun getKeyPosition(keyName: String): KeyPosition? {
        val note = keyMappings[keyName] ?: return null
        return getAllPositions().find { it.note == note }
    }
    
    fun getAllPositions(): List<KeyPosition> {
        val json = sharedPreferences.getString("positions", "[]")
        return gson.fromJson(json, keyPositionListType) ?: emptyList()
    }
    
    fun savePositions(positions: List<KeyPosition>) {
        val json = gson.toJson(positions)
        sharedPreferences.edit().putString("positions", json).apply()
    }
    
    fun clearAllPositions() {
        sharedPreferences.edit().remove("positions").apply()
    }
    
    fun getKeyNameByNote(note: Int): String {
        return keyMappings.entries.find { it.value == note }?.key ?: "Unknown"
    }
    
    fun getNoteByKeyName(keyName: String): Int? {
        return keyMappings[keyName]
    }
    
    fun isCalibrated(keyName: String): Boolean {
        return getKeyPosition(keyName) != null
    }
    
    fun getCalibrationProgress(): Int {
        val totalKeys = keyMappings.size
        val calibratedKeys = keyMappings.keys.count { isCalibrated(it) }
        return (calibratedKeys * 100 / totalKeys)
    }
    
    fun exportCalibrationData(): String {
        val positions = getAllPositions()
        return gson.toJson(positions)
    }
    
    fun importCalibrationData(json: String): Boolean {
        return try {
            val positions: ArrayList<KeyPosition>? = gson.fromJson(json, keyPositionListType)
            if (positions != null) {
                savePositions(positions)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }
}