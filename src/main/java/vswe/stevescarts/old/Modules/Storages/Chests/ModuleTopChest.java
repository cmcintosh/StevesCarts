package vswe.stevescarts.old.Modules.Storages.Chests;

import vswe.stevescarts.vehicles.entities.EntityModularCart;

public class ModuleTopChest extends ModuleChest {
	public ModuleTopChest(EntityModularCart cart) {
		super(cart);
	}

	@Override
	protected int getInventoryWidth()
	{
		return 6;
	}
	@Override
	protected int getInventoryHeight() {
		return 3;
	}

}