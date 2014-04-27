package exter.fodc;

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
import exter.fodc.block.BlockAutomaticOreConverter;
import exter.fodc.block.BlockOreConversionTable;
import exter.fodc.item.ItemOreConverter;
import exter.fodc.network.ODCPacketHandler;
import exter.fodc.proxy.CommonODCProxy;
import exter.fodc.registry.OreNameRegistry;
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
  public void load(FMLInitializationEvent event)
  {

    ItemStack iron_stack = new ItemStack(Items.iron_ingot);
    ItemStack redstone_stack = new ItemStack(Items.redstone);
    ItemStack workbench_stack = new ItemStack(Blocks.crafting_table);
    ItemStack wood_stack = new ItemStack(Blocks.planks,1,OreDictionary.WILDCARD_VALUE);
    ItemStack cobble_stack = new ItemStack(Blocks.cobblestone,1,OreDictionary.WILDCARD_VALUE);
    ItemStack oreconverter_stack = new ItemStack(item_oreconverter);
    GameRegistry.registerTileEntity(TileEntityAutomaticOreConverter.class, "AutoOreConverter");
    
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
