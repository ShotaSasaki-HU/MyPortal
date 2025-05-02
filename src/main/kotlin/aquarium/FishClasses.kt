package aquarium

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.res.loadImageBitmap
import androidx.compose.ui.res.useResource
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import kotlin.random.Random

/*--------------- 群れる性質ここから ---------------*/

// ネオンテトラ
class NeonTetra(
    vTankSize: Offset,
    vSandHeight: Float = 20f,
    cPosition: Offset = Offset(vTankSize.x * Random.nextFloat(), vTankSize.y * Random.nextFloat()),
    v0: Offset = Offset(0f, 0f),
    acc0: Offset = Offset(0f, 0f),
    cSize: IntSize = IntSize(18, 10),
    fAte: Int = 0,
    fNess: Double = 0.0,
    hPoint: Double = 1.0,
    lTime: Float = 60f * 60f * 24f * 365f * 1.5f
): SchoolingFish() {
    // プロパティ
    override val virtualTankSize: Offset = vTankSize
    override val virtualSandHeight: Float = vSandHeight
    override var centerPosition: Offset = cPosition
    override var topLeftPosition: Offset = Offset(0f, 0f)
    override var headPosition: Offset = Offset(0f, 0f)
    override var velocity: Offset = v0
    override val maxVelocity: Float = 280f
    override var acc: Offset = acc0
    override val maxAcc: Float = 470f

    override val image: ImageBitmap = useResource(resourcePath = "aquarium/kansyoufish/1261.png", ::loadImageBitmap)
    override val sourceRect: IntRect = IntRect(left = 3, right = 29, top = 8, bottom = 22) // 画像のトリミング
    override var currentSize: IntSize = cSize // 現在の表示サイズ（ピクセル）
    override val minSize: IntSize = IntSize(18, 10) // 最小サイズ
    override val maxSize: IntSize = IntSize(35, 19) // 最大サイズ
    override val foodRequired: Int = 1500 // 最大まで成長するために必要なエサの数
    override var foodAte: Int = fAte // 今まで摂取したエサの数

    override var fullness: Double = fNess // 満腹度（0.0 ~ 1.0）
    override var hp: Double = hPoint // 体力（0.0 ~ 1.0）
    override var isFacingRight: Boolean = false // 右を向いていますか？
    override var isAlive: Boolean = true
    override var lifeTime: Float = lTime

    override val detectionRange: Float = 200f // 視認距離（ピクセル）
    override val kpChaseFood: Float = 3f // エサ追いのPゲイン
    override val cruisingSpeed: Float = 40f // 巡航速さ
    override val kpCruising: Float = 0.6f // 巡航のPゲイン
    override val threats: MutableSet<AbstractFish> = mutableSetOf() // その時点で脅威と認識している魚のリスト
    override val kpEscape: Float = 0.01f // 退避のPゲイン
    override val separationDist: Float = 40f // 仲間との適正セパレート距離
    override val kpSeparation: Float = 0.1f // セパレートのPゲイン
    override val kpAlignment: Float = 0.05f // 整列のPゲイン
    override val kpCenter: Float = 0.05f // 中央指向のPゲイン

    override fun update(deltaTime: Float, fishes: MutableList<AbstractFish>, foods: MutableList<Food>, waterQuality: Float) {
        this.resetAcc() // 忘れずに。
        super.update(deltaTime, fishes, foods, waterQuality)
    }
}

// カージナルテトラ
class CardinalTetra(
    vTankSize: Offset,
    vSandHeight: Float = 20f,
    cPosition: Offset = Offset(vTankSize.x * Random.nextFloat(), vTankSize.y * Random.nextFloat()),
    v0: Offset = Offset(0f, 0f),
    acc0: Offset = Offset(0f, 0f),
    cSize: IntSize = IntSize(18, 9),
    fAte: Int = 0,
    fNess: Double = 0.0,
    hPoint: Double = 1.0,
    lTime: Float = 60f * 60f * 24f * 365f * 3f
): SchoolingFish() {
    // プロパティ
    override val virtualTankSize: Offset = vTankSize
    override val virtualSandHeight: Float = vSandHeight
    override var centerPosition: Offset = cPosition
    override var topLeftPosition: Offset = Offset(0f, 0f)
    override var headPosition: Offset = Offset(0f, 0f)
    override var velocity: Offset = v0
    override val maxVelocity: Float = 330f
    override var acc: Offset = acc0
    override val maxAcc: Float = 470f

    override val image: ImageBitmap = useResource(resourcePath = "aquarium/kansyoufish/1262.png", ::loadImageBitmap)
    override val sourceRect: IntRect = IntRect(left = 2, right = 30, top = 8, bottom = 22) // 画像のトリミング
    override var currentSize: IntSize = cSize // 現在の表示サイズ（ピクセル）
    override val minSize: IntSize = IntSize(18, 9) // 最小サイズ
    override val maxSize: IntSize = IntSize(45, 23) // 最大サイズ
    override val foodRequired: Int = 1500 // 最大まで成長するために必要なエサの数
    override var foodAte: Int = fAte // 今まで摂取したエサの数

    override var fullness: Double = fNess // 満腹度（0.0 ~ 1.0）
    override var hp: Double = hPoint // 体力（0.0 ~ 1.0）
    override var isFacingRight: Boolean = false // 右を向いていますか？
    override var isAlive: Boolean = true
    override var lifeTime: Float = lTime

    override val detectionRange: Float = 200f // 視認距離（ピクセル）
    override val kpChaseFood: Float = 3f // エサ追いのPゲイン
    override val cruisingSpeed: Float = 50f // 巡航速さ
    override val kpCruising: Float = 1f // 巡航のPゲイン
    override val threats: MutableSet<AbstractFish> = mutableSetOf() // その時点で脅威と認識している魚のリスト
    override val kpEscape: Float = 0.01f // 退避のPゲイン
    override val separationDist: Float = 45f // 仲間との適正セパレート距離
    override val kpSeparation: Float = 1f // セパレートのPゲイン
    override val kpAlignment: Float = 1f // 整列のPゲイン
    override val kpCenter: Float = 0.6f // 中央指向のPゲイン

    override fun update(deltaTime: Float, fishes: MutableList<AbstractFish>, foods: MutableList<Food>, waterQuality: Float) {
        this.resetAcc() // 忘れずに。
        super.update(deltaTime, fishes, foods, waterQuality)
    }
}

// ブラックネオンテトラ
class BlackNeonTetra(
    vTankSize: Offset,
    vSandHeight: Float = 20f,
    cPosition: Offset = Offset(vTankSize.x * Random.nextFloat(), vTankSize.y * Random.nextFloat()),
    v0: Offset = Offset(0f, 0f),
    acc0: Offset = Offset(0f, 0f),
    cSize: IntSize = IntSize(18, 9),
    fAte: Int = 0,
    fNess: Double = 0.0,
    hPoint: Double = 1.0,
    lTime: Float = 60f * 60f * 24f * 365f * 3f
): SchoolingFish() {
    // プロパティ
    override val virtualTankSize: Offset = vTankSize
    override val virtualSandHeight: Float = vSandHeight
    override var centerPosition: Offset = cPosition
    override var topLeftPosition: Offset = Offset(0f, 0f)
    override var headPosition: Offset = Offset(0f, 0f)
    override var velocity: Offset = v0
    override val maxVelocity: Float = 280f
    override var acc: Offset = acc0
    override val maxAcc: Float = 470f

    override val image: ImageBitmap = useResource(resourcePath = "aquarium/kansyoufish/1263.png", ::loadImageBitmap)
    override val sourceRect: IntRect = IntRect(left = 2, right = 30, top = 8, bottom = 22) // 画像のトリミング
    override var currentSize: IntSize = cSize // 現在の表示サイズ（ピクセル）
    override val minSize: IntSize = IntSize(18, 9) // 最小サイズ
    override val maxSize: IntSize = IntSize(40, 20) // 最大サイズ
    override val foodRequired: Int = 1500 // 最大まで成長するために必要なエサの数
    override var foodAte: Int = fAte // 今まで摂取したエサの数

    override var fullness: Double = fNess // 満腹度（0.0 ~ 1.0）
    override var hp: Double = hPoint // 体力（0.0 ~ 1.0）
    override var isFacingRight: Boolean = false // 右を向いていますか？
    override var isAlive: Boolean = true
    override var lifeTime: Float = lTime

    override val detectionRange: Float = 200f // 視認距離（ピクセル）
    override val kpChaseFood: Float = 3f // エサ追いのPゲイン
    override val cruisingSpeed: Float = 50f // 巡航速さ
    override val kpCruising: Float = 1f // 巡航のPゲイン
    override val threats: MutableSet<AbstractFish> = mutableSetOf() // その時点で脅威と認識している魚のリスト
    override val kpEscape: Float = 0.01f // 退避のPゲイン
    override val separationDist: Float = 45f // 仲間との適正セパレート距離
    override val kpSeparation: Float = 1f // セパレートのPゲイン
    override val kpAlignment: Float = 1f // 整列のPゲイン
    override val kpCenter: Float = 0.6f // 中央指向のPゲイン

    override fun update(deltaTime: Float, fishes: MutableList<AbstractFish>, foods: MutableList<Food>, waterQuality: Float) {
        this.resetAcc() // 忘れずに。
        super.update(deltaTime, fishes, foods, waterQuality)
    }
}

// アルビノネオンテトラ
class AlbinoNeonTetra(
    vTankSize: Offset,
    vSandHeight: Float = 20f,
    cPosition: Offset = Offset(vTankSize.x * Random.nextFloat(), vTankSize.y * Random.nextFloat()),
    v0: Offset = Offset(0f, 0f),
    acc0: Offset = Offset(0f, 0f),
    cSize: IntSize = IntSize(18, 10),
    fAte: Int = 0,
    fNess: Double = 0.0,
    hPoint: Double = 1.0,
    lTime: Float = 60f * 60f * 24f * 365f * 1.5f
): SchoolingFish() {
    // プロパティ
    override val virtualTankSize: Offset = vTankSize
    override val virtualSandHeight: Float = vSandHeight
    override var centerPosition: Offset = cPosition
    override var topLeftPosition: Offset = Offset(0f, 0f)
    override var headPosition: Offset = Offset(0f, 0f)
    override var velocity: Offset = v0
    override val maxVelocity: Float = 280f
    override var acc: Offset = acc0
    override val maxAcc: Float = 470f

    override val image: ImageBitmap = useResource(resourcePath = "aquarium/kansyoufish/1264.png", ::loadImageBitmap)
    override val sourceRect: IntRect = IntRect(left = 3, right = 29, top = 8, bottom = 22) // 画像のトリミング
    override var currentSize: IntSize = cSize // 現在の表示サイズ（ピクセル）26, 14
    override val minSize: IntSize = IntSize(18, 10) // 最小サイズ
    override val maxSize: IntSize = IntSize(35, 19) // 最大サイズ
    override val foodRequired: Int = 1500 // 最大まで成長するために必要なエサの数
    override var foodAte: Int = fAte // 今まで摂取したエサの数

    override var fullness: Double = fNess // 満腹度（0.0 ~ 1.0）
    override var hp: Double = hPoint // 体力（0.0 ~ 1.0）
    override var isFacingRight: Boolean = false // 右を向いていますか？
    override var isAlive: Boolean = true
    override var lifeTime: Float = lTime

    override val detectionRange: Float = 200f // 視認距離（ピクセル）
    override val kpChaseFood: Float = 3f // エサ追いのPゲイン
    override val cruisingSpeed: Float = 40f // 巡航速さ
    override val kpCruising: Float = 0.6f // 巡航のPゲイン
    override val threats: MutableSet<AbstractFish> = mutableSetOf() // その時点で脅威と認識している魚のリスト
    override val kpEscape: Float = 0.01f // 退避のPゲイン
    override val separationDist: Float = 40f // 仲間との適正セパレート距離
    override val kpSeparation: Float = 0.1f // セパレートのPゲイン
    override val kpAlignment: Float = 0.05f // 整列のPゲイン
    override val kpCenter: Float = 0.05f // 中央指向のPゲイン

    override fun update(deltaTime: Float, fishes: MutableList<AbstractFish>, foods: MutableList<Food>, waterQuality: Float) {
        this.resetAcc() // 忘れずに。
        super.update(deltaTime, fishes, foods, waterQuality)
    }
}

/*--------------- 群れる性質ここまで ---------------*/
