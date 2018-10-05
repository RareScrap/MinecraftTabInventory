package ru.rarescrap.tabinventory.network;

import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;

/**
 * Утильный класс, предназначенный для удобного встраивания либы в моды
 */
public class NetworkUtils {
    /**
     * Регистрирует сообщения, необходимые для работы библиотеки MinecraftTabInventory. ВЫ ОБЯЗАНЫ вызвать его вовремя
     * init-фазы загрузки вашего мода!
     * @param chanel Канал, на котором нужно зарегистрировать сообщения
     * @param descriminator id, используемые при регистрации каждого сообщения. После регистрации сообщения увеличивается на 1.
     * @return Значение свободного дискриминатора, получившиеся после регистрации всех пакетов. Может использовать другими
     *         модами для регистрации их пакетов.
     */
    public int registerMessages(SimpleNetworkWrapper chanel, int descriminator) {
        chanel.registerMessage(SetTabSlotMessage.MessageHandler.class, SetTabSlotMessage.class, descriminator++, Side.CLIENT);
        chanel.registerMessage(TabInventoryItemsMessage.MessageHandler.class, TabInventoryItemsMessage.class, descriminator++, Side.CLIENT);
        return descriminator;
    }
}
