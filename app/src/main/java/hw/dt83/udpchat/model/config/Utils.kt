package hw.dt83.udpchat.model.config

import android.util.Log
import com.google.gson.Gson
import hw.dt83.udpchat.model.HostInfo
import java.io.IOException
import java.net.*

class Utils {

    companion object {

        private const val LOG_TAG = "UDP Message"

        @JvmStatic
        fun ping(address: InetAddress, port: Int) {
            //val start = System.currentTimeMillis()
            try {
                val msg = "PING"
                val s = DatagramSocket()
                val hi = DatagramPacket(msg.toByteArray(), msg.length,
                        address, port)
                s.send(hi)
            } catch (e: Exception) {
                e.printStackTrace()
                print(address)
            }
        }

        @JvmStatic
        fun pong(address: InetAddress, port: Int) {
            //val start = System.currentTimeMillis()
            try {
                val msg = "PONG"
                val s = DatagramSocket()
                val hi = DatagramPacket(msg.toByteArray(), msg.length,
                        address, port)
                s.send(hi)
            } catch (e: Exception) {
                e.printStackTrace()
                print(address)
            }
        }

        @JvmStatic
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

        @JvmStatic
        fun serializeHostInfoSet2StringList(list: Set<HostInfo>): ArrayList<String> {
            var gson = Gson()
            var out = ArrayList<String>()
            for(p in list) {
                out.add(gson.toJson(p))
            }
            return out
        }

        @JvmStatic
        fun deserializeStringList2HostInfoSet(list: List<String>?): MutableSet<HostInfo> {
            var gson = Gson()
            var out = mutableSetOf<HostInfo>()
            if (list != null) {
                for(s in list) {
                    out.add(gson.fromJson(s, HostInfo::class.java))
                }
            }
            return out
        }

        @JvmStatic
        fun deserializeStringSet2HostInfoSet(list: Set<String>): MutableSet<HostInfo> {
            var gson = Gson()
            var out = mutableSetOf<HostInfo>()
            for(s in list) {
                out.add(gson.fromJson(s, HostInfo::class.java))
            }
            return out
        }
    }


}