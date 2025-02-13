/*
 * Copyright (C) 2025 FallenCrystal / NBTGenerator Contributors
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package dev.akkariin.nbtgenerator.components

import com.google.gson.JsonElement
import dev.akkariin.nbtgenerator.components.ItemComponent.Container
import dev.akkariin.nbtgenerator.components.ItemComponent.TextComponentContainer
import dev.akkariin.nbtgenerator.components.containers.*
import dev.akkariin.nbtgenerator.components.enums.Rarity
import dev.akkariin.nbtgenerator.util.JsonUtil.exceptedAsJsonObject

@Suppress("MemberVisibilityCanBePrivate", "unused", "SameParameterValue")
object ItemComponents {

    val attributeModifiers = register("attribute_modifiers") { AttributeModifiersComponent(it.exceptedAsJsonObject()) }
    val consumable = register("consumable") { ConsumableComponent(it.exceptedAsJsonObject()) }
    val damage = registerInt("damage")
    @Suppress("SpellCheckingInspection")
    val enchantable = registerInt("enchantable")
    val enchantments = register("enchantments") { EnchantmentComponent(it.exceptedAsJsonObject()) }
    val food = register("food") { FoodComponent(it.exceptedAsJsonObject()) }
    val itemModel = registerString("item_model")
    val itemName = register("item_name", ::TextComponentContainer)
    val customName = register("custom_name", ::TextComponentContainer)
    val lore = register("lore") { LoreComponent(it.asJsonArray) }
    val maxDamage = registerInt("max_damage")
    val maxStackSize = registerInt("max_stack_size")
    val rarity = registerEnum("rarity", Rarity::valueOf)
    val repairCost = registerInt("repair_cost")
    // This is a EntryList. Maybe should make a flexible EntryList reader.
    val repairable = registerSingleton("repairable", "items", JsonElement::getAsString)


    val registered = mutableMapOf<String, ItemComponent<*>>()
    private fun <T : Container<*>> register(name: String, read: (JsonElement) -> T) = ItemComponent
        .create("minecraft:$name", read)
        .also { registered[it.name()] = it }

    private fun registerInt(name: String) = register(name) { ItemComponent.IntContainer(it.asJsonPrimitive) }
    private fun registerString(name: String) = register(name) { ItemComponent.StringContainer(it.asJsonPrimitive) }
    private fun <T : Enum<T>> registerEnum(name: String, valueOf: (String) -> T) = register(name)
    { ItemComponent.EnumContainer(it.asJsonPrimitive, valueOf(it.asString.uppercase())) }
    private fun <T : Any> registerSingleton(name: String, fieldName: String, reader: (JsonElement) -> T) = register(name) {
        ItemComponent.SingletonFieldContainer(it.exceptedAsJsonObject(), fieldName, reader)
    }
}