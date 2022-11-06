package app.atomofiron.searchboxapp.model.explorer

sealed class NodeError {
    object NoSuchFile : NodeError()
    object PermissionDenied : NodeError()
    object Unknown : NodeError()
    object Multiply : NodeError()
    class Message(val message: String) : NodeError()
}