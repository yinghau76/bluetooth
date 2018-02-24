package com.simplypatrick.gattserver

enum class ConnectionState(val state: Int) {
    Disconnected(0),
    Connecting(1),
    Connected(2),
    Disconnecting(3);

    companion object {
        private val map = ConnectionState.values().associateBy(ConnectionState::state)
        fun fromInt(state: Int) = map[state]
    }
}