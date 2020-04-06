package ru.atomofiron.regextool.injectable.service.explorer.model

interface XFile {
    val files: List<XFile>?
    val isOpened: Boolean
    val isCached: Boolean
    val isCacheActual: Boolean
    val isDirectory: Boolean
    val isFile: Boolean
    val exists: Boolean

    /* путь + / */
    val completedPath: String
    /* путь + / */
    val completedParentPath: String

    // -rw-r-----  1 root everybody   5348187 2019-06-13 18:19 Magisk-v19.3.zip
    val access: String
    val owner: String
    val group: String
    val size: String
    val date: String
    val time: String
    val name: String
    val suffix: String

    val root: Int
    val isRoot: Boolean
    val isChecked: Boolean
    val isDeleting: Boolean
}