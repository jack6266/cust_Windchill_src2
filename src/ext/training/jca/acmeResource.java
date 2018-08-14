package ext.training.jca;

import wt.util.resource.RBEntry;
import wt.util.resource.RBUUID;
import wt.util.resource.WTListResourceBundle;

@RBUUID("ext.training.jca.acmeResource")
public class acmeResource extends WTListResourceBundle {
	
	
	@RBEntry("ACME Administration")
	public static final String ACME_ADMINISTRATION = "acme_administration";
	
	@RBEntry("Pet Administration")
	public static final String PET_ADMINISTRATION = "pet_administration";
	
	@RBEntry("CRUD opererations for pets")
	public static final String PET_ADMINISTRATION_DESCRIPTION =	"pet_administration_description";

	
}
