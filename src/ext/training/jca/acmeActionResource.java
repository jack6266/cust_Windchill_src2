package ext.training.jca;

import wt.util.resource.RBEntry;
import wt.util.resource.RBUUID;
import wt.util.resource.WTListResourceBundle;

@RBUUID("ext.taining.jca.acmeActionResource")
public class acmeActionResource extends WTListResourceBundle {
	
	@RBEntry("New Pet")
	public static final String PET_CREATE_TITLE = "pet.create.title";
	
	@RBEntry("New Pet")
	public static final String PET_CREATE_TOOLTIP = "pet.create.tooltip";
	
	@RBEntry("New Pet")
	public static final String PET_CREATE_DESCRIPTION = "pet.create.description";
	
	@RBEntry("createPackage.gif")
	public static final String PET_CREATE_ICON = "pet.create.icon";

	
}
