package `in`.bluetooth

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MessageAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var mMessages: MutableList<Message> = arrayListOf()

    class In(view: View) : RecyclerView.ViewHolder(view) {
        private var text = view.findViewById<TextView>(R.id.message)

        fun bind(message: String?) {
            text.text = message
        }
    }

    class Out(view: View) : RecyclerView.ViewHolder(view) {
        private var text = view.findViewById<TextView>(R.id.message)

        fun bind(message: String?) {
            text.text = message
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if (viewType == MessageType.IN.Code)
            return In(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.item_message_in,
                    parent,
                    false
                )
            )
        else
            return Out(
                LayoutInflater.from(parent.context).inflate(
                    R.layout.item_message_out,
                    parent,
                    false
                )
            )
    }

    override fun getItemCount(): Int {
        return mMessages.size
    }

    override fun getItemViewType(position: Int): Int {
        return mMessages[position].messageType.Code
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = mMessages[position]
        if (message.messageType.Code == MessageType.IN.Code)
            (holder as In).bind(message.message)
        else
            (holder as Out).bind(message.message)
    }

    fun add(message: Message) {
        mMessages.add(message)
        notifyItemInserted(mMessages.size)
    }

}