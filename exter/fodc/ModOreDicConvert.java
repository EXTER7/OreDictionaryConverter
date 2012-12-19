package exter.fodc;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;


import net.minecraft.block.Block;
import net.minecraft.block.BlockWorkbench;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapelessOreRecipe;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.Init;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.Mod.PostInit;
import cpw.mods.fml.common.Mod.PreInit;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.network.NetworkMod;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.common.registry.LanguageRegistry;

@Mod(modid = "fodc", name = "OreDicConvert", version = "1.2.0")
@NetworkMod(clientSideRequired = true, serverSideRequired = false)
public class ModOreDicConvert
{
  
  //Block/Item IDs
  private int oc_id;
  private int oct_id;
  
  //List of string that the ore name must begin with
  private String[] prefixes;

  public static ItemOreConverter item_oreconverter = null;
  @Instance("fodc")
  public static ModOreDicConvert instance;

  // Says where the client and server 'proxy' code is loaded.
  @SidedProxy(clientSide = "exter.fodc.ClientODCProxy", serverSide = "exter.fodc.CommonODCProxy")
  public static CommonODCProxy proxy;
  public static BlockOreConvertionTable block_oreconvtable;
  public ArrayList<String> valid_ore_names;
  
  private static Logger log = Logger.getLogger("OreDicConvert");

  // Find the ore name of a item stack in the dictionary.
  public String FindOreName(ItemStack it)
  {
    for (String name : ModOreDicConvert.instance.valid_ore_names)
    {
      for (ItemStack ore : OreDictionary.getOres(name))
      {
        if (it.isItemEqual(ore))
        {
          return name;
        }
      }
    }
    return null;
  }

  @PreInit
  public void preInit(FMLPreInitializationEvent event)
  {
    Configuration config = new Configuration(event.getSuggestedConfigurationFile());
    config.load();
    String classes = config.get(Configuration.CATEGORY_GENERAL, "classprefixes", "ore,ingot").value;
    oc_id = config.get(Configuration.CATEGORY_ITEM, "oreconverter", 9001).getInt(9001);
    oct_id = config.get(Configuration.CATEGORY_BLOCK, "oreconveriontable", 3826).getInt(3826);
    config.save();
    prefixes = classes.split(",");
    valid_ore_names = new ArrayList<String>();
    // Stub Method
  }

  @Init
  public void load(FMLInitializationEvent event)
  {
    NetworkRegistry.instance().registerGuiHandler(this, proxy);
    proxy.registerRenderers();

    block_oreconvtable = (BlockOreConvertionTable) (new BlockOreConvertionTable(oct_id)).setHardness(2.5F).setStepSound(Block.soundWoodFootstep).setBlockName("oreConvTable");
    item_oreconverter = new ItemOreConverter(oc_id);

    ItemStack iron_stack = new ItemStack(Item.ingotIron);
    ItemStack workbench_stack = new ItemStack(Block.workbench);
    ItemStack wood_stack = new ItemStack(Block.planks,1,-1);
    
    LanguageRegistry.addName(item_oreconverter, "Ore Converter");
    LanguageRegistry.addName(block_oreconvtable, "Ore Convertion Table");
    GameRegistry.registerBlock(block_oreconvtable);

    GameRegistry.addRecipe(new ItemStack(item_oreconverter), "ICI", 'I', iron_stack, 'C', workbench_stack);
    GameRegistry.addRecipe(new ItemStack(block_oreconvtable), "ICI","WWW", 'I', iron_stack, 'C', workbench_stack, 'W', wood_stack);
    
    GameRegistry.registerCraftingHandler(new ODCCraftingHandler());
  }

  private void AddConversionRecipes(String type, List<ItemStack> ores)
  {
    int i;
    ItemStack oc_stack = new ItemStack(item_oreconverter);
    for (i = 0; i < ores.size(); i++)
    {
      Object[] args;
      GameRegistry.addShapelessRecipe(ores.get(0), oc_stack, ores.get(i));
    }

    for (i = 2; i < 9; i++)
    {
      int j;
      Object[] args = new Object[i + 1];
      args[0] = oc_stack;
      for (j = 0; j < i; j++)
      {
        args[j + 1] = type;
      }
      ItemStack dest = ores.get(0);
      CraftingManager.getInstance().getRecipeList().add(new ShapelessOreRecipe(new ItemStack(dest.getItem(), i, dest.getItemDamage()), args));
    }
  }

  @PostInit
  public void postInit(FMLPostInitializationEvent event)
  {
    log.setParent(FMLLog.getLogger());
    String[] ore_names = OreDictionary.getOreNames();
    for (String name : ore_names)
    {
      int i;
      boolean found = false;
      for (String cl : prefixes)
      {
        if (name.startsWith(cl))
        {
          found = true;
          break;
        }
      }
      if (!found)
      {
        
        log.info("Ignored " + name);
        continue;
      }
      valid_ore_names.add(name);

      List<ItemStack> ores = OreDictionary.getOres(name);
      if (ores.size() > 1)
      {
        AddConversionRecipes(name, ores);
        log.info("Added recipes for " + name);
      }
    }
  }

}
