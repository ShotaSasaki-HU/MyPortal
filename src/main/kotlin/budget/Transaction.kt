package budget

import java.time.LocalDate

// 収支インターフェース
interface Transaction {
    var id: Int            // 識別子（非負整数）
    var item: String       // 項目名
    var date: LocalDate    // 日付
    var amount: Int        // 金額
    var memo: String       // メモ
    var isPrivate: Boolean // プライベートフラグ

    fun showInfo(): String
    fun inputCheck(): Boolean
    fun deepCopy(): Transaction // 参照型プロパティに注意せよ。
    fun containsKeyword(keyword: String): Boolean
}
