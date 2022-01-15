package douplo.resource;

import com.google.gson.JsonObject;
import com.mojang.serialization.Lifecycle;
import douplo.RpgMod;
import douplo.item.ServerOnlyItem;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.item.Item;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;

import java.io.IOException;
import java.util.Optional;

public class ServerOnlyItemLoader implements SimpleSynchronousResourceReloadListener {

    private static final String DIRECTORY_NAME = "server_items";

    @Override
    public Identifier getFabricId() {
        return new Identifier(RpgMod.MODID, DIRECTORY_NAME);
    }

    private static void registerItemOrReplace(ServerOnlyItem item) {

        Optional<Item> optional = Registry.ITEM.getOrEmpty(item.getId());
        if (optional.isEmpty()) {
            Registry.register(Registry.ITEM, item.getId(), item);
        } else {

            Item oldItem = optional.get();
            int rawId = Registry.ITEM.getRawId(oldItem);
            Registry.ITEM.set(rawId, RegistryKey.of(Registry.ITEM_KEY, item.getId()), item, Lifecycle.stable());

        }

    }

    @Override
    public void reload(ResourceManager manager) {

        ServerOnlyItem.clearCache();

        for (Identifier filename : manager.findResources(DIRECTORY_NAME, path -> path.endsWith(".json"))) {

            Identifier itemId = ReloadUtils.filenameToObjectId(filename);
            try {
                JsonObject itemData = ReloadUtils.jsonFromStream(manager.getResource(filename).getInputStream());
                ServerOnlyItem item = ServerOnlyItem.fromJson(itemId, itemData);

                registerItemOrReplace(item);

            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        ResourcePackServer.createResourcePack();

    }
}
