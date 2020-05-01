package ru.atomofiron.regextool.utils

import ru.atomofiron.regextool.log2
import java.io.InputStream
import java.io.OutputStream

object Shell {
    private const val LOG_LIMIT = 128
    private const val SU = "su"
    private const val SH = "sh"
    private const val SUCCESS = 0

    const val TOUCH = "%s touch \"%s\""
    const val MKDIR = "%s mkdir \"%s\""
    const val RM_RF = "%s rm -rf \"%s\""
    const val MV = "%s mv \"%s\" \"%s\""
    const val LS_LAHL = "%s ls -lAhL \"%s\""
    const val NATIVE_CHMOD_X = "chmod +x \"%s\""
    const val FIND = "cd %1\$s && find %1\$s -maxdepth %2\$d -exec ls -lAhLd \"{}\" \\;"
    const val FOR_LS = "for f in `ls -A \"%s\"`; do echo \$f; ls -lAh \"%s\$f\"; done"
    //for f in `ls -A "/sdcard/"`; do ls -ld "/sdcard/$f"; if [ -d /sdcard/$f ]; then ls -lAh "/sdcard/$f"; fi; done

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

    fun exec(cmd: String, su: Boolean): Output {
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

            osw.write(String.format("%s\n", cmd))
            osw.flush()
            osw.close()

            val tik = System.currentTimeMillis()
            output = inputStream.reader().readText()
            error = errorStream.reader().readText()
            success = process.waitFor() == SUCCESS

            log2("waitFor ${System.currentTimeMillis() - tik} $cmd")
        } catch (e: Exception) {
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