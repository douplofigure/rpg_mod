package douplo.command;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.ServerCommandSource;


public class Commands {


    public static void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher, boolean dedicated) {

        ClassCommand.registerClassCommand(dispatcher);
        SkillCommand.registerCommand(dispatcher);

    }

}
