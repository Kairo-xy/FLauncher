package com.verya.launcher.data

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

data class NotifEntry(val app: String, val title: String, val text: String)

object NotificationStore {
    var items by mutableStateOf(listOf<NotifEntry>())
}

class VeryaNotificationService : NotificationListenerService() {

    override fun onListenerConnected() {
        refresh()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        refresh()
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        refresh()
    }

    private fun refresh() {
        val list = try {
            activeNotifications?.toList().orEmpty()
        } catch (e: Exception) {
            emptyList()
        }
        NotificationStore.items = list
            .filter { it.packageName != packageName }
            .map { sbn ->
                val extras = sbn.notification.extras
                NotifEntry(
                    appLabel(sbn.packageName),
                    extras.getCharSequence(Notification.EXTRA_TITLE)?.toString().orEmpty(),
                    extras.getCharSequence(Notification.EXTRA_TEXT)?.toString().orEmpty()
                )
            }
            .filter { it.title.isNotBlank() || it.text.isNotBlank() }
    }

    private fun appLabel(pkg: String): String = try {
        packageManager.getApplicationLabel(packageManager.getApplicationInfo(pkg, 0)).toString()
    } catch (e: Exception) {
        pkg
    }
}
