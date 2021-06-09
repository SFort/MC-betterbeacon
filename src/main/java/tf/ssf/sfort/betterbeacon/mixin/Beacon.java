package tf.ssf.sfort.betterbeacon.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BeaconBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Interface;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tf.ssf.sfort.betterbeacon.BeaconAccessor;
import tf.ssf.sfort.betterbeacon.Config;

@Mixin(value = BeaconBlockEntity.class,priority = 4101)
public class Beacon extends BlockEntity implements BeaconAccessor{
	@Dynamic double range = 0.0;
	@Shadow int level;

	public Beacon(BlockPos pos, BlockState state) {
		super(BlockEntityType.BEACON, pos, state);
	}

	@Inject(method = "updateLevel(Lnet/minecraft/world/World;III)I", at=@At("HEAD"))
	private static void updateLevel(World world, int x, int y, int z, CallbackInfoReturnable<Integer> cir) {
		BlockEntity entity = world.getBlockEntity(new BlockPos(x,y,z));
		if (entity instanceof BeaconBlockEntity) {
			((BeaconAccessor) entity).resetRange();
			for(int i = 1; i <= 4; i++) {
				int j = y - i;
				if (j < 0) break;
				for(int k = x - i; k <= x + i; ++k)
					for(int l = z - i; l <= z + i; ++l)
						((BeaconAccessor) entity).addRange(Config.additive.getOrDefault(world.getBlockState(new BlockPos(k, j, l)).getBlock(),0.0));
			}
		}
	}

	@ModifyVariable(method = "applyPlayerEffects(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;ILnet/minecraft/entity/effect/StatusEffect;Lnet/minecraft/entity/effect/StatusEffect;)V",at=@At(value = "STORE", ordinal = 0))
	private static double d(double d, World world, BlockPos pos, int beaconLevel){
		BlockEntity entity = world.getBlockEntity(pos);
		if (entity instanceof BeaconBlockEntity)
			return ((BeaconAccessor) entity).getRange()+Config.lvl_mul*beaconLevel;
		return d;
	}

	@Override
	public void addRange(double d) {
		range+=d;
	}
	@Override
	public void resetRange() {
		range=Config.add;
	}
	@Override
	public double getRange() {
		return range;
	}
}
