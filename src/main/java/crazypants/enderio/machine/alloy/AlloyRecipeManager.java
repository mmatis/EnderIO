package crazypants.enderio.machine.alloy;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.item.ItemStack;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import crazypants.enderio.Log;
import crazypants.enderio.ModObject;
import crazypants.enderio.config.Config;
import crazypants.enderio.machine.MachineRecipeInput;
import crazypants.enderio.machine.MachineRecipeRegistry;
import crazypants.enderio.machine.recipe.BasicManyToOneRecipe;
import crazypants.enderio.machine.recipe.CustomTagHandler;
import crazypants.enderio.machine.recipe.IManyToOneRecipe;
import crazypants.enderio.machine.recipe.IRecipe;
import crazypants.enderio.machine.recipe.ManyToOneMachineRecipe;
import crazypants.enderio.machine.recipe.ManyToOneRecipeManager;
import crazypants.enderio.machine.recipe.Recipe;
import crazypants.enderio.machine.recipe.RecipeConfig;
import crazypants.enderio.machine.recipe.RecipeConfigParser;
import crazypants.enderio.machine.recipe.RecipeInput;
import crazypants.util.Util;

public class AlloyRecipeManager extends ManyToOneRecipeManager {

  public static final String IMC_KEY = "recipe:alloysmelter";
  
  private static final String CORE_FILE_NAME = "AlloySmelterRecipes_Core.xml";
  private static final String CUSTOM_FILE_NAME = "AlloySmelterRecipes_User.xml";

  static final AlloyRecipeManager instance = new AlloyRecipeManager();

  public static AlloyRecipeManager getInstance() {
    return instance;
  }

  VanillaSmeltingRecipe vanillaRecipe = new VanillaSmeltingRecipe();

  public AlloyRecipeManager() {
    super(CORE_FILE_NAME, CUSTOM_FILE_NAME, "Alloy Smelter");
  }

  public VanillaSmeltingRecipe getVanillaRecipe() {
    return vanillaRecipe;
  }

  public void setVanillaRecipe(VanillaSmeltingRecipe vanillaRecipe) {
    this.vanillaRecipe = vanillaRecipe;
  }
  
  protected CustomTagHandler createCustomTagHandler() {
    return new VanillaFurnaceTagHandler();
  }

  public void loadRecipesFromConfig() {
    super.loadRecipesFromConfig();
    MachineRecipeRegistry.instance.registerRecipe(ModObject.blockAlloySmelter.unlocalisedName, new ManyToOneMachineRecipe("AlloySmelterRecipe", ModObject.blockAlloySmelter.unlocalisedName, this));
    //vanilla alloy furnace recipes    
    MachineRecipeRegistry.instance.registerRecipe(ModObject.blockAlloySmelter.unlocalisedName, vanillaRecipe);
  }


  private static final String ELEMENT_ROOT = "vanillaFurnaceRecipes";
  private static final String ELEMENT_EXCLUDE = "exclude";


  private class VanillaFurnaceTagHandler implements CustomTagHandler {

    private boolean inTag = false;

    private boolean inExcludes = false;

    private Boolean enabled = null;

    private List<RecipeInput> excludes = new ArrayList<RecipeInput>();

    @Override
    public boolean startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
      if(ELEMENT_ROOT.equals(localName)) {
        inTag = true;
        if(RecipeConfigParser.hasAttribute(RecipeConfigParser.AT_ENABLED, attributes)) {
          boolean defVal = true;
          if(enabled != null) {
            defVal = enabled;
          }
          enabled = RecipeConfigParser.getBooleanValue(RecipeConfigParser.AT_ENABLED, attributes, defVal);
        }
      } else if(ELEMENT_EXCLUDE.equals(localName)) {
        inExcludes = true;
      } else if(inExcludes && RecipeConfigParser.ELEMENT_ITEM_STACK.equals(localName)) {
        RecipeInput ri = RecipeConfigParser.getItemStack(attributes);
        excludes.add(ri);
      }
      return inTag;
    }

    @Override
    public boolean endElement(String uri, String localName, String qName) throws SAXException {
      if(ELEMENT_ROOT.equals(localName)) {
        inTag = false;
      } else if(ELEMENT_EXCLUDE.equals(localName)) {
        inExcludes = false;
      }
      return inTag;
    }

    @Override
    public void configProcessed() {
      if(enabled != null) {
        Log.info("AlloyRecipeManager: Vannila smelting in AlloySmelting enabled=" + enabled);
        vanillaRecipe.setEnabled(enabled.booleanValue());
      }
      for (RecipeInput ri : excludes) {
        Log.info("Excluding furnace recipe from Alloy Smelter: " + ri);
        vanillaRecipe.addExclude(ri);
      }
    }

  }

}
