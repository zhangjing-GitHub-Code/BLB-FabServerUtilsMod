package cn.jing2hang.zjfbsrvutils;

import cn.jing2hang.zjfbsrvutils.posyp.allpos;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.LiteralTextContent;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

import static cn.jing2hang.zjfbsrvutils.ConfigUtil.*;
import static net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.*;
import static net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking.getServer;
import static net.minecraft.server.command.CommandManager.*;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents.ServerStopping;
import net.minecraft.world.GameMode;
import net.minecraft.world.World;
import org.apache.logging.log4j.core.jmx.Server;
import org.joml.Vector3d;
import org.joml.Vector3f;

import java.io.ObjectInputFilter;
import java.util.HashMap;
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
    public static HashMap<String,allpos>TspcPos;
    public static HashMap<String,String>CCmdMap;
    @Override
    public void onInitialize() {
        TspcPos=new HashMap<String,allpos>();
        CCmdMap=ConfigUtil.LoadConf();
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
                    ServerPlayerEntity pl=ctx.getSource().getPlayer();
                    ///
                    assert pl != null;
                    if(TspcPos.containsKey(pl.getUuidAsString())){
                        pl.sendMessage(Text.literal("You already used /p"));
                        return -1;
                    }
                    //pl.getPit
                    Vec3d tmp3d=pl.getPos();
                    allpos newpos=new allpos();
                    newpos.x= tmp3d.x;
                    newpos.y= tmp3d.y;
                    newpos.z= tmp3d.z;
                    newpos.yaw= pl.getYaw();
                    newpos.pitch= pl.getPitch();
                    TspcPos.put(pl.getUuidAsString(),newpos);
                    pl.changeGameMode(GameMode.SPECTATOR);
                    pl.sendMessage(Text.literal("Entering temporary SPECTATOR mode."));
                    return 1;
                }))));
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(literal("s")
                .executes((ctx->{
                    ServerPlayerEntity pl=ctx.getSource().getPlayer();
                    ///
                    assert pl != null;
                    if(!TspcPos.containsKey(pl.getUuidAsString())){
                        pl.sendMessage(Text.literal("You haven't used /p"));
                        return -1;
                    }
                    allpos p=TspcPos.get(pl.getUuidAsString());
                    //pl.setPosition(orig);
                    pl.teleport(pl.getServerWorld(),p.x,p.y,p.z,p.yaw,p.pitch);
                    pl.sendMessage(Text.literal("Teleported back to ("+(int)p.x+","+(int)p.y+","+(int)p.z+")"));
                    TspcPos.remove(pl.getUuidAsString());
                    pl.changeGameMode(GameMode.SURVIVAL);
                    return 1;
                }))));
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(literal("qr")
                .then(argument("Alias",StringArgumentType.greedyString())
                    .executes((ctx->{
                        ServerPlayerEntity pl=ctx.getSource().getPlayer();
                    ///
                        assert pl != null;
                        String cmdals=StringArgumentType.getString(ctx,"Alias");;
                        if(!CCmdMap.containsKey(cmdals)){
                            pl.sendMessage(Text.literal("The quick command "+cmdals+" did not set!"));
                            return -1;
                        }
                        //ctx.getSource().exe
                        pl.getServer().getCommandManager().executeWithPrefix(ctx.getSource(),CCmdMap.get(cmdals));
                        return 1;
                    }))
                )
        ));
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(literal("qed")
                .then(literal("add").requires(src->src.hasPermissionLevel(4))
                    .then(argument("alias",StringArgumentType.string())
                    .then(argument("command",StringArgumentType.greedyString())
                        .executes((ctx->{
                            //ServerPlayerEntity pl=ctx.getSource().getPlayer();
                            ///
                            //assert pl != null;
                            //if(!CCmdMap.containsKey(ctx.getCommand().toString())){
                                //pl.sendMessage(Text.literal("The quick command has not set!"));
                                //return -1;
                            //}
                            //pl.getServer().getCommandManager().execute(ctx.getSource(),"");
                            ServerCommandSource src=ctx.getSource();
                            String alias=StringArgumentType.getString(ctx,"alias");
                            String cmd=StringArgumentType.getString(ctx,"command");
                            if(CCmdMap.containsKey(alias)){
                                src.sendMessage(Text.literal("Alias '"+alias+"' already exists."));
                                return -1;
                            }
                            CCmdMap.put(alias,cmd);
                            src.sendMessage(Text.literal("Added '"+alias+"' -> '/"+cmd+"'."));
                            return 1;
                        }))
                    ))
                ).then(literal("list")
                    .executes((ctx->{
                        ctx.getSource().sendMessage(Text.literal("List of Quick Commands: "));
                        for(String alias:CCmdMap.keySet()){
                            ctx.getSource().sendMessage(Text.literal("- '"+alias+"' -> '/"+CCmdMap.get(alias)+"'"));
                        }
                        return 1;
                    }))
                ).then(literal("save").requires(src-> src.hasPermissionLevel(3))
                        .executes((ctx->{
                            ctx.getSource().sendMessage(Text.literal("Saving Command maps to json..."));
                            ConfigUtil.writeConf(CCmdMap);
                            return 1;
                        }))
                )
        ));
    }

    //ServerLifecycleEvents.ServerStopping.
    //ServerStopCallback.EVENT;
    //ServerStopping.register();
    //ServerLifecycleEvents;
    //ServerLifecycleEvents.SERVER_STOPPING.register();
}
