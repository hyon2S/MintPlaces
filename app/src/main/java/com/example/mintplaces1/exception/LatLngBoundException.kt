package com.example.mintplaces1.exception

import java.lang.RuntimeException

class LatLngBoundException: RuntimeException("우리 나라를 벗어난 위치는 표시할 수 없습니다.")