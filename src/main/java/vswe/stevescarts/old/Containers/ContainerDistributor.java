package vswe.stevescarts.old.Containers;
import java.util.ArrayList;

import vswe.stevescarts.containers.ContainerBase;
import vswe.stevescarts.old.TileEntities.TileEntityBase;
import net.minecraft.inventory.IInventory;
import vswe.stevescarts.old.Helpers.DistributorSide;
import vswe.stevescarts.old.TileEntities.TileEntityDistributor;

public class ContainerDistributor extends ContainerBase
{

	public IInventory getMyInventory() {
		return null;
	}
	
	public TileEntityBase getTileEntity() {
		return distributor;
	}		
	

    private TileEntityDistributor distributor;
    public ContainerDistributor(IInventory invPlayer, TileEntityDistributor distributor)
    {
        this.distributor = distributor;

		cachedValues = new ArrayList<Short>();
		for (DistributorSide side : distributor.getSides()) {
			cachedValues.add((short)0);
			cachedValues.add((short)0);
		}		
    }

   
	
	public ArrayList<Short> cachedValues;
	
}
