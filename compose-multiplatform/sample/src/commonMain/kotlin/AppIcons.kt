@file:Suppress("ktlint:standard:backing-property-naming")

import androidx.compose.foundation.Image
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.materialIcon
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import org.jetbrains.compose.ui.tooling.preview.Preview

val Icons.AirplaneTakeoff: ImageVector
    get() {
        if (_airplaneTakeoff != null) {
            return _airplaneTakeoff!!
        }
        _airplaneTakeoff = materialIcon(name = "Plane") {
            path(
                fill = SolidColor(Color(0xFFDCDCDC)),
                fillAlpha = 1.0F,
                strokeAlpha = 1.0F,
                strokeLineWidth = 0.0F,
                strokeLineCap = StrokeCap.Butt,
                strokeLineJoin = StrokeJoin.Miter,
                strokeLineMiter = 4.0F,
                pathFillType = PathFillType.NonZero,
            ) {
                moveTo(2.5F, 19.0F)
                horizontalLineTo(21.5F)
                verticalLineTo(21.0F)
                horizontalLineTo(2.5F)
                verticalLineTo(19.0F)
                moveTo(22.07F, 9.64F)
                curveTo(21.86F, 8.84F, 21.03F, 8.36F, 20.23F, 8.58F)
                lineTo(14.92F, 10.0F)
                lineTo(8.0F, 3.57F)
                lineTo(6.09F, 4.08F)
                lineTo(10.23F, 11.25F)
                lineTo(5.26F, 12.58F)
                lineTo(3.29F, 11.04F)
                lineTo(1.84F, 11.43F)
                lineTo(3.66F, 14.59F)
                lineTo(4.43F, 15.92F)
                lineTo(6.03F, 15.5F)
                lineTo(11.34F, 14.07F)
                lineTo(15.69F, 12.91F)
                lineTo(21.0F, 11.5F)
                curveTo(21.81F, 11.26F, 22.28F, 10.44F, 22.07F, 9.64F)
                close()
            }
        }
        return _airplaneTakeoff!!
    }

private var _airplaneTakeoff: ImageVector? = null

@Preview
@Composable
@Suppress("UnusedPrivateMember")
private fun IconAirplaneTakeoffPreview() {
    Image(imageVector = Icons.AirplaneTakeoff, contentDescription = null)
}

val Icons.AirplaneLanding: ImageVector
    get() {
        if (_airplaneLanding != null) {
            return _airplaneLanding!!
        }
        _airplaneLanding = materialIcon(name = "AirplaneLanding") {
            path(
                fill = SolidColor(Color(0xFFDCDCDC)),
                fillAlpha = 1.0F,
                strokeAlpha = 1.0F,
                strokeLineWidth = 0.0F,
                strokeLineCap = StrokeCap.Butt,
                strokeLineJoin = StrokeJoin.Miter,
                strokeLineMiter = 4.0F,
                pathFillType = PathFillType.NonZero,
            ) {
                moveTo(2.5F, 19.0F)
                horizontalLineTo(21.5F)
                verticalLineTo(21.0F)
                horizontalLineTo(2.5F)
                verticalLineTo(19.0F)
                moveTo(9.68F, 13.27F)
                lineTo(14.03F, 14.43F)
                lineTo(19.34F, 15.85F)
                curveTo(20.14F, 16.06F, 20.96F, 15.59F, 21.18F, 14.79F)
                curveTo(21.39F, 14.0F, 20.92F, 13.17F, 20.12F, 12.95F)
                lineTo(14.81F, 11.53F)
                lineTo(12.05F, 2.5F)
                lineTo(10.12F, 2.0F)
                verticalLineTo(10.28F)
                lineTo(5.15F, 8.95F)
                lineTo(4.22F, 6.63F)
                lineTo(2.77F, 6.24F)
                verticalLineTo(11.41F)
                lineTo(4.37F, 11.84F)
                lineTo(9.68F, 13.27F)
                close()
            }
        }
        return _airplaneLanding!!
    }

private var _airplaneLanding: ImageVector? = null

@Preview
@Composable
@Suppress("UnusedPrivateMember")
private fun IconAirplaneLandingPreview() {
    Image(imageVector = Icons.AirplaneLanding, contentDescription = null)
}
