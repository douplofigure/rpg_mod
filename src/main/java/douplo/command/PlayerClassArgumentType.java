package douplo.command;

import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import douplo.RpgMod;
import douplo.playerclass.PlayerClass;
import net.minecraft.util.Identifier;

import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public class PlayerClassArgumentType implements ArgumentType<PlayerClass> {

    @Override
    public PlayerClass parse(StringReader reader) throws CommandSyntaxException {
        Identifier id = new Identifier(reader.getString());
        RpgMod.LOGGER.info("Parsing command found id: " + id);
        return PlayerClass.getFromId(id);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        return ArgumentType.super.listSuggestions(context, builder);
    }

    @Override
    public Collection<String> getExamples() {
        return ArgumentType.super.getExamples();
    }

    public static PlayerClassArgumentType classes() {
        return new PlayerClassArgumentType();
    }

}
