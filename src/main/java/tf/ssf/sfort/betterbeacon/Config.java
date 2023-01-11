package tf.ssf.sfort.betterbeacon;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.Identifier;
import net.minecraft.registry.Registries;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import tf.ssf.sfort.ini.SFIni;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;
import java.util.function.Consumer;

public class Config implements ModInitializer {
	public static Logger LOGGER = LogManager.getLogger();
	public static final String MOD_ID = "tf.ssf.sfort.betterbeacon";
	public static Map<Block, Double> beacon_additive = new HashMap<>();
	static {
		beacon_additive.put(Blocks.IRON_BLOCK,0.0);
		beacon_additive.put(Blocks.GOLD_BLOCK,0.0);
		beacon_additive.put(Blocks.DIAMOND_BLOCK,0.5);
		beacon_additive.put(Blocks.EMERALD_BLOCK,0.5);
		beacon_additive.put(Blocks.NETHERITE_BLOCK,2.0);
	}
	public static Map<Block, Double> conduit_additive = new HashMap<>();
	public static boolean keep_vanilla = true;
	public static Set<Block> to_add = new HashSet<>();
	public static double add = 10.0;
	public static double lvl_mul = 10.0;
	@Override
	public void onInitialize() {
		SFIni defIni = new SFIni();
		defIni.load(String.join("\n", new String[]{
				"; Beacon: Default range  [10.0] 0.0+",
				"beacon.rangeBase=10.0",
				"; Beacon: Added range per level  [10.0] 0.0+",
				"beacon.rangePerLevel=10.0",
				"; Beacon: Added range per block of type",
				"; [minecraft:iron_block;0.0, minecraft:gold_block;0.0, minecraft:diamond_block;0.5, minecraft:emerald_block;0.5, minecraft:netherite_block;2.0]",
				"; {ID;AMOUNT}",
				"beacon.rangePerBlock=minecraft:iron_block;0.0",
				".=minecraft:gold_block;0.0",
				".=minecraft:diamond_block;0.5",
				".=minecraft:emerald_block;0.5",
				".=minecraft:netherite_block;2.0",
				"; Conduit: Add vanilla range [true] true | false",
				"conduit.addVanillaRange=true",
				"; Conduit: Added range per block of type [] {ID;AMOUNT}",
				"conduit.rangePerBlock=",

		}));
		ConfigLegacy.loadLegacy(defIni);
		File confFile = new File(
			FabricLoader.getInstance().getConfigDir().toString(),
			"BetterBeacons.sf.ini"
		);
		if (!confFile.exists()) {
			try {
				Files.write(confFile.toPath(), defIni.toString().getBytes());
				LOGGER.log(Level.INFO,MOD_ID+" successfully created config file");
				loadIni(defIni);
			} catch (IOException e) {
				LOGGER.log(Level.ERROR,MOD_ID+" failed to create config file, using defaults", e);
			}
			return;
		}
		try {
			SFIni ini = new SFIni();
			String text = Files.readString(confFile.toPath());
			int hash = text.hashCode();
			ini.load(text);
			for (Map.Entry<String, List<SFIni.Data>> entry : defIni.data.entrySet()) {
				List<SFIni.Data> list = ini.data.get(entry.getKey());
				if (list == null || list.isEmpty()) {
					ini.data.put(entry.getKey(), entry.getValue());
				} else {
					list.get(0).comments = entry.getValue().get(0).comments;
				}
			}
			loadIni(ini);
			String iniStr = ini.toString();
			if (hash != iniStr.hashCode()) {
				Files.write(confFile.toPath(), iniStr.getBytes());
			}
		} catch (IOException e) {
			LOGGER.log(Level.ERROR,MOD_ID+" failed to load config file, using defaults", e);
		}

	}

	public static void setOrResetBool(SFIni ini, String key, Consumer<Boolean> set, boolean bool) {
		try {
			set.accept(ini.getBoolean(key));
		} catch (Exception e) {
			SFIni.Data data = ini.getLastData(key);
			if (data != null) data.val = Boolean.toString(bool);
			LOGGER.log(Level.ERROR,MOD_ID+" failed to load "+key+", setting to default value", e);
		}
	}
	public static void setOrResetDouble(SFIni ini, String key, Consumer<Double> set, double bool) {
		try {
			set.accept(ini.getDouble(key));
		} catch (Exception e) {
			SFIni.Data data = ini.getLastData(key);
			if (data != null) data.val = Double.toString(bool);
			LOGGER.log(Level.ERROR,MOD_ID+" failed to load "+key+", setting to default value", e);
		}
	}
	public static void fillBlockRangeMap(SFIni ini, String key, Map<Block, Double> map) {
		List<SFIni.Data> list = ini.data.get(key);
		map.clear();
		if (list == null) return;
		int i=0;
		while (i<list.size()) {
			String val = list.get(i).val;
			try {
				int index = val.lastIndexOf(';');
				if (index == -1) {
					i++;
					continue;
				}
				map.put(Registries.BLOCK.get(new Identifier(val.substring(0, index))), Double.parseDouble(val.substring(index + 1)));
				i++;
			} catch (Exception e) {
				list.remove(i);
				LOGGER.log(Level.WARN, MOD_ID + " failed to load " + key + ", removing value", e);
			}
		}
	}
	public void loadIni(SFIni ini) {
		setOrResetDouble(ini, "beacon.rangeBase", d -> add = d , add);
		setOrResetDouble(ini, "beacon.rangeBase", d -> lvl_mul = d , lvl_mul);
		fillBlockRangeMap(ini, "beacon.rangePerBlock", beacon_additive);
		setOrResetBool(ini, "conduit.addVanillaRange", b -> keep_vanilla = b, keep_vanilla);
		fillBlockRangeMap(ini, "conduit.rangePerBlock", conduit_additive);

		LOGGER.log(Level.INFO, MOD_ID + " successfully loaded config file");
	}

}
