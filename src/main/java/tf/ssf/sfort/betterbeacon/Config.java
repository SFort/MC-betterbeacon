package tf.ssf.sfort.betterbeacon;

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
import java.util.*;
import java.util.stream.Collectors;

public class Config implements ModInitializer {
	public static Logger LOGGER = LogManager.getLogger();
	public static final String MOD_ID = "tf.ssf.sfort.betterbeacon";
	public static Map<Block, Double> beacon_additive = new HashMap<>();
	public static Map<Block, Double> conduit_additive = new HashMap<>();
	public static boolean keep_vanilla = true;
	public static Set<Block> to_add = new HashSet<>();
	public static double add = 10.0;
	public static double lvl_mul = 10.0;
	@Override
	public void onInitialize() {
		File confFile = new File(
			FabricLoader.getInstance().getConfigDir().toString(),
			"BetterBeacons.conf"
	);
		try {
			//TODO move to mixin system so each class can be unloaded
			boolean existing = !confFile.createNewFile();
			List<String> la = Files.readAllLines(confFile.toPath());
			List<String> defaultDesc = Arrays.asList(
					"^-Beacon: Default range  [10.0] 0.0 - ...",
					"^-Beacon: Added range per level  [10.0] 0.0 - ...",
					"^-Beacon: Added range per block of type [minecraft:iron_block;0.0;minecraft:gold_block;0.0;minecraft:diamond_block;0.5;minecraft:emerald_block;0.5;minecraft:netherite_block;2.0] ID;AMOUNT;...",
					"^-Conduit: Add vanilla range [true] true | false",
					"^-Conduit: Added range per block of type [] ID;AMOUNT;..."
			);
			String[] ls = la.toArray(new String[Math.max(la.size(), defaultDesc.size() * 2)|1]);
			int hash = Arrays.hashCode(ls);
			for (int i = 0; i<defaultDesc.size();++i)
				ls[i*2+1]= defaultDesc.get(i);

			int i = 0;
			try{ add = Double.parseDouble(ls[i]);}catch (Exception e){if(existing)LOGGER.log(Level.WARN, MOD_ID +" #"+i+"\n"+e);}
			ls[i] = String.valueOf(add);
			i+=2;
			try{ lvl_mul = Double.parseDouble(ls[i]);}catch (Exception e){if(existing)LOGGER.log(Level.WARN, MOD_ID +" #"+i+"\n"+e);}
			ls[i] = String.valueOf(lvl_mul);
			i+=2;
			try{
			String[] in = ls[i].split("\\s*;\\s*");
			for (int j =0; j<in.length/2;++j) {
				try{
					beacon_additive.put(Registry.BLOCK.get(new Identifier(in[j*2])),Double.parseDouble(in[j*2+1]));}catch (Exception e){if(existing)LOGGER.log(Level.WARN, MOD_ID +" #"+i+"\n"+e);}
			}}catch (Exception e){if(existing)LOGGER.log(Level.WARN, MOD_ID +" #"+i+"\n"+e);}
			ls[i] = beacon_additive.keySet().stream().map(key -> Registry.BLOCK.getId(key)+";"+ beacon_additive.get(key).toString()).collect(Collectors.joining(";"));
			i+=2;
			try {
				if(ls[i] != null)
				keep_vanilla = Boolean.parseBoolean(ls[i]);
			}catch (Exception e){if(existing)LOGGER.log(Level.WARN, MOD_ID +" #"+i+"\n"+e);}
			ls[i] = String.valueOf(keep_vanilla);
			i+=2;
			try{
				String[] in = ls[i].split("\\s*;\\s*");
				for (int j =0; j<in.length/2;++j) {
					try{
						Block id = Registry.BLOCK.get(new Identifier(in[j*2]));
						conduit_additive.put(id,Double.parseDouble(in[j*2+1]));
						to_add.add(id);
					}catch (Exception e){if(existing)LOGGER.log(Level.WARN, MOD_ID +" #"+i+"\n"+e);}
				}}catch (Exception e){if(existing)LOGGER.log(Level.WARN, MOD_ID +" #"+i+"\n"+e);}
			ls[i] = conduit_additive.keySet().stream().map(key -> Registry.BLOCK.getId(key) +";"+ conduit_additive.get(key).toString()).collect(Collectors.joining(";"));


			if(hash != Arrays.hashCode(ls))
				Files.write(confFile.toPath(), Arrays.asList(ls));
			LOGGER.log(Level.INFO,"tf.ssf.sfort.beaconbalance successfully loaded config file");
		} catch(Exception e) {
			LOGGER.log(Level.ERROR,"tf.ssf.sfort.beaconbalance failed to load config file, using defaults\n"+e);
		}
	}
	static {
		beacon_additive.put(Blocks.IRON_BLOCK,0.0);
		beacon_additive.put(Blocks.GOLD_BLOCK,0.0);
		beacon_additive.put(Blocks.DIAMOND_BLOCK,0.5);
		beacon_additive.put(Blocks.EMERALD_BLOCK,0.5);
		beacon_additive.put(Blocks.NETHERITE_BLOCK,2.0);
	}
}
