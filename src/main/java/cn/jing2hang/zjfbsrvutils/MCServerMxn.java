package cn.jing2hang.zjfbsrvutils;

import cn.jing2hang.zjfbsrvutils.posyp.allpos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.world.GameMode;
import org.apache.logging.log4j.spi.LoggerRegistry;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import cn.jing2hang.zjfbsrvutils.ZjServerUtils.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public abstract class MCServerMxn {
    //@Inject(at = @At("HEAD"), method = "stop(Z)V")
    //@reason fuck
    @Shadow
    boolean running;

    @Shadow public abstract PlayerManager getPlayerManager();

    @Shadow public abstract void sendMessage(Text message);
    @Shadow
    Thread serverThread;
    @Shadow
    static
    Logger LOGGER;

    /**
     * Stop the server
     * @author jing_2hang1105
     * @reason Need fork stop event but not found
     * //@param waitForShutdown block until shutdown
     */
    @Inject(method="shutdown",at=@At("HEAD"))
    public void mixShutdown(CallbackInfo ci){
        for(String UUIDs:ZjServerUtils.TspcPos.keySet()){
            ServerPlayerEntity pl=/*getPlayerFromUUID();*/this.getPlayerManager().getPlayer(UUIDs);
            if(pl==null)continue; // Offline Already?
            allpos p=ZjServerUtils.TspcPos.get(UUIDs);
            //pl.setPosition(orig);

            pl.teleport(pl.getServerWorld(),p.x,p.y,p.z,p.yaw,p.pitch);
            pl.sendMessage(Text.literal("Teleported back as Srv stoppping to ("+(int)p.x+","+(int)p.y+","+(int)p.z+")"));
            ZjServerUtils.TspcPos.remove(pl.getUuidAsString());
            pl.changeGameMode(GameMode.SURVIVAL);
        }
        LOGGER.info("Tp back spec players done.");
        ConfigUtil.writeConf(ZjServerUtils.CCmdMap);
        LOGGER.info("Saved custom commands.");
        // Below Original Function
        //this.running = false;
        /*if (waitForShutdown) {
            try {
                this.serverThread.join();
            } catch (InterruptedException var3) {
                LOGGER.error("Error while shutting down", var3);
            }
        }*/
    }
}
