// Copyright (C) 2025 huang1057  
// SPDX-License-Identifier: GPL-3.0-or-later  
package com.huang1057.rskymusicassistant

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material3.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material.LinearProgressIndicator
import com.huang1057.rskymusicassistant.ui.theme.SkyMusicAssistantTheme
import androidx.compose.material.ExperimentalMaterialApi

@Composable
fun ClickPositionGrid(modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        repeat(3) { row ->
            Row(modifier = Modifier.fillMaxWidth()) {
                repeat(7) { col ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .border(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Text(
                            text = "${row + 1}-${col + 1}",
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                }
            }
        }
    }
}

class FloatingWindowState {
    var isWindowVisible by mutableStateOf(false)
    var isMinimized by mutableStateOf(false)
    var isPlaying by mutableStateOf(false)
    var progress by mutableStateOf(0f)
}

@Composable
fun rememberFloatingWindowState() = remember { FloatingWindowState() }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FloatingButtonContent(
    state: FloatingWindowState,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .size(48.dp)
            .padding(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary
        ),
        onClick = {
            state.isWindowVisible = !state.isWindowVisible
            onClick()
        }
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.MusicNote,
                contentDescription = "展开悬浮窗",
                tint = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FloatingWindowContent(
    state: FloatingWindowState,
    onClose: () -> Unit,
    onMinimize: () -> Unit,
    onPlayClick: (x: Float, y: Float) -> Unit
) {
    
    if (state.isMinimized) {
        Card(
            modifier = Modifier
                .size(48.dp)
                .padding(4.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                IconButton(
                    onClick = { state.isMinimized = false },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowUp,
                        contentDescription = "展开",
                        tint = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }
    } else {
        Card(
            modifier = Modifier
                .width(300.dp)
                .wrapContentHeight()
                .padding(8.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "光遇自动弹琴助手",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        IconButton(
                            onClick = { state.isMinimized = true },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "最小化"
                            )
                        }
                        
                        IconButton(
                            onClick = onClose,
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "关闭"
                            )
                        }
                    }
                }
                
                HorizontalDivider()
                
                Text(
                    text = "悬浮窗已启动",
                    style = MaterialTheme.typography.bodyMedium
                )

                // 点击位置网格
                ClickPositionGrid(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                        .padding(vertical = 8.dp)
                )
                
                // 播放控制区域
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // 进度条
                    LinearProgressIndicator(
                        progress = state.progress,
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    // 播放控制按钮
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { 
                                state.isPlaying = !state.isPlaying
                                onPlayClick(100f, 100f) 
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (state.isPlaying) MaterialTheme.colorScheme.errorContainer 
                                else MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Text(if (state.isPlaying) "暂停" else "播放")
                        }
                        
                        Button(
                            onClick = { 
                                state.isPlaying = false
                                state.progress = 0f
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("停止")
                        }
                    }
                    
                    // 曲目信息
                    Text(
                        text = "当前曲目: 未选择",
                        style = MaterialTheme.typography.bodySmall
                    )

                Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}
