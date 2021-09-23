import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.shape.CutCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp

private val primary = Color(java.awt.Color.decode("#184957").rgb)
private val primaryVariant = Color(java.awt.Color.decode("#184957").rgb)
private val secondary = Color(java.awt.Color.decode("#FCCF00").rgb)
private val background = Color(java.awt.Color.decode("#091A1F").rgb)
private val onBackground = Color(java.awt.Color.decode("#2d304e").rgb)

val DarkColors = darkColors(
    primary = primary,
    primaryVariant = primaryVariant,
    surface = Color(java.awt.Color.decode("#0A1D22").rgb),
    secondary = secondary,
    background = background,
    onPrimary = Color(java.awt.Color.decode("#A8DBFC").rgb)
)

//val SmolShapes = MaterialTheme.shapes.run {
//    this.copy(
//        small = this.small.copy(
//
//        )
//    )
//}

@Composable
@OptIn(ExperimentalMaterialApi::class)
fun SmolButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    elevation: ButtonElevation? = ButtonDefaults.elevation(),
    shape: Shape? = null,
    border: BorderStroke? = null,
    colors: ButtonColors = ButtonDefaults.buttonColors(),
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    content: @Composable RowScope.() -> Unit
) {
    Button(
        modifier = modifier,
        border = border,
        shape = shape ?: smolNormalButtonShape(),
        onClick = onClick,
        enabled = enabled,
        interactionSource = interactionSource,
        elevation = elevation,
        colors = colors,
        contentPadding = contentPadding,
        content = content,
    )
}

fun smolNormalButtonShape() = CutCornerShape(topStart = 12.dp, bottomEnd = 12.dp)
fun smolFullyClippedButtonShape() = CutCornerShape(size = 8.dp)

@Composable
@OptIn(ExperimentalMaterialApi::class)
fun SmolSecondaryButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    elevation: ButtonElevation? = null,
    shape: Shape? = null,
    border: BorderStroke? = null,
    colors: ButtonColors = ButtonDefaults.buttonColors(
        backgroundColor = MaterialTheme.colors.primary.copy(alpha = ContentAlpha.medium),
        contentColor = MaterialTheme.colors.onPrimary.copy(alpha = ContentAlpha.high)
    ),
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    content: @Composable RowScope.() -> Unit
) {
    SmolButton(
        modifier = modifier,
        border = border,
        shape = shape ?: smolNormalButtonShape(),
        onClick = onClick,
        enabled = enabled,
        interactionSource = interactionSource,
        elevation = elevation,
        colors = colors,
        contentPadding = contentPadding,
        content = content,
    )
}