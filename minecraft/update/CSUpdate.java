package clashsoft.cslib.minecraft.update;

import java.util.*;

import clashsoft.cslib.minecraft.CSLib;
import clashsoft.cslib.minecraft.util.CSWeb;
import clashsoft.cslib.util.CSLog;
import clashsoft.cslib.util.CSString;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.LoaderState;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraftforge.common.MinecraftForge;

/**
 * The class CSUpdate.
 * <p>
 * This class adds several utils for updating mods.
 * 
 * @author Clashsoft
 */
public class CSUpdate
{
	/** The updates already found. */
	public static Map<String, ModUpdate>	updates					= new HashMap();
	
	/** The Constant CURRENT_VERSION. */
	public static final String				CURRENT_VERSION			= MinecraftForge.MC_VERSION;
	
	/** The Constant CLASHSOFT_ADFLY. */
	public static final String				CLASHSOFT_ADFLY			= "http://adf.ly/2175784/";
	
	/** The Constant CLASHSOFT_UPDATE_NOTES. */
	public static final String				CLASHSOFT_UPDATE_NOTES	= "https://dl.dropboxusercontent.com/s/pxm1ki6wbtxlvuv/update.txt";
	
	/**
	 * Creates a version String for Clashsoft mods.
	 * 
	 * @param rev
	 *            the rev
	 * @return the string
	 */
	public static String version(int rev)
	{
		return CURRENT_VERSION + "-" + rev;
	}
	
	public static String version(int major, int minor, int rev)
	{
		return String.format("%s_%d.%d.%d", CURRENT_VERSION, major, minor, rev);
	}
	
	public static String version(String version)
	{
		return String.format("%s_%s", CURRENT_VERSION, version);
	}
	
	public static ModUpdate readUpdateLine(String line, String modName, String acronym, String version)
	{
		int i0 = line.indexOf(':');
		int i1 = line.indexOf('=');
		int i2 = line.lastIndexOf('@');
		
		if (i0 == -1)
		{
			return null;
		}
		
		String key = line.substring(0, i0);
		
		if (modName == null)
		{
			modName = key;
		}
		
		if (key.equals(modName) || key.equals(acronym))
		{
			String newVersion = line.substring(i0 + 1, i1);
			if (version == null || !newVersion.equals(version))
			{
				String updateNotes = null;
				String updateUrl = null;
				if (i1 != -1)
				{
					updateNotes = line.substring(i1 + 1, i2 == -1 ? line.length() : i2);
				}
				if (i2 != -1)
				{
					updateUrl = line.substring(i2 + 1);
				}
				
				return new ModUpdate(modName, version, newVersion, updateNotes, updateUrl);
			}
		}
		return null;
	}
	
	public static List<ModUpdate> getUpdates(boolean invalidUpdates)
	{
		Collection<ModUpdate> collection = updates.values();
		List<ModUpdate> list = new ArrayList(collection.size());
		
		if (invalidUpdates)
		{
			list.addAll(collection);
		}
		else
		{
			for (ModUpdate update : collection)
			{
				if (update.isValid())
				{
					list.add(update);
				}
			}
		}
		
		return list;
	}
	
	public static ModUpdate addUpdate(ModUpdate update)
	{
		if (update != null)
		{
			ModUpdate update1 = getUpdate(update.getModName());
			if (update1 != null)
			{
				update1.combine(update);
				return update1;
			}
			else
			{
				updates.put(update.getModName(), update);
			}
		}
		return update;
	}
	
	public static ModUpdate getUpdate(String modName)
	{
		return updates.get(modName);
	}
	
	public static ModUpdate getUpdate(String modName, String version)
	{
		ModUpdate update = getUpdate(modName);
		if (update != null)
		{
			update.setMod(modName, version);
		}
		return update;
	}
	
	public static ModUpdate getUpdate(String modName, String acronym, String version, String[] updateFile)
	{
		ModUpdate update = getUpdate(modName, version);
		if (update != null)
		{
			return update;
		}
		
		for (String line : updateFile)
		{
			update = readUpdateLine(line, modName, acronym, version);
			if (update != null)
			{
				addUpdate(update);
			}
		}
		return getUpdate(modName);
	}
	
	public static void updateCheck(String url)
	{
		updateCheck(CSWeb.readWebsite(url));
	}
	
	public static void updateCheck(String[] updateFile)
	{
		for (String line : updateFile)
		{
			ModUpdate update = readUpdateLine(line, null, null, null);
			addUpdate(update);
		}
	}
	
	public static void updateCheckCS(String modName, String acronym, String version)
	{
		updateCheck(modName, acronym, version, CLASHSOFT_UPDATE_NOTES);
	}
	
	public static void updateCheck(String modName, String version, String url)
	{
		updateCheck(modName, CSString.getAcronym(modName), version, url);
	}
	
	public static void updateCheck(String modName, String acronym, String version, String url)
	{
		if (!Loader.instance().hasReachedState(LoaderState.INITIALIZATION))
		{
			CSLog.warning("The mod " + modName + " is attempting an update check before the post-init state.");
		}
		
		if (CSLib.updateCheck)
		{
			new CheckUpdateThread(modName, acronym, version, url).start();
		}
	}
	
	public static void updateCheck(String modName, String acronym, String version, String[] updateLines)
	{
		if (CSLib.updateCheck)
		{
			new CheckUpdateThread(modName, acronym, version, updateLines).start();
		}
	}
	
	public static void notifyAll(EntityPlayer player)
	{
		if (!updates.isEmpty())
		{
			player.addChatMessage(new ChatComponentTranslation("update.found"));
			for (ModUpdate update : updates.values())
			{
				notify(player, update);
			}
		}
	}
	
	public static void notify(EntityPlayer player, ModUpdate update)
	{
		if (update != null && update.isValid())
		{
			player.addChatMessage(new ChatComponentTranslation("update.notification", update.getModName(), update.getNewVersion(), update.getVersion()));
			
			if (!update.getUpdateNotes().isEmpty())
			{
				player.addChatMessage(new ChatComponentTranslation("update.notes"));
				
				for (String line : update.getUpdateNotes())
				{
					player.addChatMessage(new ChatComponentText(line));
				}
			}
			
			if (CSLib.autoUpdate)
			{
				update.install(player);
			}
			else
			{
				player.addChatMessage(new ChatComponentTranslation("update.automatic.disabled", update.getModName()));
			}
		}
	}
	
	public static void update(EntityPlayer player, String modName)
	{
		ModUpdate update = getUpdate(modName);
		if (update != null)
		{
			update.install(player);
		}
		else
		{
			player.addChatMessage(new ChatComponentTranslation("update.none", modName));
		}
	}
	
	public static void updateAll(EntityPlayer player)
	{
		if (!updates.isEmpty())
		{
			for (ModUpdate update : updates.values())
			{
				update.install(player);
			}
		}
	}
	
	public static int compareVersion(String version1, String version2)
	{
		if (version1 == null)
		{
			return version2 == null ? 0 : -1;
		}
		else if (version2 == null)
		{
			return 1;
		}
		
		String[] split1 = version1.split("\\p{Punct}");
		String[] split2 = version2.split("\\p{Punct}");
		
		int len = Math.max(split1.length, split2.length);
		int[] ints1 = new int[len];
		int[] ints2 = new int[len];
		
		for (int i = 0; i < split1.length; i++)
		{
			ints1[i] = Integer.parseInt(split1[i], 36);
		}
		for (int i = 0; i < split2.length; i++)
		{
			ints2[i] = Integer.parseInt(split2[i], 36);
		}
		
		for (int i = len - 1; i >= 0; i--)
		{
			int compare = Integer.compare(ints1[i], ints2[i]);
			if (compare != 0)
			{
				return compare;
			}
		}
		
		return 0;
	}
}
