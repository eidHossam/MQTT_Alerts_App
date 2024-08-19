package com.hossameid.iotalerts.utils

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Context.WINDOW_SERVICE
import android.graphics.PixelFormat
import android.os.Build
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.view.WindowManager.LayoutParams
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import com.hossameid.iotalerts.R
import com.hossameid.iotalerts.domain.models.TopicResponseModel
import com.hossameid.iotalerts.domain.repo.AlertsRepo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


@SuppressLint("InflateParams")
class AlertReceivedDialog(
    context: Context,
    private val alertsRepo: AlertsRepo
) : AlertDialog(context) {

    private lateinit var layoutParams: LayoutParams
    private val windowManager: WindowManager
    private val dialogView: View

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            layoutParams = LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT,
                LayoutParams.TYPE_APPLICATION_OVERLAY,
                LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
            )

            layoutParams.horizontalMargin = 10.0f
        }

        dialogView = LayoutInflater.from(context).inflate(R.layout.alert_dialog, null)

        layoutParams.gravity = Gravity.CENTER
        windowManager = context.getSystemService(WINDOW_SERVICE) as WindowManager
    }

    private fun open() {
        windowManager.addView(dialogView, layoutParams)
    }

    private fun close() {
        windowManager.removeView(dialogView)

        //Stop the currently playing alarm
        MediaPlayer.stopAlarm()
    }

    fun showDialog(alert: TopicResponseModel) {
        val topicName: TextView = dialogView.findViewById(R.id.topicNameTextView)
        val closeBtn: ImageButton = dialogView.findViewById(R.id.closeButton)
        val dateTextView: TextView = dialogView.findViewById(R.id.dateContentTextView)
        val messageTextView: TextView = dialogView.findViewById(R.id.messageContentTextView)
        val acknowledgeBtn: Button = dialogView.findViewById(R.id.acknowledgeBtn)

        topicName.text = alert.topic
        dateTextView.text = alert.timestamp
        messageTextView.text = alert.message

        //Change the text color based on the type of the alert
        when (alert.alertType) {
            0 -> messageTextView.setTextColor(
                context.getColor(R.color.normalAlertColor)
            )

            1 -> messageTextView.setTextColor(
                context.getColor(R.color.warningAlertColor)
            )

            2 -> messageTextView.setTextColor(
                context.getColor(R.color.criticalAlertColor)
            )
        }

        //Set the listener for the overlay close button
        closeBtn.setOnClickListener {
            close()
        }

        acknowledgeBtn.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                alertsRepo.acknowledgeAlert(alert.timestamp)
            }
            close()
        }

        open()
    }
}
