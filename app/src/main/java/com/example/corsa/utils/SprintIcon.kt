package com.example.corsa.utils

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp

@Suppress("CheckReturnValue")
val sprint: ImageVector
    get() {
        if (_sprint != null) {
            return _sprint!!
        }
        _sprint =
            ImageVector.Builder(
                name = "sprint",
                defaultWidth = 24.dp,
                defaultHeight = 24.dp,
                viewportWidth = 24f,
                viewportHeight = 24f,
            )
                .apply {
                    path(
                        fill = SolidColor(Color.Black),
                        fillAlpha = 1f,
                        stroke = null,
                        strokeAlpha = 1f,
                        strokeLineWidth = 1f,
                        strokeLineCap = StrokeCap.Butt,
                        strokeLineJoin = StrokeJoin.Bevel,
                        strokeLineMiter = 1f,
                        pathFillType = PathFillType.Companion.NonZero,
                    ) {
                        moveTo(5.4f, 20f)
                        lineTo(4f, 18.6f)
                        lineTo(13.6f, 9f)
                        horizontalLineTo(11f)
                        verticalLineToRelative(2f)
                        horizontalLineTo(9f)
                        verticalLineTo(7f)
                        horizontalLineToRelative(5.83f)
                        quadToRelative(0.4f, 0f, 0.78f, 0.15f)
                        reflectiveQuadToRelative(0.65f, 0.43f)
                        lineToRelative(3f, 2.97f)
                        quadToRelative(0.68f, 0.68f, 1.65f, 1.05f)
                        reflectiveQuadTo(23f, 12f)
                        verticalLineToRelative(2f)
                        quadToRelative(-1.55f, 0f, -2.81f, -0.48f)
                        quadTo(18.93f, 13.05f, 17.95f, 12.1f)
                        lineToRelative(-1f, -1.05f)
                        lineToRelative(-2.2f, 2.2f)
                        lineTo(17f, 15.5f)
                        lineToRelative(-6.55f, 3.77f)
                        lineToRelative(-1f, -1.72f)
                        lineToRelative(4.3f, -2.48f)
                        lineToRelative(-1.7f, -1.7f)
                        lineTo(5.4f, 20f)
                        close()
                        moveTo(3f, 13f)
                        verticalLineTo(11f)
                        horizontalLineTo(8f)
                        verticalLineToRelative(2f)
                        horizontalLineTo(3f)
                        close()
                        moveTo(1f, 10f)
                        verticalLineTo(8f)
                        horizontalLineTo(6f)
                        verticalLineToRelative(2f)
                        horizontalLineTo(1f)
                        close()
                        moveTo(19.48f, 8f)
                        quadTo(18.65f, 8f, 18.05f, 7.41f)
                        reflectiveQuadTo(17.45f, 6f)
                        reflectiveQuadToRelative(0.6f, -1.41f)
                        reflectiveQuadTo(19.48f, 4f)
                        reflectiveQuadTo(20.9f, 4.59f)
                        quadTo(21.5f, 5.18f, 21.5f, 6f)
                        reflectiveQuadTo(20.9f, 7.41f)
                        reflectiveQuadTo(19.48f, 8f)
                        close()
                        moveTo(3f, 7f)
                        verticalLineTo(5f)
                        horizontalLineTo(8f)
                        verticalLineTo(7f)
                        horizontalLineTo(3f)
                        close()
                    }
                }
                .build()
        return _sprint!!
    }

private var _sprint: ImageVector? = null
