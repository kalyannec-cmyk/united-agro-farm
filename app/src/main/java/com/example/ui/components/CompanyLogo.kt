package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.FarmOfficialInfo

val LogoNavy = Color(0xFF0D254C)
val LogoNavyDark = Color(0xFF071731)

/**
 * High-fidelity representation of the official Lalitpur United Agro Farm logo stamp:
 * Triangular perimeter with twin birds, stylized 'UAF' monogram,
 * "Lalitpur United Agro Farm Pvt. Ltd." and "Estd. 2021".
 */
@Composable
fun CompanyLogo(
    modifier: Modifier = Modifier,
    size: Dp = 90.dp,
    showSubtext: Boolean = true,
    inverted: Boolean = false
) {
    val primaryColor = if (inverted) Color.White else LogoNavy
    val bgColor = if (inverted) LogoNavy else Color.White

    Surface(
        modifier = modifier
            .size(size)
            .testTag("company_logo"),
        shape = RoundedCornerShape(16.dp),
        color = bgColor,
        shadowElevation = if (inverted) 0.dp else 4.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(4.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = this.size.width
                val h = this.size.height

                // Draw rounded triangle boundary
                val path = Path().apply {
                    val pTop = Offset(w * 0.5f, h * 0.06f)
                    val pBottomRight = Offset(w * 0.94f, h * 0.90f)
                    val pBottomLeft = Offset(w * 0.06f, h * 0.90f)

                    moveTo(pTop.x, pTop.y)
                    lineTo(pBottomRight.x, pBottomRight.y)
                    lineTo(pBottomLeft.x, pBottomLeft.y)
                    close()
                }

                drawPath(
                    path = path,
                    color = primaryColor,
                    style = Stroke(
                        width = w * 0.035f,
                        cap = StrokeCap.Round,
                        join = StrokeJoin.Round
                    )
                )

                // Draw twin birds silhouettes (facing inward)
                drawBird(
                    center = Offset(w * 0.40f, h * 0.40f),
                    scale = w * 0.16f,
                    facingRight = true,
                    color = primaryColor
                )
                drawBird(
                    center = Offset(w * 0.60f, h * 0.40f),
                    scale = w * 0.16f,
                    facingRight = false,
                    color = primaryColor
                )
            }

            // Typography overlay
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = (size.value * 0.36f).dp, bottom = (size.value * 0.10f).dp)
            ) {
                // Stylized UAF monogram
                Text(
                    text = "UAF",
                    color = primaryColor,
                    fontSize = (size.value * 0.22f).sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = (-1).sp,
                    fontFamily = FontFamily.SansSerif,
                    lineHeight = (size.value * 0.22f).sp
                )

                if (showSubtext && size >= 70.dp) {
                    Spacer(modifier = Modifier.height((size.value * 0.015f).dp))
                    Text(
                        text = "Lalitpur United Agro Farm",
                        color = primaryColor,
                        fontSize = (size.value * 0.062f).coerceAtLeast(7f).sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                    Text(
                        text = "Estd. 2021",
                        color = primaryColor.copy(alpha = 0.85f),
                        fontSize = (size.value * 0.055f).coerceAtLeast(6f).sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

private fun DrawScope.drawBird(center: Offset, scale: Float, facingRight: Boolean, color: Color) {
    val dir = if (facingRight) 1f else -1f
    val birdPath = Path().apply {
        // Bird body
        moveTo(center.x - scale * 0.3f * dir, center.y + scale * 0.1f)
        // Tail
        lineTo(center.x - scale * 0.8f * dir, center.y + scale * 0.25f)
        lineTo(center.x - scale * 0.4f * dir, center.y)
        // Back
        quadraticBezierTo(
            center.x, center.y - scale * 0.25f,
            center.x + scale * 0.2f * dir, center.y - scale * 0.35f
        )
        // Head & beak
        lineTo(center.x + scale * 0.35f * dir, center.y - scale * 0.32f)
        lineTo(center.x + scale * 0.22f * dir, center.y - scale * 0.20f)
        // Chest & belly
        quadraticBezierTo(
            center.x + scale * 0.1f * dir, center.y + scale * 0.15f,
            center.x - scale * 0.3f * dir, center.y + scale * 0.1f
        )
        close()
    }
    drawPath(path = birdPath, color = color)

    // Bird legs
    drawLine(
        color = color,
        start = Offset(center.x - scale * 0.05f * dir, center.y + scale * 0.08f),
        end = Offset(center.x - scale * 0.05f * dir, center.y + scale * 0.24f),
        strokeWidth = scale * 0.08f,
        cap = StrokeCap.Round
    )
    drawLine(
        color = color,
        start = Offset(center.x + scale * 0.08f * dir, center.y + scale * 0.08f),
        end = Offset(center.x + scale * 0.08f * dir, center.y + scale * 0.24f),
        strokeWidth = scale * 0.08f,
        cap = StrokeCap.Round
    )
}
