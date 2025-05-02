package budget

import androidx.compose.runtime.mutableStateListOf
import org.json.JSONObject
import org.apache.commons.lang3.StringUtils
import java.io.File

object CategoryInfo {
    private val _categories = mutableStateListOf<Category>()

    init {
        // 初期化時にJSONファイルからカテゴリーデータを読み込む
        this.loadCategoriesFromJson()
    }

    // _categories を値渡し
    fun getCategoriesDeepCopy(): List<Category> {
        return _categories.map { it.deepCopy() }
    }

    // JSONファイルを読み込んでカテゴリーデータに変換する関数
    fun loadCategoriesFromJson() {
        // この関数を支出データが追加・編集・削除されるたびに実行する事で、ユーザの支出データから追加したエイリアスを最新に保つ。
        _categories.clear()

        // JSONファイルの読み込み
        // ドキュメントフォルダの取得
        val documentsDir = System.getProperty("user.home") + "/Documents"
        val appFolderPath = "$documentsDir/MyPortal"
        val jsonInput = File("$appFolderPath/categories_expenditure.json").readText(Charsets.UTF_8)
        val fileContent = JSONObject(jsonInput)
        val jsonArray = fileContent.getJSONArray("categories")

        // JSON配列からカテゴリーデータを抽出
        for (i in 0 until jsonArray.length()) {
            val jsonCategory = jsonArray.getJSONObject(i)
            this._categories.add(parseCategory(jsonCategory))
        }

        // ユーザの支出データからエイリアスを追加
        this.addAliasesFromUserExpenditures()
    }

    // JSONオブジェクトをCategoryオブジェクトに変換する関数（loadCategoriesFromJson で使用）
    private fun parseCategory(jsonCategory: JSONObject): Category {
        val id = jsonCategory.getInt("id")
        val name = jsonCategory.getString("name")

        // サブカテゴリがある場合は再帰的に処理
        val subcategories = if (jsonCategory.has("subcategories")) {
            val subcategoryArray = jsonCategory.getJSONArray("subcategories")
            val subcategoryList = mutableListOf<Category>()
            for (i in 0 until subcategoryArray.length()) {
                subcategoryList.add(parseCategory(subcategoryArray.getJSONObject(i)))
            }
            subcategoryList
        } else {
            emptyList()
        }

        val aliases = if (jsonCategory.has("aliases")) {
            val aliasesArray = jsonCategory.getJSONArray("aliases")
            List(aliasesArray.length()) { i -> aliasesArray.getString(i) }
        } else {
            emptyList()
        }

        return Category(id, name, subcategories, aliases)
    }

    // ルートからリーフまでを繋いだパスを全パターン取得
    fun getPathsRootToLeaf(scrapedCategories: List<Category> = _categories): List<String> {
        val result = mutableListOf<String>() // パスの文字列を格納

        for (category in scrapedCategories) {
            if (category.aliases.isNotEmpty()) { // リーフに到達
                result.add(category.name)
            } else { // まだ深く潜れる場合
                val fromLeafToCurrent: List<String> = getPathsRootToLeaf(scrapedCategories = category.subcategories)
                for (path in fromLeafToCurrent) {
                    // category 配下の全パターンについて、category.name を先頭に付与した文字列を作る。
                    result.add(category.name + " ＞ " + path)
                }
            }
        }

        return result
    }

    // 自動カテゴリー割り当て
    fun autoCategorization(keywords: List<String>): String {
        var bestPath: List<String>? = null // 最適なカテゴリーのパス
        // scoreMin: Levenshtein距離の最小値
        var scoreMin = Int.MAX_VALUE // Int.MAX_VALUEは、KotlinのInt型が取り得る最大の値を表す。

        for (keyword in keywords) {
            for (category in _categories) {
                val currentPath = mutableListOf(category.name) // 現在のパス
                recursiveCategories(keyword, category, currentPath) { matchedCategory, path ->
                    for (alias in matchedCategory.aliases) {
                        val distance = StringUtils.getLevenshteinDistance(keyword, alias) // 非推奨。直したい。
                        if (distance < scoreMin) {
                            scoreMin = distance
                            bestPath = path.toList() // 現在のパスをコピーして保存
                        }
                    }
                }
            }
        }

        // 最適なパスを " ＞ " で結合して返す
        return bestPath?.joinToString(" ＞ ") ?: ""
    }

    // autoCategorization の中で使う再帰的関数
    private fun recursiveCategories(keyword: String, category: Category, currentPath: MutableList<String>, action: (Category, List<String>) -> Unit) {
        // aliases が空でない場合は探索を終了
        if (category.aliases.isNotEmpty()) {
            action(category, currentPath)
            return
        }

        // 子カテゴリを再帰的に探索
        for (subcategory in category.subcategories) {
            currentPath.add(subcategory.name) // 子カテゴリをパスに追加
            recursiveCategories(keyword, subcategory, currentPath, action)
            currentPath.removeAt(currentPath.size - 1) // 探索後にパスを元に戻す
        }
    }

    // transaction.csv に記録されている支出のカテゴリー割り当てを aliases に追加
    private fun addAliasesFromUserExpenditures() {
        // 支出データを全て取得
        val userExpenditures: List<Expenditure> = TransactionManager.getExpendituresByCategoryAndDateRange()

        for (expenditure in userExpenditures) {
            val path: String = expenditure.category
            var processingCategories: List<Category> = _categories
            for (name in path.split(" ＞ ")) {
                for (category in processingCategories) {
                    if (category.name == name) {
                        if (category.aliases.isNotEmpty() && !category.aliases.contains(expenditure.item)) {
                            category.aliases = category.aliases + expenditure.item
                        } else {
                            processingCategories = category.subcategories
                        }
                        break
                    }
                }
            }
        }
    }
}
