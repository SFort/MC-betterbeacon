package tf.ssf.sfort.betterbeacon.mixin;

import net.minecraft.block.Block;
import net.minecraft.block.entity.ConduitBlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tf.ssf.sfort.betterbeacon.Config;

import java.util.Arrays;
import java.util.List;

@Mixin(value = ConduitBlockEntity.class,priority = 4101)
public class Conduit {
    @Mutable @Final @Shadow
    private static Block[] ACTIVATING_BLOCKS;

    @Inject(method = "<clinit>", at=@At("TAIL"))
    private static void updateBlocks(CallbackInfo ci){
        if (Config.to_add.size()>0){
            Block[] update = Arrays.copyOf(ACTIVATING_BLOCKS, ACTIVATING_BLOCKS.length+Config.to_add.size());
            System.arraycopy(Config.to_add.toArray(Block[]::new),0, update, ACTIVATING_BLOCKS.length, Config.to_add.size());
            ACTIVATING_BLOCKS=update;
        }
    }
    @ModifyVariable(method = "givePlayersEffects(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Ljava/util/List;)V",
            at=@At(value = "STORE"),ordinal = 1)
    private static int changeRange(int i, World world, BlockPos pos, List<BlockPos> activatingBlocks){
        if(Config.reset)i=0;
        for(BlockPos p : activatingBlocks)
            i+=Config.conduit_additive.getOrDefault(world.getBlockState(p).getBlock(), 0.0);
        return i;
    }
}