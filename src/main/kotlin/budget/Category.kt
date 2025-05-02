package budget

class Category (ide: Int, n: String, sub: List<Category>, al: List<String>) {
    // プロパティ
    val id: Int = ide
    val name: String = n
    val subcategories: List<Category> = sub
    var aliases: List<String> = al

    // 深いコピー（サブカテゴリーも再帰的にコピー）
    fun deepCopy(): Category {
        val copiedSubcategories = this.subcategories.map { it.deepCopy() }
        return Category(
            ide = this.id,
            n = this.name,
            sub = copiedSubcategories,
            al = this.aliases.toList()  // toList() で新しいリストを作成
        )
    }
}
