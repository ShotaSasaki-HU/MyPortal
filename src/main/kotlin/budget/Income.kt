package budget

import writeError
import java.time.LocalDate

// インターフェイス budget.Transaction を実装した収入クラス budget.Income
class Income(
    ide: Int,
    it: String,
    d: LocalDate,
    a: Int,
    m: String,
    ip: Boolean
): Transaction {
    // プロパティ
    override var id: Int = ide           // 識別子（非負整数）
    override var item: String = it       // 項目名
    override var date: LocalDate = d     // 日付
    override var amount: Int = a         // 金額
    override var memo: String = m        // メモ
    override var isPrivate: Boolean = ip // プライベートフラグ

    // セカンダリコンストラクタ
    constructor() : this(-1, "", LocalDate.now(), -1, "", false)

    // 情報をフォーマットして文字列化するメソッド
    // error.txt への書き込み用に使う事があるため、各変数は成形しない。
    override fun showInfo(): String {
        val privacyStatus = if (isPrivate) "非公開" else "公開"
        return """
            [収入情報]
            ID          : $id
            項目名       : $item
            日付         : $date
            金額         : $amount
            メモ         : $memo
            プライバシー  : $privacyStatus
        """.trimIndent()
    }

    // 入力データのチェック
    override fun inputCheck(): Boolean {
        // idが非負整数であることを確認
        if (id < 0) {
            writeError("[budget.Income] inputCheck: IDは非負整数でなければなりません。")
            return false
        }

        // amount（金額）が正であることを確認
        if (amount <= 0) {
            writeError("[budget.Income] inputCheck: 金額は自然数でなければなりません。")
            return false
        }

        // itemが空でないことを確認
        if (item.isBlank()) {
            writeError("[budget.Income] inputCheck: 項目名は空ではいけません。")
            return false
        }

        // LocalDate が現実的な範囲に収まっているか確認
        val lowerBound = LocalDate.now().minusYears(100) // 過去100年
        val upperBound = LocalDate.now().plusYears(10)   // 未来10年
        if (date !in lowerBound..upperBound) {
            writeError("[budget.Income] inputCheck: 日付が無効です。")
            return false
        }

        // すべてのチェックを通過した場合、trueを返す。
        return true
    }

    // 深いコピー（参照型プロパティに注意せよ。）
    override fun deepCopy(): Income {
        return Income(
            ide = this.id,
            it = this.item,
            d = this.date,
            a = this.amount,
            m = this.memo,
            ip = this.isPrivate
        )
    }

    // キーワードを含むか判定
    override fun containsKeyword(keyword: String): Boolean {
        return this.item.contains(keyword, ignoreCase = true) ||
                this.date.toString().contains(keyword, ignoreCase = true) ||
                this.memo.contains(keyword, ignoreCase = true) ||
                "Income・収入".contains(keyword, ignoreCase = true)
    }
}
