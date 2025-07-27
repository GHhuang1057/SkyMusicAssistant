// Copyright (C) 2025 huang1057  
// SPDX-License-Identifier: GPL-3.0-or-later  
package com.huang1057.rskymusicassistant

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.huang1057.rskymusicassistant.FloatingWindowService
import com.huang1057.rskymusicassistant.ui.theme.SkyMusicAssistantTheme
import com.huang1057.rskymusicassistant.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    
    private val REQUEST_CODE_OVERLAY = 1
    private val REQUEST_CODE_ACCESSIBILITY = 2
    private lateinit var viewModel: MainViewModel
    
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        viewModel = androidx.lifecycle.ViewModelProvider(this)[MainViewModel::class.java]
        
        setContent {
            SkyMusicAssistantTheme {
                val viewModel: MainViewModel = viewModel()
                val context = LocalContext.current
                
                MainScreen(
                    onStartFloatingWindow = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(context)) {
                            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, 
                                Uri.parse("package:$packageName"))
                            startActivityForResult(intent, REQUEST_CODE_OVERLAY)
                        } else {
                            startService(Intent(this@MainActivity, FloatingWindowService::class.java))
                            checkAccessibilityPermission(context)
                        }
                    },
                    onStopFloatingWindow = {
                        stopService(Intent(this@MainActivity, FloatingWindowService::class.java))
                    },
                    onRequestAccessibilityPermission = {
                        val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
                        startActivityForResult(intent, REQUEST_CODE_ACCESSIBILITY)
                    },
                    hasAccessibilityPermission = viewModel.hasAccessibilityPermission.collectAsState().value
                )
            }
        }
    }
    
    private fun checkAccessibilityPermission(context: android.content.Context) {
        val accessibilityEnabled = Settings.Secure.getInt(
            context.contentResolver,
            Settings.Secure.ACCESSIBILITY_ENABLED
        ) == 1
        
        viewModel.updateAccessibilityPermission(accessibilityEnabled)
    }
    
    
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    onStartFloatingWindow: () -> Unit,
    onStopFloatingWindow: () -> Unit,
    onRequestAccessibilityPermission: () -> Unit,
    hasAccessibilityPermission: Boolean
) {
    var isFloatingWindowActive by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("SkyMusicAssistant") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "权限状态",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("无障碍权限: ${if (hasAccessibilityPermission) "已启用" else "未启用"}")
                        if (!hasAccessibilityPermission) {
                            Button(onClick = onRequestAccessibilityPermission) {
                                Text("开启权限")
                            }
                        }
                    }
                }
            }
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "悬浮窗控制",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = {
                                onStartFloatingWindow()
                                isFloatingWindowActive = true
                            },
                            enabled = !isFloatingWindowActive
                        ) {
                            Text("启动悬浮窗")
                        }
                        
                        Button(
                            onClick = {
                                onStopFloatingWindow()
                                isFloatingWindowActive = false
                            },
                            enabled = isFloatingWindowActive
                        ) {
                            Text("停止悬浮窗")
                        }
                    }
                }
            }
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "使用说明",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text("1. 授予悬浮窗（部分手机为显示在其他应用上层）权限\n2. 启动悬浮窗进行琴键校准\n3. 选择Xml文件开始播放(或者自己创建一个xml文件！)")
                }
            }
        }
    }
}