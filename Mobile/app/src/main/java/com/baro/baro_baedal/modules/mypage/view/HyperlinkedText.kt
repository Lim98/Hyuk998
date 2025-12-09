package com.baro.baro_baedal.modules.mypage.view

import android.content.Intent
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.core.net.toUri

private val urlRegex =
    "(https?://[A-Za-z0-9\\-._~:/?#\\[\\]@!$&'()*+,;=%]+)".toRegex()

@Composable
fun HyperlinkedText(
    text: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    val annotatedString = buildAnnotatedString {
        var lastIndex = 0

        urlRegex.findAll(text).forEach { result ->
            val range = result.range

            // URL 앞 일반 텍스트
            append(text.substring(lastIndex, range.first))

            val url = result.value

            pushStringAnnotation(
                tag = "URL",
                annotation = url
            )
            withStyle(
                style = SpanStyle(
                    color = MaterialTheme.colorScheme.primary,
                    textDecoration = TextDecoration.Underline
                )
            ) {
                append(url)
            }
            pop()

            lastIndex = range.last + 1
        }

        // 마지막 URL 뒤 텍스트
        if (lastIndex < text.length) {
            append(text.substring(lastIndex))
        }
    }

    ClickableText(
        text = annotatedString,
        modifier = modifier,
        onClick = { offset ->
            annotatedString.getStringAnnotations(
                tag = "URL",
                start = offset,
                end = offset
            ).firstOrNull()?.let {
                val intent = Intent(Intent.ACTION_VIEW, it.item.toUri())
                context.startActivity(intent)
            }
        }
    )
}