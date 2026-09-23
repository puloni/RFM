package com.example.rfm.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

object RfmAudioEngine {

    var isEnabled: Boolean = true
    private val scope = CoroutineScope(Dispatchers.Default)

    fun setMuted(muted: Boolean) {
        isEnabled = !muted
    }

    fun playWhistle(longWhistle: Boolean = false) {
        if (longWhistle) playWhistleFullTime() else playWhistleShort()
    }
    fun playBallKick() = playKickSound()
    fun playMenuBlip() = playClickSound()
    fun playButtonClick() = playClickSound()
    fun playCashChime() {
        if (!isEnabled) return
        scope.launch {
            playDualTone(987.77, 1318.51, 120, volume = 0.5f)
        }
    }

    fun playWhistleShort() {
        if (!isEnabled) return
        scope.launch {
            playDualTone(2800.0, 3100.0, 180, volume = 0.7f)
        }
    }

    fun playWhistleFullTime() {
        if (!isEnabled) return
        scope.launch {
            playDualTone(2800.0, 3100.0, 150, volume = 0.7f)
            Thread.sleep(60)
            playDualTone(2800.0, 3100.0, 150, volume = 0.7f)
            Thread.sleep(60)
            playDualTone(2800.0, 3100.0, 350, volume = 0.8f)
        }
    }

    fun playGoalCheer() {
        if (!isEnabled) return
        scope.launch {
            // Crowd roar noise synthesized
            playNoiseRoar(durationMs = 900, volume = 0.75f)
        }
    }

    fun playKickSound() {
        if (!isEnabled) return
        scope.launch {
            // Low punch thump
            playSineFrequencyDrop(startFreq = 180.0, endFreq = 45.0, durationMs = 80, volume = 0.6f)
        }
    }

    fun playClickSound() {
        if (!isEnabled) return
        scope.launch {
            // High pitch menu blip
            playSineFrequencyDrop(startFreq = 880.0, endFreq = 1100.0, durationMs = 35, volume = 0.4f)
        }
    }

    private fun playDualTone(freq1: Double, freq2: Double, durationMs: Int, volume: Float) {
        try {
            val sampleRate = 22050
            val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
            val buffer = ShortArray(numSamples)

            for (i in 0 until numSamples) {
                val t = i.toDouble() / sampleRate
                // Modulation amplitude envelope
                val env = when {
                    i < numSamples * 0.1 -> i / (numSamples * 0.1)
                    i > numSamples * 0.8 -> (numSamples - i) / (numSamples * 0.2)
                    else -> 1.0
                }
                val sample = (sin(2 * PI * freq1 * t) * 0.5 + sin(2 * PI * freq2 * t) * 0.5) * env * volume * Short.MAX_VALUE
                buffer[i] = sample.toInt().toShort()
            }
            writeToAudioTrack(buffer, sampleRate)
        } catch (_: Exception) {}
    }

    private fun playSineFrequencyDrop(startFreq: Double, endFreq: Double, durationMs: Int, volume: Float) {
        try {
            val sampleRate = 22050
            val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
            val buffer = ShortArray(numSamples)

            var phase = 0.0
            for (i in 0 until numSamples) {
                val progress = i.toDouble() / numSamples
                val currentFreq = startFreq + (endFreq - startFreq) * progress
                val env = 1.0 - progress
                phase += 2 * PI * currentFreq / sampleRate
                val sample = sin(phase) * env * volume * Short.MAX_VALUE
                buffer[i] = sample.toInt().toShort()
            }
            writeToAudioTrack(buffer, sampleRate)
        } catch (_: Exception) {}
    }

    private fun playNoiseRoar(durationMs: Int, volume: Float) {
        try {
            val sampleRate = 22050
            val numSamples = (sampleRate * (durationMs / 1000.0)).toInt()
            val buffer = ShortArray(numSamples)
            var lastSample = 0.0

            for (i in 0 until numSamples) {
                val progress = i.toDouble() / numSamples
                // Envelope swells up then decays
                val env = when {
                    progress < 0.2 -> progress / 0.2
                    else -> (1.0 - progress) / 0.8
                }
                val white = (Random.nextDouble() * 2.0 - 1.0)
                // Low-pass filter for crowd roar sound
                lastSample = lastSample * 0.85 + white * 0.15
                val sample = lastSample * env * volume * Short.MAX_VALUE
                buffer[i] = sample.toInt().toShort()
            }
            writeToAudioTrack(buffer, sampleRate)
        } catch (_: Exception) {}
    }

    private fun writeToAudioTrack(buffer: ShortArray, sampleRate: Int) {
        var track: AudioTrack? = null
        try {
            track = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(buffer.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            track.write(buffer, 0, buffer.size)
            track.play()
            Thread.sleep((buffer.size * 1000L / sampleRate) + 30)
            track.stop()
            track.release()
        } catch (_: Exception) {
            track?.release()
        }
    }
}
