package net.author

import net.botwithus.rs3.game.hud.interfaces.Component
import net.botwithus.rs3.game.js5.types.configs.ConfigManager
import net.botwithus.rs3.game.minimenu.MiniMenu
import net.botwithus.rs3.game.queries.builders.components.ComponentQuery
import net.botwithus.rs3.game.vars.VarManager
import java.util.ArrayList
import java.util.function.Predicate
import java.util.regex.Pattern
import java.util.stream.Collectors

//Courtesy of Ceru
object Shop {
    fun isOpen(): Boolean {
        return VarManager.getVarc(2874) == 18
    }
    fun getItems(filter: Predicate<Component>): ArrayList<Component> {
        return ArrayList<Component>(
            ComponentQuery.newQuery(1265)
                .componentIndex(24)
                .results()
                .stream()
                .filter(filter)
                .collect(Collectors.toList()))
    }

    fun getItems(pattern: Pattern): ArrayList<Component> {
        return ArrayList(getItems(nameMatcher(pattern)))
    }

    fun getItems(): ArrayList<Component> {
        return ArrayList(getItems(Predicate { true }))
    }
    fun contains(filter: Predicate<Component>): Boolean {
        return getItems(filter).isNotEmpty()
    }

    fun contains(pattern: Pattern): Boolean {
        return getItems(nameMatcher(pattern)).isNotEmpty()
    }

    fun getAmount(filter: Predicate<Component>): Int {
        return getItems(filter).sumOf { it.itemAmount.takeIf { it > 0 } ?: 0 }
    }

    fun getAmount(pattern: Pattern): Int {
        return getAmount(nameMatcher(pattern))
    }

    fun getAmount(): Int {
        return getAmount(Predicate { true })
    }

    fun buyAll(filter: Predicate<Component>): Boolean {
        val components = getItems(filter)
        if (components.isEmpty()) {
            return false
        }

        return components.mapNotNull { component ->
            val amount = component.itemAmount
            if (amount > 0) {
                val param2 = if (amount == 1) 2 else 7
                val param4 = (component.interfaceIndex shl 16 or component.componentIndex) - 4
                MiniMenu.interact(14, param2, component.subComponentIndex, param4)
            } else null
        }.isNotEmpty()
    }

    fun buyAll(pattern: Pattern): Boolean {
        return buyAll(nameMatcher(pattern))
    }

    fun close(): Boolean {
        if (!isOpen()) {
            return false
        }
        val component = ComponentQuery.newQuery(1265)
            .componentIndex(217)
            .subComponentIndex(1)
            .results()
            .first()
        return component?.interact(1) ?: false
    }

    internal fun nameMatcher(pattern: Pattern): Predicate<Component> {
        return Predicate { component ->
            val type = ConfigManager.getItemType(component.itemId)
            type?.name?.matches(pattern.toRegex()) ?: false
        }

    }
}
