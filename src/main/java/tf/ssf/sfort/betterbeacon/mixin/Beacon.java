package tf.ssf.sfort.betterbeacon.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BeaconBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import tf.ssf.sfort.betterbeacon.BeaconAccessor;
import tf.ssf.sfort.betterbeacon.Config;

import java.util.HashMap;
import java.util.Map;

@Mixin(value = BeaconBlockEntity.class,priority = 4101)
public class Beacon extends BlockEntity implements BeaconAccessor{
	private static final Map<Vec3i, BeaconAccessor> betterbeacon$updatingBeacons = new HashMap<>();
	double betterbeacon$range = 0.0;

	public Beacon(BlockPos pos, BlockState state) {
		super(BlockEntityType.BEACON, pos, state);
	}

	@Inject(method="updateLevel(Lnet/minecraft/world/World;III)I", at=@At(value="HEAD"))
	private static void addUpdate(World world, int x, int y, int z, CallbackInfoReturnable<Integer> cir) {
		BlockEntity entity = world.getBlockEntity(new BlockPos(x,y,z));
		if (entity instanceof BeaconAccessor e) {
			e.resetRange();
			betterbeacon$updatingBeacons.put(new Vec3i(x, y, z), e);
		}
	}
	@Inject(method="updateLevel(Lnet/minecraft/world/World;III)I", at=@At(value="TAIL"))
	private static void removeUpdate(World world, int x, int y, int z, CallbackInfoReturnable<Integer> cir) {
		betterbeacon$updatingBeacons.remove(new Vec3i(x, y, z));
	}
	@Inject(method = "updateLevel(Lnet/minecraft/world/World;III)I", locals=LocalCapture.CAPTURE_FAILHARD, at=@At(value="INVOKE", target="Lnet/minecraft/world/World;getBlockState(Lnet/minecraft/util/math/BlockPos;)Lnet/minecraft/block/BlockState;"))
	private static void updateLevel(World world, int x, int y, int z, CallbackInfoReturnable<Integer> cir, int i, int j, int k, boolean bl, int l, int m) {
		BeaconAccessor entity = betterbeacon$updatingBeacons.get(new Vec3i(x, y, z));
		if (entity != null) {
			entity.addRange(Config.beacon_additive.getOrDefault(world.getBlockState(new BlockPos(l, k, m)).getBlock(),0.0));
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
		betterbeacon$range+=d;
	}
	@Override
	public void resetRange() {
		betterbeacon$range=Config.add;
	}
	@Override
	public double getRange() {
		return betterbeacon$range;
	}
}
