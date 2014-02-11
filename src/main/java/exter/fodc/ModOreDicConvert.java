package exter.fodc;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.oredict.OreDictionary;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.network.FMLEventChannel;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;
import exter.fodc.block.BlockAutomaticOreConverter;
import exter.fodc.block.BlockOreConversionTable;
import exter.fodc.item.ItemOreConverter;
import exter.fodc.network.ODCPacketHandler;
import exter.fodc.proxy.CommonODCProxy;
import exter.fodc.tileentity.TileEntityAutomaticOreConverter;

@Mod(
    modid = "fodc",
    name = "OreDicConvert",
    version = "1.4.2",
    dependencies = "required-after:Forge@[9.10.0.842,)"
    )
public class ModOreDicConvert
{
  
  //List of string that the ore name must begin with
  private String[] prefixes;

  public static ItemOreConverter item_oreconverter = null;
  @Instance("fodc")
  public static ModOreDicConvert instance;

  // Says where the client and server 'proxy' code is loaded.
  @SidedProxy(clientSide = "exter.fodc.proxy.ClientODCProxy", serverSide = "exter.fodc.proxy.CommonODCProxy")
  public static CommonODCProxy proxy;
  public static BlockOreConversionTable block_oreconvtable;
  public static BlockAutomaticOreConverter block_oreautoconv;
  public ArrayList<String> valid_ore_names;
  
  public static Logger log = Logger.getLogger("OreDicConvert");

  public static FMLEventChannel network_channel;
  
  public static ODCPacketHandler net_handler;
  
  // Find all ore names of a item stack in the dictionary.
  public Set<String> FindAllOreNames(ItemStack it)
  {
    Set<String> results = new HashSet<String>();
    for (String name : valid_ore_names)
    {
      for (ItemStack ore : OreDictionary.getOres(name))
      {
        if (it.isItemEqual(ore))
        {
          results.add(name);
        }
      }
    }
    return results;
  }

  @EventHandler
  public void preInit(FMLPreInitializationEvent event)
  {
    Configuration config = new Configuration(event.getSuggestedConfigurationFile());
    config.load();
    String classes = config.get(Configuration.CATEGORY_GENERAL, "classprefixes", "ore,ingot,dust,block").getString();
    config.save();
    prefixes = classes.split(",");
    valid_ore_names = new ArrayList<String>();

    NetworkRegistry.INSTANCE.registerGuiHandler(this, proxy);

    block_oreconvtable = (BlockOreConversionTable) (new BlockOreConversionTable()).setHardness(2.5F).setStepSound(Block.soundTypeWood);
    block_oreautoconv = (BlockAutomaticOreConverter) (new BlockAutomaticOreConverter()).setHardness(2.5F).setStepSound(Block.soundTypeStone);
    item_oreconverter = new ItemOreConverter();
    network_channel = NetworkRegistry.INSTANCE.newEventDrivenChannel("EXTER.FODC");
    GameRegistry.registerBlock(block_oreconvtable,"oreConvTable");
    GameRegistry.registerBlock(block_oreautoconv,"oreAutoconverter");
    GameRegistry.registerItem(item_oreconverter, "oreConverter");
    
    net_handler = new ODCPacketHandler();
    network_channel.register(net_handler);
  }

  @EventHandler
  public void load(FMLInitializationEvent event)
  {

    ItemStack iron_stack = new ItemStack(Items.iron_ingot);
    ItemStack redstone_stack = new ItemStack(Items.redstone);
    ItemStack workbench_stack = new ItemStack(Blocks.crafting_table);
    ItemStack wood_stack = new ItemStack(Blocks.planks,1,OreDictionary.WILDCARD_VALUE);
    ItemStack cobble_stack = new ItemStack(Blocks.cobblestone,1,OreDictionary.WILDCARD_VALUE);
    ItemStack oreconverter_stack = new ItemStack(item_oreconverter);
    LanguageRegistry.addName(item_oreconverter, "Ore Converter");
    LanguageRegistry.addName(block_oreconvtable, "Ore Conversion Table");
    LanguageRegistry.addName(block_oreautoconv, "Automatic Ore Converter");
    GameRegistry.registerTileEntity(TileEntityAutomaticOreConverter.class, "AutoOreConverter");
    proxy.Init();
    
    GameRegistry.addRecipe(
        oreconverter_stack,
        "I",
        "C",
        "B",
        'I', iron_stack,
        'C', cobble_stack,
        'B', workbench_stack);
    GameRegistry.addRecipe(
        new ItemStack(block_oreconvtable),
        "O",
        "W",
        'O', oreconverter_stack,
        'W', wood_stack);
    GameRegistry.addRecipe(
        new ItemStack(block_oreautoconv),
        "IOI",
        "CRC",
        "ICI",
        'I', iron_stack,
        'O', oreconverter_stack,
        'R', redstone_stack,
        'C', cobble_stack);
  }

  private void RegisterOreName(String name)
  {
    if(valid_ore_names.contains(name))
    {
      return;
    }
    boolean found = false;
    for (String cl : prefixes)
    {
      if (cl != null && name.startsWith(cl))
      {
        found = true;
        break;
      }
    }
    if(found)
    {
      valid_ore_names.add(name);
      log.info("registered ore name: " + name);
    }
  }

  @EventHandler
  public void postInit(FMLPostInitializationEvent event)
  {
    //log.setParent(FMLLog.getLogger());
    String[] ore_names = OreDictionary.getOreNames();
    for (String name : ore_names)
    {
      if(name == null)
      {
        log.warning("null name in Ore Dictionary.");
        continue;
      }
      RegisterOreName(name);
    }
    MinecraftForge.EVENT_BUS.register(this);
  }

  @SubscribeEvent
  public void OnOreDictionaryRegister(OreDictionary.OreRegisterEvent event)
  {
    RegisterOreName(event.Name);
  }
}
