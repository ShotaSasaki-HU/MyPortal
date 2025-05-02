import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.String
import kotlin.collections.sum
import kotlin.collections.zip
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow

// グラフ描画まとめ

// 色相環の角度からRGBを得る関数（0°：赤, 120°：緑, 240°：青）
private fun hsvToRgb(angle: Float, isComplementary: Boolean): Color {
    val h = angle % 360 // 色相は0~360に正規化
    // val s = 1.0f       // 彩度 (最大値)
    // val v = 1.0f       // 明度 (最大値)
    val s = 0.55f      // 彩度
    val v = 0.9f       // 明度

    val c = v * s
    val x = c * (1 - abs((h / 60) % 2 - 1))
    val m = v - c

    val (rPrime, gPrime, bPrime) = when {
        h < 60  -> Triple(c, x, 0f)
        h < 120 -> Triple(x, c, 0f)
        h < 180 -> Triple(0f, c, x)
        h < 240 -> Triple(0f, x, c)
        h < 300 -> Triple(x, 0f, c)
        else    -> Triple(c, 0f, x)
    }

    val r = ((rPrime + m) * 255).toInt()
    val g = ((gPrime + m) * 255).toInt()
    val b = ((bPrime + m) * 255).toInt()

    if (!isComplementary) {
        return Color(r, g, b)
    } else {
        val rComp = maxOf(r,g,b) + minOf(r,g,b) - r
        val gComp = maxOf(r,g,b) + minOf(r,g,b) - g
        val bComp = maxOf(r,g,b) + minOf(r,g,b) - b

        return Color(rComp, gComp, bComp) // 算出した色の補色を返す。
    }
}

// 円グラフ
@Composable
fun pieChart(
    data: List<Float>,                       // 数値データ
    legend: List<String>,                    // 凡例の文字列
    canvasSize: Float = 250f,                // 円グラフのサイズ
    onLegendNameClick: (String) -> Unit = {} // 凡例クリック時の処理
) {
    // エラー処理
    if (data.size != legend.size) {
        writeError("[Visualization] pieChart: データ数と凡例の数が異なっています。")
        Text("[Visualization] pieChart: データ数と凡例の数が異なっています。", color = Color.Red)
        return
    }

    val listColor = mutableListOf<Color>() // 配色のリスト

    Row(verticalAlignment = Alignment.CenterVertically) {
        // 円グラフ
        Canvas(modifier = Modifier.size(canvasSize.dp)) {
            val total = data.sum()
            var startAngle = 270f

            // 配色
            var colorDegree = 0f // 色相環の角度
            val shift = 50f // 色を進める時にシフトさせる角度
            var isComplementary = false // 補色フラグ

            for (value in data) {
                val sweepAngle = (value / total) * 360f
                drawArc(
                    color = hsvToRgb(colorDegree, isComplementary),
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = true
                )
                listColor.add(hsvToRgb(colorDegree, isComplementary))
                if (!isComplementary) {     // colorDegree通りの色を使った後
                    isComplementary = true  // 次はcolorDegreeの補色を使用
                } else {                    // 補色を使った後
                    isComplementary = false // 次はcolorDegree通りの色を使用
                    colorDegree += shift    // 色相環の角度をシフト
                    colorDegree %= 360f     // 0~359に正規化
                }
                startAngle += sweepAngle
                startAngle %= 360f
            }
        }
        // 凡例
        Column(modifier = Modifier.padding(start = 16.dp)) {
            for ((index, legendName) in legend.withIndex()) {
                Row(
                    modifier = Modifier
                        .padding(bottom = 6.dp)
                        .height(50.dp) // これがないと表示できない。
                        .clickable { onLegendNameClick(legendName) },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Canvas(modifier = Modifier.fillMaxHeight().width(50.dp)) {
                        val width = size.width
                        val height = size.height
                        drawCircle(
                            color = listColor[index],
                            center = Offset(x = width / 2, y = height / 2),
                            radius = size.minDimension / 4
                        )
                    }
                    Column {
                        Text(
                            legendName,
                            modifier = Modifier.weight(1f),
                            color = AppColors.darkBlue
                        )
                        Row(
                            modifier = Modifier.weight(1.5f),
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Text(
                                "${data[index].toInt()}円",
                                fontSize = 24.sp,
                                color = AppColors.darkBlue
                            )
                            Text(
                                String.format("%.1f", (data[index] / data.sum()) * 100f) + '%',
                                modifier = Modifier.padding(start = 8.dp),
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
            // 合計金額
            Row(
                modifier = Modifier
                    .padding(bottom = 6.dp)
                    .height(50.dp) // これがないと表示できない。
                    .drawBehind { // 領域の上辺に線を引く。
                        drawLine(
                            color = Color.Gray,
                            start = Offset(0f, 0f),
                            end = Offset(size.width, 0f),
                            strokeWidth = 1.dp.toPx()
                        )
                    },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Canvas(modifier = Modifier.fillMaxHeight().width(50.dp)) {
                    val width = size.width
                    val height = size.height
                    drawCircle(
                        color = Color.Transparent,
                        center = Offset(x = width / 2, y = height / 2),
                        radius = size.minDimension / 4
                    )
                }
                Column {
                    Text(
                        "合計",
                        modifier = Modifier.weight(1f),
                        color = AppColors.darkBlue
                    )
                    Row(
                        modifier = Modifier.weight(1.5f),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            "${data.sum().toInt()}円",
                            fontSize = 24.sp,
                            color = AppColors.darkBlue
                        )
                        Text(
                            "100.0%",
                            modifier = Modifier.padding(start = 8.dp),
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}

// 積み上げ棒グラフ
// 円グラフと違ってグラフ領域に fillMaxWidth をかけている。
@Composable
fun stackedBarChart(
    data: List<List<Float>>,      // 数値データ
    axisHorizontal: List<String>, // 横軸ラベルの文字列
    legend: List<String>,         // 凡例の文字列
    weightBar: Float = 2f,        // バーの横幅の比率
    weightSpacing: Float = 1f,    // 間隔の横幅の比率
    chartHeightDp: Float = 500f,  // キャンバスの高さ
    verticalUnit: String = "円"   // 縦軸の単位
) {
    // エラー処理
    if (data.size != axisHorizontal.size) {
        writeError("[Visualization] stackedBarChart: データ数と横軸ラベルの数が異なっています。")
        Text("[Visualization] stackedBarChart: データ数と横軸ラベルの数が異なっています。", color = Color.Red)
        return
    }
    val firstBarSize: Int = data.first().size
    for (bar in data) {
        if (bar.size != firstBarSize) {
            writeError("[Visualization] stackedBarChart: 要素数の異なるバーを使用することはできません。")
            Text("[Visualization] stackedBarChart: 要素数の異なるバーを使用することはできません。", color = Color.Red)
            return
        }
    }
    if (firstBarSize != legend.size) {
        writeError("[Visualization] stackedBarChart: バー内の要素数と凡例の数が異なっています。")
        Text("[Visualization] stackedBarChart: バー内の要素数と凡例の数が異なっています。", color = Color.Red)
        return
    }

    // 配色
    var colorDegree = 0f // 色相環の角度
    val shift = 50f // 色を進める時にシフトさせる角度
    var isComplementary = false // 補色フラグ

    val colorList: MutableList<Color> = mutableListOf()
    legend.forEach { _ ->
        colorList.add(hsvToRgb(colorDegree, isComplementary))

        if (!isComplementary) {     // colorDegree通りの色を使った後
            isComplementary = true  // 次はcolorDegreeの補色を使用
        } else {                    // 補色を使った後
            isComplementary = false // 次はcolorDegree通りの色を使用
            colorDegree += shift    // 色相環の角度をシフト
            colorDegree %= 360f     // 0~359に正規化
        }
    }

    // 棒グラフで計算した目盛り間隔 d と、その比率を縦軸目盛りへ渡すための変数
    // rememberにより、棒グラフがこれらに代入すると縦軸目盛りが再描画される。
    var d by remember { mutableStateOf(0f) }
    // 最初の描画をエラーにしない・データが無いとき縦軸を0円のみにするための初期値 1.1f
    var dWeight by remember { mutableStateOf(1.1f) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(chartHeightDp.dp)
    ) {
        // 縦軸にweightを設定せずグラフに密着させるためにRowで括る。
        Row(modifier = Modifier.fillMaxHeight().weight(5f)) {
            // 縦軸目盛りに対応する文字列
            Column(modifier = Modifier.fillMaxHeight()) {
                Column(modifier = Modifier.weight(9f)) {
                    val dNum = (1 / dWeight).toInt()
                    if ((1 - (dWeight * dNum)) > 0f) { // weightが0fになるとエラーになるため。
                        Box(
                            modifier = Modifier.weight(1 - (dWeight * dNum)),
                            contentAlignment = Alignment.BottomStart
                        ) {
                            Text("${(dNum * d).toInt()}$verticalUnit", color = Color.Gray)
                        }
                    }
                    for (i in dNum-1 downTo 0) {
                        Box(
                            modifier = Modifier.weight(dWeight),
                            contentAlignment = Alignment.BottomStart
                        ) {
                            Text("${(i * d).toInt()}$verticalUnit", color = Color.Gray)
                        }
                    }
                }
                Text("", modifier = Modifier.weight(1f)) // 横軸ラベルと同じ高さのスペーサー
            }

            // 棒グラフと横軸ラベル
            Column(modifier = Modifier.fillMaxSize()) {
                // 棒グラフ
                Canvas(
                    modifier = Modifier.fillMaxSize().weight(9f)
                ) {
                    // 枠線を描画（データが存在せず return する時もグラフ領域がわかるように先に書く。）
                    drawRect(
                        color = Color.Gray,           // 枠線の色
                        size = size,                  // Canvas 全体のサイズ
                        style = Stroke(width = 1f)    // 線の太さ
                    )

                    val canvasWidth = size.width
                    val canvasHeight = size.height
                    val barWidth = canvasWidth * (weightBar / (data.size * (weightBar + weightSpacing)))
                    val barSpacing = canvasWidth * (weightSpacing / (data.size * (weightBar + weightSpacing)))

                    var maxBarSum: Float = 0f // バー内の総和の最大値
                    for (bar in data) {
                        if (bar.sum() > maxBarSum) {
                            maxBarSum = bar.sum()
                        }
                    }

                    // 縦軸目盛りを決定
                    val verticalRange: Float = (maxBarSum * 1.2f) - 0f // データの範囲（最大値の120%）
                    d = verticalRange / 2f // 初期値: 目盛り間隔 d の最大値
                    // 1・2・5の3通り、あるいはその10倍や100倍、1/10倍や1/100倍などになるまで d を減らす。
                    while (true) {
                        // normalized * 10^n == d となる normalized を求める。
                        var normalized = d / 10.0.pow( floor(log10(d.toDouble())) )
                        normalized = floor(normalized * 10f) / 10f // 小数第2位を切り捨て
                        // println("d = $d, normalized = $normalized")
                        if (normalized == 1.0 || normalized == 2.0 || normalized == 5.0) { // 最適な d を発見
                            d = (normalized * 10.0.pow( floor(log10(d.toDouble())) )).toFloat()
                            break
                        }
                        // エラー処理
                        if (d <= 0) {
                            // データが無い時に writeError すると大量に書き込まれるため。
                            if (maxBarSum > 0) {
                                writeError("[Visualization] stackedBarChart: 縦軸の目盛り間隔を決定できませんでした。")
                            }
                            return@Canvas
                        }
                        // normalized の小数第1位に当たる桁を1減らす。
                        d -= 10.0.pow( floor(log10(d.toDouble())) - 1 ).toFloat()
                    }
                    // println(d)
                    dWeight = d / verticalRange

                    // 縦軸目盛りに対応する横線
                    var currentLineHeight: Float = canvasHeight
                    val shiftLineHeight = (d / verticalRange) * canvasHeight
                    while (currentLineHeight > 0) {
                        drawLine(
                            color = Color.LightGray,
                            start = Offset(x = 0f, y = currentLineHeight),
                            end = Offset(x = canvasWidth, y = currentLineHeight),
                            strokeWidth = 1.dp.toPx()
                        )
                        currentLineHeight -= shiftLineHeight
                    }

                    // バーの描画
                    for ((index, bar) in data.withIndex()) {
                        // バーの高さを計算
                        val currentBarHeight = (bar.sum() / verticalRange) * canvasHeight
                        // バーの左上の座標を計算
                        val xOffset = (barSpacing / 2f) + (index * (barWidth + barSpacing))
                        var yOffset = canvasHeight - currentBarHeight

                        // ひと月について四角を積み重ねていく。（上から下に描くけど。）
                        for ((stack, color) in bar.zip(colorList)) {
                            val stackHeight = (stack / bar.sum()) * currentBarHeight

                            drawRect(
                                color = color,
                                topLeft = Offset(x = xOffset, y = yOffset),
                                size = Size(width = barWidth, height = stackHeight)
                            )

                            // 次のstackのために、y座標を更新しておく。
                            yOffset += stackHeight
                        }
                    }
                }

                // 横軸ラベル
                Row(modifier = Modifier.fillMaxSize().weight(1f)) {
                    for (str in axisHorizontal) {
                        Text(
                            str,
                            color = Color.Gray,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        // 凡例
        Column(
            modifier = Modifier.fillMaxHeight().weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            for ((legendName, color) in legend.zip(colorList)) {
                Row(
                    modifier = Modifier
                        .padding(bottom = 0.dp)
                        .height(50.dp), // これがないと表示できない。
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Canvas(modifier = Modifier.fillMaxHeight().width(50.dp)) {
                        val width = size.width
                        val height = size.height
                        drawCircle(
                            color = color,
                            center = Offset(x = width / 2, y = height / 2),
                            radius = size.minDimension / 4
                        )
                    }
                    Text(
                        legendName,
                        color = AppColors.darkBlue
                    )
                }
            }
        }
    }
}

// 折れ線グラフ
// 円グラフと違ってグラフ領域に fillMaxWidth をかけている。
@Composable
fun lineChart(
    data: List<Float>,            // 数値データ
    axisHorizontal: List<String>, // 横軸ラベルの文字列
    chartHeightDp: Float = 500f,  // キャンバスの高さ
    color: Color = Color.Black,   // グラフの色
    verticalUnit: String = "円",  // 縦軸の単位
    radius: Float = 10f            // 点の半径
) {
    // エラー処理
    if (data.size != axisHorizontal.size) {
        writeError("[Visualization] lineChart: データ数と横軸ラベルの数が異なっています。")
        Text("[Visualization] lineChart: データ数と横軸ラベルの数が異なっています。", color = Color.Red)
        return
    }

    // 縦軸目盛り間隔
    var d by remember { mutableStateOf(0f) }
    // 縦軸目盛り間隔のキャンバスに対する割合
    var dWeight by remember { mutableStateOf(1.1f) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(chartHeightDp.dp)
    ) {
        // 縦軸目盛りに対応する文字列
        Column(modifier = Modifier.fillMaxHeight()) {
            Column(modifier = Modifier.weight(9f)) {
                val dNum = (1 / dWeight).toInt()
                if ((1 - (dWeight * dNum)) > 0f) { // weightが0fになるとエラーになるため。
                    Box(
                        modifier = Modifier.weight(1 - (dWeight * dNum)),
                        contentAlignment = Alignment.BottomStart
                    ) {
                        Text("${(dNum * d).toInt()}$verticalUnit", color = Color.Gray)
                    }
                }
                for (i in dNum-1 downTo 0) {
                    Box(
                        modifier = Modifier.weight(dWeight),
                        contentAlignment = Alignment.BottomStart
                    ) {
                        Text("${(i * d).toInt()}$verticalUnit", color = Color.Gray)
                    }
                }
            }
            Text("", modifier = Modifier.weight(1f)) // 横軸ラベルと同じ高さのスペーサー
        }

        // 折れ線グラフと横軸ラベル
        Column(modifier = Modifier.fillMaxSize()) {
            // 折れ線グラフ
            Canvas(
                modifier = Modifier.fillMaxSize().weight(9f)
            ) {
                // 枠線を描画（データが存在せず return する時もグラフ領域がわかるように先に書く。）
                drawRect(
                    color = Color.Gray,           // 枠線の色
                    size = size,                  // Canvas 全体のサイズ
                    style = Stroke(width = 1f)    // 線の太さ
                )

                val canvasWidth = size.width
                val canvasHeight = size.height

                var maxValue: Float = data.max() // 最大値

                var verticalRange: Float = 1f
                // データが全て0fでも描画できるための条件分岐
                if (maxValue > 0f) {
                    // 縦軸目盛りを決定
                    verticalRange = (maxValue * 1.2f) - 0f // データの範囲（最大値の120%）
                    d = verticalRange / 2f // 初期値: 目盛り間隔 d の最大値
                    // 1・2・5の3通り、あるいはその10倍や100倍、1/10倍や1/100倍などになるまで d を減らす。
                    while (true) {
                        // normalized * 10^n == d となる normalized を求める。
                        var normalized = d / 10.0.pow( floor(log10(d.toDouble())) )
                        normalized = floor(normalized * 10f) / 10f // 小数第2位を切り捨て
                        // println("d = $d, normalized = $normalized")
                        if (normalized == 1.0 || normalized == 2.0 || normalized == 5.0) { // 最適な d を発見
                            d = (normalized * 10.0.pow( floor(log10(d.toDouble())) )).toFloat()
                            break
                        }
                        // エラー処理
                        if (d <= 0) {
                            writeError("[Visualization] lineChart: 縦軸の目盛り間隔を決定できませんでした。")
                            return@Canvas
                        }
                        // normalized の小数第1位に当たる桁を1減らす。
                        d -= 10.0.pow( floor(log10(d.toDouble())) - 1 ).toFloat()
                    }
                    // println(d)
                    dWeight = d / verticalRange
                } else { // データが全て0fの場合
                    d = 1f
                    dWeight = 1.1f
                }

                // 縦軸目盛りに対応する横線
                var currentLineHeight: Float = canvasHeight
                val shiftLineHeight = (d / verticalRange) * canvasHeight
                while (currentLineHeight > 0) {
                    drawLine(
                        color = Color.LightGray,
                        start = Offset(x = 0f, y = currentLineHeight),
                        end = Offset(x = canvasWidth, y = currentLineHeight),
                        strokeWidth = 1.dp.toPx(),
                    )
                    currentLineHeight -= shiftLineHeight
                }

                // 折れ線の描画
                for (index in 0..data.size-2) {
                    val clearance: Float = canvasWidth / data.size // 横軸の間隔

                    // スタートポイントの座標
                    val startX: Float = (clearance / 2) + (index * clearance)
                    val startY: Float = canvasHeight - ((data[index] / verticalRange) * canvasHeight)

                    // エンドポイントの座標
                    val endX: Float = (clearance / 2) + ((index + 1) * clearance)
                    val endY: Float = canvasHeight - ((data[index + 1] / verticalRange) * canvasHeight)

                    drawLine(
                        color = color,
                        start = Offset(x = startX, y = startY),
                        end = Offset(x = endX, y = endY),
                        strokeWidth = 3.dp.toPx(),
                    )
                    drawCircle(
                        color = color,
                        center = Offset(x = startX, y = startY),
                        radius = radius
                    )
                    if (index == (data.size - 2)) {
                        drawCircle(
                            color = color,
                            center = Offset(x = endX, y = endY),
                            radius = radius
                        )
                    }
                }
            }

            // 横軸ラベル
            Row(modifier = Modifier.fillMaxSize().weight(1f)) {
                for (str in axisHorizontal) {
                    Text(
                        str,
                        color = Color.Gray,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
