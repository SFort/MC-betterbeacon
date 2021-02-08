package tf.ssf.sfort.mixin;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ConcretePowderBlock;
import net.minecraft.block.entity.BeaconBlockEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.PlayerEntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.tag.BlockTags;
import net.minecraft.tag.Tag;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tf.ssf.sfort.Config;

@Mixin(value = BeaconBlockEntity.class,priority = 4101)
public class Beacon extends BlockEntity{
	@Dynamic double range = 0.0;
	@Shadow private int level;

	public Beacon(BlockEntityType<?> type) {
		super(type);
	}

	//This does make the beacon check twice as process intensive so that's something.
	@Inject(method = "updateLevel", at=@At("HEAD"),cancellable = true)
	private void updateLevel(int x, int y, int z, CallbackInfo info) {
		range=Config.add;
		for(int i = 1; i <= 4; i++) {
			int j = y - i;
			if (j < 0) { break; }
			for(int k = x - i; k <= x + i; ++k)
				for(int l = z - i; l <= z + i; ++l)
					range+= Config.additive.getOrDefault(this.world.getBlockState(new BlockPos(k, j, l)).getBlock(),0.0);
		}
	}
	/*
	No clue what i fucked up with redirects again so gonna use a copy paste inject

	@Redirect(method = "updateLevel(III)V",at=@At(value = "INVOKE", target = "Lnet/minecraft/block/AbstractBlock$AbstractBlockState;isIn(Lnet/minecraft/tag/Tag;)Z"))
	private boolean isIn(AbstractBlock.AbstractBlockState state, Tag<Block> tag){
		range+= Config.additive.getOrDefault(state.getBlock(),0.0);
		return state.isIn(tag);
	}
	*/
	@ModifyVariable(method = "applyPlayerEffects",at=@At(value = "STORE", ordinal = 0))
	private double d(double in){
		return range+Config.lvl_mul*level;
	}
}
