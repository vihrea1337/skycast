package io.github.vihrea1337.skycast.core.designsystem.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.vihrea1337.skycast.core.designsystem.theme.SkyCastTheme

/** Секция контента с заголовком: «Сегодня по часам», «7 дней» и т.п. */
@Composable
fun SectionCard(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp),
        )
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(12.dp),
                content = content,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SectionCardPreview() {
    SkyCastTheme {
        SectionCard(title = "Сегодня по часам") {
            Text("Содержимое секции")
        }
    }
}
