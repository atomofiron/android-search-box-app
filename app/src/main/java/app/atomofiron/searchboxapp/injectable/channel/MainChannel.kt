package app.atomofiron.searchboxapp.injectable.channel

import android.net.Uri
import kotlinx.coroutines.channels.Channel

class MainChannel {
    val fileToReceive = Channel<Uri>()
}