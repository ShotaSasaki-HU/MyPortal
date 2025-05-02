package top

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.DragData
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.delay
import androidx.compose.ui.platform.LocalDensity


@Composable
fun featureTop() {
    var boxSizeInDp by remember { mutableStateOf(0.dp to 0.dp) }
    val density = LocalDensity.current
    var shift by remember { mutableStateOf(0) }

    /*
    LaunchedEffect(Unit) {
        while (true) {
            for (i in 0 downTo -560) {
                shift = i
                delay(10)
            }
            delay(2000)
            for (i in -560..0) {
                shift = i
                delay(10)
            }
            delay(2000)
        }
    }*/

    /*
    Box(
        modifier = Modifier
            .background(color = Color.Gray)
            .fillMaxWidth()
            .height(560.dp)
            .onGloballyPositioned { coordinates ->
                val sizeInPx = coordinates.size
                boxSizeInDp = with(density) {
                    sizeInPx.width.toDp() to sizeInPx.height.toDp()
                }
            }
    ) {
        Image(
            painter = painterResource("toad-character-space-video-games-mario-kart-8-wallpaper.jpg"),
            contentDescription = "toad",
            modifier = Modifier
                .offset(x = ((boxSizeInDp.first / 2) - 280.dp + shift.dp)) // 画像のオフセットを設定
                .size(width = 560.dp, height = 560.dp),
            contentScale = ContentScale.Crop
        )
        Image(
            painter = painterResource("20220512-eht-fig-full.jpg"),
            contentDescription = "bh",
            modifier = Modifier
                .offset(x = ((boxSizeInDp.first / 2) + 280.dp + shift.dp)) // 画像のオフセットを設定
                .size(width = 560.dp, height = 560.dp),
            contentScale = ContentScale.Crop
        )
    }*/
    Text("トップページ\n天気や家計簿のサマリーなど。", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
    // Text("^", modifier = Modifier.fillMaxWidth(), textAlign = TextAlign.Center)
    /*
    // 画像リスト（適切な画像リソースを追加）
    val images = listOf(
        painterResource("20220512-eht-fig-full.jpg"), // 画像1
        painterResource("SPACEX.jpg"), // 画像2
        painterResource("toad-character-space-video-games-mario-kart-8-wallpaper.jpg")  // 画像3
    )

    var offset by remember { mutableStateOf(0f) } // 画像のオフセット
    var currentImageIndex by remember { mutableStateOf(0) }

    LaunchedEffect(Unit) {
        while (true) {
            // 横に流れる動き
            for (i in 0..images.size-1) {
                currentImageIndex = i
                offset = 0f // 初期位置に戻す
                // 画像が右から左に流れるアニメーション
                for (x in 3000 downTo 0 step 5) { // 横に動かすステップ
                    offset = x.toFloat()
                    delay(10) // アニメーションのスピード調整
                }
                // 一度流れるとオフセットをリセット
                offset = -3000f
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        // アニメーション中の画像
        Image(
            painter = images[currentImageIndex],
            contentDescription = "Scrolling Image",
            modifier = Modifier
                .offset(x = offset.dp) // 画像のオフセットを設定
                .fillMaxHeight()
        )
    }*/
}
