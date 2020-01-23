package ru.atomofiron.regextool.iss.service.model

import java.io.File

interface XFile {
    val file: File
    val files: List<XFile>?
    val isOpened: Boolean
    val isCached: Boolean
    val isCacheActual: Boolean
    val isDirectory: Boolean

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
}