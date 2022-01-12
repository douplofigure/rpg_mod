package douplo.playerclass;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.WorldSavePath;
import net.minecraft.world.PersistentState;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerClassMap extends PersistentState {

    public static final PlayerClass DEFAULT = new PlayerClass(new Identifier("rpg_mod:default"), Text.of("Default"));

    private Map<UUID, Identifier> classes = new HashMap<>();
    private boolean dirty = false;

    public PlayerClass getPlayerClassFromUUID(UUID playerUUID) {
        if (classes.containsKey(playerUUID)) {
            PlayerClass clazz = PlayerClass.getFromId(classes.get(playerUUID));
            if (clazz == null)
                return DEFAULT;
            return clazz;
        } else
            return DEFAULT;
    }

    private static Path getSavePath(MinecraftServer server) {
        Path rootPath = server.getSavePath(WorldSavePath.PLAYERDATA);
        return rootPath.resolve("player_classes.dat");
    }

    public void saveIfDirty(MinecraftServer server) {
        if (!dirty)
            return;

        save(server);

    }

    public void save(MinecraftServer server) {
        Path filePath = getSavePath(server);
        NbtCompound compound = this.writeNbt(new NbtCompound());
        try {
            net.minecraft.nbt.NbtIo.writeCompressed(compound, filePath.toFile());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static PlayerClassMap load(MinecraftServer server) {

        Path filePath = getSavePath(server);

        try {
            NbtCompound compount = NbtIo.readCompressed(filePath.toFile());
            return PlayerClassMap.loadNbt(compount);
        } catch (FileNotFoundException e) {
            return new PlayerClassMap();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new PlayerClassMap();
    }

    public void setPlayerClass(PlayerEntity player, PlayerClass playerClass) {
        classes.put(player.getUuid(), playerClass.getId());

        playerClass.onPlayerSet(player);

        dirty = true;
    }

    public static PlayerClassMap loadNbt(NbtCompound nbt) {

        NbtList ls = nbt.getList("playerClasses", NbtCompound.COMPOUND_TYPE);

        PlayerClassMap map = new PlayerClassMap();

        for (int i = 0; i < ls.size(); ++i) {

            NbtCompound elem = ls.getCompound(i);
            UUID player = NbtHelper.toUuid(elem.get("player"));
            Identifier id = new Identifier(elem.getString("class"));

            map.classes.put(player, id);

        }
        return map;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound nbt) {

        NbtList ls = new NbtList();

        for (Map.Entry<UUID, Identifier> entry : classes.entrySet()) {
            NbtCompound elem = new NbtCompound();
            elem.put("player", NbtHelper.fromUuid(entry.getKey()));
            elem.putString("class", entry.getValue().toString());
            ls.add(elem);
        }
        nbt.put("playerClasses", ls);
        return nbt;
    }
}
