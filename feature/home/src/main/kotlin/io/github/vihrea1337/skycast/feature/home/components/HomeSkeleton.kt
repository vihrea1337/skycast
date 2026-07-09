package io.github.vihrea1337.skycast.feature.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.vihrea1337.skycast.core.designsystem.component.SkeletonBox

/** Скелетон, повторяющий макет WeatherContent — экран не «прыгает» при загрузке. */
@Composable
internal fun HomeSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        SkeletonBox(modifier = Modifier.size(width = 140.dp, height = 14.dp))
        Spacer(Modifier.height(28.dp))
        SkeletonBox(modifier = Modifier.size(96.dp), shape = CircleShape)
        Spacer(Modifier.height(12.dp))
        SkeletonBox(modifier = Modifier.size(width = 120.dp, height = 56.dp))
        Spacer(Modifier.height(12.dp))
        SkeletonBox(modifier = Modifier.size(width = 160.dp, height = 16.dp))
        Spacer(Modifier.height(36.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            repeat(3) {
                SkeletonBox(modifier = Modifier.size(width = 96.dp, height = 56.dp))
            }
        }
        Spacer(Modifier.height(36.dp))
        SkeletonBox(
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp),
        )
        Spacer(Modifier.height(24.dp))
        SkeletonBox(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
        )
    }
}
