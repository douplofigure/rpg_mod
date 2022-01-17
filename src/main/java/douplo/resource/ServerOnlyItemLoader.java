package douplo.resource;

import com.google.gson.JsonObject;
import com.mojang.serialization.Lifecycle;
import douplo.RpgMod;
import douplo.item.LoadedMaterial;
import douplo.item.GenericServerItem;
import douplo.item.ServerOnlyItem;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.item.Item;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;

import java.io.IOException;
import java.util.Optional;
import java.util.OptionalInt;

public class ServerOnlyItemLoader implements Reloader {

    private static final String ITEM_DIRECTORY = "server_items";
    private static final String MATERIAL_DIRECTORY = "materials";


    private static void registerItemOrReplace(ServerOnlyItem item) {

        Optional<Item> optional = Registry.ITEM.getOrEmpty(item.getId());
        if (optional.isEmpty()) {
            Registry.register(Registry.ITEM, item.getId(), item.asItem());
        } else {

            Item oldItem = optional.get();
            int rawId = Registry.ITEM.getRawId(oldItem);
            Registry.ITEM.replace(OptionalInt.of(rawId), RegistryKey.of(Registry.ITEM_KEY, item.getId()), item.asItem(), Lifecycle.stable());

        }

    }

    @Override
    public void reload(ResourceManager manager) {

        ServerOnlyItem.clearCache();

        for (Identifier filename : manager.findResources(MATERIAL_DIRECTORY, path -> path.endsWith(".json"))) {
            Identifier materialId = ReloadUtils.filenameToObjectId(filename);
            try {
                JsonObject materialData = ReloadUtils.jsonFromStream(manager.getResource(filename).getInputStream());
                LoadedMaterial.loadFromJson(materialId, materialData);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        for (Identifier filename : manager.findResources(ITEM_DIRECTORY, path -> path.endsWith(".json"))) {
            Identifier itemId = ReloadUtils.filenameToObjectId(filename);
            try {
                JsonObject itemData = ReloadUtils.jsonFromStream(manager.getResource(filename).getInputStream());
                ServerOnlyItem item = ServerOnlyItem.fromJson(itemId, itemData);
                registerItemOrReplace(item);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        RpgMod.reloadOccured = true;

    }
}
