package `in`.bluetooth

import android.bluetooth.BluetoothAdapter
import android.os.Bundle
import android.os.Handler
import android.text.TextUtils
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import java.lang.ref.WeakReference

class MessageActivity : AppCompatActivity(), View.OnClickListener, TextView.OnEditorActionListener {

    private var mAdapter: MessageAdapter? = null
    private var mAddress: String? = ""
    private lateinit var mInputMessage: EditText
    private lateinit var mActionSendMessage: ImageButton
    private lateinit var mListMessages: RecyclerView

    private var mService: ConnectorService? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message)
        processIntent()
        initViews()
        setEvents()
    }

    private fun processIntent() {
        intent.let { intent ->
            intent.extras.let { bundle ->
                bundle?.containsKey("user_name").let {
                    val name = bundle?.getString("user_name")
                    supportActionBar?.title = name
                }
                bundle?.containsKey("address").let {
                    mAddress = bundle?.getString("address")
                }
            }
        }
    }

    private fun initViews() {
        mInputMessage = findViewById(R.id.input_message)
        mActionSendMessage = findViewById(R.id.action_send_message)
        mListMessages = findViewById(R.id.list_messages)

        mAdapter = MessageAdapter()
        mListMessages.adapter = mAdapter
    }

    private fun setEvents() {
        mActionSendMessage.setOnClickListener(this)
        mInputMessage.setOnEditorActionListener(this)
    }

    override fun onStart() {
        super.onStart()
        initService()
    }

    override fun onResume() {
        super.onResume()
        val adapter = BluetoothAdapter.getDefaultAdapter()
        val device = adapter.getRemoteDevice(mAddress)
        mService?.connect(device)
    }

    override fun onDestroy() {
        super.onDestroy()
        mService?.stop()
    }

    private fun initService() {
        if (mService == null)
            mService = ConnectorService(
                MessageHandler(
                    WeakReference(mAdapter),
                    WeakReference(mListMessages)
                )
            )
    }

    override fun onClick(v: View?) {
        sendMessage()
    }

    override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
        if (actionId == EditorInfo.IME_ACTION_DONE && event?.action == null) {
            sendMessage()
        }
        return true
    }

    private fun sendMessage() {
        mInputMessage.text.let { it ->
            it.toString().let {
                if (!TextUtils.isEmpty(it)) {
                    mService?.write(it.toByteArray())
                    val message = StringBuffer("")
                    mInputMessage.setText(message)
                }
            }
        }
    }

    class MessageHandler(
        private val adapter: WeakReference<MessageAdapter?>,
        private val list: WeakReference<RecyclerView>
    ) : Handler() {
        override fun handleMessage(msg: android.os.Message) {
            var adapter: MessageAdapter? = null
            this.adapter.get()?.let {
                adapter = it
            }
            var list: RecyclerView? = null
            this.list.get()?.let {
                list = it
            }
            when (msg.what) {
                MessageType.OUT.Code -> {
                    val buffer: ByteArray = msg.obj as ByteArray
                    adapter?.add(
                        Message(
                            String(buffer),
                            MessageType.OUT
                        )
                    )
                    adapter?.itemCount?.let {
                        list?.smoothScrollToPosition(it - 1)
                    }
                }
                MessageType.IN.Code -> {
                    val buffer: ByteArray = msg.obj as ByteArray
                    adapter?.add(
                        Message(
                            String(buffer, 0, msg.arg1),
                            MessageType.IN
                        )
                    )
                    adapter?.itemCount?.let {
                        list?.smoothScrollToPosition(it - 1)
                    }
                }
                else -> {

                }
            }
        }
    }
}