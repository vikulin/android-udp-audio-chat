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
        allDNS: List<HostInfo>,
        currentDNS: MutableSet<HostInfo>
) : ArrayAdapter<HostInfo?> (context, 0, allDNS) {

    private val mContext: Context = context
    private var allDNS: MutableList<HostInfo> = allDNS as MutableList<HostInfo>
    private var currentDNS: MutableSet<HostInfo> = currentDNS

    override fun getItem(position: Int): HostInfo? {
        return allDNS[position]
    }

    override fun getCount(): Int {
        return allDNS.size
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var dnsInfoHolder = DNSInfoHolder()
        var listItem: View? = convertView
        if (listItem == null) {
            listItem = LayoutInflater.from(mContext).inflate(R.layout.host_list_item_edit, parent, false)
            dnsInfoHolder.checkbox = listItem.findViewById(R.id.checkbox) as CheckBox
            dnsInfoHolder.dnsInfoText = listItem.findViewById(R.id.hostInfoText) as TextView
            dnsInfoHolder.ping = listItem.findViewById(R.id.ping) as TextView
            listItem.tag = dnsInfoHolder
        } else {
            dnsInfoHolder = listItem.tag as DNSInfoHolder
        }
        val currentDNS = allDNS[position]
        val dnsId = currentDNS.toString()
        if(currentDNS.ping == Int.MAX_VALUE){
            dnsInfoHolder.dnsInfoText.text = dnsId
            dnsInfoHolder.ping.text=""
            dnsInfoHolder.dnsInfoText.setTextColor(Color.GRAY)
        } else {
            dnsInfoHolder.dnsInfoText.text = dnsId
            dnsInfoHolder.ping.text = currentDNS.ping.toString() + " ms"
            dnsInfoHolder.dnsInfoText.setTextColor(Color.WHITE)
        }
        dnsInfoHolder.checkbox.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked){
                if(!this.currentDNS.contains(currentDNS)){
                    this.currentDNS.add(currentDNS)
                }
            } else {
                if(this.currentDNS.contains(currentDNS)){
                    this.currentDNS.remove(currentDNS)
                }
            }
        }
        dnsInfoHolder.checkbox.isChecked = this.currentDNS.contains(currentDNS)
        return listItem!!
    }

    fun getSelectedHost(): Set<HostInfo> {
        return currentDNS
    }

    fun addItem(peerInfo: HostInfo){
        allDNS.add(peerInfo)
    }

    fun addItem(index: Int, peerInfo: HostInfo){
        allDNS.add(index, peerInfo)
    }

    fun addAll(index: Int, dnsInfo: ArrayList<HostInfo>){
        currentDNS.addAll(dnsInfo)
        allDNS.removeAll(dnsInfo)
        allDNS.addAll(index, dnsInfo)
        this.notifyDataSetChanged()
    }

    fun sort(){
        allDNS = ArrayList(allDNS.sortedWith(compareBy { it.ping }))
        this.notifyDataSetChanged()
    }

    class DNSInfoHolder {
        lateinit var checkbox: CheckBox
        lateinit var dnsInfoText: TextView
        lateinit var ping: TextView
    }
}