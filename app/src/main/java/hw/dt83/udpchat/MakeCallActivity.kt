package hw.dt83.udpchat

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.View
import android.widget.Button
import android.widget.TextView
import hw.dt83.udpchat.model.config.Utils.Companion.deserializeStringList2HostInfoSet
import java.io.IOException
import java.net.*

class MakeCallActivity : Activity() {
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
        sendMessage("CAL:$displayName", 50003)
    }

    private fun endCall() {
        // Ends the chat sessions
        stopListener()
        if (IN_CALL) {
            call!!.endCall()
        }
        sendMessage("END:", BROADCAST_PORT)
        finish()
    }

    private fun startListener() {
        // Create listener thread
        LISTEN = true
        val listenThread = Thread(Runnable {
            try {
                Log.i(LOG_TAG, "Listener started!")
                val socket = DatagramSocket(BROADCAST_PORT)
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
                            call = AudioCall(packet.address)
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

    private fun sendMessage(message: String, port: Int) {
        // Creates a thread used for sending notifications
        val replyThread = Thread(Runnable {
            try {
                val data = message.toByteArray()
                val socket = DatagramSocket()
                val packet = DatagramPacket(data, data.size, address, port)
                socket.send(packet)
                Log.i(LOG_TAG, "Sent message( $message ) to $address")
                socket.disconnect()
                socket.close()
            } catch (e: UnknownHostException) {
                Log.e(LOG_TAG, "Failure. UnknownHostException in sendMessage: $address")
            } catch (e: SocketException) {
                Log.e(LOG_TAG, "Failure. SocketException in sendMessage: $e")
            } catch (e: IOException) {
                Log.e(LOG_TAG, "Failure. IOException in sendMessage: $e")
            }
        })
        replyThread.start()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.make_call, menu)
        return true
    }

    companion object {
        private const val LOG_TAG = "MakeCall"
        private const val BROADCAST_PORT = 50002
        private const val BUF_SIZE = 1024
    }
}