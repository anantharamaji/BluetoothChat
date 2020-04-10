package `in`.bluetooth

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothSocket
import android.os.Handler
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*

private const val CONNECTOR_NAME = "Bluetooth"

class ConnectorService() {

    private var mAdapter: BluetoothAdapter? = null
    private var mHandler: Handler? = null
    private val uuid: UUID = UUID.fromString("5fc03087-d265-11e7-b8c6-83e29cd24f4c");
    private var mConnectionState: ConnectionState? = null

    private var mListenTo: ListenTo? = null
    private var mConnectTo: ConnectTo? = null
    private var mConnector: Connector? = null

    constructor(handler: Handler) : this() {
        mAdapter = BluetoothAdapter.getDefaultAdapter()
        mHandler = handler
        mConnectionState = ConnectionState.NONE
    }

    fun start() {
        synchronized(this@ConnectorService) {
            mConnectTo?.let {
                mConnectTo?.close()
                mConnectTo = null
            }
            mConnector?.let {
                mConnector?.close()
                mConnector = null
            }
            if (mListenTo == null) {
                mListenTo = ListenTo()
                mListenTo?.start()
            }
            mConnectionState = ConnectionState.CONNECTING
        }
    }

    fun connect(device: BluetoothDevice) {
        synchronized(this@ConnectorService) {
            if (mConnectionState == ConnectionState.CONNECTING)
                mConnectTo?.let {
                    mConnectTo?.close()
                    mConnectTo = null
                }
            mConnector?.let {
                mConnector?.close()
                mConnector = null
            }
            if (mConnectTo == null) {
                mConnectTo = ConnectTo(device)
                mConnectTo?.start()
            }
            mConnectionState = ConnectionState.CONNECTING
        }
    }

    fun stop() {
        synchronized(this@ConnectorService) {
            mListenTo?.let {
                mListenTo?.close()
                mListenTo = null
            }
            mConnectTo?.let {
                mConnectTo?.close()
                mConnectTo = null
            }
            mConnector?.let {
                mConnector?.close()
                mConnector = null
            }
            mConnectionState = ConnectionState.CONNECTING
        }
    }

    private fun connected(socket: BluetoothSocket) {
        synchronized(this@ConnectorService) {
            // region Close existing connections
            mListenTo?.let {
                mListenTo?.close()
                mListenTo = null
            }
            mConnectTo?.let {
                mConnectTo?.close()
                mConnectTo = null
            }
            mConnector?.let {
                mConnector?.close()
                mConnector = null
            }
            // endregion
            mConnector = Connector(socket)
            mConnector?.start()
            mConnectionState = ConnectionState.CONNECTED
        }
    }

    fun write(buffer: ByteArray) {
        mConnector?.write(buffer)
    }

    private inner class ListenTo() : Thread() {

        private var mSocket: BluetoothServerSocket? = null

        init {
            mSocket = mAdapter?.listenUsingRfcommWithServiceRecord(CONNECTOR_NAME, uuid)
        }

        override fun run() {
            var socket: BluetoothSocket? = null
            while (mConnectionState != ConnectionState.CONNECTED) {
                try {
                    socket = mSocket?.accept()
                } catch (e: IOException) {

                }
                socket?.let {
                    synchronized(this@ConnectorService) {
                        when (mConnectionState) {
                            ConnectionState.CONNECTING -> {
                                connected(socket)
                            }
                            ConnectionState.CONNECTED, ConnectionState.NONE -> {
                                socket.close()
                            }
                        }
                    }
                }
            }
        }

        fun close() {
            mSocket?.close()
        }

    }

    private inner class ConnectTo(device: BluetoothDevice) : Thread() {
        private var socket: BluetoothSocket? = null
        private var device: BluetoothDevice? = null

        init {
            this.device = device
            val socket = device.createRfcommSocketToServiceRecord(uuid)
            this.socket = socket
        }

        override fun run() {
            mAdapter?.cancelDiscovery()
            try {
                socket?.connect()
            } catch (e: IOException) {
                socket?.close()
                this@ConnectorService.start()
                return
            }
            mConnectTo = null
            socket?.let { socket ->
                connected(socket)
            }
        }

        fun close() {
            socket?.close()
        }
    }

    private inner class Connector(socket: BluetoothSocket) : Thread() {
        private var socket: BluetoothSocket? = null
        private var inStream: InputStream? = null
        private var outStream: OutputStream? = null

        init {
            this.socket = socket
            var input: InputStream? = null
            var output: OutputStream? = null
            try {
                input = socket.inputStream
                output = socket.outputStream
            } catch (e: IOException) {

            }
            inStream = input
            outStream = output
        }

        override fun run() {
            val bytes = ByteArray(1024)
            var buffer = 0
            while (true) {
                try {
                    inStream?.read(bytes)?.let {
                        buffer = it
                    }
                    // update UI
                    mHandler?.obtainMessage(MessageType.IN.Code, buffer, 0, bytes)?.sendToTarget()
                } catch (e: IOException) {

                }
            }
        }

        fun write(bytes: ByteArray) {
            outStream?.write(bytes)
            // update UI
            mHandler?.obtainMessage(MessageType.OUT.Code, 0, 0, bytes)?.sendToTarget()
        }

        fun close() {
            socket?.close()
        }
    }

}