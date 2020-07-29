package hw.dt83.udpchat

import android.util.Log
import java.net.*
import java.util.*

class ContactManager {
    val contacts: HashMap<String, InetAddress>
    private var broadcastIP: InetAddress? = null

    fun addContact(name: String, address: InetAddress) {
        // If the contact is not already known to us, add it
        if (!contacts.containsKey(name)) {
            Log.i(LOG_TAG, "Adding contact: $name")
            contacts[name] = address
            Log.i(LOG_TAG, "#Contacts: " + contacts.size)
            return
        }
        Log.i(LOG_TAG, "Contact already exists: $name")
        return
    }

    fun removeContact(name: String) {
        // If the contact is known to us, remove it
        if (contacts.containsKey(name)) {
            Log.i(LOG_TAG, "Removing contact: $name")
            contacts.remove(name)
            Log.i(LOG_TAG, "#Contacts: " + contacts.size)
            return
        }
        Log.i(LOG_TAG, "Cannot remove contact. $name does not exist.")
        return
    }



    companion object {
        private const val LOG_TAG = "ContactManager"
        const val BROADCAST_PORT = 50001 // Socket on which packets are sent/received
        private const val BROADCAST_INTERVAL = 10000 // Milliseconds
        private const val BROADCAST_BUF_SIZE = 1024
    }

    init {
        contacts = HashMap()
        broadcastIP = broadcastIP
        //listen();
        //broadcastName(name, broadcastIP);
    }
}