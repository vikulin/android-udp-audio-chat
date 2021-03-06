package hw.dt83.udpchat

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import hw.dt83.udpchat.model.HostInfo
import hw.dt83.udpchat.model.config.SelectHostInfoListAdapter
import hw.dt83.udpchat.model.config.Utils.Companion.ping
import hw.dt83.udpchat.model.config.Utils.Companion.pong
import hw.dt83.udpchat.model.config.Utils.Companion.serializeHostInfoSet2StringList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.SocketException

class MainActivity : Activity() {

    companion object {
        const val HOST_LIST = "HOST_LIST"
        const val LOG_TAG = "UDPchat"
        const val LISTENER_PORT = 50003
        const val BROADCAST_PORT = 50002
        private const val BUF_SIZE = 1024
        const val EXTRA_CONTACT = "hw.dt83.udpchat.CONTACT"
        const val EXTRA_IP = "hw.dt83.udpchat.IP"
    }

    private var contactManager: ContactManager? = null
    private var displayName: String? = null
    private var STARTED = false
    private var IN_CALL = false
    private var LISTEN = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Log.i(LOG_TAG, "UDPChat started")
        contactManager = ContactManager(this.baseContext)

        var hostList = contactManager!!.getContacts()
        var adapter = SelectHostInfoListAdapter(this, ArrayList(hostList), HashSet())
        findViewById<ListView>(R.id.peerList).adapter = adapter
        startCallListener(adapter)
        val btnAddContact = findViewById<View>(R.id.buttonAddContact) as Button
        btnAddContact.setOnClickListener {
            addNewContact(adapter)
        }

        // CALL BUTTON
        // Attempts to initiate an audio chat session with the selected device
        val btnCall = findViewById<View>(R.id.buttonCall) as Button
        btnCall.setOnClickListener(View.OnClickListener {

            var selectedHost = adapter.getSelectedHost()

            if (selectedHost.isEmpty()) {
                // If no device was selected, present an error message to the user
                Log.w(LOG_TAG, "Warning: no contact selected")
                val alert = AlertDialog.Builder(this@MainActivity).create()
                alert.setTitle("Oops")
                alert.setMessage("You must select a contact first")
                alert.setButton(-1, "OK") { dialog, which -> alert.dismiss() }
                alert.show()
                return@OnClickListener
            }
            // Collect details about the selected contact
            IN_CALL = true

            // Send this information to the MakeCallActivity and start that activity
            val intent = Intent(this@MainActivity, MakeCallActivity::class.java)
            intent.putStringArrayListExtra(HOST_LIST, serializeHostInfoSet2StringList(selectedHost))
            startActivity(intent)
        })
        if (ContextCompat.checkSelfPermission(this,
                        Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.RECORD_AUDIO),
                    1234);
        }
    }

    private fun addNewContact(adapter: SelectHostInfoListAdapter) {
        val view: View = LayoutInflater.from(this).inflate(R.layout.new_host_dialog, null)

        val ab: AlertDialog.Builder = AlertDialog.Builder(this)
        ab.setCancelable(true).setView(view)
        var ad = ab.show()
        var addButton = view.findViewById<Button>(R.id.add)
        addButton.setOnClickListener{
            var ipInput = view.findViewById<TextView>(R.id.ipInput)
            var ip = ipInput.text.toString().toLowerCase()
            GlobalScope.launch {
                var di = HostInfo(InetAddress.getByName("["+ip+"]"),"User contact")
                withContext(Dispatchers.Main) {
                    adapter.addItem(0, di)
                    adapter.notifyDataSetChanged()
                    ad.dismiss()
                    contactManager?.addContact(di)
                }
            }
        }
    }

    private fun startCallListener(adapter: SelectHostInfoListAdapter) {
        // Creates the listener thread
        LISTEN = true
        val pingMap = mutableMapOf<String, Long?>()
        val pongMap = mutableMapOf<String, Long?>()
        val listener = Thread(Runnable {
            try {
                // Set up the socket and packet to receive
                Log.i(LOG_TAG, "Incoming call listener started")
                val socket = DatagramSocket(LISTENER_PORT)
                socket.soTimeout = 1000
                val buffer = ByteArray(BUF_SIZE)
                val packet = DatagramPacket(buffer, BUF_SIZE)
                while (LISTEN) {
                    // Listen for incoming call requests
                    try {
                        Log.i(LOG_TAG, "Listening for incoming calls")
                        socket.receive(packet)
                        val data = String(buffer, 0, packet.length)
                        Log.i(LOG_TAG, "Packet received from " + packet.address + " with contents: " + data)
                        val action = data.substring(0, 4)
                        if (action == "CAL:") {
                            // Received a call request. Start the ReceiveCallActivity
                            val address = packet.address.toString()
                            val name = data.substring(4, packet.length)
                            val intent = Intent(this@MainActivity, ReceiveCallActivity::class.java)
                            intent.putExtra(EXTRA_CONTACT, name)
                            intent.putExtra(EXTRA_IP, address.substring(1, address.length))
                            IN_CALL = true
                            LISTEN = false;
                            stopCallListener();
                            startActivity(intent)
                        } else
                        if (action == "PING") {
                            // Received a ping request. Respond with pong
                            //val address = packet.address.toString()
                            val timestamp = data.substring(4, packet.length).toLong()
                            pong(packet.address, timestamp, LISTENER_PORT)
                        } else
                        if (action == "PONG") {
                            // Received a pong response
                            //update contact list with ping timestamp delta
                            val timestamp = data.substring(4, packet.length).toLong()
                            pongMap[packet.address.toString()] = System.currentTimeMillis()
                            var delta = System.currentTimeMillis() - timestamp
                            runOnUiThread {
                                adapter.updatePing(packet.address, delta.toInt())
                            }
                        } else {
                            // Received an invalid request
                            Log.w(LOG_TAG, packet.address.toString() + " sent invalid message: " + data)
                        }
                    } catch (e: Exception) {
                    }
                }
                Log.i(LOG_TAG, "Call Listener ending")
                socket.disconnect()
                socket.close()
            } catch (e: SocketException) {
                Log.e(LOG_TAG, "SocketException in listener $e")
            }
        })
        val pinger = Thread(Runnable {
            while (LISTEN) {
                //Send ping to all
                var hosts = adapter.getAllItems()
                for (h in hosts) {
                    val timestamp = System.currentTimeMillis()
                    pingMap[h.address.toString()] = timestamp
                    ping(h.address, timestamp, LISTENER_PORT)
                }
                Thread.sleep(10)
            }
        })

        val updater = Thread(Runnable {
            while (LISTEN) {
                var hosts = adapter.getAllItems()
                for (h in hosts) {
                    if(pongMap[h.address.toString()]!=null) {
                        var delta = pingMap[h.address.toString()]!! - pongMap[h.address.toString()]!!
                        if(delta>=5000){
                            adapter.updatePing(h.address, Int.MAX_VALUE)
                        }
                    }
                }
                runOnUiThread {
                    adapter.notifyDataSetChanged()
                }
                Thread.sleep(2000)
            }
        })

        listener.start()
        pinger.start()
        updater.start()
    }

    private fun stopCallListener() {
        // Ends the listener thread
        LISTEN = false
    }

    public override fun onPause() {
        super.onPause()
        if (STARTED) {

            //contactManager.bye(displayName);
            //contactManager.stopBroadcasting();
            //contactManager.stopListening();
            STARTED = false;
        }
        stopCallListener()
        Log.i(LOG_TAG, "App paused!")
    }

    public override fun onStop() {
        super.onStop()
        Log.i(LOG_TAG, "App stopped!")
        stopCallListener()
        if (!IN_CALL) {
            finish()
        }
    }

    public override fun onRestart() {
        super.onRestart()
        Log.i(LOG_TAG, "App restarted!")
        IN_CALL = false
        STARTED = true
        contactManager = ContactManager(this.baseContext)
        startCallListener(findViewById<ListView>(R.id.peerList).adapter as SelectHostInfoListAdapter)
    }

}