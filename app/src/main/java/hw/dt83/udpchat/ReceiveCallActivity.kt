package hw.dt83.udpchat

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.View
import android.widget.Button
import android.widget.TextView
import java.io.IOException
import java.net.*

class ReceiveCallActivity : Activity() {
    private var contactIp: String? = null
    private var contactName: String? = null
    private var LISTEN = true
    private var IN_CALL = false
    private var call: AudioCall? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_receive_call)
        val intent = intent
        contactName = intent.getStringExtra(MainActivity.EXTRA_CONTACT)
        contactIp = intent.getStringExtra(MainActivity.EXTRA_IP)
        val textView = findViewById<View>(R.id.textViewIncomingCall) as TextView
        textView.text = "Incoming call: $contactName"
        val endButton = findViewById<View>(R.id.buttonEndCall1) as Button
        endButton.visibility = View.INVISIBLE
        startListener()

        // ACCEPT BUTTON
        val acceptButton = findViewById<View>(R.id.buttonAccept) as Button
        acceptButton.setOnClickListener {
            try {
                // Accepting call. Send a notification and start the call
                sendMessage("ACC:")
                val address = InetAddress.getByName(contactIp)
                Log.i(LOG_TAG, "Calling $address")
                IN_CALL = true
                call = AudioCall(address)
                call!!.startCall()
                // Hide the buttons as they're not longer required
                val accept = findViewById<View>(R.id.buttonAccept) as Button
                accept.isEnabled = false
                val reject = findViewById<View>(R.id.buttonReject) as Button
                reject.isEnabled = false
                endButton.visibility = View.VISIBLE
            } catch (e: UnknownHostException) {
                Log.e(LOG_TAG, "UnknownHostException in acceptButton: $e")
            } catch (e: Exception) {
                Log.e(LOG_TAG, "Exception in acceptButton: $e")
            }
        }

        // REJECT BUTTON
        val rejectButton = findViewById<View>(R.id.buttonReject) as Button
        rejectButton.setOnClickListener { // Send a reject notification and end the call
            sendMessage("REJ:")
            endCall()
        }

        // END BUTTON
        endButton.setOnClickListener { endCall() }
    }

    private fun endCall() {
        // End the call and send a notification
        stopListener()
        if (IN_CALL) {
            call!!.endCall()
        }
        sendMessage("END:")
        finish()
    }

    private fun startListener() {
        // Creates the listener thread
        LISTEN = true
        val listenThread = Thread(Runnable {
            try {
                Log.i(LOG_TAG, "Listener started!")
                val socket = DatagramSocket(BROADCAST_PORT)
                socket.soTimeout = 1500
                val buffer = ByteArray(BUF_SIZE)
                val packet = DatagramPacket(buffer, BUF_SIZE)
                while (LISTEN) {
                    try {
                        Log.i(LOG_TAG, "Listening for packets")
                        socket.receive(packet)
                        val data = String(buffer, 0, packet.length)
                        Log.i(LOG_TAG, "Packet received from " + packet.address + " with contents: " + data)
                        val action = data.substring(0, 4)
                        if (action == "END:") {
                            // End call notification received. End call
                            endCall()
                        } else {
                            // Invalid notification received.
                            Log.w(LOG_TAG, packet.address.toString() + " sent invalid message: " + data)
                        }
                    } catch (e: IOException) {
                        Log.e(LOG_TAG, "IOException in Listener $e")
                    }
                }
                Log.i(LOG_TAG, "Listener ending")
                socket.disconnect()
                socket.close()
                return@Runnable
            } catch (e: SocketException) {
                Log.e(LOG_TAG, "SocketException in Listener $e")
                endCall()
            }
        })
        listenThread.start()
    }

    private fun stopListener() {
        // Ends the listener thread
        LISTEN = false
    }

    private fun sendMessage(message: String) {
        // Creates a thread for sending notifications
        val replyThread = Thread(Runnable {
            try {
                val address = InetAddress.getByName(contactIp)
                val data = message.toByteArray()
                val socket = DatagramSocket()
                val packet = DatagramPacket(data, data.size, address, BROADCAST_PORT)
                socket.send(packet)
                Log.i(LOG_TAG, "Sent message( $message ) to $contactIp")
                socket.disconnect()
                socket.close()
            } catch (e: UnknownHostException) {
                Log.e(LOG_TAG, "Failure. UnknownHostException in sendMessage: $contactIp")
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
        menuInflater.inflate(R.menu.receive_call, menu)
        return true
    }

    companion object {
        private const val LOG_TAG = "ReceiveCall"
        private const val BROADCAST_PORT = 50002
        private const val BUF_SIZE = 1024
    }
}