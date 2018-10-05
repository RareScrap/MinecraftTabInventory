package ru.rarescrap.tabinventory.network;

import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;

/**
 * Утильный класс, предназначенный для удобного встраивания либы в моды
 */
public class NetworkUtils {
    /** Сетевая обертку, которую использует либа MinecraftTabInventory для обмена сообщениями между клиентом
     * и сервером. Для ее использования выдолжны вывать {@link #registerMessages(SimpleNetworkWrapper, int)}. */
    private static SimpleNetworkWrapper NETWORK_WRAPPER;

    /**
     * Регистрирует сообщения, необходимые для работы библиотеки MinecraftTabInventory. ВЫ ОБЯЗАНЫ вызвать его вовремя
     * init-фазы загрузки вашего мода!
     * @param chanel Канал, на котором нужно зарегистрировать сообщения
     * @param descriminator id, используемые при регистрации каждого сообщения. После регистрации сообщения увеличивается на 1.
     * @return Значение свободного дискриминатора, получившиеся после регистрации всех пакетов. Может использовать другими
     *         модами для регистрации их пакетов.
     * @see "https://mcforge.readthedocs.io/en/latest/networking/simpleimpl/#registering-packets"
     */
    public int registerMessages(SimpleNetworkWrapper chanel, int descriminator) {
        chanel.registerMessage(SetTabSlotMessage.MessageHandler.class, SetTabSlotMessage.class, descriminator++, Side.CLIENT);
        chanel.registerMessage(TabInventoryItemsMessage.MessageHandler.class, TabInventoryItemsMessage.class, descriminator++, Side.CLIENT);
        NETWORK_WRAPPER = chanel;
        return descriminator;
    }

    /**
     * Возвращает сетевую обертку, которую использует либа MinecraftTabInventory.
     * ВНИМАНИЕ! Чтобы использовать этот метод, вы обязаны единожды в вашем моде вызвать
     * {@link #registerMessages(SimpleNetworkWrapper, int)} во время регистрации ваших пакетов.
     * Чаще всего это делается во время init-фазы загрузки мода.
     * @return Сетевая обертка с зарегистрированными на ней сообщениями. Null, если
     * {@link #registerMessages(SimpleNetworkWrapper, int)} не был вызван, что является серьезной ошибкой.
     */
    public static SimpleNetworkWrapper getNetworkWrapper() {
        return NETWORK_WRAPPER;
    }
}
