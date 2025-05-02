import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogWindow
import androidx.compose.ui.window.rememberDialogState

// メッセージダイアログ
@Composable
fun messageDialog(
    msg: String,
    textNeutral: String = "",
    onNeutralClick: () -> Unit = {},
    textColorNeutral: Color = AppColors.darkBlue,
    textNegative: String = "CLOSE",
    onNegativeClick: () -> Unit = {},
    textColorNegative: Color = AppColors.darkBlue,
    textPositive: String = "",
    onPositiveClick: () -> Unit = {},
    textColorPositive: Color = AppColors.darkBlue,
    dialogWidth: Int = 210,
    dialogHeight: Int = 200
) {
    DialogWindow(
        title = "MESSAGE",
        state = rememberDialogState(width = dialogWidth.dp, height = dialogHeight.dp),
        onCloseRequest = onNegativeClick // 注意！
    ) {
        // サーフェスでダイアログの背景色を指定
        Surface(color = Color.Companion.White) {
            Column(modifier = Modifier.fillMaxSize().padding(10.dp)) {
                Text(msg, modifier = Modifier.weight(1f), color = AppColors.darkBlue)

                Row {
                    if (textNeutral.isNotBlank()) {
                        Column(modifier = Modifier.weight(1f)) {
                            Button(
                                onClick = onNeutralClick,
                                colors = ButtonDefaults.buttonColors(
                                    backgroundColor = AppColors.lightBlue
                                ),
                                modifier = Modifier.padding(5.dp, 5.dp, 5.dp, 5.dp),
                                shape = RoundedCornerShape(18.dp, 18.dp, 18.dp, 18.dp)
                            ) {
                                Text(text = textNeutral, color = textColorNeutral)
                            }
                        }
                    }
                    if (textNegative.isNotBlank()) {
                        Column(modifier = Modifier.weight(1f)) {
                            Button(
                                onClick = onNegativeClick,
                                colors = ButtonDefaults.buttonColors(
                                    backgroundColor = AppColors.lightBlue
                                ),
                                modifier = Modifier.padding(5.dp, 5.dp, 5.dp, 5.dp),
                                shape = RoundedCornerShape(18.dp, 18.dp, 18.dp, 18.dp)
                            ) {
                                Text(text = textNegative, color = textColorNegative)
                            }
                        }
                    }
                    if (textPositive.isNotBlank()) {
                        Column(modifier = Modifier.weight(1f)) {
                            Button(
                                onClick = onPositiveClick,
                                colors = ButtonDefaults.buttonColors(
                                    backgroundColor = AppColors.lightBlue
                                ),
                                modifier = Modifier.padding(5.dp, 5.dp, 5.dp, 5.dp),
                                shape = RoundedCornerShape(18.dp, 18.dp, 18.dp, 18.dp)
                            ) {
                                Text(text = textPositive, color = textColorPositive)
                            }
                        }
                    }
                }
            }
        }
    }
}
