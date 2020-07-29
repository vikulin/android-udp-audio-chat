package hw.dt83.udpchat

import android.Manifest
import android.content.pm.PackageManager
import android.media.*
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.io.IOException
import java.net.*

class AudioCall(// Address to call
        private val address: InetAddress) {
    private val port = 50000 // Port the packets are addressed to
    private var mic = false // Enable mic?
    private var speakers = false // Enable speakers?
    fun startCall() {
        startMic()
        startSpeakers()
    }

    fun endCall() {
        Log.i(LOG_TAG, "Ending call!")
        muteMic()
        muteSpeakers()
    }

    fun muteMic() {
        mic = false
    }

    fun muteSpeakers() {
        speakers = false
    }

    fun startMic() {
        // Creates the thread for capturing and transmitting audio
        mic = true
        val thread = Thread(Runnable {
            // Create an instance of the AudioRecord class
            Log.i(LOG_TAG, "Send thread started. Thread id: " + Thread.currentThread().id)
            val audioRecorder = AudioRecord(MediaRecorder.AudioSource.VOICE_COMMUNICATION, SAMPLE_RATE,
                    AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT,
                    AudioRecord.getMinBufferSize(SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT) * 10)
            var bytes_read = 0
            var bytes_sent = 0
            val buf = ByteArray(BUF_SIZE)
            try {
                // Create a socket and start recording
                Log.i(LOG_TAG, "Packet destination: $address")
                val socket = DatagramSocket()
                audioRecorder.startRecording()
                while (mic) {
                    // Capture audio from the mic and transmit it
                    bytes_read = audioRecorder.read(buf, 0, BUF_SIZE)
                    val packet = DatagramPacket(buf, bytes_read, address, port)
                    socket.send(packet)
                    bytes_sent += bytes_read
                    Log.i(LOG_TAG, "Total bytes sent: $bytes_sent")
                    Thread.sleep(SAMPLE_INTERVAL.toLong(), 0)
                }
                // Stop recording and release resources
                audioRecorder.stop()
                audioRecorder.release()
                socket.disconnect()
                socket.close()
                mic = false
                return@Runnable
            } catch (e: InterruptedException) {
                Log.e(LOG_TAG, "InterruptedException: $e")
                mic = false
            } catch (e: SocketException) {
                Log.e(LOG_TAG, "SocketException: $e")
                mic = false
            } catch (e: UnknownHostException) {
                Log.e(LOG_TAG, "UnknownHostException: $e")
                mic = false
            } catch (e: IOException) {
                Log.e(LOG_TAG, "IOException: $e")
                mic = false
            }
        })
        thread.start()
    }

    fun startSpeakers() {
        // Creates the thread for receiving and playing back audio
        if (!speakers) {
            speakers = true
            val receiveThread = Thread(Runnable {
                // Create an instance of AudioTrack, used for playing back audio
                Log.i(LOG_TAG, "Receive thread started. Thread id: " + Thread.currentThread().id)
                val track = AudioTrack(AudioManager.STREAM_MUSIC, SAMPLE_RATE, AudioFormat.CHANNEL_OUT_MONO,
                        AudioFormat.ENCODING_PCM_16BIT, BUF_SIZE, AudioTrack.MODE_STREAM)
                track.play()
                try {
                    // Define a socket to receive the audio
                    val socket = DatagramSocket(port)
                    val buf = ByteArray(BUF_SIZE)
                    while (speakers) {
                        // Play back the audio received from packets
                        val packet = DatagramPacket(buf, BUF_SIZE)
                        socket.receive(packet)
                        Log.i(LOG_TAG, "Packet received: " + packet.length)
                        track.write(packet.data, 0, BUF_SIZE)
                    }
                    // Stop playing back and release resources
                    socket.disconnect()
                    socket.close()
                    track.stop()
                    track.flush()
                    track.release()
                    speakers = false
                    return@Runnable
                } catch (e: SocketException) {
                    Log.e(LOG_TAG, "SocketException: $e")
                    speakers = false
                } catch (e: IOException) {
                    Log.e(LOG_TAG, "IOException: $e")
                    speakers = false
                }
            })
            receiveThread.start()
        }
    }

    companion object {
        private const val LOG_TAG = "AudioCall"
        private const val SAMPLE_RATE = 8000 // Hertz
        private const val SAMPLE_INTERVAL = 20 // Milliseconds
        private const val SAMPLE_SIZE = 2 // Bytes
        private const val BUF_SIZE = SAMPLE_INTERVAL * SAMPLE_INTERVAL * SAMPLE_SIZE * 2 //Bytes
    }

}