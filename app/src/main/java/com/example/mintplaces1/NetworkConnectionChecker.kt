package com.example.mintplaces1

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkRequest
import android.widget.Toast

// 네트워크 연결 상태 확인
// 연결이 끊겨있으면 연결해주지는 않고, 연결해달라고 메시지만 띄움.
class NetworkConnectionChecker(val context: Context) {
    private val connectivityManager: ConnectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    init {
        connectivityManager.registerNetworkCallback(NetworkRequest.Builder().build(), object: ConnectivityManager.NetworkCallback(){
            // 네트워크 연결이 끊기면 메시지 띄움.
            override fun onLost(network: Network) {
                super.onLost(network)
                // "네트워크 연결 상태를 확인해주세요."
                Toast.makeText(context, context.getString(R.string.request_network_connection), Toast.LENGTH_SHORT).show()
            }
        })
    }

    // 호출했을 시점에 네트워크 연결이 끊겨있으면 메시지 띄움.
    fun notifyNetworkConnection() {
        if (!isNetworkConnected())
        // "네트워크 연결 상태를 확인해주세요."
            Toast.makeText(context, context.getString(R.string.request_network_connection), Toast.LENGTH_SHORT).show()
    }

    // https://youngest-programming.tistory.com/32 참고
    private fun isNetworkConnected(): Boolean {
        val nw = connectivityManager.activeNetwork ?: return false
        val actNw = connectivityManager.getNetworkCapabilities(nw) ?: return false
        return true
    }
}
