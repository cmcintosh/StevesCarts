package vswe.stevescarts.old.Modules.Workers.Tools;
import java.util.ArrayList;

import net.minecraft.block.Block;
import net.minecraft.block.BlockCrops;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Vec3;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.common.IPlantable;
import vswe.stevescarts.client.interfaces.GuiVehicle;
import vswe.stevescarts.vehicles.entities.EntityModularCart;
import vswe.stevescarts.old.Helpers.Localization;
import vswe.stevescarts.old.Modules.ICropModule;
import vswe.stevescarts.old.Modules.ISuppliesModule;
import vswe.stevescarts.modules.ModuleBase;
import vswe.stevescarts.old.Slots.SlotBase;
import vswe.stevescarts.old.Slots.SlotSeed;

public abstract class ModuleFarmer extends ModuleTool implements ISuppliesModule, ICropModule  {
	public ModuleFarmer(EntityModularCart cart) {
		super(cart);
	}

	
	protected abstract int getRange();
	
	public int getExternalRange() {
		return getRange();
	}
	
	private ArrayList<ICropModule> plantModules;
	@Override
	public void init() {
		super.init();
		plantModules = new ArrayList<ICropModule>();
		
		for (ModuleBase module : getCart().getModules()) {
			if (module instanceof ICropModule) {
				plantModules.add((ICropModule)module);
			}
		}
	}	
	
		
	//lower numbers are prioritised
	public byte getWorkPriority() {
		return 80;
	}

	@Override
	public boolean hasGui() {
		return true;
	}

	@Override
	public void drawForeground(GuiVehicle gui) {
	    drawString(gui, Localization.MODULES.TOOLS.FARMER.translate(), 8, 6, 0x404040);
	}


	@Override
	protected int getInventoryWidth() {
		return super.getInventoryWidth() + 3;
	}
	
	@Override
	protected SlotBase getSlot(int slotId, int x, int y) {
		if (x == 0) {
			return super.getSlot(slotId, x, y);
		}else{
			x--;
			return new SlotSeed(getCart(), this, slotId,8+x*18,28+y*18);
		}
	}		
	

	//return true when the work is done, false allow other modules to continue the work
	public boolean work() {
       //get the next block so the cart knows where to mine
        Vec3 next = getNextblock();
        //save thee coordinates for easy access
        int x = (int) next.xCoord;
        int y = (int) next.yCoord;
        int z = (int) next.zCoord;

        //loop through the blocks in the "hole" in front of the cart

        for (int i = -getRange(); i <= getRange(); i++)
        {
            for (int j = -getRange(); j <= getRange(); j++)
            {
                //calculate the coordinates of this "hole"
                int coordX = x + i;
                int coordY = y - 1;
                int coordZ = z + j;

				if (farm(coordX, coordY, coordZ))
                {
                    return true;
                }
                else if (till(coordX, coordY, coordZ))
                {
                    return true;
                }
                else if (plant(coordX, coordY, coordZ))
                {
                    return true;
                }
            }
        }

		return false;
    }

   protected boolean till(int x, int y, int z)
    {
        Block b = getCart().worldObj.getBlock(x, y, z);

        if (getCart().worldObj.isAirBlock(x, y + 1, z) && (b == Blocks.grass || b == Blocks.dirt))
        {
            if (doPreWork())
            {
				startWorking(10);
                return true;
            }
            else
            {
                stopWorking();
                getCart().worldObj.setBlock(x, y, z, Blocks.farmland);
            }
        }

        return false;
    }

   protected boolean plant(int x, int y, int z)
   {
		int hasSeeds = -1;

		Block soilblock = getCart().worldObj.getBlock(x, y, z);
		
		if (soilblock != null) {
			
				
			//check if there's any seeds to place
			for (int i = 0; i < getInventorySize(); i++)
			{
				//check if the slot contains seeds
				if (getStack(i) != null)
				{
					if (isSeedValidHandler(getStack(i)))
					{

						Block cropblock = getCropFromSeedHandler(getStack(i));
						
						if (cropblock != null && cropblock instanceof IPlantable && getCart().worldObj.isAirBlock(x, y + 1, z)  && soilblock.canSustainPlant(getCart().worldObj, x, y, z, ForgeDirection.UP, ((IPlantable)cropblock))) {
							hasSeeds = i;
							break;							
						}

					}
				}
			}
	
			if (hasSeeds != -1)
			{
				if (doPreWork())
				{
					startWorking(25);
					return true;
				}
				else
				{
					stopWorking();


                    Block cropblock = getCropFromSeedHandler(getStack(hasSeeds));
										
					getCart().worldObj.setBlock(x, y + 1, z, cropblock);
										
					getStack(hasSeeds).stackSize--;
	
					if (getStack(hasSeeds).stackSize <= 0)
					{
						setStack(hasSeeds,null);
					}
						
					
				}
			}
		}
        

        return false;
    }

   protected boolean farm(int x, int y, int z)
    {
		if (!isBroken()) {
			Block block = getCart().worldObj.getBlock(x, y + 1 , z);
	        int m = getCart().worldObj.getBlockMetadata(x, y + 1, z);
	
	        if (isReadyToHarvestHandler(x, y + 1, z))
	        {
	            if (doPreWork())
	            {
	            	int efficiency = enchanter != null ? enchanter.getEfficiencyLevel() : 0;	
	            	int workingtime = (int)(getBaseFarmingTime() / Math.pow(1.3F, efficiency));
					setFarming(workingtime * 4);
	                startWorking(workingtime);
	                return true;
	            }
	            else
	            {
	                stopWorking();
	                
	                
	                ArrayList<ItemStack> stuff;
	        		if (shouldSilkTouch(block, x, y, z, m)) {
	        			stuff = new ArrayList<ItemStack>();
	        			ItemStack stack = getSilkTouchedItem(block, m);
	        			if (stack != null) {
	        				stuff.add(stack);
	        			}
	        		}else{
	        			int fortune = enchanter != null ? enchanter.getFortuneLevel() : 0;
	                	stuff = block.getDrops(getCart().worldObj, x, y + 1, z, m, fortune);
	        		}
	        		
	                
	                for (ItemStack iStack : stuff)
	                {
	                    getCart().addItemToChest(iStack);
	
	                    if (iStack.stackSize != 0)
	                    {
	                        EntityItem entityitem = new EntityItem(getCart().worldObj, getCart().posX, getCart().posY, getCart().posZ , iStack);
	                        entityitem.motionX = (float)(x - getCart().x()) / 10;
	                        entityitem.motionY = 0.15F;
	                        entityitem.motionZ = (float)(z - getCart().z()) / 10;
	                        getCart().worldObj.spawnEntityInWorld(entityitem);
	                    }
	                }
	
	                getCart().worldObj.setBlockToAir(x, y + 1, z);
	                
	                damageTool(3);
	            }
	        }
		}

        return false;
    }

 
   protected int getBaseFarmingTime() {
	   return 25;
   }
   
	public boolean isSeedValidHandler(ItemStack seed) {
		for (ICropModule module : plantModules) {
			if (module.isSeedValid(seed)) {
				return true;
			}
		}
		
		return false;
	}
	
	protected Block getCropFromSeedHandler(ItemStack seed) {
		for (ICropModule module : plantModules) {
			if (module.isSeedValid(seed)) {
				return module.getCropFromSeed(seed);
			}
		}
		
		return null;
	}	
	

	protected boolean isReadyToHarvestHandler(int x, int y, int z) {
		for (ICropModule module : plantModules) {
			if (module.isReadyToHarvest(x,y,z)) {
				return true;
			}
		}
		
		return false;
	}		

	@Override
	public boolean isSeedValid(ItemStack seed) {
        return seed.getItem() == Items.wheat_seeds
				|| seed.getItem() == Items.potato
				|| seed.getItem() == Items.carrot;
	}	
	
	@Override
	public Block getCropFromSeed(ItemStack seed) {
		if (seed.getItem() == Items.carrot) {
			return Blocks.carrots;
		}else if(seed.getItem() == Items.potato) {
			return Blocks.potatoes;
		}else if(seed.getItem() == Items.wheat_seeds){
			return Blocks.wheat;
		}
		
		return null;
	}
	
	
	@Override
	public boolean isReadyToHarvest(int x, int y, int z) {
		Block block = getCart().worldObj.getBlock(x, y , z);
        int m = getCart().worldObj.getBlockMetadata(x, y, z);

		if (block instanceof BlockCrops && m == 7) {
			return true;
		} 
		
		return false;
	}
	
	private int farming;
	private float farmAngle;
	private float rigAngle = -(float)Math.PI * 5 / 4;

	public float getFarmAngle() {
		return farmAngle;
	}

	public float getRigAngle() {
		return rigAngle;
	}

	@Override
	public void initDw() {
		addDw(0,0);
	}
	@Override
	public int numberOfDataWatchers() {
		return 1;
	}

	private void setFarming(int val) {
		farming = val;
		updateDw(0, (byte)(val > 0 ? 1 : 0));
	}

	protected boolean isFarming() {
		if (isPlaceholder()) {
			return getSimInfo().getIsFarming();
		}else{
			return getCart().isEngineBurning() && getDw(0) != 0;
		}
	}

    /**
     Called every tick, here the necessary actions should be taken
     **/
    public void update()
    {
        //call the method from the super class, this will do all ordinary things first
        super.update();

		if (!getCart().worldObj.isRemote) {
			setFarming(farming-1);
		}else{
			float up = -(float)Math.PI * 5 / 4;
			float down = -(float)Math.PI;
			boolean flag = isFarming();
			if (flag){
				if (rigAngle < down) {
					rigAngle += 0.1F;
					if (rigAngle > down) {
						rigAngle = down;
					}
				}else{
					farmAngle = (float)((farmAngle + 0.15F) % (Math.PI * 2));
				}
			}else{
				if (rigAngle > up) {
					rigAngle -= 0.075F;
					if (rigAngle < up) {
						rigAngle = up;
					}
				}
			}
		}
    }

	
	@Override
	public boolean haveSupplies() {
		for (int i = 0; i < getInventorySize(); i++) {
			ItemStack item = getStack(i);
			if (item != null && isSeedValidHandler(item)) {
				return true;
			}
		}
		return false;
	}	
}                   