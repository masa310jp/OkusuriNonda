package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.rememberScrollableState
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.TealLight
import com.example.ui.theme.TealPrimary

/**
 * 縦方向に回転・スクロール＆上下ボタンで直感的に数値を増減できるナンバーピッカー
 */
@Composable
fun VerticalNumberPicker(
    value: Int,
    onValueChange: (Int) -> Unit,
    range: IntRange,
    label: String,
    suffix: String = "",
    modifier: Modifier = Modifier,
    testTag: String = ""
) {
    var accumulatedDelta by remember { mutableFloatStateOf(0f) }
    val thresholdPx = 40f

    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        shape = RoundedCornerShape(14.dp),
        modifier = modifier
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(horizontal = 8.dp, vertical = 8.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            // 上ボタン（増やす）
            IconButton(
                onClick = {
                    if (value < range.last) {
                        onValueChange(value + 1)
                    }
                },
                enabled = value < range.last,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (value < range.last) TealLight else Color.Transparent)
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowUp,
                    contentDescription = "増やす",
                    tint = if (value < range.last) TealPrimary else Color.Gray,
                    modifier = Modifier.size(24.dp)
                )
            }

            // ナンバー回転表示エリア（ドラッグ／スワイプで上下回転可能）
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .scrollable(
                        orientation = Orientation.Vertical,
                        state = rememberScrollableState { delta ->
                            accumulatedDelta += delta
                            if (accumulatedDelta <= -thresholdPx) {
                                // 下へスクロール -> 値を減らす
                                if (value > range.first) {
                                    onValueChange(value - 1)
                                }
                                accumulatedDelta = 0f
                            } else if (accumulatedDelta >= thresholdPx) {
                                // 上へスクロール -> 値を増やす
                                if (value < range.last) {
                                    onValueChange(value + 1)
                                }
                                accumulatedDelta = 0f
                            }
                            delta
                        }
                    )
                    .then(if (testTag.isNotEmpty()) Modifier.testTag(testTag) else Modifier)
            ) {
                // 上（前の値プレビュー）
                if (value + 1 <= range.last) {
                    Text(
                        text = "${value + 1}",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 2.dp)
                    )
                }

                // 現在の選択値
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.align(Alignment.Center)
                ) {
                    Text(
                        text = "$value",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = TealPrimary
                    )
                    if (suffix.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(3.dp))
                        Text(
                            text = suffix,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 3.dp)
                        )
                    }
                }

                // 下（次の値プレビュー）
                if (value - 1 >= range.first) {
                    Text(
                        text = "${value - 1}",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 2.dp)
                    )
                }
            }

            // 下ボタン（減らす）
            IconButton(
                onClick = {
                    if (value > range.first) {
                        onValueChange(value - 1)
                    }
                },
                enabled = value > range.first,
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (value > range.first) TealLight else Color.Transparent)
            ) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "減らす",
                    tint = if (value > range.first) TealPrimary else Color.Gray,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}
