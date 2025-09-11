import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.sp

// استفاده از فونت پیش‌فرض سیستم
val AppFontFamily = FontFamily.Default

val Typography = Typography(
    bodySmall = TextStyle(
        fontFamily = AppFontFamily,
        fontSize = 10.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = AppFontFamily,
        fontSize = 12.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = AppFontFamily,
        fontSize = 14.sp
    ),
    titleSmall = TextStyle(
        fontFamily = AppFontFamily,
        fontSize = 16.sp
    ),
    titleMedium = TextStyle(
        fontFamily = AppFontFamily,
        fontSize = 20.sp
    ),
    titleLarge = TextStyle(
        fontFamily = AppFontFamily,
        fontSize = 24.sp
    )
)
