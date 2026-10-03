package com.glassvpn.app.ui.theme

data class AppStrings(
    val appName: String,
    val home: String,
    val servers: String,
    val settings: String,
    val tapToConnect: String,
    val tapToDisconnect: String,
    val connecting: String,
    val connected: String,
    val disconnected: String,
    val currentServer: String,
    val noServerSelected: String,
    val selectServer: String,
    val fastest: String,
    val ping: String,
    val sessions: String,
    val speed: String,
    val language: String,
    val english: String,
    val myanmar: String,
    val about: String,
    val aboutText: String,
    val refreshServers: String,
    val loadingServers: String,
    val noServers: String,
    val vpnPermissionTitle: String,
    val vpnPermissionText: String,
    val allow: String,
    val cancel: String,
    val connectedTo: String,
    val disconnectedFrom: String,
    val failedToConnect: String,
    val retry: String,
)

val EnStrings = AppStrings(
    appName = "GlassVPN",
    home = "Home",
    servers = "Servers",
    settings = "Settings",
    tapToConnect = "Tap to connect",
    tapToDisconnect = "Tap to disconnect",
    connecting = "Connecting…",
    connected = "Connected",
    disconnected = "Disconnected",
    currentServer = "Current server",
    noServerSelected = "No server selected",
    selectServer = "Select server",
    fastest = "Fastest",
    ping = "Ping",
    sessions = "Sessions",
    speed = "Speed",
    language = "Language",
    english = "English",
    myanmar = "မြန်မာ",
    about = "About",
    aboutText = "GlassVPN — free VPN with iOS Liquid Glass design.\nServers via VPNGate public relays.",
    refreshServers = "Refresh",
    loadingServers = "Loading servers…",
    noServers = "No servers found. Pull to refresh.",
    vpnPermissionTitle = "VPN Permission",
    vpnPermissionText = "GlassVPN needs permission to create a VPN connection.",
    allow = "Allow",
    cancel = "Cancel",
    connectedTo = "Connected to",
    disconnectedFrom = "Disconnected",
    failedToConnect = "Connection failed",
    retry = "Retry",
)

val MmStrings = AppStrings(
    appName = "GlassVPN",
    home = "ပင်မ",
    servers = "ဆာဗာများ",
    settings = "ဆက်တင်",
    tapToConnect = "ချိတ်ဆက်ရန် နှိပ်ပါ",
    tapToDisconnect = "ဖြုတ်ရန် နှိပ်ပါ",
    connecting = "ချိတ်ဆက်နေသည်…",
    connected = "ချိတ်ဆက်ပြီး",
    disconnected = "ချိတ်ဆက်မထားပါ",
    currentServer = "လက်ရှိဆာဗာ",
    noServerSelected = "ဆာဗာ မရွေးထားပါ",
    selectServer = "ဆာဗာရွေးပါ",
    fastest = "အမြန်ဆုံး",
    ping = "Ping",
    sessions = "အသုံးပြုသူ",
    speed = "အမြန်နှုန်း",
    language = "ဘာသာစကား",
    english = "English",
    myanmar = "မြန်မာ",
    about = "အကြောင်း",
    aboutText = "GlassVPN — iOS Liquid Glass ဒီဇိုင်းနဲ့ free VPN။\nVPNGate public relay ဆာဗာများ သုံးထားသည်။",
    refreshServers = "ပြန်ရယူ",
    loadingServers = "ဆာဗာများ ရယူနေသည်…",
    noServers = "ဆာဗာ မတွေ့ပါ။ ပြန်ရယူကြည့်ပါ။",
    vpnPermissionTitle = "VPN ခွင့်ပြုချက်",
    vpnPermissionText = "VPN ချိတ်ဆက်မှု ဖန်တီးဖို့ GlassVPN က ခွင့်ပြုချက် လိုပါတယ်။",
    allow = "ခွင့်ပြုမယ်",
    cancel = "မလုပ်ဘူး",
    connectedTo = "ချိတ်ဆက်ပြီး",
    disconnectedFrom = "ဖြုတ်လိုက်ပြီ",
    failedToConnect = "ချိတ်ဆက်မှု မအောင်မြင်ပါ",
    retry = "ထပ်ကြိုးစား",
)

fun stringsFor(lang: String): AppStrings = if (lang == "my") MmStrings else EnStrings

fun countryFlag(code: String): String = when (code) {
    "SG" -> "🇸🇬"
    "TH" -> "🇹🇭"
    "MY" -> "🇲🇾"
    "JP" -> "🇯🇵"
    "US" -> "🇺🇸"
    "PH" -> "🇵🇭"
    "MM" -> "🇲🇲"
    else -> "🌐"
}
