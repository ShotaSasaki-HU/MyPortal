package budget

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.IconButton
import androidx.compose.material.RadioButton
import androidx.compose.material.RadioButtonDefaults
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.material.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDate
import java.time.format.DateTimeFormatter

import messageDialog
import pieChart
import stackedBarChart
import lineChart
import java.time.format.DateTimeParseException

@Composable
fun featureBudget() {
    var selectedOption by remember { mutableStateOf(0) }
    val optionList = listOf("EXPENDITURE", "INCOME", "TIMELINE")
    var expandMenu by remember { mutableStateOf(true) }

    Row(modifier = Modifier.fillMaxSize()) {
        // 左側のメニュー
        if (expandMenu) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(Color.White) // 背景色
            ) {
                // メニューの選択肢
                optionList.forEachIndexed { index, option ->
                    Button(
                        onClick = { selectedOption = index },
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = if (selectedOption == index) AppColors.darkBlue else Color.White
                        ),
                        modifier = Modifier
                            .padding(0.dp)
                            .fillMaxSize()
                            .weight(1f),
                        shape = RoundedCornerShape(0.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically, // アイコンとテキストを垂直方向で中央揃え
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Image(
                                /*imageVector = when(option) {
                                    "EXPENDITURE" -> Icons.Outlined.Paid
                                    "INCOME" -> Icons.Outlined.Savings
                                    "TIMELINE" -> Icons.Outlined.ViewTimeline
                                    else -> Icons.Outlined.Circle
                                },*/
                                painter = painterResource(
                                    when(option) {
                                        "EXPENDITURE" -> AppIcons.Payments
                                        "INCOME" -> AppIcons.Savings
                                        "TIMELINE" -> AppIcons.ViewTimeline
                                        else -> AppIcons.ArrowDropDown
                                    }
                                ),
                                contentDescription = "icon",
                                modifier = Modifier.size(24.dp),
                                colorFilter = ColorFilter.tint(
                                    if (selectedOption == index) Color.White else AppColors.darkBlue
                                )
                            )
                            Spacer(modifier = Modifier.width(8.dp)) // アイコンとテキストの間にスペース
                            Text(
                                text = option,
                                color = if (selectedOption == index) Color.White else AppColors.darkBlue,
                                fontSize = 18.sp
                            )
                        }
                    }
                }
                Column(modifier = Modifier.weight(8f)) {}
            }
        }
        // 境界線
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(1.dp)
                .background(AppColors.lightGray)
        )

        // フローティングアクションボタンのためにBoxで括る。
        Box(modifier = Modifier.weight(4.5f).fillMaxSize()) {
            // 右側の機能エリア
            when(optionList[selectedOption]) {
                "EXPENDITURE" -> featureExpenditure() // 支出の機能
                "INCOME" -> featureIncome()           // 収入の機能
                "TIMELINE" -> featureTimeline()       // タイムライン
                else -> {}
            }

            // フローティングアクションボタン（下の方が後から描画されるからこの位置）
            FloatingActionButton(
                onClick = { expandMenu = !expandMenu },
                modifier = Modifier.align(Alignment.TopStart).padding(0.dp),
                backgroundColor = Color.White,
                // elevation = FloatingActionButtonDefaults.elevation(0.dp), // 影を削除（任意）
                shape = RoundedCornerShape(0.dp, 0.dp, 12.dp, 0.dp)
            ) {
                Image(
                    painter = painterResource(AppIcons.Menu),
                    contentDescription = "menu",
                    modifier = Modifier.size(24.dp),
                    colorFilter = ColorFilter.tint(AppColors.darkBlue)
                )
            }
        }
    }
}

/*---------- featureExpenditureここから ----------*/

// 支出の機能
@Composable
private fun featureExpenditure() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White) // 背景色
            .padding(top = 0.dp)
            .verticalScroll(rememberScrollState()) // スクロール可能にする
    ) {
        // 支出配分エリア
        expenditureBreakdown()

        // 支出登録エリア
        expenditureEntry()

        // 支出年間推移エリア
        annualTrendOfExpenditures()
    }
}

// 支出配分エリア
@Composable
private fun expenditureBreakdown() {
    val today = LocalDate.now() // 今日の日付を取得
    val startOfThisMonth = today.withDayOfMonth(1) // 今月の初日
    val endOfThisMonth = today.withDayOfMonth(today.lengthOfMonth()) // 今月の最終日

    var pieChartDirectory by remember { mutableStateOf( mutableListOf<String>() ) } // カレントカテゴリ
    var dateFilter by remember { mutableStateOf( Pair(startOfThisMonth, endOfThisMonth) ) } // 日付フィルター
    var selectedIndex by remember { mutableStateOf(0) }  // 選択された日付フィルターを管理
    val dateFilterNameList = listOf("MONTH", "YEAR") // 日付フィルターの項目リスト

    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 24.dp, bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // タイトル
        Text(
            "EXPENDITURE BREAKDOWN",
            color = AppColors.darkBlue,
            fontSize = 25.sp,
            modifier = Modifier.padding(top = 0.dp, bottom = 16.dp),
            fontWeight = FontWeight.Bold // 太字に設定
        )

        // 表示範囲選択(月・年)
        Row {
            dateFilterNameList.forEachIndexed { index, name ->
                Button(
                    onClick = {
                        pieChartDirectory = mutableListOf()
                        selectedIndex = index

                        if (dateFilterNameList[selectedIndex] == "YEAR") {
                            val startOfYear = LocalDate.of(dateFilter.first.year, 1, 1)
                            val endOfYear = LocalDate.of(dateFilter.first.year, 12, 31)
                            dateFilter = Pair(startOfYear, endOfYear)
                        } else {
                            val startOfMonth = LocalDate.of(dateFilter.first.year, today.month, 1)
                            val endOfMonth = LocalDate.of(dateFilter.first.year, today.month, today.lengthOfMonth())
                            dateFilter = Pair(startOfMonth, endOfMonth)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = if (selectedIndex == index) AppColors.darkBlue else AppColors.lightBlue
                    ),
                    modifier = Modifier.padding(0.dp).width(200.dp),
                    shape = if (index == 0) {
                        RoundedCornerShape(topStart = 18.dp, bottomStart = 18.dp)
                    } else if (index == dateFilterNameList.size - 1) {
                        RoundedCornerShape(topEnd = 18.dp, bottomEnd = 18.dp)
                    } else {
                        RoundedCornerShape(0.dp)
                    }
                ) {
                    Text(
                        text = name,
                        color = if (selectedIndex == index) Color.White else AppColors.darkBlue // テキストの色変更
                    )
                }
            }
        }

        // アロー領域
        Row(verticalAlignment = Alignment.CenterVertically) {
            // 左向きアロー
            IconButton(
                onClick = {
                    pieChartDirectory = mutableListOf() // ROOTに戻す。

                    dateFilter = if (dateFilterNameList[selectedIndex] == "YEAR") {
                        Pair(dateFilter.first.minusYears(1), dateFilter.second.minusYears(1))
                    } else {
                        val startOfMonth = LocalDate.of(
                            dateFilter.first.minusMonths(1).year,
                            dateFilter.first.minusMonths(1).month,
                            1
                        )
                        val endOfMonth = LocalDate.of(
                            dateFilter.first.minusMonths(1).year,
                            dateFilter.first.minusMonths(1).month,
                            dateFilter.first.minusMonths(1).lengthOfMonth()
                        )
                        Pair(startOfMonth, endOfMonth)
                    }
                }
            ) {
                Image(
                    painter = painterResource(AppIcons.ArrowBackIos),
                    contentDescription = "",
                    modifier = Modifier.size(24.dp),
                    colorFilter = ColorFilter.tint(AppColors.darkBlue)
                )
            }

            // 表示対象となる時期
            Text(
                text = if (dateFilterNameList[selectedIndex] == "YEAR") {
                    "${dateFilter.first.year}年"
                } else {
                    "${dateFilter.first.year}年${dateFilter.first.monthValue}月"
                },
                color = AppColors.darkBlue,
                modifier = Modifier.padding(start = 115.dp, end = 115.dp)
            )

            // 右向きアロー
            IconButton(
                onClick = {
                    pieChartDirectory = mutableListOf() // ROOTに戻す。

                    dateFilter = if (dateFilterNameList[selectedIndex] == "YEAR") {
                        Pair(dateFilter.first.plusYears(1), dateFilter.second.plusYears(1))
                    } else {
                        val startOfMonth = LocalDate.of(
                            dateFilter.first.plusMonths(1).year,
                            dateFilter.first.plusMonths(1).month,
                            1
                        )
                        val endOfMonth = LocalDate.of(
                            dateFilter.first.plusMonths(1).year,
                            dateFilter.first.plusMonths(1).month,
                            dateFilter.first.plusMonths(1).lengthOfMonth()
                        )
                        Pair(startOfMonth, endOfMonth)
                    }
                }
            ) {
                Image(
                    painter = painterResource(AppIcons.ArrowForwardIos),
                    contentDescription = "",
                    modifier = Modifier.size(24.dp),
                    colorFilter = ColorFilter.tint(AppColors.darkBlue)
                )
            }
        }

        // カレントディレクトリ（カテゴリー）
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 8.dp)
        ) {
            Text(
                "ROOT",
                color = AppColors.darkBlue,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(bottom = 3.dp)
                    .clickable {
                        pieChartDirectory = mutableListOf()
                    },
                textDecoration = TextDecoration.Underline
            )
            for ((index, directory) in pieChartDirectory.withIndex()) {
                Text(" ＞ ", color = AppColors.darkBlue, fontWeight = FontWeight.Bold)
                Text(
                    directory,
                    color = AppColors.darkBlue,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clickable {
                            pieChartDirectory = pieChartDirectory.subList(0, index + 1)
                        },
                    textDecoration = TextDecoration.Underline
                )
            }
        }

        // グラフエリア
        val expenditures = TransactionManager.getExpendituresByCategoryAndDateRange(
            parentCategory = pieChartDirectory.joinToString(" ＞ "),
            dateFilter = dateFilter
        )
        val mapLegendToValue = mutableMapOf<String, Float>() // ユニークなカテゴリー名と対応する総額
        for (expenditure in expenditures) {
            val key: String = expenditure.category.split(" ＞ ")[pieChartDirectory.size]
            val value: Float = expenditure.amount.toFloat()
            // println("$key, $value, ${expenditure.date}")

            // キーが存在する場合は加算、存在しない場合は新規追加
            mapLegendToValue[key] = mapLegendToValue.getOrDefault(key, 0f) + value
        }
        // println("---------------")
        // バリュー（総額）の降順でソート
        val sortedMapLegendToValue = mapLegendToValue.toList().sortedByDescending { (_, value) -> value }.toMap()

        pieChart(
            data = sortedMapLegendToValue.values.toList(),
            legend = sortedMapLegendToValue.keys.toList(),
            canvasSize = 250f
        ) { clickedLegendName ->
            // クリックされたディレクトリに進んだとしたら nextDirectory のようになる。
            val nextDirectory = if (pieChartDirectory.isNotEmpty()) {
                pieChartDirectory.joinToString(" ＞ ") + " ＞ $clickedLegendName"
            } else {
                clickedLegendName // pieChartDirectory が空ならば区切り文字は不必要
            }
            // これが、ルートからリーフを繋ぐパスのどれかに一致したら、もう進めないということ。
            if (nextDirectory !in CategoryInfo.getPathsRootToLeaf()) {
                pieChartDirectory = pieChartDirectory.toMutableList().apply { add(clickedLegendName) }
            }
        }
    }
}

// 支出登録エリア
@Composable
private fun expenditureEntry(
    ide: Int = -1,
    it: String = "",
    d: String = LocalDate.now().toString(),
    a: String = "",
    pm: String = "PayPay",
    m: String = "",
    c: String = "",
    ip: Boolean = false,
    editMode: Boolean = false,
    onCompleted: () -> Unit = {}
) {
    var textItem by remember { mutableStateOf(it) } // 項目名
    var textDate by remember { mutableStateOf(d) } // 取引の日付
    var textAmount by remember { mutableStateOf(a) } // 金額
    var textMemo by remember { mutableStateOf(m) } // メモ
    var textCategory by remember { mutableStateOf(c) } // カテゴリー
    var isPrivate by remember { mutableStateOf(ip) } // プライベート登録
    var textKeywords by remember { mutableStateOf("") } // ざっくり入力欄

    // 支払い方法の選択肢
    val paymentMethods = listOf("現金", "PayPay", "クレジットカード", "振込・振替")
    // 現在選択されている支払い方法
    var selectedPaymentMethod by remember { mutableStateOf(pm) }
    // ドロップダウンメニューが開いているかどうかの状態
    var expanded by remember { mutableStateOf(false) }

    var isSuccessDialogOpen by remember { mutableStateOf(false) } // ダイアログ
    var isFailureDialogOpen by remember { mutableStateOf(false) } // ダイアログ

    Column(
        modifier = Modifier.fillMaxWidth().background(AppColors.lightBlue).padding(top = 24.dp, bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // タイトル
        Text(
            text = if (!editMode) { "EXPENDITURE ENTRY" } else {"EXPENDITURE EDIT"},
            color = AppColors.darkBlue,
            fontSize = 25.sp,
            modifier = Modifier.padding(top = 0.dp, bottom = 24.dp),
            fontWeight = FontWeight.Bold // 太字に設定
        )

        // 表（入力エリア）
        Column(
            modifier = Modifier.fillMaxWidth().padding(bottom = 0.dp, start = 8.dp, end = 8.dp)
        ) {
            // ヘッダー行
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(text = "ITEM", modifier = Modifier.weight(1f).padding(start = 12.dp), color = AppColors.darkBlue)
                Text(text = "DATE", modifier = Modifier.weight(0.6f).padding(start = 12.dp), color = AppColors.darkBlue)
                Text(text = "AMOUNT", modifier = Modifier.weight(0.5f).padding(start = 12.dp), color = AppColors.darkBlue)
                Text(text = "PAYMENT METHOD", modifier = Modifier.weight(0.7f).padding(start = 12.dp), color = AppColors.darkBlue)
                Text(text = "NOTE", modifier = Modifier.weight(1f).padding(start = 12.dp), color = AppColors.darkBlue)
            }

            // 2行目のTextField入力
            Row(modifier = Modifier.fillMaxWidth()) {
                TextField(
                    value = textItem,
                    onValueChange = { textItem = it },
                    modifier = Modifier.weight(1f).padding(8.dp).border(1.dp, AppColors.darkBlue),
                    colors = TextFieldDefaults.textFieldColors(
                        backgroundColor = Color.White,
                        focusedIndicatorColor = Color.Transparent, // 下線を消す
                        unfocusedIndicatorColor = Color.Transparent, // 下線を消す
                        cursorColor = AppColors.darkBlue, // カーソルの色
                        textColor = AppColors.darkBlue
                    ),
                    singleLine = true
                )
                TextField(
                    value = textDate,
                    onValueChange = { textDate = it },
                    modifier = Modifier.weight(0.6f).padding(8.dp).border(1.dp, AppColors.darkBlue),
                    colors = TextFieldDefaults.textFieldColors(
                        backgroundColor = Color.White,
                        focusedIndicatorColor = Color.Transparent, // 下線を消す
                        unfocusedIndicatorColor = Color.Transparent, // 下線を消す
                        cursorColor = AppColors.darkBlue, // カーソルの色
                        textColor = AppColors.darkBlue
                    ),
                    singleLine = true
                )
                TextField(
                    value = textAmount,
                    onValueChange = { input ->
                        // 入力値が数字の場合のみ更新
                        if (input.all { it.isDigit() }) {
                            textAmount = input
                        }
                    },
                    modifier = Modifier.weight(0.5f).padding(8.dp).border(1.dp, AppColors.darkBlue),
                    colors = TextFieldDefaults.textFieldColors(
                        backgroundColor = Color.White,
                        focusedIndicatorColor = Color.Transparent, // 下線を消す
                        unfocusedIndicatorColor = Color.Transparent, // 下線を消す
                        cursorColor = AppColors.darkBlue, // カーソルの色
                        textColor = AppColors.darkBlue
                    ),
                    singleLine = true
                )
                // 支払い方法の選択ドロップダウンメニュー
                Box(modifier = Modifier.weight(0.7f).padding(8.dp)) {
                    // テキスト表示領域
                    TextField(
                        value = selectedPaymentMethod,
                        onValueChange = {},
                        colors = TextFieldDefaults.textFieldColors(
                            backgroundColor = Color.White,
                            focusedIndicatorColor = Color.Transparent, // 下線を消す
                            unfocusedIndicatorColor = Color.Transparent, // 下線を消す
                            cursorColor = AppColors.darkBlue, // カーソルの色
                            textColor = AppColors.darkBlue,
                            disabledTextColor = AppColors.darkBlue, // 無効化時のテキストの色
                            disabledTrailingIconColor = AppColors.darkBlue // 無効化時の後方アイコンの色
                        ),
                        trailingIcon = {
                            Image(
                                painter = painterResource(AppIcons.ArrowDropDown),
                                contentDescription = "icon",
                                modifier = Modifier.size(24.dp),
                                colorFilter = ColorFilter.tint(AppColors.darkBlue)
                            )
                        },
                        readOnly = true, // テキストフィールドが編集不可にする
                        enabled = false, // TextFieldをクリックするため
                        modifier = Modifier
                            .border(1.dp, AppColors.darkBlue)
                            .clickable { expanded = true } // クリックでドロップダウンを開く
                    )

                    // ドロップダウンメニュー
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false } // メニューを閉じる
                    ) {
                        for (option in paymentMethods) {
                            DropdownMenuItem(
                                onClick = {
                                    selectedPaymentMethod = option // 選択されたオプションを設定
                                    expanded = false // メニューを閉じる
                                }
                            ) {
                                Text(text = option, color = AppColors.darkBlue)
                            }
                        }
                    }
                }
                TextField(
                    value = textMemo,
                    onValueChange = { textMemo = it },
                    modifier = Modifier.weight(1f).padding(8.dp).border(1.dp, AppColors.darkBlue),
                    colors = TextFieldDefaults.textFieldColors(
                        backgroundColor = Color.White,
                        focusedIndicatorColor = Color.Transparent, // 下線を消す
                        unfocusedIndicatorColor = Color.Transparent, // 下線を消す
                        cursorColor = AppColors.darkBlue, // カーソルの色
                        textColor = AppColors.darkBlue
                    ),
                    singleLine = true
                )
            }

            // 3行目（カテゴリー）
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "CATEGORY", modifier = Modifier.padding(start = 12.dp), color = AppColors.darkBlue)
                    TextField(
                        value = textCategory,
                        onValueChange = { textCategory = it },
                        modifier = Modifier.fillMaxWidth().padding(8.dp).border(1.dp, AppColors.darkBlue),
                        colors = TextFieldDefaults.textFieldColors(
                            backgroundColor = Color.White,
                            focusedIndicatorColor = Color.Transparent, // 下線を消す
                            unfocusedIndicatorColor = Color.Transparent, // 下線を消す
                            cursorColor = AppColors.darkBlue, // カーソルの色
                            textColor = AppColors.darkBlue
                        ),
                        singleLine = true,
                        readOnly = true // ユーザによる編集不可
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(16.dp)
                ) {
                    // UIの呼び出し
                    categorySelectionUI(categories = CategoryInfo.getCategoriesDeepCopy()) { text -> textCategory = text }
                }
            }
        }

        // プライベート登録
        Row {
            // スペーサー
            Column(modifier = Modifier.weight(6f)) {}

            // ラジオボタン
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                RadioButton(
                    selected = isPrivate,
                    onClick = {
                        isPrivate = !isPrivate
                    },
                    colors = RadioButtonDefaults.colors(
                        selectedColor = AppColors.darkBlue,  // 選択時の色
                        unselectedColor = Color.Gray // 未選択時の色
                    )
                )
                Text(
                    "PRIVATE",
                    modifier = Modifier.padding(start = 0.dp).clickable{ isPrivate = !isPrivate },
                    color = AppColors.darkBlue,
                    // fontWeight = FontWeight.Bold // 太字に設定
                )
            }
        }

        // ざっくり入力欄
        TextField(
            value = textKeywords,
            onValueChange = { textKeywords = it },
            placeholder = { Text("Enter keywords, starting with the item name, to autofill fields.", color = AppColors.darkBlue) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 0.dp)
                .border(1.dp, AppColors.darkBlue, shape = RoundedCornerShape(30.dp)),
            singleLine = true,
            shape = RoundedCornerShape(30.dp), // 丸い角を設定
            colors = TextFieldDefaults.textFieldColors(
                backgroundColor = Color.White,
                focusedIndicatorColor = Color.Transparent, // 下線を消す
                unfocusedIndicatorColor = Color.Transparent, // 下線を消す
                cursorColor = AppColors.darkBlue, // カーソルの色
                textColor = AppColors.darkBlue
            ),
            leadingIcon = null, // 左端にアイコンは無し
            trailingIcon = {
                IconButton(
                    modifier = Modifier.padding(end = 8.dp), // 左にずらす
                    onClick = {
                        // アイコンクリック時の処理
                        // Expenditureクラスに情報を代入
                        val expenditure: Expenditure = Expenditure()
                        expenditure.id = ide
                        expenditure.item = textItem
                        try {
                            expenditure.date = LocalDate.parse(textDate, DateTimeFormatter.ISO_DATE)
                        } catch (_: DateTimeParseException) {
                            expenditure.date = LocalDate.MIN
                        }
                        if (textAmount.isNotEmpty()) {
                            if (textAmount.length >= 2 && (textAmount[0] == '0' || textAmount[0] == '０')) {
                                expenditure.amount = -1
                            } else {
                                expenditure.amount = textAmount.toInt()
                            }
                        } else {
                            expenditure.amount = -1
                        }
                        expenditure.paymentMethod = selectedPaymentMethod
                        expenditure.memo = textMemo
                        expenditure.category = textCategory
                        expenditure.isPrivate = isPrivate

                        // csvへの書き込み
                        if (!editMode) { // 新規登録
                            if (TransactionManager.addTransaction(expenditure)) {
                                isSuccessDialogOpen = true

                                textItem = ""
                                // textDate = LocalDate.now().toString() // 変えない方がいいかな。
                                textAmount = ""
                                // textPaymentMethod = paymentMethods[0] // 変えない方がいいかな。
                                textMemo = ""
                                textCategory = ""
                                isPrivate = false
                            } else {
                                isFailureDialogOpen = true
                            }
                        } else { // 更新モード
                            if (TransactionManager.updateTransaction(id = expenditure.id, newTransaction = expenditure)) {
                                isSuccessDialogOpen = true

                                textItem = ""
                                // textDate = LocalDate.now().toString() // 変えない方がいいかな。
                                textAmount = ""
                                // textPaymentMethod = paymentMethods[0] // 変えない方がいいかな。
                                textMemo = ""
                                textCategory = ""
                                isPrivate = false
                            } else {
                                isFailureDialogOpen = true
                            }
                        }
                    }
                ) {
                    Image(
                        painter = painterResource(AppIcons.Save),
                        contentDescription = "save",
                        modifier = Modifier.size(36.dp),
                        colorFilter = ColorFilter.tint(AppColors.darkBlue)
                    )
                }
            },
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Done // IMEアクションを設定
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    // エンターキーまたはIMEアクションを押したときの処理
                    val (keywords, numeric) = detectAmount(textKeywords.toString()) // 金額の予測
                    if (numeric != null) { textAmount = numeric } // 金額入力欄に代入
                    if (keywords.isNotEmpty()) {
                        textItem = keywords[0] // キーワードの先頭を項目名入力欄に代入
                        val bestPath = CategoryInfo.autoCategorization(keywords.take(1)) // カテゴリーの予測（keywordsそのまま渡してもいいけど、メモに引っ張られると変な結果になる。）
                        textCategory = bestPath // カテゴリー入力欄に代入
                        textMemo = keywords.subList(1, keywords.size).joinToString("，") // メモ入力欄に代入
                    }
                    textKeywords = ""
                }
            )
        )
    }

    if (isSuccessDialogOpen) {
        messageDialog(
            msg = "支出が正常に記録されました。",
            textNegative = "CLOSE",
            onNegativeClick = {
                onCompleted() // 一連の流れが完了した時の処理
                isSuccessDialogOpen = false
            }
        )
    }
    if (isFailureDialogOpen) {
        messageDialog(
            msg = "入力欄に不備があります。",
            textNegative = "CLOSE",
            onNegativeClick = {
                isFailureDialogOpen = false
            }
        )
    }
}

// 支出年間推移エリア
@Composable
private fun annualTrendOfExpenditures() {
    val today = LocalDate.now() // 今日の日付を取得
    val startOfThisYear = LocalDate.of(today.year, 1, 1)
    val endOfThisYear = LocalDate.of(today.year, 12, 31)
    var dateFilter by remember { mutableStateOf( Pair(startOfThisYear, endOfThisYear) ) } // 日付フィルター

    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 24.dp, bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // タイトル
        Text(
            "ANNUAL TREND OF EXPENDITURES",
            color = AppColors.darkBlue,
            fontSize = 25.sp,
            modifier = Modifier.padding(top = 0.dp, bottom = 16.dp),
            fontWeight = FontWeight.Bold // 太字に設定
        )

        // アロー領域
        Row(verticalAlignment = Alignment.CenterVertically) {
            // 左向きアロー
            IconButton(
                onClick = {
                    dateFilter = Pair(dateFilter.first.minusYears(1), dateFilter.second.minusYears(1))
                }
            ) {
                Image(
                    painter = painterResource(AppIcons.ArrowBackIos),
                    contentDescription = "",
                    modifier = Modifier.size(24.dp),
                    colorFilter = ColorFilter.tint(AppColors.darkBlue)
                )
            }

            // 表示対象となる時期
            Text(
                text = "${dateFilter.first.year}年",
                color = AppColors.darkBlue,
                modifier = Modifier.padding(start = 115.dp, end = 115.dp)
            )

            // 右向きアロー
            IconButton(
                onClick = {
                    dateFilter = Pair(dateFilter.first.plusYears(1), dateFilter.second.plusYears(1))
                }
            ) {
                Image(
                    painter = painterResource(AppIcons.ArrowForwardIos),
                    contentDescription = "",
                    modifier = Modifier.size(24.dp),
                    colorFilter = ColorFilter.tint(AppColors.darkBlue)
                )
            }
        }

        // 積み上げ棒グラフ
        Row {
            Box(modifier = Modifier.weight(0.5f)) {}
            // 棒グラフ領域
            Box(modifier = Modifier.weight(9f)) {
                // 凡例のリストを作成
                val categories: List<Category> = CategoryInfo.getCategoriesDeepCopy()
                val legend: MutableList<String> = mutableListOf()
                for (category in categories) { legend.add(category.name) }
                // println(legend)

                // データ・横軸ラベルの作成
                val data: MutableList<List<Float>> = mutableListOf()
                val axisHorizontal: MutableList<String> = mutableListOf()
                for (month in 1..12) {
                    axisHorizontal.add("${month}月")
                    val bar: MutableList<Float> = MutableList(legend.size) { 0f } // 月別集計（凡例の数 ＝ 要素数）

                    // 月のフィルターを作成
                    val startOfMonth = LocalDate.of(dateFilter.first.year, month, 1)
                    val endOfMonth = LocalDate.of(dateFilter.first.year, month, startOfMonth.lengthOfMonth())
                    val dateFilterThisMonth = Pair(startOfMonth, endOfMonth)

                    // バー1本分のデータを作成
                    for ((index, legendName) in legend.withIndex()) {
                        val expenditures: List<Expenditure> = TransactionManager.getExpendituresByCategoryAndDateRange(
                            parentCategory = legendName,
                            dateFilter = dateFilterThisMonth
                        )
                        for (expenditure in expenditures) {
                            bar[index] += expenditure.amount
                        }
                    }
                    data.add(bar) // この月のデータ完成
                }

                stackedBarChart(data = data, axisHorizontal = axisHorizontal, legend = legend, chartHeightDp = 550f)
            }
            Box(modifier = Modifier.weight(0.5f)) {}
        }
    }
}

// 分割されたキーワードリストと半角数字の変数（金額）を返す
private fun detectAmount(input: String): Pair<List<String>, String?> {
    // 1. 全角数字を半角数字に変換
    val normalizedInput = input.map {
        if (it in '０'..'９') {
            ('0' + (it - '０')) // 全角数字を半角数字に変換
        } else {
            it
        }
    }.joinToString("")

    // 2. 半角・全角空白でキーワードを分割（空文字列が入っても除去）
    val keywords = normalizedInput.split(" ", "　").filter { it.isNotBlank() }

    // 3. 半角数字のみのキーワードを変数に格納し、それをリストから排除
    var numericKeyword: String? = null
    val filteredKeywords = keywords.filter { keyword ->
        if (keyword.all { it.isDigit() }) {
            numericKeyword = keyword
            false // 半角数字のみのキーワードはリストに含めない（filter が false）
        } else {
            true
        }
    }

    // 分割されたキーワードリストと半角数字の変数を返す
    return Pair(filteredKeywords, numericKeyword)
}

// 支出登録エリアのカテゴリー選択UI
@Composable
private fun categorySelectionUI(categories: List<Category>, onCategorySelected: (String) -> Unit) {
    var currentCategories by remember { mutableStateOf(categories) }
    var breadcrumbs by remember { mutableStateOf<List<String>>(emptyList()) }

    // 現在のカテゴリを表示
    Column {
        // パンくずリストを表示
        Text("ROOT ＞ ${breadcrumbs.joinToString(" ＞ ")}", color = AppColors.darkBlue, fontWeight = FontWeight.Bold)

        // カテゴリー選択肢を行ごとに表示
        currentCategories.chunked(4).forEach { categoryRow ->
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            ) {
                categoryRow.forEach { category ->
                    if (category.name != "非公開") {
                        Button(
                            colors = ButtonDefaults.buttonColors(backgroundColor = Color.White),
                            border = BorderStroke(1.dp, AppColors.darkBlue),
                            shape = RoundedCornerShape(18.dp),
                            onClick = {
                                // サブカテゴリがあれば次の階層を表示
                                if (category.subcategories.isEmpty()) {
                                    onCategorySelected("${breadcrumbs.joinToString(" ＞ ")} ＞ ${category.name}")
                                } else {
                                    breadcrumbs = breadcrumbs + category.name
                                    currentCategories = category.subcategories
                                }
                            }
                        ) {
                            Text(category.name, color = AppColors.darkBlue)
                        }
                    }
                }
            }
        }

        // 戻るボタン
        if (breadcrumbs.isNotEmpty()) {
            Button(
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = AppColors.darkBlue
                ),
                shape = RoundedCornerShape(18.dp),
                onClick = {
                    breadcrumbs = breadcrumbs.dropLast(1)
                    // パンくずリストに対応する階層を表示
                    val parentCategory = breadcrumbs.fold(categories) { currentLevel, breadcrumb ->
                        currentLevel.firstOrNull { it.name == breadcrumb }?.subcategories ?: emptyList()
                    }
                    currentCategories = parentCategory
                }
            ) {
                Text("戻る", color = Color.White, fontWeight = FontWeight.Bold)
            }
        }
    }
}

/*---------- featureExpenditureここまで ----------*/

/*---------- featureIncomeここから ----------*/

@Composable
private fun featureIncome() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White) // 背景色
            .padding(top = 0.dp)
            .verticalScroll(rememberScrollState()) // スクロール可能にする
    ) {
        // 収入推移エリア
        trendOfIncomes()

        // 収入登録エリア
        incomeEntry()
    }
}

// 収入推移エリア
@Composable
private fun trendOfIncomes() {
    val today = LocalDate.now()
    val startOfRange = LocalDate.of(today.minusMonths(11).year, today.minusMonths(11).month, 1)
    val endOfRange = LocalDate.of(today.year, today.month, today.lengthOfMonth())
    var dateFilter by remember { mutableStateOf( Pair(startOfRange, endOfRange) ) } // 日付フィルター

    Column(
        modifier = Modifier.fillMaxWidth().padding(top = 24.dp, bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // タイトル
        Text(
            "TREND OF INCOMES",
            color = AppColors.darkBlue,
            fontSize = 25.sp,
            modifier = Modifier.padding(top = 0.dp, bottom = 16.dp),
            fontWeight = FontWeight.Bold // 太字に設定
        )

        // アロー領域
        Row(verticalAlignment = Alignment.CenterVertically) {
            // 左向きアロー
            IconButton(
                onClick = {
                    dateFilter = Pair(dateFilter.first.minusMonths(1), dateFilter.second.minusMonths(1))
                }
            ) {
                Image(
                    painter = painterResource(AppIcons.ArrowBackIos),
                    contentDescription = "",
                    modifier = Modifier.size(24.dp),
                    colorFilter = ColorFilter.tint(AppColors.darkBlue)
                )
            }

            // 表示対象となる時期
            Text(
                text = "${dateFilter.first.year}年${dateFilter.first.monthValue}月〜${dateFilter.second.year}年${dateFilter.second.monthValue}月",
                color = AppColors.darkBlue,
                modifier = Modifier.padding(start = 115.dp, end = 115.dp)
            )

            // 右向きアロー
            IconButton(
                onClick = {
                    dateFilter = Pair(dateFilter.first.plusMonths(1), dateFilter.second.plusMonths(1))
                }
            ) {
                Image(
                    painter = painterResource(AppIcons.ArrowForwardIos),
                    contentDescription = "",
                    modifier = Modifier.size(24.dp),
                    colorFilter = ColorFilter.tint(AppColors.darkBlue)
                )
            }
        }

        // 折れ線グラフ領域
        Row {
            Box(modifier = Modifier.weight(0.5f)) {} // スペーサー
            // 折れ線グラフ
            Box(modifier = Modifier.weight(9f)) {
                val data: MutableList<Float> = MutableList(12) { 0f } // 12ヶ月分の収入データ
                val axisHorizontal: MutableList<String> = mutableListOf()
                for (i in 0..11) {
                    val startOfMonth = LocalDate.of(
                        dateFilter.first.plusMonths(i.toLong()).year,
                        dateFilter.first.plusMonths(i.toLong()).month,
                        1
                    )
                    val endOfMonth = LocalDate.of(
                        dateFilter.first.plusMonths(i.toLong()).year,
                        dateFilter.first.plusMonths(i.toLong()).month,
                        dateFilter.first.plusMonths(i.toLong()).lengthOfMonth()
                    )
                    val dateFilterMonth = Pair(startOfMonth, endOfMonth)
                    val incomes: List<Income> = TransactionManager.getIncomesByDateRange(dateFilter = dateFilterMonth)
                    for (income in incomes) {
                        data[i] += income.amount
                    }
                    axisHorizontal.add("${dateFilterMonth.first.year}年\n${dateFilterMonth.first.monthValue}月")
                }
                lineChart(data = data, axisHorizontal = axisHorizontal, color = AppColors.darkBlue, chartHeightDp = 500f)
            }
            Box(modifier = Modifier.weight(0.5f)) {} // スペーサー
        }
    }
}

// 収入登録エリア
@Composable
private fun incomeEntry(
    ide: Int = -1,
    it: String = "",
    d: String = LocalDate.now().toString(),
    a: String = "",
    m: String = "",
    ip: Boolean = false,
    editMode: Boolean = false,
    onCompleted: () -> Unit = {}
) {
    var textItem by remember { mutableStateOf(it) } // 項目名
    var textDate by remember { mutableStateOf(d) } // 日付
    var textAmount by remember { mutableStateOf(a) } // 金額
    var textMemo by remember { mutableStateOf(m) } // メモ
    var isPrivate by remember { mutableStateOf(ip) } // プライベート登録
    var textKeywords by remember { mutableStateOf("") } // ざっくり入力欄

    var isSuccessDialogOpen by remember { mutableStateOf(false) } // ダイアログ
    var isFailureDialogOpen by remember { mutableStateOf(false) } // ダイアログ

    Column(
        modifier = Modifier.fillMaxWidth().background(AppColors.lightBlue).padding(top = 24.dp, bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // タイトル
        Text(
            text = if (!editMode) { "INCOME ENTRY" } else { "INCOME EDIT" },
            color = AppColors.darkBlue,
            fontSize = 25.sp,
            modifier = Modifier.padding(top = 0.dp, bottom = 24.dp),
            fontWeight = FontWeight.Bold // 太字に設定
        )

        // 表（入力エリア）
        Column(
            modifier = Modifier.fillMaxWidth().padding(bottom = 0.dp, start = 8.dp, end = 8.dp)
        ) {
            // ヘッダー行
            Row(modifier = Modifier.fillMaxWidth()) {
                Text(text = "ITEM", modifier = Modifier.weight(1f).padding(start = 12.dp), color = AppColors.darkBlue)
                Text(text = "DATE", modifier = Modifier.weight(0.6f).padding(start = 12.dp), color = AppColors.darkBlue)
                Text(text = "AMOUNT", modifier = Modifier.weight(0.5f).padding(start = 12.dp), color = AppColors.darkBlue)
                Text(text = "NOTE", modifier = Modifier.weight(1f).padding(start = 12.dp), color = AppColors.darkBlue)
            }

            // 2行目のTextField入力
            Row(modifier = Modifier.fillMaxWidth()) {
                TextField(
                    value = textItem,
                    onValueChange = { textItem = it },
                    modifier = Modifier.weight(1f).padding(8.dp).border(1.dp, AppColors.darkBlue),
                    colors = TextFieldDefaults.textFieldColors(
                        backgroundColor = Color.White,
                        focusedIndicatorColor = Color.Transparent, // 下線を消す
                        unfocusedIndicatorColor = Color.Transparent, // 下線を消す
                        cursorColor = AppColors.darkBlue, // カーソルの色
                        textColor = AppColors.darkBlue
                    ),
                    singleLine = true
                )
                TextField(
                    value = textDate,
                    onValueChange = { textDate = it },
                    modifier = Modifier.weight(0.6f).padding(8.dp).border(1.dp, AppColors.darkBlue),
                    colors = TextFieldDefaults.textFieldColors(
                        backgroundColor = Color.White,
                        focusedIndicatorColor = Color.Transparent, // 下線を消す
                        unfocusedIndicatorColor = Color.Transparent, // 下線を消す
                        cursorColor = AppColors.darkBlue, // カーソルの色
                        textColor = AppColors.darkBlue
                    ),
                    singleLine = true
                )
                TextField(
                    value = textAmount,
                    onValueChange = { input ->
                        // 入力値が数字の場合のみ更新
                        if (input.all { it.isDigit() }) {
                            textAmount = input
                        }
                    },
                    modifier = Modifier.weight(0.5f).padding(8.dp).border(1.dp, AppColors.darkBlue),
                    colors = TextFieldDefaults.textFieldColors(
                        backgroundColor = Color.White,
                        focusedIndicatorColor = Color.Transparent, // 下線を消す
                        unfocusedIndicatorColor = Color.Transparent, // 下線を消す
                        cursorColor = AppColors.darkBlue, // カーソルの色
                        textColor = AppColors.darkBlue
                    ),
                    singleLine = true
                )
                TextField(
                    value = textMemo,
                    onValueChange = { textMemo = it },
                    modifier = Modifier.weight(1f).padding(8.dp).border(1.dp, AppColors.darkBlue),
                    colors = TextFieldDefaults.textFieldColors(
                        backgroundColor = Color.White,
                        focusedIndicatorColor = Color.Transparent, // 下線を消す
                        unfocusedIndicatorColor = Color.Transparent, // 下線を消す
                        cursorColor = AppColors.darkBlue, // カーソルの色
                        textColor = AppColors.darkBlue
                    ),
                    singleLine = true
                )
            }
        }

        // プライベート登録
        Row {
            // スペーサー
            Column(modifier = Modifier.weight(6f)) {}

            // ラジオボタン
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                RadioButton(
                    selected = isPrivate,
                    onClick = {
                        isPrivate = !isPrivate
                    },
                    colors = RadioButtonDefaults.colors(
                        selectedColor = AppColors.darkBlue,  // 選択時の色
                        unselectedColor = Color.Gray // 未選択時の色
                    )
                )
                Text(
                    "PRIVATE",
                    modifier = Modifier.padding(start = 0.dp).clickable{ isPrivate = !isPrivate },
                    color = AppColors.darkBlue,
                    // fontWeight = FontWeight.Bold // 太字に設定
                )
            }
        }

        // ざっくり入力欄
        TextField(
            value = textKeywords,
            onValueChange = { textKeywords = it },
            placeholder = { Text("Enter keywords, starting with the item name, to autofill fields.", color = AppColors.darkBlue) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, bottom = 0.dp)
                .border(1.dp, AppColors.darkBlue, shape = RoundedCornerShape(30.dp)),
            singleLine = true,
            shape = RoundedCornerShape(30.dp), // 丸い角を設定
            colors = TextFieldDefaults.textFieldColors(
                backgroundColor = Color.White,
                focusedIndicatorColor = Color.Transparent, // 下線を消す
                unfocusedIndicatorColor = Color.Transparent, // 下線を消す
                cursorColor = AppColors.darkBlue, // カーソルの色
                textColor = AppColors.darkBlue
            ),
            leadingIcon = null, // 左端にアイコンは無し
            trailingIcon = {
                IconButton(
                    modifier = Modifier.padding(end = 8.dp), // 左にずらす
                    onClick = {
                        // アイコンクリック時の処理
                        // Incomeクラスに情報を代入
                        val income: Income = Income()
                        income.id = ide
                        income.item = textItem
                        try {
                            income.date = LocalDate.parse(textDate, DateTimeFormatter.ISO_DATE)
                        } catch (_: DateTimeParseException) {
                            income.date = LocalDate.MIN
                        }
                        if (textAmount.isNotEmpty()) {
                            if (textAmount.length >= 2 && (textAmount[0] == '0' || textAmount[0] == '０')) {
                                income.amount = -1
                            } else {
                                income.amount = textAmount.toInt()
                            }
                        } else {
                            income.amount = -1
                        }
                        income.memo = textMemo
                        income.isPrivate = isPrivate

                        // csvへの書き込み
                        if (!editMode) { // 新規登録
                            if (TransactionManager.addTransaction(income)) {
                                isSuccessDialogOpen = true

                                textItem = ""
                                // textDate = LocalDate.now().toString() // 変えない方がいいかな。
                                textAmount = ""
                                textMemo = ""
                                isPrivate = false
                            } else {
                                isFailureDialogOpen = true
                            }
                        } else { // 更新モード
                            if (TransactionManager.updateTransaction(id = income.id, newTransaction = income)) {
                                isSuccessDialogOpen = true

                                textItem = ""
                                // textDate = LocalDate.now().toString() // 変えない方がいいかな。
                                textAmount = ""
                                textMemo = ""
                                isPrivate = false
                            } else {
                                isFailureDialogOpen = true
                            }
                        }
                    }
                ) {
                    Image(
                        painter = painterResource(AppIcons.Save),
                        contentDescription = "save",
                        modifier = Modifier.size(36.dp),
                        colorFilter = ColorFilter.tint(AppColors.darkBlue)
                    )
                }
            },
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Done // IMEアクションを設定
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    // エンターキーまたはIMEアクションを押したときの処理
                    val (keywords, numeric) = detectAmount(textKeywords.toString()) // 金額の予測
                    if (numeric != null) { textAmount = numeric } // 金額入力欄に代入
                    if (keywords.isNotEmpty()) {
                        textItem = keywords[0] // キーワードの先頭を項目名入力欄に代入
                        textMemo = keywords.subList(1, keywords.size).joinToString("，") // メモ入力欄に代入
                    }
                    textKeywords = ""
                }
            )
        )
    }

    if (isSuccessDialogOpen) {
        messageDialog(
            msg = "収入が正常に記録されました。",
            textNegative = "CLOSE",
            onNegativeClick = {
                onCompleted() // 一連の流れが完了した時の処理
                isSuccessDialogOpen = false
            }
        )
    }
    if (isFailureDialogOpen) {
        messageDialog(
            msg = "入力欄に不備があります。",
            textNegative = "CLOSE",
            onNegativeClick = {
                isFailureDialogOpen = false
            }
        )
    }
}

/*---------- featureIncomeここまで ----------*/

/*---------- featureTimelineここから ----------*/

@Composable
private fun featureTimeline() {
    var textSearch by remember { mutableStateOf("") } // 検索窓のテキスト

    var isTransactionDetailVisible by remember { mutableStateOf(false) } // スタックUIの表示状態を管理
    var selectedTransaction by remember{ mutableStateOf<Transaction>(Expenditure()) } // 選択された収支データ

    var isDeleteDialogVisible by remember { mutableStateOf(false) } // 本当に削除するのかダイアログ

    // タイムライン本体
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White) // 背景色
            .padding(top = 6.dp, bottom = 6.dp, start = 128.dp, end = 128.dp)
            .verticalScroll(rememberScrollState()), // スクロール可能にする
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth().padding(bottom = 68.dp)) {} // スペーサー

        var transactions: List<Transaction> = TransactionManager.searchTransactionsByKeywords(keywords = textSearch)
        transactions = transactions.sortedByDescending { it.date }
        for (index in 0..transactions.size-1) {
            when (transactions[index]) {
                is Expenditure -> {
                    expenditureRow(expenditure = transactions[index] as Expenditure) { expenditure ->
                        selectedTransaction = expenditure
                        isTransactionDetailVisible = true
                    }
                }
                is Income -> {
                    incomeRow(income = transactions[index] as Income) { income ->
                        selectedTransaction = income
                        isTransactionDetailVisible = true
                    }
                }
                else -> {}
            }
        }

        Box(modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)) {} // スペーサー
    }

    // 検索窓
    TextField(
        value = textSearch,
        onValueChange = { textSearch = it },
        placeholder = { Text("Type to AND search...", color = AppColors.darkBlue) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 192.dp, end = 192.dp, top = 12.dp, bottom = 12.dp)
            .border(1.dp, AppColors.darkBlue, shape = RoundedCornerShape(30.dp)),
        singleLine = true,
        shape = RoundedCornerShape(30.dp), // 丸い角を設定
        colors = TextFieldDefaults.textFieldColors(
            backgroundColor = Color.White,
            focusedIndicatorColor = Color.Transparent, // 下線を消す
            unfocusedIndicatorColor = Color.Transparent, // 下線を消す
            cursorColor = AppColors.darkBlue, // カーソルの色
            textColor = AppColors.darkBlue
        ),
        trailingIcon = {
            Image(
                painter = painterResource(AppIcons.Search),
                contentDescription = "save",
                modifier = Modifier.padding(end = 12.dp).size(36.dp),
                colorFilter = ColorFilter.tint(AppColors.darkBlue)
            )
        }
    )

    // スタックするUI（上に被さる）
    if (isTransactionDetailVisible) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(color = Color.Transparent)
                .clickable(
                    onClick = {},
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                )
        ) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(50.dp) // 縁を透明にするためのパディング
                    .fillMaxSize()
                    .shadow(elevation = 6.dp, shape = RoundedCornerShape(16.dp))
                    .background(AppColors.lightBlue)
                    //.padding(20.dp)
            ) {
                Column {
                    // 戻る・削除
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(start = 8.dp, end = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween, // ボタンを両端に配置
                        verticalAlignment = Alignment.CenterVertically // 垂直方向は中央揃え
                    ) {
                        // キャンセルボタン
                        Button(
                            onClick = { isTransactionDetailVisible = false },
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = Color.White
                            ),
                            modifier = Modifier.padding(5.dp, 5.dp, 5.dp, 5.dp),
                            border = BorderStroke(1.dp, AppColors.darkBlue),
                            shape = RoundedCornerShape(18.dp, 18.dp, 18.dp, 18.dp)
                        ) {
                            Text(
                                text = "CANCEL",
                                color = AppColors.darkBlue
                            )
                        }
                        // 削除ボタン
                        Button(
                            onClick = {
                                isDeleteDialogVisible = true
                            },
                            colors = ButtonDefaults.buttonColors(
                                backgroundColor = Color.White
                            ),
                            modifier = Modifier.padding(5.dp, 5.dp, 5.dp, 5.dp),
                            border = BorderStroke(1.dp, AppColors.darkBlue),
                            shape = RoundedCornerShape(18.dp, 18.dp, 18.dp, 18.dp)
                        ) {
                            Text(
                                text = "REMOVE",
                                color = Color.Red
                            )
                        }
                    }

                    // 登録UIを流用した更新UI
                    when (selectedTransaction) {
                        is Expenditure -> {
                            expenditureEntry(
                                ide = selectedTransaction.id,
                                it = selectedTransaction.item,
                                d = selectedTransaction.date.toString(),
                                a = selectedTransaction.amount.toString(),
                                pm = (selectedTransaction as Expenditure).paymentMethod,
                                m = selectedTransaction.memo,
                                c = (selectedTransaction as Expenditure).category,
                                ip = selectedTransaction.isPrivate,
                                editMode = true
                            ) {
                                isTransactionDetailVisible = false
                            }
                        }
                        is Income -> {
                            incomeEntry(
                                ide = selectedTransaction.id,
                                it = selectedTransaction.item,
                                d = selectedTransaction.date.toString(),
                                a = selectedTransaction.amount.toString(),
                                m = selectedTransaction.memo,
                                ip = selectedTransaction.isPrivate,
                                editMode = true
                            ) {
                                isTransactionDetailVisible = false
                            }
                        }
                        else -> {}
                    }
                }
            }

            if (isDeleteDialogVisible) {
                messageDialog(
                    msg = "本当に削除しますか？",
                    textNegative = "CANCEL",
                    onNegativeClick = { isDeleteDialogVisible = false },
                    textPositive = "REMOVE",
                    onPositiveClick = {
                        TransactionManager.removeTransactionById(id = selectedTransaction.id)
                        isDeleteDialogVisible = false
                        isTransactionDetailVisible = false
                    },
                    textColorPositive = Color.Red,
                    dialogWidth = 240
                )
            }
        }
    }
}

@Composable
private fun expenditureRow(expenditure: Expenditure, onClickItem: (Expenditure) -> Unit = {}) {
    Column(
        modifier = Modifier
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(16.dp))
            .height(160.dp)
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(16.dp))
            .padding(12.dp)
            .clickable { onClickItem(expenditure) }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f).fillMaxWidth()
        ) {
            // 項目名
            Text(
                text = expenditure.item,
                modifier = Modifier.weight(3.5f),
                color = AppColors.darkBlue,
                fontSize = 20.sp
            )

            // 日付
            Text(
                text = "${expenditure.date.year}年${expenditure.date.monthValue}月${expenditure.date.dayOfMonth}日",
                modifier = Modifier.weight(1f).fillMaxWidth(),
                color = Color.Gray,
                textAlign = TextAlign.End
            )
        }

        // 金額
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Image(
                painter = painterResource(AppIcons.Payments),
                contentDescription = null,
                modifier = Modifier.padding(start = 0.dp, end = 8.dp).size(28.dp),
                colorFilter = ColorFilter.tint(AppColors.darkBlue)
            )
            Text(
                text = "${expenditure.amount}円",
                color = AppColors.darkBlue,
                fontSize = 20.sp
            )
            // 支払い方法
            Text(
                text = expenditure.paymentMethod,
                modifier = Modifier.padding(start = 8.dp),
                color = Color.Gray
            )
        }

        // メモ
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = AppColors.lightGray, RoundedCornerShape(8.dp)),
        ) {
            Text(
                text = expenditure.memo,
                modifier = Modifier.padding(start = 8.dp),
                color = AppColors.darkBlue
            )
        }

        // カテゴリー
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Image(
                painter = painterResource(AppIcons.Category),
                contentDescription = null,
                modifier = Modifier.padding(start = 0.dp, end = 8.dp).size(24.dp),
                colorFilter = ColorFilter.tint(Color.Gray)
            )
            Text(
                text = expenditure.category,
                color = Color.Gray
            )
        }
    }
}

@Composable
private fun incomeRow(income: Income, onClickItem: (Income) -> Unit = {}) {
    Column(
        modifier = Modifier
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(16.dp))
            .height(120.dp)
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(16.dp))
            .padding(12.dp)
            .clickable { onClickItem(income) }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f).fillMaxWidth()
        ) {
            // 項目名
            Text(
                text = income.item,
                modifier = Modifier.weight(3.5f),
                color = AppColors.darkBlue,
                fontSize = 20.sp
            )

            // 日付
            Text(
                text = "${income.date.year}年${income.date.monthValue}月${income.date.dayOfMonth}日",
                modifier = Modifier.weight(1f).fillMaxWidth(),
                color = Color.Gray,
                textAlign = TextAlign.End
            )
        }

        // 金額
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Image(
                painter = painterResource(AppIcons.Savings),
                contentDescription = null,
                modifier = Modifier.padding(start = 0.dp, end = 8.dp).size(28.dp),
                colorFilter = ColorFilter.tint(AppColors.darkBlue)
            )
            Text(
                text = "${income.amount}円",
                color = AppColors.darkBlue,
                fontSize = 20.sp
            )
        }

        // メモ
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = AppColors.lightGray, RoundedCornerShape(8.dp)),
        ) {
            Text(
                text = income.memo,
                modifier = Modifier.padding(start = 8.dp),
                color = AppColors.darkBlue
            )
        }
    }
}

/*---------- featureTimelineここまで ----------*/

// やる事リスト

// AMOUNTに巨大な数を食わせた時のエラー処理
// AMOUNTの頭が0だったら本当にエラーになるのか。
