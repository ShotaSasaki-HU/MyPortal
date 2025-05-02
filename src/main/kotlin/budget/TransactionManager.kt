package budget

import androidx.compose.runtime.mutableStateListOf
import writeError
import writeToCsv
import java.io.File
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.collections.map

object TransactionManager {
    private val _transactions = mutableStateListOf<Transaction>()

    init {
        // 初期化時にCSVファイルから収支データを読み込む
        this.loadTransactionsFromCsv()
    }

    // CSVファイルから収支を読み込んで _transactions に追加
    private fun loadTransactionsFromCsv() {
        try {
            // ドキュメントフォルダの取得
            val documentsDir = System.getProperty("user.home") + "/Documents"
            val appFolderPath = "$documentsDir/MyPortal"
            val filePath = "$appFolderPath/transaction.csv"
            val file = File(filePath)
            if (file.exists()) {
                file.readLines().drop(1) // ヘッダ行をスキップ
                    .forEach { line ->
                        val fields = line.split(",")
                        if (fields.size == 9) {
                            val id = fields[0].toInt()
                            val type = fields[1]
                            val item = fields[2]
                            val date = LocalDate.parse(fields[3], DateTimeFormatter.ISO_DATE)
                            val amount = fields[4].toInt()
                            val paymentMethod = fields[5]
                            val memo = fields[6]
                            val category = fields[7]
                            val isPrivate = fields[8].toBoolean()

                            // 読み込んだデータを Expenditure か Income に変換してリストに追加
                            if (type == "Expenditure") {
                                this.addTransaction(
                                    Expenditure(ide = id, it = item, d = date, a = amount, pm = paymentMethod, m = memo, c = category, ip = isPrivate)
                                )
                            } else if (type == "Income") {
                                this.addTransaction(
                                    Income(ide = id, it = item, d = date, a = amount, m = memo, ip = isPrivate)
                                )
                            } else {
                                writeError("[budget.TransactionManager] loadTransactionFromCsv: typeが不正です。この行は自動的に削除されます。: $line")
                            }
                        } else {
                            writeError("[budget.TransactionManager] loadTransactionFromCsv: 行データが破損しています。この行は自動的に削除されます。: $line")
                        }
                    }
            } else {
                writeError("[budget.TransactionManager] loadTransactionFromCsv: ファイルが存在しません。: $filePath")
            }
        } catch (e: Exception) {
            writeError("[budget.TransactionManager] loadTransactionFromCsv: CSV読み込み中にエラーが発生しました。: ${e.message}")
        }
    }

    // _transactions のデータをCSVファイルに出力
    fun saveTransactionsToCsv() {
        try {
            // ドキュメントフォルダの取得
            val documentsDir = System.getProperty("user.home") + "/Documents"
            val appFolderPath = "$documentsDir/MyPortal"
            val tempFilePath = "$appFolderPath/temp_transaction.csv"
            val finalFilePath = "$appFolderPath/transaction.csv"

            val tempFile = File(tempFilePath)
            val finalFile = File(finalFilePath)

            tempFile.writeText("") // 一時ファイルをクリア
            val header = listOf("id", "type", "item", "date", "amount", "paymentMethod", "memo", "category", "isPrivate")
            writeToCsv(filePath = tempFilePath, data = header) // ヘッダーを書き込み

            // _transactions をCSV形式に変換して書き込み
            for (transaction in _transactions) {
                // inputCheckで検証
                if (transaction.inputCheck()) {
                    val data = when (transaction) {
                        is Expenditure -> listOf<String>(
                            transaction.id.toString(),
                            transaction::class.simpleName.toString(),
                            transaction.item,
                            transaction.date.toString(),
                            transaction.amount.toString(),
                            transaction.paymentMethod,
                            transaction.memo,
                            transaction.category,
                            transaction.isPrivate.toString()
                        )
                        is Income -> listOf<String>(
                            transaction.id.toString(),
                            transaction::class.simpleName.toString(),
                            transaction.item,
                            transaction.date.toString(),
                            transaction.amount.toString(),
                            "",
                            transaction.memo,
                            "",
                            transaction.isPrivate.toString()
                        )
                        else -> {
                            writeError("[budget.TransactionManager] saveTransactionsToCsv: クラスが不正なため、このデータは保存されませんでした。:\n${transaction.showInfo()}")
                            continue
                        }
                    }
                    writeToCsv(filePath = tempFilePath, data = data)
                } else {
                    writeError("[budget.TransactionManager] saveTransactionsToCsv: inputCheckにより書き込みが阻止されました。:\n${transaction.showInfo()}")
                }
            }

            // 書き込み成功後、一時ファイルを正式なファイルに置き換え
            if (tempFile.exists()) {
                tempFile.copyTo(finalFile, overwrite = true)
                tempFile.delete()
            }
        } catch (e: Exception) {
            writeError("[budget.TransactionManager] saveTransactionsToCsv: ${e.message}")
        }
    }

    // 収支を追加するメソッド
    fun addTransaction(transaction: Transaction): Boolean {
        val usedIds = _transactions.map { it.id }.toSet() // 既に使用中のIDをセットに格納
        // IDが割り当てられていない場合は自動割り当て
        if (transaction.id < 0) {
            // 最小の非負整数を探す
            var availableId = 0
            while (availableId in usedIds) { availableId++ }
            transaction.id = availableId
        }
        // IDの重複チェック
        if (transaction.id in usedIds) {
            writeError("[budget.TransactionManager] addTransaction: IDが重複するため、収支の追加が阻止されました。:\n${transaction.showInfo()}")
            return false
        }

        if (transaction.inputCheck()) {
            _transactions.add(transaction)
            this.saveTransactionsToCsv()
            CategoryInfo.loadCategoriesFromJson() // エイリアスの更新
            return true
        } else {
            writeError("[budget.TransactionManager] addTransaction: inputCheckにより収支の追加が阻止されました。:\n${transaction.showInfo()}")
            return false
        }
    }

    // 収支を削除するメソッド（IDで削除）
    fun removeTransactionById(id: Int): Boolean {
        if (_transactions.removeIf { it.id == id }) {
            this.saveTransactionsToCsv()
            CategoryInfo.loadCategoriesFromJson() // エイリアスの更新
            return true
        } else {
            writeError("[budget.TransactionManager] removeTransactionById: ID $id に一致する収支が見つかりませんでした。")
            return false
        }
    }

    // 収支を更新するメソッド
    fun updateTransaction(id: Int, newTransaction: Transaction): Boolean {
        if (!newTransaction.inputCheck()) {
            writeError("[budget.TransactionManager] updateTransaction: inputCheckにより収支の更新が阻止されました。:\n${newTransaction.showInfo()}")
            return false
        }

        val index = _transactions.indexOfFirst { it.id == id } // indexOfFirst: マッチするものが無い場合は -1 を返す。
        if (index != -1) {
            if (_transactions[index]::class != newTransaction::class) {
                writeError("[budget.TransactionManager] updateTransaction: 収支データ（ID $id）を異なるクラスで上書きすることはできません。")
                return false
            }
            _transactions[index] = newTransaction
            this.saveTransactionsToCsv()
            CategoryInfo.loadCategoriesFromJson() // エイリアスの更新
            return true
        } else {
            writeError("[budget.TransactionManager] updateTransaction: ID $id に一致する収支が見つかりませんでした。")
            return false
        }
    }

    // 指定のカテゴリ配下・日付範囲の支出を取得
    fun getExpendituresByCategoryAndDateRange(
        parentCategory:String = "",
        dateFilter: Pair<LocalDate, LocalDate> = Pair(LocalDate.MIN, LocalDate.MAX)
    ): List<Expenditure> {
        val expenditures: List<Expenditure> = _transactions.filterIsInstance<Expenditure>()
        val expendituresByCategory: List<Expenditure> = expenditures
            .filter { it.category.startsWith(parentCategory) } // カテゴリが一致するものをフィルタリング
        return expendituresByCategory
            .filter { it.date in dateFilter.first..dateFilter.second } // 日付フィルター
            .map{ it.deepCopy() }
    }

    // 日付範囲での収入を取得
    fun getIncomesByDateRange(dateFilter: Pair<LocalDate, LocalDate>): List<Income> {
        val incomes: List<Income> = _transactions.filterIsInstance<Income>()
        return incomes
            .filter { it.date in dateFilter.first..dateFilter.second }
            .map{ it.deepCopy() }
    }

    // 空白で区切られたキーワードで収支を検索
    fun searchTransactionsByKeywords(keywords: String): List<Transaction> {
        // 全角数字を半角数字に変換
        val keywordsWithNoFullWidth = keywords.map {
            if (it in '０'..'９') {
                ('0' + (it - '０')) // 全角数字を半角数字に変換
            } else {
                it
            }
        }.joinToString("")

        // 半角・全角空白でキーワードを分割（空文字列が入っても除去）
        val keywordList = keywordsWithNoFullWidth.split(" ", "　").filter { it.isNotBlank() }

        return _transactions.filter { transaction ->
            keywordList.all { keyword ->
                when (transaction) {
                    is Expenditure -> transaction.containsKeyword(keyword = keyword)
                    is Income -> transaction.containsKeyword(keyword = keyword)
                    else -> false
                }
            }
        }.map{ it.deepCopy() }
    }
}
