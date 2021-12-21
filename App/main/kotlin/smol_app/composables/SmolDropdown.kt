package smol_app.composables

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import smol_app.themes.SmolTheme


data class SmolDropdownMenuItem(
    val text: String,
    val backgroundColor: Color? = null,
    val contentColor: Color? = null,
    val onClick: () -> Unit,
    val customItemContent: @Composable (RowScope.(item: SmolDropdownMenuItem) -> Unit)? = null
)

@Composable
fun SmolDropdownWithButton(
    modifier: Modifier = Modifier,
    items: List<SmolDropdownMenuItem>,
    initiallySelectedIndex: Int
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedIndex by remember { mutableStateOf(initiallySelectedIndex) }
    Box(modifier) {
        val selectedItem = items[selectedIndex]
        val backgroundColor = selectedItem.backgroundColor ?: MaterialTheme.colors.primary
        SmolButton(
            onClick = { expanded = expanded.not() },
            modifier = Modifier.wrapContentWidth()
                .align(Alignment.CenterStart),
            shape = SmolTheme.smolFullyClippedButtonShape(),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = backgroundColor
            )
        ) {
            Text(
                text = selectedItem.text,
                fontWeight = FontWeight.Bold,
                color = selectedItem.contentColor ?: contentColorFor(backgroundColor)
            )
            SmolDropdownArrow(
                Modifier
                    .align(Alignment.CenterVertically),
                expanded
            )
        }
        DropdownMenu(
            expanded = expanded,
            modifier = Modifier.background(
                MaterialTheme.colors.background
            ),
            onDismissRequest = { expanded = false }
        ) {
            items.forEachIndexed { index, item ->
                DropdownMenuItem(
                    modifier = Modifier.let { if (item.backgroundColor != null) it.background(item.backgroundColor) else it },
                    onClick = {
                        selectedIndex = index
                        expanded = false
                        items[index].onClick()
                    }) {
                    Row {
                        if (item.customItemContent != null) {
                            item.customItemContent.invoke(this, item)
                        } else {
                            Text(
                                text = item.text,
                                modifier = Modifier.weight(1f),
                                fontWeight = FontWeight.Bold,
                                color = item.contentColor ?: contentColorFor(
                                    item.backgroundColor ?: MaterialTheme.colors.surface
                                )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SmolDropdownArrow(modifier: Modifier = Modifier, expanded: Boolean) {
    Image(
        modifier = modifier
            .width(16.dp)
            .offset(x = 4.dp)
            .run {
                if (expanded) this.rotate(180f)
                else this
            },
        painter = painterResource("menu-down.svg"),
        colorFilter = ColorFilter.tint(SmolTheme.dimmedIconColor()),
        contentDescription = null
    )
}