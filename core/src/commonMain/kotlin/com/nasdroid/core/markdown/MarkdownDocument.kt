package com.nasdroid.core.markdown

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import com.nasdroid.core.markdown.components.MarkdownBlockQuote
import com.nasdroid.core.markdown.components.MarkdownCodeBlock
import com.nasdroid.core.markdown.components.MarkdownHeading
import com.nasdroid.core.markdown.components.MarkdownHtmlBlock
import com.nasdroid.core.markdown.components.MarkdownOrderedList
import com.nasdroid.core.markdown.components.MarkdownParagraph
import com.nasdroid.core.markdown.components.MarkdownRule
import com.nasdroid.core.markdown.components.MarkdownTable
import com.nasdroid.core.markdown.components.MarkdownUnorderedList
import com.nasdroid.core.markdown.generator.MarkdownBlockQuote
import com.nasdroid.core.markdown.generator.MarkdownCodeBlock
import com.nasdroid.core.markdown.generator.MarkdownHeading
import com.nasdroid.core.markdown.generator.MarkdownHtmlBlock
import com.nasdroid.core.markdown.generator.MarkdownNode
import com.nasdroid.core.markdown.generator.MarkdownNodeGenerator
import com.nasdroid.core.markdown.generator.MarkdownOrderedList
import com.nasdroid.core.markdown.generator.MarkdownParagraph
import com.nasdroid.core.markdown.generator.MarkdownRule
import com.nasdroid.core.markdown.generator.MarkdownTable
import com.nasdroid.core.markdown.generator.MarkdownUnorderedList
import com.nasdroid.core.markdown.style.BlockQuoteStyle
import com.nasdroid.core.markdown.style.CodeBlockStyle
import com.nasdroid.core.markdown.style.RuleStyle
import com.nasdroid.core.markdown.style.TextStyleModifiers
import com.nasdroid.core.markdown.style.TextStyles
import org.intellij.markdown.flavours.gfm.GFMFlavourDescriptor
import org.intellij.markdown.parser.MarkdownParser

/**
 * Displays a Markdown document.
 */
@Composable
public fun MarkdownDocument(
    markdown: String,
    textStyles: TextStyles,
    textStyleModifiers: TextStyleModifiers,
    blockQuoteStyle: BlockQuoteStyle,
    codeBlockStyle: CodeBlockStyle,
    ruleStyle: RuleStyle,
    modifier: Modifier = Modifier,
    sectionSpacing: Dp = textStyles.textStyle.fontSize.toDp()
) {
    val parsedMarkdownNodes = remember(markdown) {
        val flavor = GFMFlavourDescriptor()
        val tree = MarkdownParser(flavor).buildMarkdownTreeFromString(markdown)
        MarkdownNodeGenerator(markdown, tree).generateNodes()
    }
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(sectionSpacing)
    ) {
        parsedMarkdownNodes.forEach {
            MarkdownNode(
                node = it,
                textStyles = textStyles,
                textStyleModifiers = textStyleModifiers,
                blockQuoteStyle = blockQuoteStyle,
                codeBlockStyle = codeBlockStyle,
                ruleStyle = ruleStyle
            )
        }
    }
}

@Composable
internal fun TextUnit.toDp(): Dp {
    return with(LocalDensity.current) {
        this@toDp.toPx().toDp()
    }
}

@Composable
internal fun MarkdownNode(
    node: MarkdownNode,
    textStyles: TextStyles,
    textStyleModifiers: TextStyleModifiers,
    blockQuoteStyle: BlockQuoteStyle,
    codeBlockStyle: CodeBlockStyle,
    ruleStyle: RuleStyle,
    modifier: Modifier = Modifier
) {
    when (node) {
        is MarkdownBlockQuote -> MarkdownBlockQuote(
            blockQuote = node,
            style = blockQuoteStyle,
            textStyles = textStyles,
            textStyleModifiers = textStyleModifiers,
            codeBlockStyle = codeBlockStyle,
            ruleStyle = ruleStyle,
            modifier = modifier,
        )
        is MarkdownCodeBlock -> MarkdownCodeBlock(
            codeBlock = node,
            style = codeBlockStyle,
            textStyle = textStyles.textStyle.copy(fontFamily = FontFamily.Monospace),
            modifier = modifier,
        )
        is MarkdownHeading -> MarkdownHeading(
            heading = node,
            modifier = modifier,
            textStyles = textStyles,
            textStyleModifiers = textStyleModifiers,
        )
        is MarkdownOrderedList -> MarkdownOrderedList(
            list = node,
            textStyles = textStyles,
            textStyleModifiers = textStyleModifiers,
            blockQuoteStyle = blockQuoteStyle,
            codeBlockStyle = codeBlockStyle,
            ruleStyle = ruleStyle,
            modifier = modifier
        )
        is MarkdownParagraph -> MarkdownParagraph(
            paragraph = node,
            textStyle = textStyles.textStyle,
            textStyleModifiers = textStyleModifiers,
            modifier = modifier
        )
        MarkdownRule -> MarkdownRule(
            ruleStyle = ruleStyle,
            modifier = modifier
        )
        is MarkdownTable -> MarkdownTable(
            table = node,
            textStyle = textStyles.textStyle,
            textStyleModifiers = textStyleModifiers,
            ruleStyle = ruleStyle,
            modifier = modifier
        )
        is MarkdownHtmlBlock -> MarkdownHtmlBlock(
            htmlBlock = node,
            textStyle = textStyles.textStyle,
            modifier = modifier
        )
        is MarkdownUnorderedList -> MarkdownUnorderedList(
            list = node,
            textStyles = textStyles,
            textStyleModifiers = textStyleModifiers,
            blockQuoteStyle = blockQuoteStyle,
            codeBlockStyle = codeBlockStyle,
            ruleStyle = ruleStyle,
            modifier = modifier
        )
    }
}
