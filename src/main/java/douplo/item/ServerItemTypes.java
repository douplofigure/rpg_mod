package douplo.item;

import douplo.RpgMod;
import net.minecraft.util.Identifier;

public class ServerItemTypes {

    public static final ServerOnlyItem.Type<GenericServerItem> GENERIC = new ServerOnlyItem.Type<>(GenericServerItem.GENERIC_SERIALIZER);
    public static final ServerOnlyItem.Type<ServerToolItem> TOOL = new ServerOnlyItem.Type<>(ServerToolItem.SERIALIZER);
    public static final ServerOnlyItem.Type<ServerSwordItem> SWORD = new ServerOnlyItem.Type<>(ServerSwordItem.SERIALIZER);
    public static final ServerOnlyItem.Type<ServerArmorItem> ARMOR = new ServerOnlyItem.Type<>(ServerArmorItem.SERIALIZER);

    public static void registerTypes() {

        ServerOnlyItem.registerType(new Identifier(RpgMod.MODID, "generic"), GENERIC);
        ServerOnlyItem.registerType(new Identifier(RpgMod.MODID, "tool"), TOOL);
        ServerOnlyItem.registerType(new Identifier(RpgMod.MODID, "sword"), SWORD);
        ServerOnlyItem.registerType(new Identifier(RpgMod.MODID, "armor"), ARMOR);

    }

}
