package vswe.stevescarts.old.Modules.Storages.Chests;

import vswe.stevescarts.vehicles.entities.EntityModularCart;

public class ModuleSideChests extends ModuleChest {
	public ModuleSideChests(EntityModularCart cart) {
		super(cart);
	}

	@Override
	protected int getInventoryWidth()
	{
		return 5;
	}
	@Override
	protected int getInventoryHeight() {
		return 3;
	}


}