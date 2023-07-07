package cn.jing2hang.zjfbsrvutils;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import static net.minecraft.server.command.CommandManager.*;
import org.joml.Vector3d;
import org.joml.Vector3f;
import static com.mojang.brigadier.arguments.StringArgumentType.getString;
// word()
import static com.mojang.brigadier.arguments.StringArgumentType.word;
// literal("foo")
import static net.minecraft.server.command.CommandManager.literal;
// argument("bar", word())
import static net.minecraft.server.command.CommandManager.argument;
// Import everything in the CommandManager
import static net.minecraft.server.command.CommandManager.*;
//import static net.minecraft.server.command.CommandManager.*;
public class ZjServerUtils implements ModInitializer {
    @Override
    public void onInitialize() {
    CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(literal("whereami")
        .executes(context -> {
            //if(context.getSource()==p)
            final PlayerEntity src=context.getSource().getPlayer();
            Vec3d spos=src.getPos();
            context.getSource().sendMessage(literal("玩家调用/whereami，你的位置是"+spos.x+","+spos.y+","+spos.z));
                return 1;
        })));
}
}
