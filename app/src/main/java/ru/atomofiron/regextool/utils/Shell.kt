package ru.atomofiron.regextool.utils

import ru.atomofiron.regextool.logE
import ru.atomofiron.regextool.logI
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream


object Shell {
    const val SU = "su"
    const val SH = "sh"
    const val SUCCESS = 0

    private val TOYBOX = "{toybox}"
    lateinit var toyboxPath: String

    const val TOUCH = "{toybox} touch \"%s\""
    const val MKDIR = "{toybox} mkdir \"%s\""
    const val RM_RF = "{toybox} rm -rf \"%s\""
    const val MV = "{toybox} mv \"%s\" \"%s\""
    const val LS_LAHL = "{toybox} ls -lAhL \"%s\""

    // grep: No 'E' with 'F'
    const val FIND_GREP = "{toybox} find %s -type f -maxdepth %d \\( %s \\) | xargs {toybox} grep -c -s -E \"%s\""
    const val FIND_GREP_I = "{toybox} find %s -type f -maxdepth %d \\( %s \\) | xargs {toybox} grep -c -s -i -E \"%s\""
    const val FIND_GREP_F = "{toybox} find %s -type f -maxdepth %d \\( %s \\) | xargs {toybox} grep -c -s -F -e \"%s\""
    const val FIND_GREP_IF = "{toybox} find %s -type f -maxdepth %d \\( %s \\) | xargs {toybox} grep -c -s -i -F -e \"%s\""

    const val FIND_EXEC_GREP = "{toybox} find %s -type f -maxdepth %d \\( %s \\) -exec {toybox} grep -H -c -s -E \"%s\" {} \\;"
    const val FIND_EXEC_GREP_I = "{toybox} find %s -type f -maxdepth %d \\( %s \\) -exec {toybox} grep -H -c -s -i -E \"%s\" {} \\;"
    const val FIND_EXEC_GREP_F = "{toybox} find %s -type f -maxdepth %d \\( %s \\) -exec {toybox} grep -H -c -s -F -E \"%s\" {} \\;"
    const val FIND_EXEC_GREP_IF = "{toybox} find %s -type f -maxdepth %d \\( %s \\) -exec {toybox} grep -H -c -s -i -F -E \"%s\" {} \\;"

    const val FIND = "{toybox} find %s -type f -maxdepth %d -exec ls -lAhLd \"{}\" \\;"
    const val FOR_LS = "for f in `ls -A \"%s\"`; do echo \$f; ls -lAh \"%s\$f\"; done"
    //for f in `ls -A "/sdcard/"`; do ls -ld "/sdcard/$f"; if [ -d /sdcard/$f ]; then ls -lAh "/sdcard/$f"; fi; done

    // %s grep -c -s -F -i -e "%s" "%s"

    // FASTEST toybox find %s -name "*.%s" -type f | xargs grep "%s" -c
    // find . -maxdepth 2 -exec grep -H -c -s "k[e]" {} \;

    /*
    toybox find /storage/emulated/0/0/ -type f -maxdepth 1024 \( -name '*.txt' -o -name '*.java' -o -name '*.xml' -o -name '*.html' -o -name '*.htm' -o -name '*.smali' -o -name '*.log' -o -name '*.js' -o -name '*.css' -o -name '*.json' -o -name '*.kt' -o -name '*.md' -o -name '*.mkd' -o -name '*.markdown' -o -name '*.cm' -o -name '*.ad' -o -name '*.adoc' \) | xargs toybox grep -c -s -F -e "kva"
     */

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

    fun exec(cmd: String, su: Boolean, forEachLine: ((String) -> Unit)? = null): Output {
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
            inputStream = process.inputStream
            outputStream = process.outputStream
            errorStream = process.errorStream
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