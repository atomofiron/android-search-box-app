package ru.atomofiron.regextool.utils

import ru.atomofiron.regextool.log
import ru.atomofiron.regextool.tik
import java.io.InputStream
import java.io.OutputStream

object Shell {
    private const val SU = "su"
    private const val SH = "sh"
    private const val SUCCESS = 0

    const val LS_LAHL = "%s ls -lAhL \"%s\""
    const val NATIVE_CHMOD_X = "chmod +x \"%s\""
    const val FIND = "cd %1\$s && find %1\$s -maxdepth %2\$d -exec ls -lAhLd \"{}\" \\;"
    const val FOR_LS = "for f in `ls -A \"%s\"`; do echo \$f; ls -lAh \"%s\$f\"; done"
    //for f in `ls -A "/sdcard/"`; do ls -ld "/sdcard/$f"; if [ -d /sdcard/$f ]; then ls -lAh "/sdcard/$f"; fi; done

    fun checkSu(): Boolean {
        var ok: Boolean
        var process: Process? = null
        var outputStream: OutputStream? = null

        try {
            process = Runtime.getRuntime().exec(SU)
            outputStream = process.outputStream
            val osw = outputStream.writer()

            osw.write(SU)
            osw.flush()
            osw.close()

            ok = process.waitFor() == 0
        } catch (e: Exception) {
            ok = false
            log(e.toString())
        } finally {
            try {
                outputStream?.close()
                process?.destroy()
            } catch (e: Exception) { }
        }
        return ok
    }

    fun exec(cmd: String, su: Boolean = false): Output {
        tik("exec: $cmd")
        var success: Boolean
        var output = ""
        var error = ""

        var process: Process? = null
        var inputStream: InputStream? = null
        var outputStream: OutputStream? = null
        var errorStream: InputStream? = null

        tik("try $cmd")
        try {
        tik("try. $cmd")
            process = Runtime.getRuntime().exec(if (su) SU else SH)
            inputStream = process.inputStream
            outputStream = process.outputStream
            errorStream = process.errorStream
            val osw = outputStream.writer()

            tik("write $cmd")
            osw.write(String.format("%s\n", cmd))
            tik("flush $cmd")
            osw.flush()
            tik("close $cmd")
            osw.close()
            tik("close. $cmd")

            output = inputStream.reader().readText()
            error = errorStream.reader().readText()
            tik("readText. $cmd")
            if (output.length > 128) {
                log("output: ${output.substring(output.length - 128, output.length)}")
            } else {
                log("output: $output")
            }
            log("error: $error")
            tik("waitFor $cmd")
            success = process.waitFor() == SUCCESS
            tik("waitFor. $cmd")
        } catch (e: Exception) {
            success = false
            log(e.toString())
        } finally {
            tik("finally $cmd")
            try {
                inputStream?.close()
                outputStream?.close()
                errorStream?.close()
                process?.destroy()
            } catch (e: Exception) {
                log(e.toString())
            }
            tik("finally. $cmd")
        }
        return Output(success, output, error)
    }

    class Output(
            val success: Boolean,
            val output: String,
            val error: String
    )
}