package com.huang1057.rskymusicassistant.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.huang1057.rskymusicassistant.R
import com.huang1057.rskymusicassistant.viewmodel.MainViewModel



@Composable
fun FloatingWindowContent(
    onClose: () -> Unit,
    onMinimize: () -> Unit
) {
    var showCalibration by remember { mutableStateOf(false) }
    var showFilePicker by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .size(300.dp, 400.dp)
            .background(
                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                shape = RoundedCornerShape(16.dp)
            )
            .border(
                width = 2.dp,
                color = MaterialTheme.colorScheme.primary,
                shape = RoundedCornerShape(16.dp)
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 标题栏
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "光遇琴键助手",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Row {
                    IconButton(
                        onClick = onMinimize,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "最小化")
                    }
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "关闭")
                    }
                }
            }

            Divider()

            // 控制按钮
            ControlButtons(
                onCalibrate = { showCalibration = true },
                onSelectFile = { showFilePicker = true }
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 琴键预览
            PianoKeyPreview()

            ClickPositionGrid()

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showCalibration) {
        CalibrationDialog(onDismiss = { showCalibration = false })
    }

    if (showFilePicker) {
        FilePickerDialog(onDismiss = { showFilePicker = false })
    }
}

@Composable
fun ControlButtons(
    onCalibrate: () -> Unit,
    onSelectFile: () -> Unit
) {
    val viewModel: MainViewModel = viewModel()
    val isPlaying by viewModel.isPlaying.collectAsState()
    val currentConfig by viewModel.currentConfig.collectAsState()

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = onCalibrate,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text("琴键校准")
        }

        Button(
            onClick = onSelectFile,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary
            )
        ) {
            Text("加载配置文件")
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { viewModel.togglePlay() },
                modifier = Modifier.weight(1f),
                enabled = currentConfig != null
            ) {
                Text(if (isPlaying) "■ 停止" else "▶ 播放")
            }
        }
    }
}

@Composable
fun PianoKeyPreview() {
    val keys = listOf("C", "D", "E", "F", "G", "A", "B")

    Column {
        Text(
            text = "琴键预览",
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            keys.forEach { key ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(40.dp)
                        .background(
                            color = Color.White,
                            shape = RoundedCornerShape(4.dp)
                        )
                        .border(1.dp, Color.Gray, RoundedCornerShape(4.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = key,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Black
                    )
                }
            }
        }
    }
}

@Composable
fun CalibrationDialog(onDismiss: () -> Unit) {
    var selectedKey by remember { mutableStateOf(0) }
    val keys = listOf("C4", "D4", "E4", "F4", "G4", "A4", "B4", "C5")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("琴键校准") },
        text = {
            Column {
                Text("选择要校准的琴键：")
                Spacer(modifier = Modifier.height(8.dp))

                keys.forEachIndexed { index, key ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedKey = index }
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = selectedKey == index,
                            onClick = { selectedKey = index }
                        )
                        Text(key, modifier = Modifier.padding(start = 8.dp))
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "点击屏幕上的对应琴键位置进行校准",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("完成")
            }
        }
    )
}


@Composable
fun FilePickerDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("加载配置文件") },
        text = {
            Column {
                Text("支持的格式：")
                Text("• XML (.xml)")
                Text("• 自动保存到应用内部存储")
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text("确定")
            }
        }
    )
}


@Composable
fun ClickPositionGrid() {
    val rows = 3
    val columns = 7

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = "模拟点击位置",
            style = MaterialTheme.typography.labelMedium,
            modifier = Modifier.padding(bottom = 4.dp)
        )

        repeat(rows) { rowIndex ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                repeat(columns) { colIndex ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .aspectRatio(1f)
                            .background(
                                color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f),
                                shape = RoundedCornerShape(4.dp)
                            )
                            .border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(4.dp))
                            .clickable {
                                // 处理点击位置逻辑
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${rowIndex + 1}-${colIndex + 1}",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}


