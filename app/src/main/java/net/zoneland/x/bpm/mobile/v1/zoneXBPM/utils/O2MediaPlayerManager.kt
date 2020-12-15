package net.zoneland.x.bpm.mobile.v1.zoneXBPM.utils

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import java.io.File


/**
 * Created by fancyLou on 2020-12-15.
 * Copyright © 2020 O2. All rights reserved.
 */


class O2MediaPlayerManager private constructor() {
    companion object {
        private var instance: O2MediaPlayerManager? = null
        fun instance() : O2MediaPlayerManager {
            if (instance == null) {
                instance = O2MediaPlayerManager()
            }
            return instance!!
        }
    }

    private val mPlayer: MediaPlayer by lazy { MediaPlayer() }
    private lateinit var context: Context
    private lateinit var audioManager:AudioManager


    fun init(context: Context) {
        this.context = context
        audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }

    /**
     * 切换到外放
     */
    fun changeToSpeaker() {
        audioManager.mode = AudioManager.MODE_NORMAL
        audioManager.isSpeakerphoneOn = true
    }

    /**
     * 切换到听筒
     */
    fun changeToReceiver() {
        audioManager.isSpeakerphoneOn = false
        audioManager.mode = AudioManager.MODE_IN_COMMUNICATION
    }
    /**
     * 开始播放
     */
    fun startPlay(filePath: String, completed: ()->Unit) {
        XLog.debug("uri : $filePath")
        val uri = Uri.fromFile(File(filePath))
        mPlayer.reset()
        mPlayer.setDataSource(context, uri)
        mPlayer.setOnPreparedListener {
            mPlayer.start()
        }
        mPlayer.setOnCompletionListener {
            XLog.debug("播音结束！")
            completed()
        }
        mPlayer.setVolume(1.0f, 1.0f)
        mPlayer.prepare()
    }

    fun stopPlay() {
        if (isPlaying()) {
            mPlayer.stop()
        }
    }

    fun isPlaying() : Boolean {
        return (mPlayer.isPlaying)
    }
}