package tf.ssf.sfort.betterbeacon;

import net.fabricmc.loader.api.FabricLoader;
import org.apache.logging.log4j.Level;
import tf.ssf.sfort.ini.SFIni;

import java.io.File;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigLegacy {
	public static void loadLegacy(SFIni inIni) {
		Map<String, String> oldConf = new HashMap<>();
		Map<String, String[]> oldConfArr = new HashMap<>();
		File confFile = new File(
			FabricLoader.getInstance().getConfigDir().toString(),
			"BetterBeacons.conf"
		);
		if (!confFile.exists()) return;
		try {
			List<String> la = Files.readAllLines(confFile.toPath());
			String[] ls = la.toArray(new String[Math.max(la.size(), 10)|1]);

			try {
				oldConf.put("beacon.rangeBase", Double.toString(Double.parseDouble(ls[0])));
			} catch (Exception ignore) {}
			try {
				oldConf.put("beacon.rangePerLevel", Double.toString(Double.parseDouble(ls[2])));
			} catch (Exception ignore) {}
			{
				String[] in = ls[4].split("\\s*;\\s*");
				String[] ret = new String[in.length / 2];
				for (int i = 0; i < ret.length; i++) {
					ret[i] = in[i+i] + ";" + in[i+i+1];
				}
				if (ret.length > 0) {
					oldConfArr.put("beacon.rangePerBlock", ret);
				}
			}
			try {
				oldConf.put("conduit.addVanillaRange", Boolean.toString(Boolean.parseBoolean(ls[6])));
			} catch (Exception ignore) {}
			{
				String[] in = ls[8].split("\\s*;\\s*");
				String[] ret = new String[in.length / 2];
				for (int i = 0; i < ret.length; i++) {
					ret[i] = in[i+i] + ";" + in[i+i+1];
				}
				if (ret.length > 0) {
					oldConfArr.put("conduit.rangePerBlock", ret);
				}
			}
			for (Map.Entry<String, String> entry : oldConf.entrySet()) {
				SFIni.Data data = inIni.getLastData(entry.getKey());
				if (data != null) {
					data.val = entry.getValue();
				}
			}
			for (Map.Entry<String, String[]> entry : oldConfArr.entrySet()) {
				List<SFIni.Data> data = inIni.data.get(entry.getKey());
				if (data != null) {
					if (data.isEmpty()) {
						for (String s : entry.getValue()) {
							data.add(new SFIni.Data(s, null));
						}
					} else {
						String[] val = entry.getValue();
						{
							List<String> comments = data.get(0).comments;
							data.clear();
							data.add(new SFIni.Data(val[0], comments));
						}
						for (int i=1; i<val.length; i++) {
							data.add(new SFIni.Data(val[i], null));
						}
					}
				}
			}

			Files.delete(confFile.toPath());
			Config.LOGGER.log(Level.INFO,Config.MOD_ID+" successfully loaded legacy config file");
		} catch(Exception e) {
			Config.LOGGER.log(Level.ERROR,Config.MOD_ID+" failed to load legacy config file, using defaults", e);
		}
	}

}
