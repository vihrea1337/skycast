package io.github.vihrea1337.skycast.core.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * Типографика M3. displayLarge переопределён под главный элемент приложения —
 * крупную температуру на Home-экране и в виджете.
 */
val SkyCastTypography = Typography().let { base ->
    base.copy(
        displayLarge = base.displayLarge.copy(
            fontSize = 64.sp,
            fontWeight = FontWeight.Light,
        ),
    )
}
