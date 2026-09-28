package org.yodgorbek.fieldai

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform