package hw.dt83.udpchat

import android.media.AudioFormat
import android.media.AudioRecord
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.View
import android.widget.Button
import android.widget.TextView
import hw.dt83.udpchat.model.config.Utils.Companion.deserializeStringList2HostInfoSet
import java.io.IOException
import java.net.*


class MakeCallActivity : UDPMessageActivity() {

    companion object {
        private const val LOG_TAG = "MakeCall"
        private const val BUF_SIZE = 1024
    }

    private var displayName: String? = null
    private var contactName: String? = null
    private var address: InetAddress? = null
    private var LISTEN = true
    private var IN_CALL = false
    private var call: AudioCall? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_make_call)
        Log.i(LOG_TAG, "MakeCallActivity started!")
        var extras = intent.extras
        var cd = deserializeStringList2HostInfoSet(
                extras!!.getStringArrayList(MainActivity.HOST_LIST)!!
        )
        var host = cd.iterator().next()
        contactName = host.description
        displayName  =host.description
        address = host.address
        val textView = findViewById<View>(R.id.textViewCalling) as TextView
        textView.text = "Calling: $contactName"
        startListener()
        makeCall()
        val endButton = findViewById<View>(R.id.buttonEndCall) as Button
        endButton.setOnClickListener { // Button to end the call has been pressed
            endCall()
        }
    }

    private fun makeCall() {
        // Send a request to start a call
        sendMessage("CAL:$displayName", address, MainActivity.LISTENER_PORT)
    }

    private fun endCall() {
        // Ends the chat sessions
        stopListener()
        if (IN_CALL) {
            call!!.endCall()
        }
        sendMessage("END:", address, MainActivity.BROADCAST_PORT)
        finish()
    }

    private fun getMinSupportedSampleRate(): Int {
        /*
     * Valid Audio Sample rates
     *
     * @see <a
     * href="http://en.wikipedia.org/wiki/Sampling_%28signal_processing%29"
     * >Wikipedia</a>
     */
        val validSampleRates = intArrayOf(8000, 11025, 16000, 22050,
                32000, 37800, 44056, 44100, 47250, 48000, 50000, 50400, 88200,
                96000, 176400, 192000, 352800, 2822400, 5644800)
        /*
     * Selecting default audio input source for recording since
     * AudioFormat.CHANNEL_CONFIGURATION_DEFAULT is deprecated and selecting
     * default encoding format.
     */for (i in validSampleRates.indices) {
            val result = AudioRecord.getMinBufferSize(validSampleRates[i],
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT)
            if (result != AudioRecord.ERROR && result != AudioRecord.ERROR_BAD_VALUE && result > 0) {
                // return the mininum supported audio sample rate
                return validSampleRates[i]
            }
        }
        // If none of the sample rates are supported return -1 handle it in
        // calling method
        return -1
    }

    private fun startListener() {
        // Create listener thread
        LISTEN = true
        val listenThread = Thread(Runnable {
            try {
                Log.i(LOG_TAG, "Listener started!")
                val socket = DatagramSocket(MainActivity.BROADCAST_PORT)
                socket.soTimeout = 15000
                val buffer = ByteArray(BUF_SIZE)
                val packet = DatagramPacket(buffer, BUF_SIZE)
                while (LISTEN) {
                    try {
                        Log.i(LOG_TAG, "Listening for packets")
                        socket.receive(packet)
                        val data = String(buffer, 0, packet.length)
                        Log.i(LOG_TAG, "Packet received from " + packet.address + " with contents: " + data)
                        val action = data.substring(0, 4)
                        if (action == "ACC:") {

                            // Accept notification received. Start call
                            call = AudioCall(getMinSupportedSampleRate(), packet.address)
                            call!!.startCall()
                            IN_CALL = true
                        } else if (action == "REJ:") {
                            // Reject notification received. End call
                            endCall()
                        } else if (action == "END:") {
                            // End call notification received. End call
                            endCall()
                        } else {
                            // Invalid notification received
                            Log.w(LOG_TAG, packet.address.toString() + " sent invalid message: " + data)
                        }
                    } catch (e: SocketTimeoutException) {
                        if (!IN_CALL) {
                            Log.i(LOG_TAG, "No reply from contact. Ending call")
                            endCall()
                            return@Runnable
                        }
                    } catch (e: IOException) {
                    }
                }
                Log.i(LOG_TAG, "Listener ending")
                socket.disconnect()
                socket.close()
                return@Runnable
            } catch (e: SocketException) {
                Log.e(LOG_TAG, "SocketException in Listener")
                e.printStackTrace()
                endCall()
            }
        })
        listenThread.start()
    }

    private fun stopListener() {
        // Ends the listener thread
        LISTEN = false
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.make_call, menu)
        return true
    }
}