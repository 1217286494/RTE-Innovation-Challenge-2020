/**
 * MIT License
 * <p>
 * Copyright (C) 2020 by Qifan YANG (@underwindfall)
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.qifan.emojibattle

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.qifan.emojibattle.databinding.ActivityMainBinding
import io.agora.rtc2.ChannelMediaOptions
import io.agora.rtc2.Constants
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.video.VideoCanvas
import io.agora.rtc2.video.VideoEncoderConfiguration
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val localSurfaceView get() = binding.localSurfaceView
    private val remoteSurfaceView get() = binding.remoteSurfaceView
    private val token = BuildConfig.Token
    private val appId = BuildConfig.AppId
    private lateinit var rtcEngine: RtcEngine
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initializeAgoraEngine()
        controlVideo()
        joinChannel()
    }

    private fun initializeAgoraEngine() {
        try {
            rtcEngine = RtcEngine.create(
                baseContext,
                appId,
                object : IRtcEngineEventHandler() {
                    override fun onJoinChannelSuccess(channel: String?, userId: Int, elapsed: Int) {
                        super.onJoinChannelSuccess(channel, userId, elapsed)
                        Log.d(
                            "Qifan",
                            """
                            join channel success channel:$channel
                            userId = $userId
                            elapsed = $elapsed
                            """.trimIndent()
                        )

                    }

                    override fun onFirstLocalVideoFrame(width: Int, height: Int, elapsed: Int) {
                        super.onFirstLocalVideoFrame(width, height, elapsed)
                        Log.d("Qifan", "onFirstLocalVideoFrame")

                    }

                    override fun onFirstRemoteVideoDecoded(
                        userId: Int,
                        width: Int,
                        height: Int,
                        elapsed: Int
                    ) {
                        super.onFirstRemoteVideoDecoded(userId, width, height, elapsed)
                        Log.d("Qifan", "Remote=====")
                        runOnUiThread {
                            if (remoteSurfaceView.childCount >= 1) {
                                return@runOnUiThread
                            }
                            val surfaceView = RtcEngine.CreateRendererView(baseContext)
                            remoteSurfaceView.addView(surfaceView)
                            rtcEngine.setupRemoteVideo(
                                VideoCanvas(
                                    surfaceView,
                                    VideoCanvas.RENDER_MODE_FIT,
//                                    VideoCanvas.RENDER_SOURCE_CAMERA,
                                    userId
                                )
                            )
                        }
                    }

                    override fun onUserJoined(userId: Int, elapsed: Int) {
                        super.onUserJoined(userId, elapsed)
                        Log.d("Qifan", "onUserJoined")
                    }
                }
            )
        } catch (e: Exception) {
            throw RuntimeException(
                "NEED TO check rtc sdk init fatal error ${
                    Log.getStackTraceString(
                        e
                    )
                }"
            )
        }
    }

    private fun controlVideo() {
        rtcEngine.enableVideo()
        rtcEngine.setVideoEncoderConfiguration(
            VideoEncoderConfiguration(
                VideoEncoderConfiguration.VD_640x360,
                VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_15,
                VideoEncoderConfiguration.STANDARD_BITRATE,
                VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_FIXED_PORTRAIT
            )
        )
        val surfaceView = RtcEngine.CreateRendererView(baseContext)
        surfaceView.setZOrderMediaOverlay(true)
        localSurfaceView.addView(surfaceView)
        rtcEngine.setupLocalVideo(
            VideoCanvas(
                surfaceView,
                VideoCanvas.RENDER_MODE_FIT,
                VideoCanvas.RENDER_SOURCE_CAMERA,
                0
            )
        )
    }

    private fun joinChannel() {
        // if you do not specify the uid, Agora will assign one.
//        val channelMediaOptions = ChannelMediaOptions()
//        channelMediaOptions.publishCameraTrack = true
//        channelMediaOptions.autoSubscribeVideo = true
//        channelMediaOptions.clientRoleType = Constants.CLIENT_ROLE_BROADCASTER
//        rtcEngine.joinChannel(token, "demoChannel1", 0, channelMediaOptions)
        rtcEngine.joinChannel(token, "demoChannel1", "Extra Optional Data", 0)
    }

    private fun leaveChannel() {
        rtcEngine.leaveChannel()
    }

    override fun onDestroy() {
        super.onDestroy()
        leaveChannel()
        RtcEngine.destroy()
    }
}
