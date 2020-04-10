package `in`.bluetooth

import android.bluetooth.BluetoothDevice
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class UserAdapter : RecyclerView.Adapter<UserAdapter.ViewHolder>() {

    private val devices: MutableList<BluetoothDevice> = arrayListOf()
    private var mListener: View.OnClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view: View =
            LayoutInflater.from(parent.context).inflate(R.layout.item_user, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(devices[position], mListener)
    }

    override fun getItemCount(): Int {
        return devices.size
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val user: TextView = view.findViewById(R.id.user)

        fun bind(device: BluetoothDevice, listener: View.OnClickListener?) {
            user.text = device.name

            itemView.tag = device
            itemView.setOnClickListener(listener)
        }
    }

    fun addUser(device: BluetoothDevice) {
        devices.add(device)
        notifyItemInserted(devices.size)
    }

    fun clear() {
        devices.clear()
        notifyDataSetChanged()
    }

    fun setItemClickListener(listener: View.OnClickListener) {
        mListener = listener
    }

}