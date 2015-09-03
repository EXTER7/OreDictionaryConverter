package exter.fodc;


import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.SidedProxy;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLEventChannel;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.oredict.OreDictionary;

import java.util.logging.Logger;

import exter.fodc.block.BlockAutomaticOreConverter;
import exter.fodc.block.BlockOreConversionTable;
import exter.fodc.item.ItemOreConverter;
import exter.fodc.network.ODCPacketHandler;
import exter.fodc.proxy.CommonODCProxy;
import exter.fodc.registry.OreNameRegistry;
import exter.fodc.tileentity.TileEntityAutomaticOreConverter;

@Mod(
    modid = ModOreDicConvert.MODID,
    name = ModOreDicConvert.MODNAME,
    version = ModOreDicConvert.MODVERSION,
    dependencies = "required-after:Forge@[11.14.1.1402,)"
    )
public class ModOreDicConvert
{
  public static final String MODID = "fodc";
  public static final String MODNAME = "Ore Dictionary Converter";
  public static final String MODVERSION = "1.7.0";

  //List of string that the ore name must begin with

  public static ItemOreConverter item_oreconverter = null;
  @Instance("fodc")
  public static ModOreDicConvert instance;

  // Says where the client and server 'proxy' code is loaded.
  @SidedProxy(clientSide = "exter.fodc.proxy.ClientODCProxy", serverSide = "exter.fodc.proxy.CommonODCProxy")
  public static CommonODCProxy proxy;
  public static BlockOreConversionTable block_oreconvtable;
  public static BlockAutomaticOreConverter block_oreautoconv;
  
  public static Logger log = Logger.getLogger("OreDicConvert");

  public static FMLEventChannel network_channel;
  
  public static ODCPacketHandler net_handler;
  
  

  @EventHandler
  public void preInit(FMLPreInitializationEvent event)
  {
    Configuration config = new Configuration(event.getSuggestedConfigurationFile());
    config.load();
    OreNameRegistry.PreInit(config);
    config.save();
    
    
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
  public void init(FMLInitializationEvent event)
  {
    GameRegistry.registerTileEntity(TileEntityAutomaticOreConverter.class, "AutoOreConverter");
    proxy.Init();

    ItemStack iron_stack = new ItemStack(Items.iron_ingot);
    ItemStack redstone_stack = new ItemStack(Items.redstone);
    ItemStack workbench_stack = new ItemStack(Blocks.crafting_table);
    ItemStack wood_stack = new ItemStack(Blocks.planks,1,OreDictionary.WILDCARD_VALUE);
    ItemStack cobble_stack = new ItemStack(Blocks.cobblestone,1,OreDictionary.WILDCARD_VALUE);
    ItemStack oreconverter_stack = new ItemStack(item_oreconverter);
    
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
      OreNameRegistry.RegisterOreName(name);
    }
    MinecraftForge.EVENT_BUS.register(this);
  }

  @SubscribeEvent
  public void OnOreDictionaryRegister(OreDictionary.OreRegisterEvent event)
  {
    OreNameRegistry.RegisterOreName(event.Name);
  }
}
