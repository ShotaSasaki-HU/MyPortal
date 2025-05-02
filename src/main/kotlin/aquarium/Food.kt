package aquarium

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize

data class Food(
    val virtualTankSize: Offset,
    val virtualSandHeight: Float = 20f,
    var position: Offset,
    val velocity: Offset = Offset(0f, 10f),
    val size: IntSize = IntSize(20, 20),
    var isConsumed: Boolean = false
) {
    private var lifeTime: Float = 60f // 餌の寿命（秒）

    fun update(deltaTime: Float) {
        lifeTime -= deltaTime
        if (lifeTime <= 0) {
            isConsumed = true
        }

        if (position.y < virtualTankSize.y - virtualSandHeight) {
            position += velocity * deltaTime
        }
    }
}
