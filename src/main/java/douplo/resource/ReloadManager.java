package douplo.resource;

import douplo.RpgMod;
import douplo.item.ServerOnlyItem;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

public class ReloadManager implements SimpleSynchronousResourceReloadListener {

    private static final ServerOnlyItemLoader itemLoader = new ServerOnlyItemLoader();
    public static boolean itemsLoaded = false;

    public static void reloadItems(ResourceManager manager) {
        itemsLoaded = false;
        itemLoader.reload(manager);
        RpgMod.LOGGER.info("Done loading items");
        itemsLoaded = true;
    }

    @Override
    public Identifier getFabricId() {
        return new Identifier(RpgMod.MODID, "reload_manager");
    }

    @Override
    public void reload(ResourceManager manager) {

        ResourcePackServer.createResourcePack(manager);

    }


}
