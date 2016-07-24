package exter.fodc.registry;

import java.util.ArrayList;
import java.util.Collections;
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

  static private final String REGEX_COMMENT = "Supports multiple expressions separated by commas.\n"
      + "Uses Java's Pattern class regex syntax. See the following page for more info about Pattern regex syntax:\n"
      + "http://docs.oracle.com/javase/7/docs/api/java/util/regex/Pattern.html";
  
  static private final String WHITELIST_COMMENT = "Only names that match any of these regexes will be registered. " + REGEX_COMMENT;
  static private final String BLACKLIST_COMMENT = "Names that match any of these regexes will not be registered. " + REGEX_COMMENT;

  static public void preInit(Configuration config)
  {
    valid_ore_names = new HashSet<String>();
    String whitelist_line = config.get(Configuration.CATEGORY_GENERAL, "whitelist", "^ore.*,^ingot.*,^dust.*,^block.*",WHITELIST_COMMENT).getString();
    String blacklist_line = config.get(Configuration.CATEGORY_GENERAL, "blacklist", "",BLACKLIST_COMMENT).getString();
    whitelist = compilePatterns(whitelist_line);
    blacklist = compilePatterns(blacklist_line);
  }
  
  static private List<Pattern> compilePatterns(String line)
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
        ModOreDicConvert.log.warn("Pattern '" + t + "' has invalid syntax.");
      }
    }
    return list;
  }

  static private boolean matchesAnyPattern(String str, List<Pattern> patterns)
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
  static public Set<String> findAllOreNames(ItemStack it)
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
 
  static public void registerOreName(String name)
  {
    if(matchesAnyPattern(name,whitelist) && !matchesAnyPattern(name,blacklist))
    {
      valid_ore_names.add(name);
      if(ModOreDicConvert.log_orenames)
      {
        ModOreDicConvert.log.info("Registered ore name: " + name);
      }
    }
  }
  
  static public Set<String> getOreNames()
  {
    return Collections.unmodifiableSet(valid_ore_names);
  }
}
