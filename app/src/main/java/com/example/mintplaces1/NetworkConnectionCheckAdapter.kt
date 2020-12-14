package com.example.mintplaces1

/*
* 네트워크 연결 상태를 확인
* */
interface NetworkConnectionCheckAdapter {
    var networkConnectionChecker: NetworkConnectionChecker
    // 네트워크 연결 상태를 알려줌
    // NetworkConnectionChecker의 isNetworkConnected()를 이용해 네트워크 연결 여부를 확인하고 사용자에게 알리도록 구현할 것.
    fun notifyNetworkConnection()
}