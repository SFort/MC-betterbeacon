package tf.ssf.sfort.betterbeacon.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BeaconBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tf.ssf.sfort.betterbeacon.Config;

@Mixin(value = BeaconBlockEntity.class,priority = 4101)
public class Beacon extends BlockEntity{
	@Dynamic double range = 0.0;
	@Shadow private int level;

	public Beacon(BlockEntityType<?> type) {
		super(type);
	}

	@Inject(method = "updateLevel(III)V", at=@At("HEAD"),cancellable = true)
	private void updateLevel(int x, int y, int z, CallbackInfo info) {
		range=Config.add;
	}

	@Redirect(method = "updateLevel(III)V",at=@At(value = "INVOKE", target = "Lnet/minecraft/world/World;getBlockState(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/BlockState;"))
	private BlockState getBlockState(World world, BlockPos pos){
		BlockState state = world.getBlockState(pos);
		range+= Config.additive.getOrDefault(state.getBlock(),0.0);
		return state;
	}

	@ModifyVariable(method = "applyPlayerEffects()V",at=@At(value = "STORE", ordinal = 0))
	private double d(double in){
		return range+Config.lvl_mul*level;
	}
}
