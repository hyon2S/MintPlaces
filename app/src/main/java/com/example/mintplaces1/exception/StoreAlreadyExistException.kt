package com.example.mintplaces1.exception

import java.lang.RuntimeException

class StoreAlreadyExistException: RuntimeException("해당 매장이 이미 등록되어있습니다.")