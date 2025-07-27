package com.huang1057.rskymusicassistant

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.AccessibilityServiceInfo
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo

class AccessibilityClickService : AccessibilityService() {
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let {
            val x = it.getIntExtra("x", 0)
            val y = it.getIntExtra("y", 0)
            if (x != 0 && y != 0) {
                performClick(x, y)
            }
        }
        return super.onStartCommand(intent, flags, startId)
    }
    
    override fun onServiceConnected() {
        val info = AccessibilityServiceInfo().apply {
            eventTypes = AccessibilityEvent.TYPE_VIEW_CLICKED
            feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC
            flags = AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS
            notificationTimeout = 100
        }
        
        this.serviceInfo = info
    }
    
    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        event?.let {
            // 处理无障碍事件
        }
    }
    
    override fun onInterrupt() {
        // 服务中断处理
    }
    
    fun performClick(x: Int, y: Int) {
        // 实现模拟点击逻辑
        val rootNode = rootInActiveWindow
        rootNode?.let {
            val clickNode = findClickableNodeAt(it, x, y)
            clickNode?.performAction(AccessibilityNodeInfo.ACTION_CLICK)
        }
    }
    
    private fun findClickableNodeAt(root: AccessibilityNodeInfo, x: Int, y: Int): AccessibilityNodeInfo? {
        // 查找可点击节点
        val clickableNodes = mutableListOf<AccessibilityNodeInfo>()
        root.findAccessibilityNodeInfosByViewId("com.netease.sky:id/click_area")?.let {
            clickableNodes.addAll(it)
        }
        
        return clickableNodes.firstOrNull()
    }
}