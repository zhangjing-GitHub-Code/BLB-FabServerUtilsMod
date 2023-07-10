package cn.jing2hang.zjfbsrvutils;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralTextContent;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

import static net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.getServer;
import static net.minecraft.server.command.CommandManager.*;

import net.minecraft.world.World;
import org.joml.Vector3d;
import org.joml.Vector3f;

import java.util.Set;

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
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(literal("where")
        //.then(argument("playerName",StringArgumentType.greedyString()))
        .then(argument("target", EntityArgumentType.player())
        .executes((context -> {
            //if(context.getSource()==p)
            /*final PlayerEntity src=context.getSource().getPlayer();
            Vec3d spos=src.getPos();*/
            //Text tgp=new Text();
            //String tgPlayerName=tgp.togStrin();
            PlayerEntity targ=EntityArgumentType.getPlayer(context,"target");
            /*String TGPlayerName=null; // PLH
            TGPlayerName=StringArgumentType.getString(context,"playerName");
            MinecraftServer mcs=context.getSource().getServer();
            PlayerManager pm= mcs.getPlayerManager();
            /*Set<RegistryKey<World>> rkeys=mcs.getWorldRegistryKeys();
            for(RegistryKey<World> rkey:rkeys){
                mcs.getWorlds()
            }*\/
            //for(ServerWorld wl:mcs.getWorlds()){
                for(ServerPlayerEntity ply_ent:pm.getPlayerList()){
                    if(ply_ent.getName().toString().equals(TGPlayerName)){
                        // FOUND same name
                        Vec3d tpos=ply_ent.getPos();*/
            Vec3d tpos=targ.getPos();
                        //context.getSource().sendFeedback(Text.s("找到玩家"),false);
            context.getSource().sendMessage(Text.literal("FOUND PLAYER AT: "+(int)tpos.x+","+(int)tpos.y+","+(int)tpos.z));
            return 1;
                        /*
                    }
                }*/
            //}
            //context.getSource().sendMessage(literal("玩家调用/whereami，你的位置是"+spos.x+","+spos.y+","+spos.z));
            //context.getSource().sendMessage(Text.literal("PLAYER with given name not found."));
            //return -1;
        })))
        ));
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(literal("p")
                .executes((ctx->{
                    return 0;
                }))));
    }
}
