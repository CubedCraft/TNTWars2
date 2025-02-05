package com.jeroenvdg.minigame_utilities.commands

import com.jeroenvdg.minigame_utilities.Textial
import com.jeroenvdg.minigame_utilities.TextialParser
import com.jeroenvdg.minigame_utilities.commands.builders.setCommand
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import kotlin.math.min


data class PageData(val page: Int, var pageSize: Int, val elements: Int) {
    val pageCount get() = (elements-1) / pageSize + 1
    val start get() = (page-1) * pageSize
    val end get() = min(start + pageSize, elements)-1
    val isInvalidPage get() = page < 1 || start > elements
    val hasPages get() = pageCount > 1

    fun iterateElements(action: (index: Int) -> Unit) {
        for (i in start until end+1) action(i)
    }

    fun iterateEmpty(action: (index: Int) -> Unit) {
        if (page != pageCount) return

        val start = elements
        val max = pageSize * (pageCount)

        for (i in start until max) action(i)
    }

    fun previousPageArrow(cmdTemplate: String): TextComponent {
        val arrowComp = Component.text("«").toBuilder()
        if (page == 1) arrowComp.color(Textial.cmd.reset.color)
        else arrowComp.color(Textial.cmd.primary.color).setCommand(cmdTemplate.replace("_index_", (page-1).toString()))
        return arrowComp.build()
    }

    fun nextPageArrow(cmdTemplate: String): TextComponent {
        val arrowComp = Component.text("»").toBuilder()
        if (page == pageCount) arrowComp.color(Textial.cmd.reset.color)
        else arrowComp.color(Textial.cmd.primary.color).setCommand(cmdTemplate.replace("_index_", (page+1).toString()))
        return arrowComp.build()
    }

    fun pageTextComponent(cmdTemplate: String): TextComponent {
        return Component.text()
            .append(previousPageArrow(cmdTemplate))
            .append(Textial.cmd.parse(" &r$page/$pageCount "))
            .append(nextPageArrow(cmdTemplate))
            .build()
    }

    companion object {
        const val PaginatorDecorationSize: Int = 3
    }
}


fun paginate(header: TextComponent, textial: TextialParser, pageData: PageData, action: (index: Int) -> TextComponent): TextComponent {
    val comp = Component.text()
        .append(Textial.line).appendNewline()
        .append(textial.prefixComp)
        .append(header)
        .appendNewline()

    //  Build the body of the message

    pageData.iterateElements { i ->
        val char = chars[if (i == pageData.end) 2 else 0]
        comp.append(textial.format(" &r$char "))
        comp.append(action(i))
        comp.appendNewline()
    }

    pageData.iterateEmpty {
        comp.append(textial.format("\n"))
    }

    //  Build the footer of the message

    comp.append(Textial.line)
    return comp.build()
}