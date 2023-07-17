package cn.jing2hang.zjfbsrvutils;

import cn.jing2hang.zjfbsrvutils.posyp.allpos;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.logging.LogUtils;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
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
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TextColor;
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
public class ZjServerUtils implements ModInitializer {
    LuckPerms lpapi;
    public static HashMap<String,allpos>TspcPos; // Temp spectator Original Position
    public static HashMap<String,String>CCmdMap; // Custom Command Maps
    Style REDWARN,INFO,HELP;
    private final String opHelpStr="  ! TIP: command is with no prefix '/'\n  > /qed save      -- Trigger save maps to file.\n  > /qed loadconf -- Load and overwrite current aliases.\n  > /qed add <alias> <command: -- Add alias -> command.\n  > /qed set <alias> <command: -- Re set the alias to command.\n  > /qed rm <alias> -- Remove the alias.";
    @Override
    public void onInitialize() {
        lpapi= LuckPermsProvider.get();
        REDWARN=Style.EMPTY.withColor(TextColor.parse("red"));
        INFO=Style.EMPTY.withColor(0x66ccff);
        HELP=Style.EMPTY.withColor(0x666666);
        //REDWARN.color
        TspcPos=new HashMap<String,allpos>();
        CCmdMap=ConfigUtil.LoadConf();
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(literal("where")
        .then(argument("target", EntityArgumentType.player())
        .executes((context -> {
            PlayerEntity targ=EntityArgumentType.getPlayer(context,"target");
            Vec3d tpos=targ.getPos();
            context.getSource().sendMessage(Text.literal("FOUND PLAYER AT: "+(int)tpos.x+","+(int)tpos.y+","+(int)tpos.z).setStyle(INFO));
            return 1;
        })))
        ));
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(literal("p")
                .executes((ctx->{
                    ServerPlayerEntity pl=ctx.getSource().getPlayer();
                    ///
                    assert pl != null;
                    if(TspcPos.containsKey(pl.getUuidAsString())){
                        pl.sendMessage(Text.literal("You already used /p").setStyle(REDWARN));
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
                    pl.sendMessage(Text.literal("Entering temporary SPECTATOR mode.").setStyle(INFO));
                    return 1;
                }))));
        //
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(literal("s")
                .executes((ctx->{
                    ServerPlayerEntity pl=ctx.getSource().getPlayer();
                    ///
                    assert pl != null;
                    if(!TspcPos.containsKey(pl.getUuidAsString())){
                        pl.sendMessage(Text.literal("You haven't used /p").setStyle(REDWARN));
                        return -1;
                    }
                    allpos p=TspcPos.get(pl.getUuidAsString());
                    pl.teleport(pl.getServerWorld(),p.x,p.y,p.z,p.yaw,p.pitch);
                    pl.sendMessage(Text.literal("Teleported back to ("+(int)p.x+","+(int)p.y+","+(int)p.z+")").setStyle(INFO));
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
                            pl.sendMessage(Text.literal("The quick command "+cmdals+" did not set!").setStyle(REDWARN));
                            return -1;
                        }
                        //ctx.getSource().exe
                        pl.sendMessage(Text.literal("Executing '"+CCmdMap.get(cmdals)+"' as you.").setStyle(INFO));
                        pl.getServer().getCommandManager().executeWithPrefix(ctx.getSource(),CCmdMap.get(cmdals));
                        return 1;
                    }))
                )
        ));
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> dispatcher.register(literal("qed")
                .then(literal("add")//.requires(src->src.hasPermissionLevel(4))
                    .then(argument("alias",StringArgumentType.string())
                    .then(argument("command",StringArgumentType.greedyString())
                        .executes((ctx->{
                            ServerCommandSource src=ctx.getSource();
                            String alias=StringArgumentType.getString(ctx,"alias");
                            String cmd=StringArgumentType.getString(ctx,"command");
                            if(CCmdMap.containsKey(alias)){
                                src.sendMessage(Text.literal("Alias '"+alias+"' already exists.").setStyle(REDWARN));
                                return -1;
                            }
                            CCmdMap.put(alias,cmd);
                            src.sendMessage(Text.literal("Added '"+alias+"' -> '/"+cmd+"'.").setStyle(INFO));
                            return 1;
                        }))
                    ))
                ).then(literal("set")//.requires(src->src.hasPermissionLevel(4))
                        .then(argument("alias",StringArgumentType.string())
                                .then(argument("command",StringArgumentType.greedyString())
                                        .executes((ctx->{
                                            ServerCommandSource src=ctx.getSource();
                                            String alias=StringArgumentType.getString(ctx,"alias");
                                            String cmd=StringArgumentType.getString(ctx,"command");
                                            if(!CCmdMap.containsKey(alias)){
                                                src.sendMessage(Text.literal("Alias '"+alias+"' did not exist.").setStyle(REDWARN));
                                                return -1;
                                            }
                                            CCmdMap.put(alias,cmd);
                                            src.sendMessage(Text.literal("Set '"+alias+"' -> '/"+cmd+"'.").setStyle(INFO));
                                            return 1;
                                        }))
                                ))
                ).then(literal("rm")//.requires(src->src.hasPermissionLevel(4))
                        .then(argument("alias",StringArgumentType.string())
                                    .executes((ctx->{
                                        ServerCommandSource src=ctx.getSource();
                                        String alias=StringArgumentType.getString(ctx,"alias");
                                        if(!CCmdMap.containsKey(alias)){
                                            src.sendMessage(Text.literal("Alias '"+alias+"' did not exist.").setStyle(REDWARN));
                                            return -1;
                                        }
                                        CCmdMap.remove(alias);
                                        src.sendMessage(Text.literal("Deleted alias '"+alias+"'.").setStyle(INFO));
                                        return 1;
                                    }))
                        )
                ).then(literal("list")
                    .executes((ctx->{
                        ctx.getSource().sendMessage(Text.literal("List of Quick Commands: "));
                        for(String alias:CCmdMap.keySet()){
                            ctx.getSource().sendMessage(Text.literal("- '"+alias+"' -> '/"+CCmdMap.get(alias)+"'"));
                        }
                        return 1;
                    }))
                ).then(literal("save")//.requires(src-> src.hasPermissionLevel(3))
                        .executes((ctx->{
                            ctx.getSource().sendMessage(Text.literal("Saving Command maps to json...").setStyle(INFO));
                            ConfigUtil.writeConf(CCmdMap);
                            return 1;
                        }))
                ).then(literal("loadconf")//.requires(src-> src.hasPermissionLevel(3))
                        .executes((ctx->{
                            ctx.getSource().sendMessage(Text.literal("Loading(Force) Command maps from json...").setStyle(INFO));
                            CCmdMap=ConfigUtil.LoadConf();
                            return 1;
                        }))
                ).then(literal("help")
                        .executes((ctx->{
                            ServerCommandSource src=ctx.getSource();
                            src.sendMessage(Text.literal("/qed: manage command aliases\n Help:").setStyle(HELP));
                            if(ctx.getSource().hasPermissionLevel(3)){
                                /*ctx.getSource()*/src.sendMessage(Text.literal(opHelpStr)/*.setStyle()*/.setStyle(HELP));
                            }
                            src.sendMessage(Text.literal("  > /qed list        -- List exist command aliases.\n  > /qed help       -- Show this help.").setStyle(HELP));
                            return 1;
                        }))
                )
        ));
        LogUtils.getLogger().info("Init done.");
    }
}
