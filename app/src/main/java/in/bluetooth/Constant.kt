package `in`.bluetooth

enum class MessageType(val Code: Int) {
    IN(0),
    OUT(1)
}


enum class ConnectionState {
    NONE, CONNECTING, CONNECTED
}