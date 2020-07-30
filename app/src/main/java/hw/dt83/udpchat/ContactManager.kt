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
        const val CURRENT_HOST = "CURRENT_HOST"
    }
}