package exter.fodc;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

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
import exter.fodc.block.BlockAutomaticOreConverter;
import exter.fodc.block.BlockOreConversionTable;
import exter.fodc.item.ItemOreConverter;
import exter.fodc.network.ODCPacketHandler;
import exter.fodc.proxy.CommonODCProxy;
import exter.fodc.tileentity.TileEntityAutomaticOreConverter;

@Mod(
    modid = "fodc",
    name = "OreDicConvert",
    version = "1.5.0",
    dependencies = "required-after:Forge@[10.12.0.1057,)"
    )
public class ModOreDicConvert
{
  
  //List of string that the ore name must begin with
  private List<Pattern> whitelist;
  private List<Pattern> blacklist;

  public static ItemOreConverter item_oreconverter = null;
  @Instance("fodc")
  public static ModOreDicConvert instance;

  // Says where the client and server 'proxy' code is loaded.
  @SidedProxy(clientSide = "exter.fodc.proxy.ClientODCProxy", serverSide = "exter.fodc.proxy.CommonODCProxy")
  public static CommonODCProxy proxy;
  public static BlockOreConversionTable block_oreconvtable;
  public static BlockAutomaticOreConverter block_oreautoconv;
  public Set<String> valid_ore_names;
  
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
  
  private List<Pattern> CompilePatterns(String line)
  {
    List<Pattern> list = new ArrayList<Pattern>();
    String[] tokens = line.split(",");
    for(String t: tokens)
    {
      t = t.trim();
      if(t == null || t.isEmpty())
      {
        continue;
      }
      try
      {
        list.add(Pattern.compile(t));
      } catch(PatternSyntaxException e)
      {
        log.warning("Pattern '" + t + "' has invalid syntax.");
      }
    }
    return list;
  }
  
  private boolean MatchesAnyPattern(String str, List<Pattern> patterns)
  {
    for(Pattern p:patterns)
    {
      if(p.matcher(str).matches())
      {
        return true;
      }
    }
    return false;
  }

  @EventHandler
  public void preInit(FMLPreInitializationEvent event)
  {
    Configuration config = new Configuration(event.getSuggestedConfigurationFile());
    config.load();
    String whitelist_line = config.get(Configuration.CATEGORY_GENERAL, "whitelist", "^ore.*,^ingot.*,^dust.*,^block.*").getString();
    String blacklist_line = config.get(Configuration.CATEGORY_GENERAL, "blacklist", "").getString();
    config.save();
    valid_ore_names = new HashSet<String>();
    whitelist = CompilePatterns(whitelist_line);
    blacklist = CompilePatterns(blacklist_line);
    
    
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
    if(MatchesAnyPattern(name,whitelist) && !MatchesAnyPattern(name,blacklist))
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
