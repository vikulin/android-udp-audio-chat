package hw.dt83.udpchat

import android.content.Context
import android.content.SharedPreferences

import androidx.preference.PreferenceManager
import hw.dt83.udpchat.model.HostInfo
import hw.dt83.udpchat.model.config.Utils.Companion.deserializeStringSet2HostInfoSet
import hw.dt83.udpchat.model.config.Utils.Companion.serializeHostInfoSet2StringList
import java.net.*
import java.util.*

class ContactManager {

    constructor(baseContext: Context){
        val preferences =
                PreferenceManager.getDefaultSharedPreferences(baseContext)
        this.preferences = preferences
    }
    lateinit var preferences: SharedPreferences
    private var broadcastIP: InetAddress? = null

    fun getContacts(): MutableSet<HostInfo> {
        return deserializeStringSet2HostInfoSet(preferences.getStringSet(CURRENT_HOST, HashSet())!!)
    }

    fun addContact(hi: HostInfo) {
        var c = getContacts()
        c.add(hi)
        preferences.edit().putStringSet(CURRENT_HOST, HashSet(serializeHostInfoSet2StringList(c))).apply()
    }

    fun removeContact(name: String) {

    }



    companion object {
        private const val LOG_TAG = "ContactManager"
        const val BROADCAST_PORT = 50001 // Socket on which packets are sent/received
        private const val BROADCAST_INTERVAL = 10000 // Milliseconds
        private const val BROADCAST_BUF_SIZE = 1024
        const val CURRENT_HOST = "CURRENT_HOST"
    }

    init {
        broadcastIP = broadcastIP
        //listen();
        //broadcastName(name, broadcastIP);
    }
}