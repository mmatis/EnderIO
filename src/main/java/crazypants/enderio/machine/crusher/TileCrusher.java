package crazypants.enderio.machine.crusher;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import crazypants.enderio.ModObject;
import crazypants.enderio.machine.AbstractPoweredTaskEntity;
import crazypants.enderio.machine.IMachineRecipe;
import crazypants.enderio.machine.IPoweredTask;
import crazypants.enderio.machine.MachineRecipeInput;
import crazypants.enderio.machine.PoweredTask;
import crazypants.enderio.machine.SlotDefinition;

public class TileCrusher extends AbstractPoweredTaskEntity {

  protected IGrindingMultiplier gb;
  protected int currGbUse = 0;

  public TileCrusher() {
    super(new SlotDefinition(2, 4));
  }

  @Override
  public String getInventoryName() {
    return ModObject.blockSagMill.unlocalisedName;
  }

  @Override
  public String getMachineName() {
    return ModObject.blockSagMill.unlocalisedName;
  }

  @Override
  protected boolean isMachineItemValidForSlot(int i, ItemStack itemstack) {
    if(itemstack == null) {
      return false;
    }
    return CrusherRecipeManager.instance.isValidInput(new MachineRecipeInput(i, itemstack));
  }

  public int getBallDurationScaled(int scale) {
    if(gb == null) {
      return 0;
    }
    float res = 1 - (float)currGbUse / (float)gb.getDurationMJ();
    return (int)(res * scale);
  }


  @Override
  protected double usePower() {
    double res = super.usePower();
    if(gb != null) {
      currGbUse += res;
      if(currGbUse > gb.getDurationMJ()) {
        currGbUse = 0;
        gb = null;
      }
    }
    if(gb == null ) {
      gb = CrusherRecipeManager.getInstance().getGrindballFromStack(inventory[1]);
      if(gb != null) {
        decrStackSize(1, 1);
        markDirty();
      }
    }
    return res;
  }

  @Override
  protected void taskComplete() {
    IPoweredTask ct = currentTask;
    super.taskComplete();
    //run it again if the ball says so
    if(gb != null) {
      float chance = random.nextFloat();
      float mul = gb.getGrindingMultiplier() - 1;
      while(mul > 0) {
        if(chance <= mul){
          currentTask = ct;
          super.taskComplete();
        }
        mul--;
      }
    }
  }

  @Override
  protected PoweredTask createTask(IMachineRecipe nextRecipe, float chance) {
    PoweredTask res;
    if(gb != null) {
      res = new PoweredTask(nextRecipe, chance * gb.getChanceMultiplier(), getInputs());
      res.setRequiredEnergy(res.getRequiredEnergy() * gb.getPowerMultiplier());
    } else {
      res = new PoweredTask(nextRecipe, chance, getInputs());
    }
    return res;
  }

  @Override
  public void readCustomNBT(NBTTagCompound nbtRoot) {
    super.readCustomNBT(nbtRoot);
    gb = GrindingMultiplierNBT.readFromNBT(nbtRoot);
    currGbUse = nbtRoot.getInteger("currGbUse");
  }

  @Override
  public void writeCustomNBT(NBTTagCompound nbtRoot) {
    super.writeCustomNBT(nbtRoot);
    if(gb != null) {
      GrindingMultiplierNBT.writeToNBT(gb, nbtRoot);
    }
    nbtRoot.setInteger("currGbUse", currGbUse);
  }

  @Override
  public boolean hasCustomInventoryName() {
    return false;
  }

}