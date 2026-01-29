package com.attributecore.manager

import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import taboolib.common.platform.function.getDataFolder

object ScriptErrorLogger {

    private val logFile by lazy {
        val folder = File(getDataFolder(), "scripts")
        if (!folder.exists()) folder.mkdirs()
        File(folder, "script_errors.log").apply {
            if (!exists()) createNewFile()
        }
    }

    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

    fun logError(fileName: String, error: Exception) {
        val timestamp = LocalDateTime.now().format(dateFormatter)
        val errorLog = """
            [$timestamp] 脚本加载失败: $fileName
            错误类型: ${error.javaClass.simpleName}
            错误信息: ${error.message}
            堆栈跟踪:
            ${error.stackTraceToString()}
            ----------------------------------------
        """.trimIndent()

        synchronized(logFile) {
            BufferedWriter(FileWriter(logFile, true)).use { writer ->
                writer.write(errorLog)
                writer.newLine()
            }
        }
    }

    fun logRuntimeError(scriptId: String, functionName: String, error: Exception) {
        val timestamp = LocalDateTime.now().format(dateFormatter)
        val errorLog = """
            [$timestamp] 脚本运行时错误: $scriptId::$functionName
            错误类型: ${error.javaClass.simpleName}
            错误信息: ${error.message}
            堆栈跟踪:
            ${error.stackTraceToString()}
            ----------------------------------------
        """.trimIndent()

        synchronized(logFile) {
            BufferedWriter(FileWriter(logFile, true)).use { writer ->
                writer.write(errorLog)
                writer.newLine()
            }
        }
    }

    fun clearLog() {
        logFile.writeText("")
    }
}