package com.huang1057.rskymusicassistant

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.view.*
import androidx.compose.runtime.*
import com.huang1057.rskymusicassistant.FloatingWindowService.*
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.*
import androidx.savedstate.*
import com.huang1057.rskymusicassistant.ui.theme.SkyMusicAssistantTheme
import com.huang1057.rskymusicassistant.viewmodel.MainViewModel
import java.math.BigInteger



class FloatingWindowService : Service(), ViewModelStoreOwner, LifecycleOwner, SavedStateRegistryOwner {
    private lateinit var windowManager: WindowManager
    private lateinit var floatingView: ComposeView
    private lateinit var layoutParams: WindowManager.LayoutParams  // Class-level declaration
    override val viewModelStore = ViewModelStore()
    private val lifecycleRegistry = LifecycleRegistry(this)
    private val savedStateRegistryController = SavedStateRegistryController.create(this)

    override val lifecycle: Lifecycle
        get() = lifecycleRegistry

    override val savedStateRegistry: SavedStateRegistry
        get() = savedStateRegistryController.savedStateRegistry

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.currentState = Lifecycle.State.CREATED

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        // Initialize layoutParams here
        layoutParams = WindowManager.LayoutParams().apply {
            type = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                WindowManager.LayoutParams.TYPE_PHONE
            }
            format = PixelFormat.TRANSLUCENT
            flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            width = WindowManager.LayoutParams.WRAP_CONTENT
            height = WindowManager.LayoutParams.WRAP_CONTENT
            gravity = Gravity.TOP or Gravity.START
            x = 100
            y = 100
        }

        // 创建悬浮窗视图
        floatingView = ComposeView(this).apply {
            setContent {
                SkyMusicAssistantTheme {
                    var isExpanded by remember { mutableStateOf(false) }

                    LaunchedEffect(Unit) {
                        // 确保状态变化会触发重组
                    }

                    val windowState = rememberFloatingWindowState()
                    
                    if (isExpanded) {
                        FloatingWindowContent(
                            state = windowState,
                            onClose = { stopSelf() },
                            onMinimize = {
                                isExpanded = false
                                minimizeWindow()
                            },
                            onPlayClick = { x, y ->
                                val intent = Intent(this@FloatingWindowService, AccessibilityClickService::class.java)
                                intent.putExtra("x", x)
                                intent.putExtra("y", y)
                                startService(intent)
                            }
                        )
                    } else {
                        FloatingButtonContent(
                            state = windowState,
                            onClick = {
                                isExpanded = true
                                layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT
                                layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT
                                val newFlags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or 
                                              WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                                this@FloatingWindowService.layoutParams.flags = newFlags
                                windowManager.updateViewLayout(floatingView, layoutParams)
                            }
                        )
                    }
                }
            }
        }


        // 设置Lifecycle和ViewModel支持
        setupViewTreeOwners()

        windowManager.addView(floatingView, layoutParams)
    }
    
    private fun setupViewTreeOwners() {
        floatingView.setViewTreeLifecycleOwner(this)
        floatingView.setViewTreeViewModelStoreOwner(this)
        floatingView.setViewTreeSavedStateRegistryOwner(this)
    }
    
    private fun minimizeWindow() {
        layoutParams.width = 100
        layoutParams.height = 100
        windowManager.updateViewLayout(floatingView, layoutParams)
    }
    
    override fun onDestroy() {
        super.onDestroy()
        if (::floatingView.isInitialized) {
            windowManager.removeView(floatingView)
        }
        viewModelStore.clear()
        lifecycleRegistry.currentState = androidx.lifecycle.Lifecycle.State.DESTROYED
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
}
