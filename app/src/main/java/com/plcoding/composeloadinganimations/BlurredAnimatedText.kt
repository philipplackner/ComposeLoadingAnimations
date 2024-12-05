package com.plcoding.composeloadinganimations

import android.graphics.BlurMaskFilter
import android.graphics.Paint
import android.graphics.Rect
import android.os.Build
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.BlurEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.roundToInt

private const val ANIMATION_DURATION = 1000

@Composable
fun BlurredAnimatedText(
    text: String,
    modifier: Modifier = Modifier
) {
    val blurList = text.mapIndexed { index, character ->
        if(character == ' ') {
            remember {
                mutableFloatStateOf(0f)
            }
        } else {
            val infiniteTransition = rememberInfiniteTransition(label = "infinite transition $index")
            infiniteTransition.animateFloat(
                initialValue = 10f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(
                        durationMillis = ANIMATION_DURATION,
                        easing = LinearEasing
                    ),
                    repeatMode = RepeatMode.Reverse,
                    initialStartOffset = StartOffset(
                        offsetMillis = (ANIMATION_DURATION / text.length) * index
                    )
                ),
                label = "blur animation"
            )
        }
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        text.forEachIndexed { index, character ->
            Text(
                text = character.toString(),
                color = Color.White,
                modifier = Modifier
                    .graphicsLayer {
                        if(character != ' ') {
                            val blurAmount = blurList[index].value
                            renderEffect = BlurEffect(
                                radiusX = blurAmount,
                                radiusY = blurAmount
                            )
                        }
                    }
                    .then(
                        if(Build.VERSION.SDK_INT <= Build.VERSION_CODES.S_V2) {
                            Modifier.fullContentBlur(
                                blurRadius = { blurList[index].value.roundToInt() }
                            )
                        } else {
                            Modifier
                        }
                    )
            )
        }
    }
}

private fun Modifier.fullContentBlur(
    blurRadius: () -> Int,
    color: Color = Color.Black
): Modifier {
    return drawWithCache {
        val radius = blurRadius()
        val nativePaint = Paint().apply {
            isAntiAlias = true
            this.color = color.toArgb()

            if(radius > 0) {
                maskFilter = BlurMaskFilter(
                    radius.toFloat(),
                    BlurMaskFilter.Blur.NORMAL
                )
            }
        }

        onDrawWithContent {
            drawContent()

            drawIntoCanvas { canvas ->
                canvas.save()

                val rect = Rect(0, 0, size.width.toInt(), size.height.toInt())
                canvas.nativeCanvas.drawRect(rect, nativePaint)

                canvas.restore()
            }
        }
    }
}