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
        var dnsInfoHolder = HostInfoHolder()
        var listItem: View? = convertView
        if (listItem == null) {
            listItem = LayoutInflater.from(mContext).inflate(R.layout.host_list_item_edit, parent, false)
            dnsInfoHolder.checkbox = listItem.findViewById(R.id.checkbox) as CheckBox
            dnsInfoHolder.dnsInfoText = listItem.findViewById(R.id.hostInfoText) as TextView
            dnsInfoHolder.ping = listItem.findViewById(R.id.ping) as TextView
            listItem.tag = dnsInfoHolder
        } else {
            dnsInfoHolder = listItem.tag as HostInfoHolder
        }
        val currentHost = allHost[position]
        val dnsId = currentHost.toString()
        if(currentHost.ping == Int.MAX_VALUE){
            dnsInfoHolder.dnsInfoText.text = dnsId
            dnsInfoHolder.ping.text=""
            dnsInfoHolder.dnsInfoText.setTextColor(Color.GRAY)
        } else {
            dnsInfoHolder.dnsInfoText.text = dnsId
            dnsInfoHolder.ping.text = currentHost.ping.toString() + " ms"
            dnsInfoHolder.dnsInfoText.setTextColor(Color.WHITE)
        }
        dnsInfoHolder.checkbox.setOnCheckedChangeListener { _, isChecked ->
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
        dnsInfoHolder.checkbox.isChecked = this.currentHost.contains(currentHost)
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

    fun sort(){
        allHost = ArrayList(allHost.sortedWith(compareBy { it.ping }))
        this.notifyDataSetChanged()
    }

    class HostInfoHolder {
        lateinit var checkbox: CheckBox
        lateinit var dnsInfoText: TextView
        lateinit var ping: TextView
    }
}