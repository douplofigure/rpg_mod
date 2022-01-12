package douplo.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import douplo.skill.Skill;
import net.minecraft.command.EntitySelector;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static net.minecraft.server.command.CommandManager.literal;
import static net.minecraft.server.command.CommandManager.argument;

public class SkillCommand {

    public static void registerCommand(CommandDispatcher<ServerCommandSource> dispatcher) {

        LiteralArgumentBuilder skill = literal("skill");

        skill.then(literal("list").executes(SkillCommand::listSkills));

        LiteralArgumentBuilder get = literal("get");

        ArgumentBuilder skillName = argument("skillName", IdentifierArgumentType.identifier()).suggests(new SuggestionProvider<ServerCommandSource>() {
            @Override
            public CompletableFuture<Suggestions> getSuggestions(CommandContext<ServerCommandSource> context, SuggestionsBuilder builder) throws CommandSyntaxException {
                Set<Identifier> skills = Skill.listNamedSkills();
                for (Identifier id: skills) {
                    builder.suggest(id.toString());
                }
                return builder.buildFuture();
            }
        }).executes(new Command<ServerCommandSource>() {
            @Override
            public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
                ServerPlayerEntity player = context.getSource().getPlayer();
                Identifier id = context.getArgument("skillName", Identifier.class);
                double value = Skill.getFromId(id).getForPlayer(player);
                context.getSource().sendFeedback(Text.of("Skill level: " + value), false);
                return (int) value;
            }
        });

        ArgumentBuilder getPlayer = argument("player", EntityArgumentType.player()).executes(new Command<ServerCommandSource>() {
            @Override
            public int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
                EntitySelector selector = context.getArgument("player", EntitySelector.class);
                PlayerEntity player = selector.getPlayer(context.getSource());
                Identifier id = context.getArgument("skillName", Identifier.class);
                double value = Skill.getFromId(id).getForPlayer(player);
                context.getSource().sendFeedback(Text.of("Skill level: " + value), false);
                return (int) value;
            }
        });

        skillName.then(getPlayer);
        get.then(skillName);

        skill.then(get);

        dispatcher.register(skill);

    }

    private static int listSkills(CommandContext<ServerCommandSource> context) {

        Set<Identifier> skills = Skill.listNamedSkills();
        MutableText str = new LiteralText("Defined Skills: \n");
        for (Identifier id: skills) {
            str.append("\n").append(Skill.getFromId(id).getDisplayName()).append(new LiteralText("   (" + id.toString() + ")").getWithStyle(Style.EMPTY.withColor(0xcecece)).get(0));
        }
        context.getSource().sendFeedback(str, false);
        return 1;

    }

}
