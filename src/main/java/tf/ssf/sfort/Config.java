package tf.ssf.sfort;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Config implements ModInitializer {
	public static Logger LOGGER = LogManager.getLogger();
	public static Map<Block, Double> additive = new LinkedHashMap<>();
	public static double add = 10.0;
	public static double lvl_mul = 10.0;
	@Override
	public void onInitialize() {
		File confFile = new File(
			FabricLoader.getInstance().getConfigDir().toString(),
			"BetterBeacons.conf"
	);
		try {
			confFile.createNewFile();
			List<String> la = Files.readAllLines(confFile.toPath());
			List<String> defaultDesc = Arrays.asList(
					"^-Default range  [10.0] 0.0 - ...",
					"^-Added range per level  [10.0] 0.0 - ...",
					"^-Added range per block of type [minecraft:iron_block;0.0;minecraft:gold_block;0.0;minecraft:diamond_block;1.0;minecraft:emerald_block;1.0;minecraft:netherite_block;2.0] ID;AMOUNT;..."
			);
			String[] ls = la.toArray(new String[Math.max(la.size(), defaultDesc.size() * 2)|1]);
			int hash = Arrays.hashCode(ls);
			for (int i = 0; i<defaultDesc.size();++i)
				ls[i*2+1]= defaultDesc.get(i);

			try{ add = Double.parseDouble(ls[0]);}catch (Exception ignore){}
			ls[0] = String.valueOf(add);
			try{ lvl_mul = Double.parseDouble(ls[2]);}catch (Exception ignore){}
			ls[2] = String.valueOf(lvl_mul);
			try{
			String[] in = ls[4].split("\\s*;\\s*");
			for (int i =0; i<in.length/2;++i) {
				try{additive.put(Registry.BLOCK.get(new Identifier(in[i])),Double.parseDouble(in[i+1]));}catch (Exception ignore){}
			}}catch (Exception ignore){}
			ls[4] = additive.keySet().stream().map(key -> Registry.BLOCK.getId(key).toString()+";"+additive.get(key).toString()).collect(Collectors.joining(";"));

			if(hash != Arrays.hashCode(ls))
				Files.write(confFile.toPath(), Arrays.asList(ls));
			LOGGER.log(Level.INFO,"tf.ssf.sfort.beaconbalance successfully loaded config file");
		} catch(Exception e) {
			LOGGER.log(Level.ERROR,"tf.ssf.sfort.beaconbalance failed to load config file, using defaults\n"+e);
		}
	}
	static {
		additive.put(Blocks.IRON_BLOCK,0.0);
		additive.put(Blocks.GOLD_BLOCK,0.0);
		additive.put(Blocks.DIAMOND_BLOCK,1.0);
		additive.put(Blocks.EMERALD_BLOCK,1.0);
		additive.put(Blocks.NETHERITE_BLOCK,2.0);
	}
}
