package ru.atomofiron.regextool.utils

import ru.atomofiron.regextool.log
import java.io.InputStream
import java.io.OutputStream

object Shell {
    private const val SU = "su"
    private const val SH = "sh"
    private const val SUCCESS = 0

    const val LS_LAH = "ls -lah \"%s\""

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

            output = inputStream.reader().readText()
            error = errorStream.reader().readText()
            success = process.waitFor() == SUCCESS
        } catch (e: Exception) {
            success = false
            log(e.toString())
        } finally {
            try {
                inputStream?.close()
                outputStream?.close()
                errorStream?.close()
                process?.destroy()
            } catch (e: Exception) {
                log(e.toString())
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