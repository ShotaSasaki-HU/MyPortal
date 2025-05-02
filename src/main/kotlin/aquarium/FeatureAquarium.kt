package aquarium

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.FloatingActionButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.useResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlin.math.atan2
import kotlin.math.floor

@Composable
fun featureAquarium() {
    // 仮想水槽のサイズ（演算）（単位：ピクセル）
    val virtualTankSize: Offset = Offset(x = 500f, y = 300f)
    // 現実水槽のサイズ（描画）（単位：ピクセル）
    // 現実水槽Boxの onGloballyPositioned で更新
    var actualTankSize: Offset by remember { mutableStateOf(Offset(x = 1666f, y = 1000f)) }

    // 環境
    val fishes = remember { mutableStateListOf<AbstractFish>() } // 魚のリスト
    val foods = remember { mutableStateListOf<Food>() } // エサのリスト
    var waterQuality: Float = 0.0f
    var isLightOn by remember { mutableStateOf(false) } // ライトのオン・オフ

    var trigger by remember { mutableStateOf(false) } // Canvasの再描画トリガー

    // Offset型に対する拡張関数：現実の座標から仮想の座標に変換（ピクセル）
    fun Offset.toVirtualTank(): Offset {
        val scaleX = virtualTankSize.x / actualTankSize.x
        val scaleY = virtualTankSize.y / actualTankSize.y
        return Offset(this.x * scaleX, this.y * scaleY)
    }

    // Offset型に対する拡張関数：仮想の座標から現実の座標に変換（ピクセル）
    fun Offset.toRealTank(): Offset {
        val scaleX = actualTankSize.x / virtualTankSize.x
        val scaleY = actualTankSize.y / virtualTankSize.y
        return Offset(this.x * scaleX, this.y * scaleY)
    }

    // Offset型に対する拡張関数：Offset型からIntOffset型へ変換
    fun Offset.toIntOffset(): IntOffset {
        return IntOffset(this.x.toInt(), this.y.toInt())
    }

    // IntSize型に対する拡張関数：仮想のサイズから現実のサイズに変換（ピクセル）
    fun IntSize.toRealTank(): IntSize {
        val scaleX = actualTankSize.x / virtualTankSize.x
        val scaleY = actualTankSize.y / virtualTankSize.y
        return IntSize((this.width * scaleX).toInt(), (this.height * scaleY).toInt())
    }

    // ベクトルの向き（回転行列で画像を回す用）
    // FishBehavior の方の拡張関数とは中身が違う事に注意せよ。
    fun Offset.angle(isFacingRight: Boolean): Float {
        val sign: Int = if (!isFacingRight) { -1 } else { 1 }
        val angle: Float = Math.toDegrees(atan2(y.toDouble(), sign * x.toDouble())).toFloat()
        return -angle
    }

    var lastTime = System.nanoTime()

    fun calculateDeltaTime(): Float {
        val currentTime = System.nanoTime()
        val deltaTime = (currentTime - lastTime) / 1_000_000_000f // 秒単位に変換
        lastTime = currentTime
        return deltaTime
    }

    // 初期の魚を生成
    LaunchedEffect(Unit) {
        repeat(5) {
            fishes.add(NeonTetra(vTankSize = virtualTankSize))
        }
        repeat(5) {
            fishes.add(CardinalTetra(vTankSize = virtualTankSize))
        }
        repeat(5) {
            fishes.add(BlackNeonTetra(vTankSize = virtualTankSize))
        }
        /*
        repeat(5) {
            fishes.add(AlbinoNeonTetra(vTankSize = virtualTankSize))
        }*/
    }

    // 親レイアウトを全画面にして水槽Boxを中央揃え
    Box(
        modifier = Modifier
            .fillMaxSize() // 親Boxを画面全体に広げる
            .background(Color.Black) // 背景色（任意）
    ) {
        // 現実水槽Box（現実水槽Canvasと同じサイズ）
        Box(
            modifier = Modifier
                .aspectRatio(virtualTankSize.x / virtualTankSize.y) // 水槽のアスペクト比
                .fillMaxSize() // アスペクト比を前提にめいっぱい拡大（設定の順番が大事）
                .background(Color.Red)
                .onGloballyPositioned { coordinates ->
                    // 現実水槽のサイズをピクセルで取得
                    actualTankSize = Offset(x = coordinates.size.width.toFloat(), y = coordinates.size.height.toFloat())
                }
                .pointerInput(Unit) {
                    detectTapGestures { tapOffset -> // クリック処理
                        // 現実Pxを仮想Pxへ変換
                        val virtualTapOffset = tapOffset.toVirtualTank()

                        foods.add(
                            // Food(virtualTankSize = virtualTankSize, position = Offset(virtualTapOffset.x, 1f))
                            Food(virtualTankSize = virtualTankSize, position = virtualTapOffset)
                        )
                    }
                }
                .align(Alignment.Center)
        ) {
            // 現実水槽Canvas
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = Color.Cyan)
            ) {
                trigger

                // 草02
                drawImage(
                    image = useResource(resourcePath = "aquarium/dot-illust/kusa_02.png", ::loadImageBitmap),
                    srcOffset = IntOffset(0, 0),
                    srcSize = IntSize(500, 269),
                    dstOffset = Offset(
                        x = virtualTankSize.x - 173f,
                        y = virtualTankSize.y - (54f + 18f)
                    ).toRealTank().toIntOffset(), // 描画するキャンバス上の位置
                    dstSize = IntSize(100, 54).toRealTank() // 描画するキャンバス上のサイズ
                )
                // 岩山
                drawImage(
                    image = useResource(resourcePath = "aquarium/dot-illust/yama_iwayama.png", ::loadImageBitmap),
                    srcOffset = IntOffset(0, 0),
                    srcSize = IntSize(500, 344),
                    dstOffset = Offset(
                        x = virtualTankSize.x - 125f,
                        y = virtualTankSize.y - (86f + 17f)
                    ).toRealTank().toIntOffset(), // 描画するキャンバス上の位置
                    dstSize = IntSize(125, 86).toRealTank() // 描画するキャンバス上のサイズ
                )
                // 薬草01
                drawImage(
                    image = useResource(resourcePath = "aquarium/dot-illust/yakuso_01.png", ::loadImageBitmap),
                    srcOffset = IntOffset(0, 0),
                    srcSize = IntSize(500, 334),
                    dstOffset = Offset(
                        x = virtualTankSize.x - 173f,
                        y = virtualTankSize.y - (27f + 18f)
                    ).toRealTank().toIntOffset(), // 描画するキャンバス上の位置
                    dstSize = IntSize(40, 27).toRealTank() // 描画するキャンバス上のサイズ
                )
                // 色の薄い岩
                drawImage(
                    image = useResource(resourcePath = "aquarium/dot-illust/iwa_02.png", ::loadImageBitmap),
                    srcOffset = IntOffset(0, 0),
                    srcSize = IntSize(500, 406),
                    dstOffset = Offset(
                        x = virtualTankSize.x - 146f,
                        y = virtualTankSize.y - (41f + 1f)
                    ).toRealTank().toIntOffset(), // 描画するキャンバス上の位置
                    dstSize = IntSize(50, 41).toRealTank() // 描画するキャンバス上のサイズ
                )
                // 城
                drawImage(
                    image = useResource(resourcePath = "aquarium/dot-illust/shiro_01_gray_flag_green.png", ::loadImageBitmap),
                    srcOffset = IntOffset(0, 0),
                    srcSize = IntSize(500, 431),
                    dstOffset = Offset(
                        x = 10f,
                        y = virtualTankSize.y - (86f + 18f)
                    ).toRealTank().toIntOffset(), // 描画するキャンバス上の位置
                    dstSize = IntSize(100, 86).toRealTank() // 描画するキャンバス上のサイズ
                )
                // 砦
                drawImage(
                    image = useResource(resourcePath = "aquarium/dot-illust/toride_gray_flag_green.png", ::loadImageBitmap),
                    srcOffset = IntOffset(0, 0),
                    srcSize = IntSize(500, 677),
                    dstOffset = Offset(
                        x = 140f,
                        y = virtualTankSize.y - (68f + 18f)
                    ).toRealTank().toIntOffset(), // 描画するキャンバス上の位置
                    dstSize = IntSize(50, 68).toRealTank() // 描画するキャンバス上のサイズ
                )

                // 底砂を描画（余りの描画ややこしい。）
                val virtualSandSize: IntSize = IntSize(20, 20)
                val maxIndex: Int = floor(virtualTankSize.x / virtualSandSize.width).toInt()
                for (index in 0..maxIndex) {
                    drawImage(
                        image = useResource(resourcePath = "aquarium/dot-illust/maptile_jimen_sabaku.png", ::loadImageBitmap),
                        srcOffset = IntOffset(0, 0),
                        srcSize = IntSize(
                            width = if (index != maxIndex) {
                                500
                            } else {
                                ((actualTankSize.x - (maxIndex * virtualSandSize.toRealTank().width)) * 500f / virtualSandSize.toRealTank().width).toInt()
                            },
                            height = 500
                        ),
                        dstOffset = IntOffset(
                            x = index * virtualSandSize.toRealTank().width,
                            y = actualTankSize.y.toInt() - virtualSandSize.toRealTank().height
                        ), // 描画するキャンバス上の位置
                        dstSize = if (index != maxIndex) {
                            virtualSandSize.toRealTank()
                        } else {
                            IntSize(
                                width = (actualTankSize.x - (maxIndex * virtualSandSize.toRealTank().width)).toInt(),
                                height = virtualSandSize.toRealTank().height
                            )
                        } // 描画するキャンバス上のサイズ
                    )
                }

                // 魚を描画
                fishes.forEach { fish ->
                    val scaleX = if (fish.isFacingRight) -1f else 1f
                    val scaleY = 1f

                    val center = fish.centerPosition.toRealTank()

                    withTransform({ // ここで回転行列使えるかな。rotateで回せそう。
                        scale(scaleX, scaleY, pivot = center)
                        rotate(degrees = fish.velocity.angle(isFacingRight = fish.isFacingRight), pivot = center)
                    }) {
                        drawImage(
                            image = fish.image,
                            srcOffset = IntOffset(fish.sourceRect.left, fish.sourceRect.top),
                            srcSize = IntSize(fish.sourceRect.width, fish.sourceRect.height),
                            dstOffset = fish.topLeftPosition.toRealTank().toIntOffset(), // 描画するキャンバス上の位置
                            dstSize = fish.currentSize.toRealTank() // 描画するキャンバス上のサイズ
                        )
                        /*
                        drawRect(
                            color = Color.Red,
                            topLeft = fish.topLeftPosition.toRealTank(),
                            size = Size(fish.currentSize.toRealTank().width.toFloat(), fish.currentSize.toRealTank().height.toFloat()),
                            style = Stroke(width = 1f)
                        )*/
                    }
                    /*
                    drawCircle(
                        center = fish.centerPosition.toRealTank(),
                        radius = 5f,
                        color = Color.Red
                    )
                    drawCircle(
                        center = fish.headPosition.toRealTank(),
                        radius = 5f,
                        color = Color.Red
                    )*/
                }

                // 餌を描画
                foods.forEach { food ->
                    drawCircle(
                        color = AppColors.beige,
                        center = food.position.toRealTank(),
                        radius = 5f
                    )
                }

                // 薄い暗闇エフェクトを描画
                if (!isLightOn) {
                    drawRect(
                        color = Color.Black.copy(alpha = 0.7f),
                        size = size
                    )
                }
            }

            // ライト切り替えボタン
            FloatingActionButton(
                onClick = { isLightOn = !isLightOn },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                backgroundColor = if (isLightOn) Color.Yellow else Color.Gray
            ) {
                Image(
                    painter = painterResource(
                        if (isLightOn) { AppIcons.LightBulb } else { AppIcons.LightOff }
                    ),
                    contentDescription = null,
                    modifier = Modifier.padding(start = 0.dp, end = 0.dp).size(24.dp),
                    colorFilter = ColorFilter.tint(Color.Black)
                )
            }
        }

        // ロジックループ
        LaunchedEffect(Unit) {
            while (true) {
                val deltaTime = calculateDeltaTime()

                // 魚を更新
                fishes.forEach {
                    it.update(deltaTime = deltaTime * 1f, fishes = fishes, foods = foods, waterQuality = waterQuality)
                }
                fishes.removeIf { it.isAlive == false }

                // 餌を更新
                foods.forEach { it.update(deltaTime = deltaTime) }
                // isConsumed が true の Food をリストから削除
                foods.removeIf { it.isConsumed }

                // フレームレートを制御
                delay(16L) // 約60fps（16L = 16ミリ秒）

                trigger = !trigger // Canvasの再描画
            }
        }
    }
}
