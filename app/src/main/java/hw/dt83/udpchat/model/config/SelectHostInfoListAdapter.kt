package hw.dt83.udpchat.model.config

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.TextView
import hw.dt83.udpchat.R
import hw.dt83.udpchat.model.HostInfo
import java.net.InetAddress

class SelectHostInfoListAdapter(
        context: Context,
        allHosts: List<HostInfo>,
        currentHost: MutableSet<HostInfo>
) : ArrayAdapter<HostInfo?> (context, 0, allHosts) {

    private val mContext: Context = context
    private var allHost: MutableList<HostInfo> = allHosts as MutableList<HostInfo>
    private var currentHost: MutableSet<HostInfo> = currentHost

    override fun getItem(position: Int): HostInfo? {
        return allHost[position]
    }

    override fun getCount(): Int {
        return allHost.size
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var hostInfoHolder = HostInfoHolder()
        var listItem: View? = convertView
        if (listItem == null) {
            listItem = LayoutInflater.from(mContext).inflate(R.layout.host_list_item_edit, parent, false)
            hostInfoHolder.checkbox = listItem.findViewById(R.id.checkbox) as CheckBox
            hostInfoHolder.hostInfoText = listItem.findViewById(R.id.hostInfoText) as TextView
            hostInfoHolder.ping = listItem.findViewById(R.id.ping) as TextView
            listItem.tag = hostInfoHolder
        } else {
            hostInfoHolder = listItem.tag as HostInfoHolder
        }
        val currentHost = allHost[position]
        val hostAddress = currentHost.toString()
        if(currentHost.ping == Int.MAX_VALUE){
            hostInfoHolder.hostInfoText.text = hostAddress
            hostInfoHolder.ping.text=""
            hostInfoHolder.hostInfoText.setTextColor(Color.GRAY)
        } else {
            hostInfoHolder.hostInfoText.text = hostAddress
            hostInfoHolder.ping.text = currentHost.ping.toString() + " ms"
            hostInfoHolder.hostInfoText.setTextColor(Color.WHITE)
        }
        hostInfoHolder.checkbox.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked){
                if(!this.currentHost.contains(currentHost)){
                    this.currentHost.add(currentHost)
                }
            } else {
                if(this.currentHost.contains(currentHost)){
                    this.currentHost.remove(currentHost)
                }
            }
        }
        hostInfoHolder.checkbox.isChecked = this.currentHost.contains(currentHost)
        return listItem!!
    }

    fun getSelectedHost(): Set<HostInfo> {
        return currentHost
    }

    fun addItem(peerInfo: HostInfo){
        allHost.add(peerInfo)
    }

    fun addItem(index: Int, peerInfo: HostInfo){
        allHost.add(index, peerInfo)
    }

    fun addAll(index: Int, dnsInfo: ArrayList<HostInfo>){
        currentHost.addAll(dnsInfo)
        allHost.removeAll(dnsInfo)
        allHost.addAll(index, dnsInfo)
        this.notifyDataSetChanged()
    }

    fun updatePing(address: InetAddress, ping: Int){
        for (h in allHost){
            if (h.address.equals(address)){
                h.ping = ping
            }
        }
    }

    fun getAllItems(): MutableList<HostInfo>{
        return allHost
    }

    fun sort(){
        allHost = ArrayList(allHost.sortedWith(compareBy { it.ping }))
        this.notifyDataSetChanged()
    }

    class HostInfoHolder {
        lateinit var checkbox: CheckBox
        lateinit var hostInfoText: TextView
        lateinit var ping: TextView
    }
}