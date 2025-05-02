// ファイルIO系の共通化された関数

import java.io.File
import java.io.FileWriter
import java.io.BufferedWriter
import java.time.LocalDateTime
import java.io.IOException

// 1行分のCSVデータを書き込み
fun writeToCsv(filePath: String, data: List<String>) {
    try {
        val file = File(filePath)
        // ファイルが存在しない場合は新規作成し、ある場合は追記
        val writer = BufferedWriter(FileWriter(file, true)) // true は追記モード
        writer.append(data.joinToString(","))
        writer.newLine()
        writer.close()
    } catch (e: Exception) {
        writeError("[FileUtils] writeToCsv: ${e.message}")
    }
}

// テキストデータを書き込み
fun writeToTextFile(filePath: String, line: String) {
    try {
        val file = File(filePath)
        // ファイルが存在しない場合は新規作成し、ある場合は追記
        val writer = BufferedWriter(FileWriter(file, true)) // true は追記モード
        writer.append(line)
        writer.newLine()
        writer.close()
    } catch (e: Exception) {
        writeError("[FileUtils] writeToTextFile: ${e.message}") // writeToTextFile で失敗してるのに走るか怪しいが。
    }
}

// error.txt にエラーを書き込み
fun writeError(line: String) {
    val error = LocalDateTime.now().toString() + " " + line // タイムスタンプの追加
    println(error)

    // ドキュメントフォルダの取得
    val documentsDir = System.getProperty("user.home") + "/Documents"
    val appFolderPath = "$documentsDir/MyPortal"
    writeToTextFile(filePath = "$appFolderPath/error.txt", line = error)
}

// Document フォルダにアプリ用のフォルダとファイルを整備する関数
fun setupAppDirectoryAndFiles(): Boolean {
    // ドキュメントフォルダの取得
    val documentsDir = System.getProperty("user.home") + "/Documents"
    val appFolderPath = "$documentsDir/MyPortal"

    try {
        // 1. アプリフォルダの作成
        val appFolder = File(appFolderPath)
        if (!appFolder.exists()) {
            if (!appFolder.mkdirs()) {
                // println("アプリフォルダの作成に失敗しました: $appFolderPath")
                return false
            }
            // println("アプリフォルダを作成しました: $appFolderPath")
        } else {
            // println("アプリフォルダは既に存在します: $appFolderPath")
        }

        // 2. transaction.csv の生成
        val aCsvFile = File("$appFolderPath/transaction.csv")
        if (!aCsvFile.exists()) {
            aCsvFile.createNewFile()

            // ヘッダーを書き込み
            val header = listOf("id", "type", "item", "date", "amount", "paymentMethod", "memo", "category", "isPrivate")
            writeToCsv(filePath = "$appFolderPath/transaction.csv", data = header)

            // println("transaction.csvを作成しました: ${aCsvFile.absolutePath}")
        } else {
            // println("transaction.csvは既に存在します: ${aCsvFile.absolutePath}")
        }

        // 3. error.txt の生成
        val bTxtFile = File("$appFolderPath/error.txt")
        if (!bTxtFile.exists()) {
            bTxtFile.createNewFile()
            // println("error.txtを作成しました: ${bTxtFile.absolutePath}")
        } else {
            // println("error.txtは既に存在します: ${bTxtFile.absolutePath}")
        }

        return true
    } catch (e: IOException) {
        println("[FileUtils] setupAppDirectoryAndFiles: エラーが発生しました: ${e.message}")
        return false
    }
}
