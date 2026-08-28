package com.ownly.dash.platform

enum class DashPlatform {
    Mobile,
    Web,
}

expect val currentDashPlatform: DashPlatform

val DashPlatform.isMobile: Boolean get() = this == DashPlatform.Mobile
val DashPlatform.isWeb: Boolean get() = this == DashPlatform.Web
