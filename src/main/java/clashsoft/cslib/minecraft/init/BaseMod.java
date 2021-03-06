package clashsoft.cslib.minecraft.init;

import java.io.File;
import java.util.Collections;
import java.util.List;

import clashsoft.cslib.config.CSConfig;
import clashsoft.cslib.logging.CSLog;
import clashsoft.cslib.minecraft.common.BaseProxy;
import clashsoft.cslib.minecraft.network.CSNetHandler;
import clashsoft.cslib.minecraft.update.CSUpdate;
import clashsoft.cslib.reflect.CSReflection;
import clashsoft.cslib.util.CSString;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.ModMetadata;
import cpw.mods.fml.common.event.FMLConstructionEvent;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.EventBus;
import cpw.mods.fml.common.network.NetworkRegistry;

import net.minecraftforge.common.MinecraftForge;

/**
 * The Class BaseMod.
 * <p>
 * This class provides overrideable methods that ensure a basic mod environment.
 * It can automatically setup a configuration file, a network handler, an event
 * handler and a proxy for client- and server-side operations. The class also
 * stores information about the mod itself, including the name, the mod ID, the
 * version, the download URL, a list of authors and others.
 * 
 * @author Clashsoft
 * @version %I%
 * @param <N>
 *            the type of the net handler.
 */
public abstract class BaseMod<N extends CSNetHandler>
{
	public static final boolean	clientSide	= FMLCommonHandler.instance().getSide().isClient();
	
	public String				modID;
	public String				name;
	public String				acronym;
	public String				version;
	
	public String				description	= "";
	public String				logoFile;
	public String				url			= "";
	public List<String>			authors		= Collections.EMPTY_LIST;
	public String				credits		= "";
	
	public boolean				hasConfig;
	public File					configFile;
	
	public Class<N>				netHandlerClass;
	public N					netHandler;
	
	public Object				eventHandler;
	
	public BaseProxy			proxy;
	public boolean				isClient;
	
	public BaseMod(BaseProxy proxy, String modID, String name, String version)
	{
		this(proxy, modID, name, CSString.getAcronym(name), version);
	}
	
	public BaseMod(BaseProxy proxy, String modID, String name, String acronym, String version)
	{
		this.proxy = proxy;
		this.modID = modID;
		this.name = name;
		this.acronym = acronym;
		this.version = version;
		
		this.logoFile = "/assets/" + this.modID + "/logo.png";
		
		if (proxy == null)
		{
			CSLog.warning("The proxy of the mod %s (%s) is null. Is this a bug?", name, modID);
			this.isClient = clientSide;
		}
		else
		{
			this.isClient = proxy.isClient();
		}
	}
	
	public static <T extends BaseProxy> T createProxy(String clientClass, String serverClass)
	{
		if (clientSide)
		{
			return CSReflection.createInstance(clientClass);
		}
		else
		{
			return CSReflection.createInstance(serverClass);
		}
	}
	
	/**
	 * Reads this mods config file. It is recommended to use {@link CSConfig} to
	 * read the config, since it is already initialised with this mod's config
	 * file.
	 * 
	 * @see CSConfig
	 */
	public void readConfig()
	{
		CSLog.warning("The mod " + this.name + " claims that it has a config, but doesn't override the read method.");
	}
	
	/**
	 * Checks if an update for this mod is available. The default implementation
	 * does not check for any updates.
	 * 
	 * @see CSUpdate
	 */
	public void updateCheck()
	{
	}
	
	/**
	 * Creates a new network handler. The default implementation creates a new
	 * instance of the class {@link BaseMod#netHandlerClass} and uses it as the
	 * net handler. If this fails, a basic {@link CSNetHandler} is created to
	 * support packet sending and receiving.
	 * 
	 * @return a network handler
	 * @see CSNetHandler
	 * @see BaseMod#netHandlerClass
	 */
	public N createNetHandler()
	{
		if (this.netHandlerClass != null)
		{
			try
			{
				return this.netHandlerClass.newInstance();
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
		return null;
	}
	
	/**
	 * Populates the FML {@link ModMetadata} {@code metadata} with this mod's
	 * properties.
	 * 
	 * @param metadata
	 *            the {@link ModMetadata} to populate.
	 */
	public void writeMetadata(ModMetadata metadata)
	{
		metadata.autogenerated = false;
		metadata.name = this.name;
		metadata.modId = this.modID;
		metadata.version = this.version;
		
		metadata.description = this.description;
		metadata.logoFile = this.logoFile;
		metadata.url = this.url;
		metadata.authorList = this.authors;
		metadata.credits = this.credits;
	}
	
	/**
	 * Constructs the mod. This method should be used to apply any block or item
	 * replacements.
	 * <p>
	 * To make this method load, override this method in your mod class and mark
	 * it with the {@link Mod.EventHandler} annotation.
	 * 
	 * @param event
	 *            the event
	 */
	public void construct(FMLConstructionEvent event)
	{
	}
	
	/**
	 * Pre-initializes the mod.
	 * <p>
	 * To make this method load, override this method in your mod class and mark
	 * it with the {@link Mod.EventHandler} annotation.
	 * <p>
	 * This does the following actions:
	 * <li>Write the mod metadata to the {@link ModMetadata} instance.
	 * <li>If this has a config, it loads the config, reads it and saves it.
	 * <li>If the proxy is not null, the proxy is registered via
	 * {@link NetworkRegistry#registerGuiHandler(Object, cpw.mods.fml.common.network.IGuiHandler)}
	 * <li>If the proxy it not null, it's
	 * {@link BaseProxy#init(FMLInitializationEvent)} is called.
	 * <li>The net handler is created via { {@link BaseMod#createNetHandler()}
	 * <li>If the event handler is not null, the event handler is registered via
	 * {@link EventBus#register(Object)} for the
	 * {@link MinecraftForge#EVENT_BUS} and the {@link FMLCommonHandler#bus()}
	 * 
	 * @param event
	 *            the event
	 */
	public void preInit(FMLPreInitializationEvent event)
	{
		this.writeMetadata(event.getModMetadata());
		
		// In case the mod wants to read the config on its own
		this.configFile = new File(event.getModConfigurationDirectory(), this.name + ".cfg");
		if (this.hasConfig)
		{
			CSConfig.loadConfig(this.configFile, this.name);
			this.readConfig();
			CSConfig.saveConfig();
		}
		
		if (this.proxy != null)
		{
			NetworkRegistry.INSTANCE.registerGuiHandler(this, this.proxy);
			this.proxy.preInit(event);
		}
		
		if (this.netHandler == null)
		{
			this.netHandler = this.createNetHandler();
		}
		
		if (this.eventHandler != null)
		{
			MinecraftForge.EVENT_BUS.register(this.eventHandler);
			FMLCommonHandler.instance().bus().register(this.eventHandler);
		}
	}
	
	/**
	 * Initializes the mod.
	 * <p>
	 * To make this method load, override this method in your mod class and mark
	 * it with the {@link Mod.EventHandler} annotation.
	 * <p>
	 * This does the following actions:
	 * <li>If the proxy is not null, the proxy's
	 * {@link BaseProxy#init(FMLInitializationEvent)} is called.
	 * <li>If net handler is not null, the net handler's
	 * {@link CSNetHandler#init()} is called.
	 * 
	 * @param event
	 *            the event
	 */
	public void init(FMLInitializationEvent event)
	{
		if (this.proxy != null)
		{
			this.proxy.init(event);
		}
		
		if (this.netHandler != null)
		{
			this.netHandler.init();
		}
	}
	
	/**
	 * Post-initializes the mod.
	 * <p>
	 * To make this method load, override this method in your mod class and mark
	 * it with the {@link Mod.EventHandler} annotation.
	 * <p>
	 * This does the following actions:
	 * <li>If this mod is running on a client-side environment, it checks for
	 * any updates using {@link BaseMod#updateCheck()}.
	 * <li>If the proxy is not null, the proxy's
	 * {@link BaseProxy#postInit(FMLInitializationEvent)} is called.
	 * <li>If net handler is not null, the net handler's
	 * {@link CSNetHandler#postInit()} is called.
	 * 
	 * @param event
	 *            the event
	 */
	public void postInit(FMLPostInitializationEvent event)
	{
		if (this.isClient)
		{
			this.updateCheck();
		}
		
		if (this.proxy != null)
		{
			this.proxy.postInit(event);
		}
		
		if (this.netHandler != null)
		{
			this.netHandler.postInit();
		}
	}
}
