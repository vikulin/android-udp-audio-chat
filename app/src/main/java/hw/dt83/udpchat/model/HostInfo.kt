package hw.dt83.udpchat.model

import java.net.InetAddress


class HostInfo {

    constructor(address: InetAddress, description: String){
        this.address = address
        this.description = description
    }

    var address: InetAddress
    var description: String
    var ping: Int = Int.MAX_VALUE

    override fun toString(): String {
        return "[" + address.toString().substring(1) + "]"
    }

    override fun equals(other: Any?): Boolean {
        return toString() == other.toString()
    }

}
