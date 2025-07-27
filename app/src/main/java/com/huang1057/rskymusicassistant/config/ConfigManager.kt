// Copyright (C) 2025 huang1057  
// SPDX-License-Identifier: GPL-3.0-or-later  
package com.huang1057.rskymusicassistant.config

import android.content.Context
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import org.xmlpull.v1.XmlSerializer
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

class ConfigManager(private val context: Context) {
    
    data class Config(
        val keyMappings: Map<String, Int>,
        val settings: Map<String, String>
    )
    
    fun saveConfig(config: Config, fileName: String = "config.xml") {
        val file = File(context.filesDir, fileName)
        val outputStream = FileOutputStream(file)
        
        val serializer: XmlSerializer = XmlPullParserFactory.newInstance().newSerializer()
        serializer.setOutput(outputStream, "UTF-8")
        
        serializer.startDocument(null, true)
        serializer.startTag(null, "config")
        
        // Save key mappings
        serializer.startTag(null, "keyMappings")
        config.keyMappings.forEach { (key, value) ->
            serializer.startTag(null, "mapping")
            serializer.attribute(null, "key", key)
            serializer.attribute(null, "value", value.toString())
            serializer.endTag(null, "mapping")
        }
        serializer.endTag(null, "keyMappings")
        
        // Save settings
        serializer.startTag(null, "settings")
        config.settings.forEach { (key, value) ->
            serializer.startTag(null, "setting")
            serializer.attribute(null, "key", key)
            serializer.attribute(null, "value", value)
            serializer.endTag(null, "setting")
        }
        serializer.endTag(null, "settings")
        
        serializer.endTag(null, "config")
        serializer.endDocument()
        
        outputStream.close()
    }
    
    fun loadConfig(fileName: String = "config.xml"): Config {
        val file = File(context.filesDir, fileName)
        if (!file.exists()) return Config(emptyMap(), emptyMap())
        
        val inputStream = FileInputStream(file)
        val parser: XmlPullParser = XmlPullParserFactory.newInstance().newPullParser()
        parser.setInput(inputStream, "UTF-8")
        
        val keyMappings = mutableMapOf<String, Int>()
        val settings = mutableMapOf<String, String>()
        
        var eventType = parser.eventType
        var currentSection = ""
        
        while (eventType != XmlPullParser.END_DOCUMENT) {
            when (eventType) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "keyMappings" -> currentSection = "keyMappings"
                        "settings" -> currentSection = "settings"
                        "mapping" -> {
                            if (currentSection == "keyMappings") {
                                val key = parser.getAttributeValue(null, "key")
                                val value = parser.getAttributeValue(null, "value").toInt()
                                keyMappings[key] = value
                            }
                        }
                        "setting" -> {
                            if (currentSection == "settings") {
                                val key = parser.getAttributeValue(null, "key")
                                val value = parser.getAttributeValue(null, "value")
                                settings[key] = value
                            }
                        }
                    }
                }
            }
            eventType = parser.next()
        }
        
        inputStream.close()
        return Config(keyMappings, settings)
    }
}