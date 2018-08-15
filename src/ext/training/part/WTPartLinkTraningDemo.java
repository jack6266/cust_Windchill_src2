package ext.training.part;

import java.io.Serializable;

import wt.fc.PersistenceHelper;
import wt.fc.ReferenceFactory;
import wt.method.RemoteAccess;
import wt.part.WTPart;
import wt.part.WTPartMaster;
import wt.part.WTPartUsageLink;
import wt.type.TypeDefinitionReference;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;

public class WTPartLinkTraningDemo implements RemoteAccess, Serializable {
	
	public static ReferenceFactory referenceFactory = null;
	
	static {
		referenceFactory = new ReferenceFactory();
	}

	
	public static WTPartUsageLink createWTPartUsageLink() throws WTException, WTPropertyVetoException {
		WTPart roleA = (WTPart) referenceFactory.getReference("").getObject();
		WTPartMaster roleB = (WTPartMaster) referenceFactory.getReference("").getObject();
		WTPartUsageLink link = WTPartUsageLink.newWTPartUsageLink(roleA, roleB);
		
		TypeDefinitionReference td = null;
		link.setTypeDefinitionReference(td);
		
		link = (WTPartUsageLink) PersistenceHelper.manager.save(link);
		link = (WTPartUsageLink) PersistenceHelper.manager.refresh(link);
		return link;
	}
	
	
}
