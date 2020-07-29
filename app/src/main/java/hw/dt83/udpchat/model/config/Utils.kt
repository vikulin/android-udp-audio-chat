package hw.dt83.udpchat.model.config

import com.google.gson.Gson
import hw.dt83.udpchat.model.HostInfo
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket

class Utils {

    companion object {

        @JvmStatic
        fun ping(address: InetAddress, port: Int): Int {
            val start = System.currentTimeMillis()
            val socket = Socket()
            try {
                socket.connect(InetSocketAddress(address, port), 5000)
                socket.close()
            } catch (e: Exception) {
                e.printStackTrace()
                print(address)
                return Int.MAX_VALUE
            }
            return (System.currentTimeMillis() - start).toInt()
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