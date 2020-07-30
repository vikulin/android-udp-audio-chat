package hw.dt83.udpchat

import android.app.Activity
import android.util.Log
import java.io.IOException
import java.net.*

open class UDPMessageActivity:Activity() {

    companion object {
        private const val LOG_TAG = "MakeCall"
    }

    fun sendMessage(message: String, address: InetAddress?, port: Int) {
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
}