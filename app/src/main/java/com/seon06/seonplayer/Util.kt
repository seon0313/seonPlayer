package com.seon06.seonplayer

import java.util.Locale

fun getDisplayLanguage(languageCode: String): String {
    return try {
        val locale = Locale(languageCode)
        locale.displayLanguage // 현재 Locale에 맞는 언어 이름 반환
    } catch (e: IllegalArgumentException) {
        "unknown" // 잘못된 언어 코드인 경우 예외 처리
    }
}
