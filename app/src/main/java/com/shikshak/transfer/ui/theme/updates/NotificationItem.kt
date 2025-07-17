package com.shikshak.transfer.ui.theme.updates

data class NotificationItem(
    var id: String = "",
    val title: String = "",
    val message: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false,
    val type: String = "general", // general, match, transfer_request, etc.
    val data: Map<String, String> = emptyMap() // Additional data for deep linking
) 