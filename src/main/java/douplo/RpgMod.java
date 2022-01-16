package douplo;

import com.mojang.brigadier.CommandDispatcher;
import douplo.command.Commands;
import douplo.crafting.*;
import douplo.event.PlayerRespawnCallback;
import douplo.item.ServerItemTypes;
import douplo.item.GenericServerItem;
import douplo.item.ServerToolItem;
import douplo.loot.condition.LootConditions;
import douplo.loot.number.NumberProviderTypes;
import douplo.playerclass.PlayerClass;
import douplo.playerclass.PlayerClassLoader;
import douplo.playerclass.PlayerClassMap;
import douplo.resource.ReloadManager;
import douplo.resource.ResourcePackServer;
import douplo.resource.ServerOnlyItemLoader;
import douplo.skill.Skill;
import douplo.skill.SkillLoader;
import douplo.skill.SkillTypes;
import douplo.skill.bonus.SkillBonus;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;
import net.minecraft.block.BlockState;
import net.minecraft.block.cauldron.CauldronBehavior;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.*;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.InputStream;


public class RpgMod implements ModInitializer {

    public static final String MODID = "rpg_mod";
    public static final Logger LOGGER = LogManager.getLogger(MODID);

    public static PlayerClassMap PLAYER_CLASSES = new PlayerClassMap();

    public static boolean reloadOccured = true;

    private static final CauldronBehavior CRAFT_CAULDRON_BEHAIVIOR = new CauldronBehavior() {
        @Override
        public ActionResult interact(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, ItemStack stack) {
            if (world.isClient())
                return ActionResult.SUCCESS;

            LOGGER.info("Player clicked on cauldron with a stick!");

            NamedScreenHandlerFactory screen = new SimpleNamedScreenHandlerFactory(((syncId, inv, player1) -> new CauldronScreenHandler(syncId, inv, ScreenHandlerContext.create(world, pos), pos)), state.getBlock().getName());
            player.openHandledScreen(screen);

            return ActionResult.CONSUME;
        }
    };

    public static final Item TEST_ITEM = new GenericServerItem(new Identifier(MODID, "test_item"), new Item.Settings().group(ItemGroup.TOOLS).maxCount(1).maxDamage(100));

    public static final ToolMaterial COPPER_TOOL_MATERIAL = ToolMaterials.STONE;
    public static final Item COPPER_PICKAXE = new ServerToolItem(new Identifier(MODID, "copper_pickaxe"), COPPER_TOOL_MATERIAL, new Item.Settings().group(ItemGroup.TOOLS));

    @Override
    public void onInitialize() {

        ResourcePackServer.initializeResourcePackServer("localhost", 8000);

        ServerItemTypes.registerTypes();

        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new ReloadManager());
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
            @Override
            public Identifier getFabricId() {
                return new Identifier(MODID, "classes");
            }

            @Override
            public void reload(ResourceManager manager) {

                PlayerClass.clearCache();

                for (Identifier id : manager.findResources("classes", path -> path.endsWith(".json"))) {
                    try (InputStream stream = manager.getResource(id).getInputStream()) {
                        LOGGER.info("Loading data for " + id);
                        int startIndex = id.getPath().indexOf("/");
                        int endIndex = id.getPath().lastIndexOf(".");
                        String simplePath = id.getPath().substring(startIndex+1, endIndex);
                        Identifier name = new Identifier(id.getNamespace(), simplePath);
                        LOGGER.info("name: " + name);
                        PlayerClassLoader.load(name, stream);
                    } catch (Exception e) {
                        LOGGER.error(e);
                        e.printStackTrace();
                    }
                }

                Skill.clearSkills();

                for (Identifier id : manager.findResources("skills", path -> path.endsWith(".json"))) {
                    try (InputStream stream = manager.getResource(id).getInputStream()) {
                        LOGGER.info("Loading data for " + id);
                        int startIndex = id.getPath().indexOf("/");
                        int endIndex = id.getPath().lastIndexOf(".");
                        String simplePath = id.getPath().substring(startIndex+1, endIndex);
                        Identifier name = new Identifier(id.getNamespace(), simplePath);
                        LOGGER.info("name: " + name);
                        SkillLoader.load(name, stream);
                    } catch (Exception e) {
                        LOGGER.error(e);
                        e.printStackTrace();
                    }
                }

                SkillBonus.clearCache();

                for (Identifier id : manager.findResources("skill_bonuses", path -> path.endsWith(".json"))) {
                    try (InputStream stream = manager.getResource(id).getInputStream()) {
                        LOGGER.info("Loading data for " + id);
                        int startIndex = id.getPath().indexOf("/");
                        int endIndex = id.getPath().lastIndexOf(".");
                        String simplePath = id.getPath().substring(startIndex+1, endIndex);
                        Identifier name = new Identifier(id.getNamespace(), simplePath);
                        LOGGER.info("name: " + name);
                        SkillBonus.loadFromFile(name, stream);
                    } catch (Exception e) {
                        LOGGER.error(e);
                        e.printStackTrace();
                    }
                }

            }
        });

        CommandRegistrationCallback.EVENT.register(new CommandRegistrationCallback() {
            @Override
            public void register(CommandDispatcher<ServerCommandSource> dispatcher, boolean dedicated) {
                Commands.registerCommands(dispatcher, dedicated);
            }
        });

        ServerWorldEvents.LOAD.register(new ServerWorldEvents.Load() {
            @Override
            public void onWorldLoad(MinecraftServer server, ServerWorld world) {
                LOGGER.info("WORLD LOAD!");

                server.setResourcePack(ResourcePackServer.getPackAddress(), ResourcePackServer.getPackHash());

                PLAYER_CLASSES = PlayerClassMap.load(server);

                /*GameRules.BooleanRule rule = world.getGameRules().get(GameRules.DO_LIMITED_CRAFTING);
                rule.set(true, server);*/

            }
        });

        ServerTickEvents.END_SERVER_TICK.register(new ServerTickEvents.EndTick() {
            @Override
            public void onEndTick(MinecraftServer server) {

                PLAYER_CLASSES.saveIfDirty(server);

                if (reloadOccured) {
                    reloadOccured = false;
                    server.setResourcePack(ResourcePackServer.getPackAddress(), ResourcePackServer.getPackHash());
                    LOGGER.info(server.getResourcePackHash());
                    LOGGER.info(server.getResourcePackUrl());
                }

            }
        });

        ServerWorldEvents.UNLOAD.register(new ServerWorldEvents.Unload() {
            @Override
            public void onWorldUnload(MinecraftServer server, ServerWorld world) {
                LOGGER.info("WORLD UNLOAD");

                PLAYER_CLASSES.save(server);

            }
        });

        PlayerRespawnCallback.EVENT.register(new PlayerRespawnCallback() {
            @Override
            public ActionResult onRespawn(ServerPlayerEntity player) {
                PlayerClass clazz = PLAYER_CLASSES.getPlayerClassFromUUID(player.getUuid());
                LOGGER.info("Player respawn event for " + player.getName().getString());
                clazz.onPlayerSet(player);
                return ActionResult.PASS;
            }
        });

        Recipes.registerRecipes();

        NumberProviderTypes.register();
        LootConditions.register();

        //Registry.register(Registry.ITEM, new Identifier(RpgMod.MODID, "test_item"), TEST_ITEM);
        //Registry.register(Registry.ITEM, new Identifier(RpgMod.MODID, "copper_pickaxe"), COPPER_PICKAXE);

        CauldronBehavior.WATER_CAULDRON_BEHAVIOR.put(Items.STICK, CRAFT_CAULDRON_BEHAIVIOR);
        CauldronBehavior.EMPTY_CAULDRON_BEHAVIOR.put(Items.STICK, CRAFT_CAULDRON_BEHAIVIOR);
        CauldronBehavior.LAVA_CAULDRON_BEHAVIOR.put(Items.BLAZE_ROD, CRAFT_CAULDRON_BEHAIVIOR);
        CauldronBehavior.POWDER_SNOW_CAULDRON_BEHAVIOR.put(Items.STICK, CRAFT_CAULDRON_BEHAIVIOR);

        CauldronBehavior b = CauldronBehavior.WATER_CAULDRON_BEHAVIOR.get(Items.STICK);
        LOGGER.info("Behavior:" + b);
        if (b == null) {
            throw new NullPointerException("Cauldron behavior is null!");
        }

        LOGGER.info(SkillTypes.CRAFTING);

    }
}
