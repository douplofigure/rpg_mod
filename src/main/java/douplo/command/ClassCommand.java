package douplo.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import douplo.RpgMod;
import douplo.playerclass.PlayerClass;
import douplo.skill.Skill;
import net.minecraft.command.EntitySelector;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.*;
import net.minecraft.util.Identifier;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import static net.minecraft.server.command.CommandManager.literal;
import static net.minecraft.server.command.CommandManager.argument;

public class ClassCommand {

    public static void registerClassCommand(CommandDispatcher<ServerCommandSource> dispatcher) {

        LiteralArgumentBuilder clazz = literal("class");

        {
            LiteralArgumentBuilder get = literal("get");

            ArgumentBuilder getPlayer = argument("player", EntityArgumentType.player()).executes(ClassCommand::getClassPlayer);
            get.then(getPlayer);
            clazz.then(get);
        }

        {
            LiteralArgumentBuilder set = literal("set");
            ArgumentBuilder setPlayer = argument("player", EntityArgumentType.players());
            ArgumentBuilder className = argument("className", IdentifierArgumentType.identifier()).suggests(ClassCommand::getClassSuggestions).executes(ClassCommand::setPlayerClass);

            setPlayer.then(className);
            set.then(setPlayer);
            clazz.then(set);
        }

        {

            LiteralArgumentBuilder describe = literal("describe");
            ArgumentBuilder className = argument("className", IdentifierArgumentType.identifier()).suggests(ClassCommand::getClassSuggestions).executes(ClassCommand::getClassDesciption);

            describe.then(className);
            clazz.then(describe);

        }

        dispatcher.register(clazz);

    }

    private static final int LIST_COLOR = 0xbfbfbf;
    private static final int HEADING_COLOR = 0xefefcf;

    private static int getClassDesciption(CommandContext<ServerCommandSource> context) {

        Identifier className = (Identifier) context.getArgument("className", Identifier.class);
        PlayerClass playerClass = PlayerClass.getFromId(className);

        MutableText text = new LiteralText("");
        text.append(playerClass.getDisplayName()).append(":\n\n");

        text.append(new LiteralText("Skills:").getWithStyle(Style.EMPTY.withColor(HEADING_COLOR)).get(0));
        for (Identifier s : Skill.listNamedSkills()) {
            double mult = playerClass.getSkillMultiplier(s) * 100;
            Text displayName = Skill.getFromId(s).getDisplayName();
            if (displayName != null) {
                text.append("\n").append(displayName.getWithStyle(Style.EMPTY.withColor(LIST_COLOR)).get(0)).append(": ").append(String.format("%.02f", mult)).append(" %");
            } else {
                Text name = new LiteralText(s.toString()).getWithStyle(Style.EMPTY.withColor(LIST_COLOR)).get(0);
                text.append("\n").append(name).append(": ").append(String.format("%.02f", mult)).append(" %");
            }
        }

        text.append(new LiteralText("\n\nAttributes:").getWithStyle(Style.EMPTY.withColor(HEADING_COLOR)).get(0));
        for (EntityAttribute attr : playerClass.getModifiedAttributes()) {
            double fac = (playerClass.getAttributeValue(attr) / (attr == EntityAttributes.GENERIC_MOVEMENT_SPEED ? 0.1 : attr.getDefaultValue())) * 100;
            String formatedFac = String.format("%.02f", fac);

            text.append("\n").append(new TranslatableText(attr.getTranslationKey()).getWithStyle(Style.EMPTY.withColor(LIST_COLOR)).get(0)).append(": ").append(formatedFac).append(" %");

        }

        context.getSource().sendFeedback(text, false);
        return 1;
    }

    private static CompletableFuture<Suggestions> getClassSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder suggestionsBuilder) {

        for (Identifier id : PlayerClass.getLoadedClassIds()) {
            suggestionsBuilder.suggest(id.toString());
        }

        return suggestionsBuilder.buildFuture();
    }

    private static int setPlayerClass(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        EntitySelector selector = (EntitySelector) context.getArgument("player", EntitySelector.class);
        List<ServerPlayerEntity> players = selector.getPlayers((ServerCommandSource) context.getSource());
        Identifier className = (Identifier) context.getArgument("className", Identifier.class);
        PlayerClass playerClass = PlayerClass.getFromId(className);
        if (playerClass == null) {
            RpgMod.LOGGER.error("Unknown class " + className);
            for (Identifier id : PlayerClass.getLoadedClassIds()) {
                RpgMod.LOGGER.info(id);
            }
            ((ServerCommandSource) context.getSource()).sendError(Text.of("Unknown class " + className));
            return 0;
        }
        for (PlayerEntity player : players) {
            RpgMod.PLAYER_CLASSES.setPlayerClass(player, playerClass);
            ((ServerCommandSource) context.getSource()).sendFeedback(Text.of("Set class to " + className.toString()), true);
        }
        return 1;
    }

    private static int getClassPlayer(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        EntitySelector selector = (EntitySelector) context.getArgument("player", EntitySelector.class);
        PlayerEntity player = selector.getPlayer(context.getSource());
        PlayerClass clazz = RpgMod.PLAYER_CLASSES.getPlayerClassFromUUID(player.getUuid());
        if (clazz != null)
            context.getSource().sendFeedback(clazz.getDisplayName(), false);
        else
            context.getSource().sendFeedback(Text.of("No class found"), false);

        return 1;
    }

}
