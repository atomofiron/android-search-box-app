package ru.atomofiron.regextool.utils

import ru.atomofiron.regextool.logE
import ru.atomofiron.regextool.logI
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream


object Shell {
    private const val SU = "su"
    private const val SH = "sh"
    private const val SUCCESS = 0

    private const val TOYBOX = "{toybox}"
    lateinit var toyboxPath: String

    const val TOUCH = "{toybox} touch \"%s\""
    const val VERSION = "{toybox} --version"
    const val MKDIR = "{toybox} mkdir \"%s\""
    const val RM_RF = "{toybox} rm -rf \"%s\""
    const val MV = "{toybox} mv \"%s\" \"%s\""
    const val LS_LAHL = "{toybox} ls -lAhL \"%s\""
    const val LS_LAHLD = "{toybox} ls -lAhLd \"%s\""

    // grep: No 'E' with 'F'
    const val FIND_GREP = "{toybox} find %s -type f -maxdepth %d \\( %s \\) | xargs {toybox} grep -c -s -E \"%s\""
    const val FIND_GREP_I = "{toybox} find %s -type f -maxdepth %d \\( %s \\) | xargs {toybox} grep -c -s -i -E \"%s\""
    const val FIND_GREP_F = "{toybox} find %s -type f -maxdepth %d \\( %s \\) | xargs {toybox} grep -c -s -F -e \"%s\""
    const val FIND_GREP_IF = "{toybox} find %s -type f -maxdepth %d \\( %s \\) | xargs {toybox} grep -c -s -i -F -e \"%s\""

    const val FIND_EXEC_GREP = "{toybox} find %s -type f -maxdepth %d \\( %s \\) -exec {toybox} grep -H -c -s -E \"%s\" {} \\;"
    const val FIND_EXEC_GREP_I = "{toybox} find %s -type f -maxdepth %d \\( %s \\) -exec {toybox} grep -H -c -s -i -E \"%s\" {} \\;"
    const val FIND_EXEC_GREP_F = "{toybox} find %s -type f -maxdepth %d \\( %s \\) -exec {toybox} grep -H -c -s -F -E \"%s\" {} \\;"
    const val FIND_EXEC_GREP_IF = "{toybox} find %s -type f -maxdepth %d \\( %s \\) -exec {toybox} grep -H -c -s -i -F -E \"%s\" {} \\;"

    const val FIND_FD = "{toybox} find %s -maxdepth %d \\( -type f -o -type d \\)"
    const val FIND_F = "{toybox} find %s -maxdepth %d -type f"

    const val HEAD_TAIL = "{toybox} head %s -n %d | {toybox} tail -n %d"

    // %s grep -c -s -F -i -e "%s" "%s"

    // FASTEST toybox find %s -name "*.%s" -type f | xargs grep "%s" -c
    // find . -maxdepth 2 -exec grep -H -c -s "k[e]" {} \;

    fun checkSu(): Output {
        var success: Boolean
        var error = ""
        var process: Process? = null
        var outputStream: OutputStream? = null
        var errorStream: InputStream? = null

        try {
            process = Runtime.getRuntime().exec(SU)
            outputStream = process.outputStream
            errorStream = process.errorStream
            val osw = outputStream.writer()

            osw.write(SU)
            osw.flush()
            osw.close()

            success = process.waitFor() == 0
            error = errorStream.reader().readText()
        } catch (e: Exception) {
            success = false
            error = e.message ?: e.toString()
        } finally {
            try {
                outputStream?.close()
                errorStream?.close()
                process?.destroy()
            } catch (e: Exception) { }
        }
        return Output(success, "", error)
    }

    fun exec(cmd: String, su: Boolean, processObserver: ((Process) -> Unit)? = null, forEachLine: ((String) -> Unit)? = null): Output {
        logI("exec $cmd")
        var success: Boolean
        var output = ""
        var error = ""

        var process: Process? = null
        var inputStream: InputStream? = null
        var outputStream: OutputStream? = null
        var errorStream: InputStream? = null

        try {
            process = Runtime.getRuntime().exec(if (su) SU else SH)
            processObserver?.invoke(process)
            inputStream = process.inputStream!!
            outputStream = process.outputStream
            errorStream = process.errorStream!!
            val osw = outputStream.writer()

            val command = cmd.replace(TOYBOX, toyboxPath)
            osw.write(String.format("%s\n", command))
            osw.flush()
            osw.close()

            val tik = System.currentTimeMillis()

            when (forEachLine) {
                null -> output = inputStream.reader().readText()
                else -> InputStreamReader(inputStream, Charsets.UTF_8).forEachLine(forEachLine)
            }
            error = errorStream.reader().readText()
            success = process.waitFor() == SUCCESS

            logI("waitFor ${System.currentTimeMillis() - tik} $cmd")
        } catch (e: Exception) {
            logE(e.toString())
            output = e.toString()
            success = false
        } finally {
            try {
                inputStream?.close()
                outputStream?.close()
                errorStream?.close()
                process?.destroy()
            } catch (e: Exception) {
            }
        }
        return Output(success, output, error)
    }

    class Output(
            val success: Boolean,
            val output: String,
            val error: String
    )
}