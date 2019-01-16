package ru.rarescrap.tabinventory.network;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;

/**
 * Утильный класс, предназначенный для удобного встраивания либы в моды
 */
public class NetworkUtils {
    /** Сетевая обертку, которую использует либа MinecraftTabInventory для обмена сообщениями между клиентом
     * и сервером. Для ее использования вы должны вывать {@link #registerMessages(SimpleNetworkWrapper, int)}. */
    private static SimpleNetworkWrapper NETWORK_WRAPPER;

    /**
     * Регистрирует сообщения, необходимые для работы библиотеки MinecraftTabInventory. ВЫ ОБЯЗАНЫ вызвать его вовремя
     * init-фазы загрузки вашего мода! Используйте этот метод если вам не требуется самостоятельно хандлить клиентские
     * сообщения либы.
     * @param chanel Канал, на котором нужно зарегистрировать сообщения
     * @param discriminator id, используемые при регистрации каждого сообщения. После регистрации сообщения увеличивается на 1.
     * @return Значение свободного дискриминатора, получившиеся после регистрации всех пакетов. Может использовать другими
     *         модами для регистрации их пакетов.
     * @see "https://mcforge.readthedocs.io/en/latest/networking/simpleimpl/#registering-packets"
     * @see #registerMessages(SimpleNetworkWrapper, Class, Class, int)
     */
    public static int registerMessages(SimpleNetworkWrapper chanel, int discriminator) {
        return registerMessages(chanel, SetTabSlotMessage.MessageHandler.class, TabInventoryItemsMessage.MessageHandler.class, discriminator);
    }

    /**
     * Регистрирует сообщения, необходимые для работы библиотеки MinecraftTabInventory. ВЫ ОБЯЗАНЫ вызвать его вовремя
     * init-фазы загрузки вашего мода! Используйте этот метод, если вы хотите установить свой обработчик клиентских пакетов.
     * Это может пригодится, например, если вы хотите сделать контейнер с вкладочными инвентарями постоянно синхронизируемым.
     * Т.е. не пересоздавать его при открытии и поддерживать с ним синхронизацию даже если он не открыт. Как в случае с
     * {@link net.minecraft.entity.player.EntityPlayer#inventoryContainer}.
     * @param chanel Канал, на котором нужно зарегистрировать сообщения
     * @param setTabSlotMessageHandler Хандлер сообщения об изменении контента с слоте {@link ru.rarescrap.tabinventory.TabInventory}
     * @param tabInventoryItemsMessageHandler Хандлер сообщения об обновлении всего контента в {@link ru.rarescrap.tabinventory.TabInventory}
     * @param discriminator id, используемые при регистрации каждого сообщения. После регистрации сообщения увеличивается на 1.
     * @return Значение свободного дискриминатора, получившиеся после регистрации всех пакетов. Может использовать другими
     *         модами для регистрации их пакетов.
     * @see "https://mcforge.readthedocs.io/en/latest/networking/simpleimpl/#registering-packets"
     * @see #registerMessages(SimpleNetworkWrapper, int)
     */
    public static int registerMessages(SimpleNetworkWrapper chanel, // TODO: Создать мануал применения
                                       Class<? extends IMessageHandler<SetTabSlotMessage, IMessage>> setTabSlotMessageHandler,
                                       Class<? extends IMessageHandler<TabInventoryItemsMessage, IMessage>> tabInventoryItemsMessageHandler,
                                       int discriminator) {
        chanel.registerMessage(setTabSlotMessageHandler, SetTabSlotMessage.class, discriminator++, Side.CLIENT);
        chanel.registerMessage(tabInventoryItemsMessageHandler, TabInventoryItemsMessage.class, discriminator++, Side.CLIENT);
        chanel.registerMessage(TabClickWindowMessage.MessageHandler.class, TabClickWindowMessage.class, discriminator++, Side.SERVER);
        NETWORK_WRAPPER = chanel;
        return discriminator;
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
