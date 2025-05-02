package aquarium

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import java.lang.Float.MAX_VALUE
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.random.Random

// 基底インターフェース
interface IAbstractFish {
    fun update(deltaTime: Float, fishes: MutableList<AbstractFish>, foods: MutableList<Food>, waterQuality: Float)
}

/*--------------- 基底抽象クラス ---------------*/

abstract class AbstractFish() : IAbstractFish {
    // プロパティ
    abstract val virtualTankSize: Offset
    abstract val virtualSandHeight: Float
    abstract var centerPosition: Offset // ピクセル
    abstract var topLeftPosition: Offset // ピクセル
    abstract var headPosition: Offset // ピクセル
    abstract var velocity: Offset // ピクセル（毎秒）
    abstract val maxVelocity: Float // ピクセル（毎秒）
    abstract var acc: Offset // ピクセル（毎秒毎秒）
    abstract val maxAcc: Float // 最大加速度（毎秒毎秒）

    abstract val image: ImageBitmap // 画像のソース
    abstract val sourceRect: IntRect // 画像のトリミング
    abstract var currentSize: IntSize // 現在の表示サイズ（ピクセル）
    abstract val minSize: IntSize // 最小サイズ
    abstract val maxSize: IntSize // 最大サイズ
    abstract val foodRequired: Int // 最大まで成長するために必要なエサの数
    abstract var foodAte: Int // 今まで摂取したエサの数

    abstract var fullness: Double // 満腹度（0.0 ~ 1.0）
    abstract var hp: Double // 体力（0.0 ~ 1.0）
    abstract var isFacingRight: Boolean // 右を向いていますか？
    abstract var isAlive: Boolean // 生きていますか？
    abstract var lifeTime: Float // 寿命（秒）

    abstract val detectionRange: Float // 視認距離（ピクセル）
    abstract val kpChaseFood: Float // エサ追いのPゲイン
    abstract val cruisingSpeed: Float // 巡航速さ
    abstract val kpCruising: Float // 巡航のPゲイン
    abstract val threats: MutableSet<AbstractFish> // その時点で脅威と認識している魚のリスト
    abstract val kpEscape: Float // 退避のPゲイン

    // 加速度をゼロにリセット
    protected fun resetAcc() { acc = Offset(x = 0f, y = 0f) } // 継承先で update の頭に忘れずに書く。

    // deltaTime（秒）での変化を計算
    override fun update(deltaTime: Float, fishes: MutableList<AbstractFish>, foods: MutableList<Food>, waterQuality: Float) { // deltaTime：秒
        // this.resetAcc() // 継承先で update の頭に忘れずに書く。

        // 加速度の制限（魚が出せる力には限りがある。）
        if (acc.length() > maxAcc) {
            acc = acc.normalize() * maxAcc
        }

        // 速度の変化（加速）
        velocity += acc * deltaTime

        // 速度の制限
        if (velocity.length() > maxVelocity) {
            velocity = velocity.normalize() * maxVelocity
        }

        // 位置を更新（coerceInにより仮想水槽の範囲内に制限）
        centerPosition = Offset(
            (centerPosition.x + (velocity.x * deltaTime))
                .coerceIn(
                    minimumValue = 0f,
                    maximumValue = virtualTankSize.x - (abs(cos(Math.toRadians(velocity.angle().toDouble()))).toFloat() * currentSize.width / 2f)
                ),
            (centerPosition.y + (velocity.y * deltaTime))
                .coerceIn(
                    minimumValue = currentSize.height / 2f,
                    maximumValue = virtualTankSize.y - (currentSize.height / 2f) - virtualSandHeight
                )
        )

        // 左上の座標を更新（画像の描画位置を決めるための座標なので回転は加えない。）
        topLeftPosition = Offset(
            x = centerPosition.x - (currentSize.width / 2f),
            y = centerPosition.y - (currentSize.height / 2f)
        )

        // 左右の向きを判定
        isFacingRight = velocity.x > 0f

        // 頭の座標を更新（回転あり）
        headPosition = if (isFacingRight) {
            Offset(centerPosition.x + (currentSize.width / 2f), centerPosition.y)
        } else {
            Offset(centerPosition.x - (currentSize.width / 2f), centerPosition.y)
        }
        // 速度ベクトルの向きに合わせて回転
        headPosition = (headPosition - centerPosition).rotate(velocity.angle()) + centerPosition

        // 壁で反転（本当は自身の四角の中で一番壁に近い座標を計算する必要があるが、めんどくさい。）
        if (headPosition.x <= 0 || headPosition.x >= virtualTankSize.x) {
            velocity = velocity.copy(x = -velocity.x)
        }
        if (headPosition.y <= 0) {
            velocity = velocity.copy(y = 0f)
        }
        if (headPosition.y >= virtualTankSize.y - virtualSandHeight) {
            velocity = velocity.copy(y = -velocity.y / 4f)
        }

        // 時間経過と環境による状態の変化
        this.timeBasedEffects(deltaTime = deltaTime, waterQuality = waterQuality)

        // エサの接種による成長
        currentSize = IntSize(
            width = minSize.width + ((maxSize.width - minSize.width) * (foodAte.toFloat() / foodRequired)).toInt(),
            height = minSize.height + ((maxSize.height - minSize.height) * (foodAte.toFloat() / foodRequired)).toInt()
        )
    }

    // 時間経過による影響（空腹・体力の減少・ストレス）
    private fun timeBasedEffects(deltaTime: Float, waterQuality: Float) { // deltaTime：秒
        // 満腹度の減少
        fullness -= (1.0 / (60.0 * 60.0 * 24.0)) * deltaTime // 1日間で、1から0になる。
        fullness = fullness.coerceIn(0.0, 1.0)

        // 体力の減少（条件が良ければ増加）
        val w: Double = 1.0 / (0.7 * 60.0 * 60.0 * 24.0)
        hp -= w * (0.7 - waterQuality) * deltaTime // 水質0.0で1日間放置すると、1から0になる。
        val f: Double = 1.0 / (0.7 * 60.0 * 60.0 * 24.0 * 7.0)
        hp -= f * (0.7 - fullness) * deltaTime // 満腹度0.0で7日間放置すると、1から0になる。
        hp = hp.coerceIn(0.0, 1.0)

        // 脅威との距離感による体力の減少
        for (threat in threats) {
            val dist: Float = headPosition.distanceTo(threat.headPosition)
            // 精神的負担の計算
            // 相手の横幅の2倍の距離が境界
            val distBurden: Float = if (dist <= 2f * threat.currentSize.width) {
                ((2f * threat.currentSize.width) - dist) / (2f * threat.currentSize.width)
            } else {
                0f
            }
            val sizeBurden: Double = ((threat.currentSize.width * threat.currentSize.height) - (currentSize.width * currentSize.height)).toDouble() / (currentSize.width * currentSize.height).toDouble()
            val t: Double = 1.0 / (60.0 * 30.0)
            // println("%.10f".format(t * distBurden * sizeBurden * deltaTime))
            hp -= t * distBurden * sizeBurden * deltaTime // 距離0で面積2倍の脅威に30分詰められると、1から0になる。
        }
        hp = hp.coerceIn(0.0, 1.0)

        // 自然な寿命
        lifeTime -= deltaTime

        if (hp <= 0 || lifeTime <= 0) {
            isAlive = false
        }
    }

    // エサを追う
    protected fun chaseFood(foods: MutableList<Food>): Offset {
        if (fullness > 0.9f) { return Offset(0f, 0f) } // 食う気が無い時は速度をいじらない。

        var nearestFood: Food? = null // 自分に最も近いエサ
        var minDist: Float = MAX_VALUE
        // エサのリストを走査して一番近いやつを検出
        for (food in foods) {
            val dist: Float = headPosition.distanceTo(food.position)
            // 視認距離内に無ければ発見しない。
            if (dist < minDist && dist <= detectionRange) {
                nearestFood = food
                minDist = dist
            }
        }
        if (nearestFood != null) { // エサが存在する場合
            val e = nearestFood.position - headPosition // 誤差ベクトル（誤差値 & 方向ベクトル）
            // 食べれるならば食べる。
            if (headPosition.distanceTo(nearestFood.position) <= 3) {
                foodAte += 1
                foods.remove(nearestFood)
                fullness += 0.1
                fullness = fullness.coerceIn(0.0, 1.0)
            }
            // 体力があるほど速く、満腹度が低いほど速く向かう。
            return e * kpChaseFood * hp.toFloat() * (1 - fullness.toFloat())
        } else { // エサが存在しない場合
            return Offset(0f, 0f)
        }
    }

    // 通常巡航（横方向）
    protected fun cruising(): Offset {
        val sign: Int = if (isFacingRight) 1 else -1
        val e = Offset(
            x = sign * cruisingSpeed,
            y = if (Random.nextFloat() < 0.5) { cruisingSpeed / 3f } else { -cruisingSpeed / 3f } // ランダムな縦揺れ
        ) - velocity // 誤差ベクトル

        return e * kpCruising
    }

    // 脅威認識・退避
    protected fun escapeFromThreats(fishes: List<AbstractFish>): Offset {
        threats.clear() // 脅威認識を更新していく。
        val potentialThreats: MutableList<AbstractFish> = mutableListOf()
        for (fish in fishes) {
            if (fish == this) { continue } // 自分は除外
            if (headPosition.distanceTo(fish.centerPosition) > detectionRange) { continue } // 視認距離外は除外
            if (fish::class == this::class) { continue } // 同種は除外
            potentialThreats.add(fish)
        }
        var accChange: Offset = Offset(0f, 0f) // 戻り値
        for (fish in potentialThreats) {
            // 彼我のサイズを比較
            val sizeDiff: Int = (currentSize.width * currentSize.height) - (fish.currentSize.width * fish.currentSize.height)
            if (sizeDiff < 0) {
                threats.add(fish)
                val direction: Offset = (fish.headPosition - headPosition).normalize() // 相手の方向の真反対へ逃げる。
                accChange += direction * sizeDiff.toFloat() * kpEscape // 複数の脅威から逃げるためにベクトルを合成
            }
        }
        return accChange
    }

    // 自分から他のオブジェクトまでのユークリッド距離を求めるメソッド
    protected fun Offset.distanceTo(other: Offset): Float {
        return sqrt((other.x - this.x).pow(2) + (other.y - this.y).pow(2))
    }

    // 拡張関数: ベクトルの正規化
    protected fun Offset.normalize(): Offset {
        val length = sqrt((x * x + y * y).toDouble()).toFloat()
        return if (length > 0) Offset(x / length, y / length) else Offset.Zero
    }

    // 拡張関数: ベクトルのノルム取得
    protected fun Offset.length(): Float {
        return sqrt((x * x + y * y).toDouble()).toFloat()
    }

    // ベクトルの向き（headPositionを回転させる用）
    // FeatureAquarium の方の拡張関数とは中身が違う事に注意せよ。
    protected fun Offset.angle(): Float {
        val sign: Int = if (!isFacingRight) { -1 } else { 1 }
        val angle: Float = Math.toDegrees(atan2(y.toDouble(), sign * x.toDouble())).toFloat()
        return if (isFacingRight) { angle } else { -angle }
    }

    // Offset に回転行列を適用する拡張関数
    protected fun Offset.rotate(angle: Float): Offset {
        val radians = Math.toRadians(angle.toDouble()) // 角度をラジアンに変換
        val cosTheta = cos(radians)
        val sinTheta = sin(radians)

        val newX = (cosTheta * this.x - sinTheta * this.y).toFloat()
        val newY = (sinTheta * this.x + cosTheta * this.y).toFloat()

        return Offset(newX, newY)
    }
}

/*--------------- 基底抽象クラスここまで ---------------*/

/*--------------- 行動原理に基づいたインターフェースここから ---------------*/

// 群れる性質
interface ISchoolingFish {
    val separationDist: Float
    val kpSeparation: Float
    val kpAlignment: Float
    val kpCenter: Float

    fun findAllies(fishes: List<AbstractFish>): List<SchoolingFish> // 0
    fun keepDistance(fishes: List<AbstractFish>): Offset  // 1
    fun alignment(fishes: List<AbstractFish>): Offset     // 2
    fun steerToCenter(fishes: List<AbstractFish>): Offset // 3
}

// 群れない性質
interface ISolitaryFish {
    val territory: Rect
    val aggressionLevel: Float
}

// 漂う性質
interface IDriftingFish {
    val driftSpeed: Float
}

// 地面を移動
interface IBottomDwellingFish {
    val groundLevel: Float
}

// 他個体を襲う
interface IPredatoryFish {
    val attackRange: Float
    fun attack(target: AbstractFish)
}

// 遊泳
interface ISwimmingFish {
    val swimmingSpeed: Float
}

// 停滞する
interface IStationaryFish {
    val preferredPosition: Offset
}

/*--------------- 行動原理に基づいたインターフェースここまで ---------------*/

/*--------------- 行動原理インターフェースを実装した抽象クラスここから ---------------*/

abstract class SchoolingFish(): AbstractFish(), ISchoolingFish {
    override fun update(deltaTime: Float, fishes: MutableList<AbstractFish>, foods: MutableList<Food>, waterQuality: Float) {
        // this.resetAcc() // 忘れずに。

        acc += this.chaseFood(foods = foods)
        // chaseFood によって加速度が与えられていない場合
        if (acc == Offset(0f, 0f)) {
            acc += this.keepDistance(fishes = fishes)
            acc += this.alignment(fishes = fishes)
            acc += this.steerToCenter(fishes = fishes)
            acc += this.cruising()
        }
        // chaseFood の最中でも退避は行う。
        acc += this.escapeFromThreats(fishes = fishes)

        super.update(deltaTime, fishes, foods, waterQuality)
    }

    override fun findAllies(fishes: List<AbstractFish>): List<SchoolingFish> {
        val foundAllies: MutableList<SchoolingFish> = mutableListOf()
        for (fish in fishes) {
            // 自分以外で視認距離内の同じ魚を見つける。
            if (fish == this) { continue }
            if (headPosition.distanceTo(fish.centerPosition) > detectionRange) { continue }
            if (fish::class == this::class) {
                foundAllies.add(fish as SchoolingFish)
            }
        }
        return foundAllies
    }

    // Rule #1 (Separation) 仲間との距離をある大きさに維持することによって、ぶつかるのを避ける。
    override fun keepDistance(fishes: List<AbstractFish>): Offset {
        val allies: List<SchoolingFish> = findAllies(fishes = fishes) // 仲間を見つける。
        if (allies.isEmpty()) { return Offset(0f, 0f) } // そもそも仲間が見つからなかったら終了

        // もっとも近い仲間を求める。
        var nearestAlly: SchoolingFish = allies.first() // 初期化しないと怒られる。
        var minDist: Float = MAX_VALUE
        for (ally in allies) {
            val dist: Float = headPosition.distanceTo(ally.centerPosition)
            if (dist < minDist) {
                nearestAlly = ally
                minDist = dist
            }
        }

        // 一番近い仲間と適正な距離を保つ。
        val direction: Offset = (nearestAlly.centerPosition - this.headPosition).normalize() // 方向ベクトル
        var e: Float = (nearestAlly.centerPosition - this.headPosition).length() - separationDist // 誤差値
        // ちなみに、体力があるほど速く向かう。
        return direction * e * kpSeparation * hp.toFloat()
    }

    // Rule #2 (Alignment) 仲間とスピードと方向を合わせる。
    override fun alignment(fishes: List<AbstractFish>): Offset {
        val allies: List<SchoolingFish> = findAllies(fishes = fishes) // 仲間を見つける。
        if (allies.isEmpty()) { return Offset(0f, 0f) } // そもそも仲間が見つからなかったら終了

        // もっとも近い仲間を求める。
        var nearestAlly: SchoolingFish = allies.first() // 初期化しないと怒られる。
        var minDist: Float = MAX_VALUE
        for (ally in allies) {
            val dist: Float = headPosition.distanceTo(ally.centerPosition)
            if (dist < minDist) {
                nearestAlly = ally
                minDist = dist
            }
        }

        // 最も近い仲間に向きと速度を合わせようとする。
        val e: Offset = nearestAlly.velocity - velocity // 誤差ベクトル
        return e * kpAlignment * hp.toFloat()
    }

    // Rule #3 (Cohesion) 自分のまわりの群れのグループの中心へ向かおうとする。
    override fun steerToCenter(fishes: List<AbstractFish>): Offset {
        val allies: List<SchoolingFish> = findAllies(fishes = fishes) // 仲間を見つける。
        if (allies.isEmpty()) { return Offset(0f, 0f) } // そもそも仲間が見つからなかったら終了

        // 位置の合計を見える仲間の数で割って、中心を求める。
        var center: Offset = Offset(0f, 0f)
        for (ally in allies) {
            center += ally.centerPosition
        }
        center /= allies.size.toFloat()

        // 中心への向きを求めて、だんだんそちらによるように動かす。
        val e: Offset = center - headPosition // 誤差ベクトル
        return e * kpCenter * hp.toFloat()
    }
}

/*--------------- 行動原理インターフェースを実装した抽象クラスここまで ---------------*/
