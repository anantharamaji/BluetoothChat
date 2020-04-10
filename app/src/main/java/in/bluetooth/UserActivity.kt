package `in`.bluetooth

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar

private const val REQUEST_CODE_ENABLE_BLUETOOTH = 1002

class UserActivity : AppCompatActivity(), View.OnClickListener {

    private var mBluetoothAdapter: BluetoothAdapter? = null
    private var mUserAdapter: UserAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_users)

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()

        val recyclerView: RecyclerView = findViewById(R.id.list_users)

        val decoration =
            DividerItemDecoration(recyclerView.context, LinearLayoutManager.VERTICAL)
        recyclerView.addItemDecoration(decoration)

        mUserAdapter = UserAdapter()
        recyclerView.adapter = mUserAdapter

        mUserAdapter?.setItemClickListener(this)
    }

    override fun onStart() {
        super.onStart()
        if (mBluetoothAdapter?.isEnabled != null && !mBluetoothAdapter!!.isEnabled) {
            enableBluetooth()
        } else populateUsers()
    }

    private fun enableBluetooth() {
        val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        startActivityForResult(intent, REQUEST_CODE_ENABLE_BLUETOOTH)
    }

    private fun populateUsers() {
        mUserAdapter?.clear()
        val users = mBluetoothAdapter?.bondedDevices
        if (users?.size != 0) {
            users?.forEach {
                mUserAdapter?.addUser(it)
            }
        }
    }

    override fun onClick(v: View?) {
        val device = v?.tag as BluetoothDevice
        val intent = Intent(this, MessageActivity::class.java)
        intent.putExtra("user_name", device.name)
        intent.putExtra("address", device.address)
        startActivity(intent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_CODE_ENABLE_BLUETOOTH -> {
                if (resultCode == Activity.RESULT_OK)
                    populateUsers()
                else {
                    Snackbar.make(
                        findViewById(android.R.id.content),
                        "Please enable bluetooth",
                        Snackbar.LENGTH_INDEFINITE
                    ).setAction(android.R.string.ok) { enableBluetooth() }.show()
                }
            }
            else ->
                super.onActivityResult(requestCode, resultCode, data)
        }
    }

}