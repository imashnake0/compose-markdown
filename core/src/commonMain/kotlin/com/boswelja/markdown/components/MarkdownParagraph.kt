package com.boswelja.markdown.components

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.ResolvedTextDirection
import androidx.compose.ui.unit.sp
import com.boswelja.markdown.generator.MarkdownParagraph
import com.boswelja.markdown.style.TextStyleModifiers
import com.boswelja.markdown.style.TextUnitSize
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

/**
 * Displays a [MarkdownParagraph]. A paragraph is a group of "spans". Spans are stylized sections of
 * text, but can also include inline images and links.
 */
@OptIn(ExperimentalTextApi::class)
@Composable
internal fun MarkdownParagraph(
    paragraph: MarkdownParagraph,
    textStyle: TextStyle,
    textStyleModifiers: TextStyleModifiers,
    onLinkClick: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var onDraw: DrawScope.() -> Unit by remember { mutableStateOf({}) }
    val (annotatedString, inlineContent) = remember(paragraph) {
        paragraph.children.buildTextWithContent(textStyle, textStyleModifiers, TextUnitSize(100.sp, 100.sp))
    }
    val layoutResult = remember { mutableStateOf<TextLayoutResult?>(null) }
    val pressIndicator = Modifier.pointerInput(onLinkClick, annotatedString) {
        detectTapGestures { pos ->
            layoutResult.value?.let { layoutResult ->
                val offset = layoutResult.getOffsetForPosition(pos)
                annotatedString.getUrlAnnotations(start = offset, end = offset).firstOrNull()?.let { annotation ->
                    onLinkClick(annotation.item.url)
                }
            }
        }
    }
    BasicText(
        text = annotatedString,
        modifier = modifier.then(pressIndicator).drawBehind { onDraw() },
        inlineContent = inlineContent,
        onTextLayout = {
            layoutResult.value = it
            val annotation = annotatedString.getUrlAnnotations( 0, annotatedString.length).first()
            val textBounds = layoutResult.value!!.getBoundingBoxes(annotation.start, annotation.end)
            onDraw = {
                for (bound in textBounds) {
                    val underline = bound.copy(top = bound.bottom - 4.sp.toPx())
                    drawRect(
                        color = Color.Green,
                        topLeft = underline.topLeft,
                        size = underline.size,
                    )
                }
            }
        }
    )
}

/**
 * Reads bounds for multiple lines. This can be removed once an
 * [official API](https://issuetracker.google.com/u/1/issues/237289433) is released.
 *
 * When [flattenForFullParagraphs] is available, the bounds for one or multiple
 * entire paragraphs is returned instead of separate lines if [startOffset]
 * and [endOffset] represent the extreme ends of those paragraph.
 */
public fun TextLayoutResult.getBoundingBoxes(
    startOffset: Int,
    endOffset: Int,
    flattenForFullParagraphs: Boolean = false
): List<Rect> {
    if (startOffset == endOffset) {
        return emptyList()
    }

    val startLineNum = getLineForOffset(startOffset)
    val endLineNum = getLineForOffset(endOffset)

    if (flattenForFullParagraphs) {
        val isFullParagraph = (startLineNum != endLineNum)
                && getLineStart(startLineNum) == startOffset
                && multiParagraph.getLineEnd(endLineNum, visibleEnd = true) == endOffset

        if (isFullParagraph) {
            return listOf(
                Rect(
                    top = getLineTop(startLineNum),
                    bottom = getLineBottom(endLineNum),
                    left = 0f,
                    right = size.width.toFloat()
                )
            )
        }
    }

    // Compose UI does not offer any API for reading paragraph direction for an entire line.
    // So this code assumes that all paragraphs in the text will have the same direction.
    // It also assumes that this paragraph does not contain bi-directional text.
    val isLtr = multiParagraph.getParagraphDirection(offset = layoutInput.text.lastIndex) == ResolvedTextDirection.Ltr

    return fastMapRange(startLineNum, endLineNum) { lineNum ->
        Rect(
            top = getLineTop(lineNum),
            bottom = getLineBottom(lineNum),
            left = if (lineNum == startLineNum) {
                getHorizontalPosition(startOffset, usePrimaryDirection = isLtr)
            } else {
                getLineLeft(lineNum)
            },
            right = if (lineNum == endLineNum) {
                getHorizontalPosition(endOffset, usePrimaryDirection = isLtr)
            } else {
                getLineRight(lineNum)
            }
        )
    }
}

@OptIn(ExperimentalContracts::class)
internal inline fun <R> fastMapRange(
    start: Int,
    end: Int,
    transform: (Int) -> R
): List<R> {
    contract { callsInPlace(transform) }
    val destination = ArrayList<R>(/* initialCapacity = */ end - start + 1)
    for (i in start..end) {
        destination.add(transform(i))
    }
    return destination
}
