/**
 * Классы инвентарей, используемые для реализации системы вкладок.
 *
 * <h1>Описание</h1>
 *
 * {@link ru.rarescrap.tabinventory.TabInventory}: Инвентарь, хранящий массивы стаков в формате "ключ-значение".
 * Ключи определяются объектом TabHostInventory, к которому присоединен данный TabInventory, и соответствуют нелокализированым
 * названиям предметов в TabHostInventory. Так например, если в TabHostInventory имеет 2 стака с предметами "Алмазный
 * топор" и "Семена пшеницы", то ключи вкладок будут ""item.hatchetDiamond" и "item.seeds".
 *
 * {@link ru.rarescrap.tabinventory.TabHostInventory}: обычный инвентарь, с которым связвается экземпляр
 * TabInventory. Отвечает за выбор текущей вкладки, добавление/удаление вкладок.
 *
 */
package ru.rarescrap.tabinventory;