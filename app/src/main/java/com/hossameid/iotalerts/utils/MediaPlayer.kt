package com.hossameid.iotalerts.utils

import android.content.Context
import android.media.MediaPlayer
import com.hossameid.iotalerts.R

class MediaPlayer {

    companion object {
        private var mediaPlayer: MediaPlayer? = null

        /**
         * @brief Static function to start an alarm based on the alert type
         */
        fun playAlarm(context: Context, alarmType: Int) {
            //Release any previous playing sound to avoid overlap
            mediaPlayer?.release()

            val resourceId = when (alarmType) {
                0 -> R.raw.normal_alarm
                1 -> R.raw.warning_alert
                2 -> R.raw.danger_alert
                else -> R.raw.normal_alarm //default to normal alarm
            }

            mediaPlayer = MediaPlayer.create(context, resourceId).apply {
                isLooping = true
                start()
            }
        }

        /**
         * @brief Static function to stop the currently playing alarm
         */
        fun stopAlarm()
        {
            mediaPlayer?.let{
                if(it.isPlaying)
                {
                    it.stop()

                    it.release()
                }
            }
            mediaPlayer = null
        }
    }
}