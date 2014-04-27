package exter.fodc.registry;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import exter.fodc.ModOreDicConvert;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.oredict.OreDictionary;

public class OreNameRegistry
{
  static private Set<String> valid_ore_names;

  static private List<Pattern> whitelist;
  static private List<Pattern> blacklist;

  static public void PreInit(Configuration config)
  {  
    String whitelist_line = config.get(Configuration.CATEGORY_GENERAL, "whitelist", "^ore.*,^ingot.*,^dust.*,^block.*").getString();
    String blacklist_line = config.get(Configuration.CATEGORY_GENERAL, "blacklist", "").getString();
    whitelist = CompilePatterns(whitelist_line);
    blacklist = CompilePatterns(blacklist_line);

    
  }
  
  static private List<Pattern> CompilePatterns(String line)
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
        ModOreDicConvert.log.warning("Pattern '" + t + "' has invalid syntax.");
      }
    }
    return list;
  }

  static private boolean MatchesAnyPattern(String str, List<Pattern> patterns)
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
  
  

  // Find all ore names of a item stack in the dictionary.
  static public Set<String> FindAllOreNames(ItemStack it)
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
 
  static public void RegisterOreName(String name)
  {
    if(MatchesAnyPattern(name,whitelist) && !MatchesAnyPattern(name,blacklist))
    {
      valid_ore_names.add(name);
      ModOreDicConvert.log.info("registered ore name: " + name);
    }
  }
}
