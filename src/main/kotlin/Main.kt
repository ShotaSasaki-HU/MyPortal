import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.res.painterResource
import java.awt.GraphicsEnvironment

import top.featureTop
import budget.featureBudget
import budget.TransactionManager
import aquarium.featureAquarium

// 共通の色
object AppColors {
    val lightBlue = Color(red = 226, green = 238, blue = 244)
    val darkBlue = Color(red = 19, green = 32, blue = 66)
    val beige = Color(red = 240, green = 232, blue = 222)
    val brown = Color(red = 126, green = 41, blue = 32)
    val lightGray = Color(red = 241, green = 241, blue = 241)
}

// 共通のアイコン
object AppIcons {
    val ArrowDropDown = "icons/ArrowDropDown_24dp.svg"
    val Menu = "icons/Menu_24dp.svg"
    val Payments = "icons/Payments_24dp.svg"
    val Save = "icons/Save_24dp.svg"
    val Savings = "icons/Savings_24dp.svg"
    val ViewTimeline = "icons/ViewTimeline_24dp.svg"
    val ArrowBackIos = "icons/ArrowBackIos_24dp.svg"
    val ArrowForwardIos = "icons/ArrowForwardIos_24dp.svg"
    val Search = "icons/Search_24dp.svg"
    val Category = "icons/Category_24dp.svg"
    val LightBulb = "icons/LightBulb_24dp.svg"
    val LightOff = "icons/LightOff_24dp.svg"
}

@Composable
fun mainMenu() {
    var selectedFeature by remember { mutableStateOf(1) }  // 選択されたタブメニューを管理
    val featureList = listOf("TOP", "BUDGET", "AQUARIUM") // タブメニューの項目リスト

    Column {
        // ヘッダー
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(color = AppColors.lightBlue)
                .weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 画像
            Image(
                painter = painterResource("Gemini_Generated_Image_tr1h2ktr1h2ktr1h_satellite.jpg"),
                contentDescription = "logo",
                modifier = Modifier.size(200.dp).weight(1f).padding(12.dp)
            )

            // タブメニュー
            Row(
                modifier = Modifier.weight(8f),
                horizontalArrangement = Arrangement.Center  // Row内のボタンを中央揃え
            ) {
                featureList.forEachIndexed { index, feature ->
                    Button(
                        onClick = { selectedFeature = index },
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = if (selectedFeature == index) AppColors.darkBlue else AppColors.lightBlue
                        ),
                        modifier = Modifier.padding(5.dp, 5.dp, 5.dp, 5.dp),
                        shape = RoundedCornerShape(18.dp, 18.dp, 18.dp, 18.dp)
                    ) {
                        Text(
                            text = feature,
                            color = if (selectedFeature == index) Color.White else AppColors.darkBlue // テキストの色変更
                        )
                    }
                }
            }
        }

        Box(modifier = Modifier.fillMaxWidth().weight(8f)) {
            // 選択された機能をココに記述
            when (featureList[selectedFeature]) {
                "TOP" -> featureTop()
                "BUDGET" -> featureBudget()
                "AQUARIUM" -> featureAquarium()
                else -> {}
            }
        }
    }
}

fun main() = application {
    if (setupAppDirectoryAndFiles()) {
        println("フォルダとファイルのセットアップが完了しました")
    } else {
        println("セットアップに失敗しました")
    }

    // 画面サイズを取得
    val screenSize = GraphicsEnvironment.getLocalGraphicsEnvironment().defaultScreenDevice.displayMode

    Window(
        onCloseRequest = {
            // アプリが閉じられる前に保存処理を実行
            // 理由がよくわからないが、TransactionManagerのinit内で発生したwriteError（行の破損など）が、
            // 何かキッカケがあるまで待機し続けているため、少なくとも閉じる時にはキッカケを作ってあげる。
            TransactionManager.saveTransactionsToCsv()

            // アプリを終了
            exitApplication()
        },
        title = "MY PORTAL",
        state = WindowState(
            // 画面いっぱいにウィンドウを拡大
            width = screenSize.width.dp,
            height = screenSize.height.dp
        )
    ) {
        MaterialTheme {
            mainMenu()
        }
    }
}
