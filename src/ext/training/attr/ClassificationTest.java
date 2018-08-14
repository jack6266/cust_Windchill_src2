package ext.training.attr;

import java.util.Locale;
import java.util.Set;

import com.ptc.core.meta.common.AttributeTypeIdentifier;
import com.ptc.windchill.csm.common.CsmConstants;

import wt.facade.classification.ClassificationFacade;
import wt.method.RemoteMethodServer;
import wt.util.WTException;

public class ClassificationTest {

	
	
	public static void main(String[] args) throws WTException {
		RemoteMethodServer server = RemoteMethodServer.getDefault();
		server.setUserName("wcadmin");
		server.setPassword("wcadmin");
		
		ClassificationFacade facadeInstance = ClassificationFacade.getInstance(); 
		String localizedName = facadeInstance.getLocalizedDisplayNameForClassificationNode("com.traning.ITrainAtt", CsmConstants.NAMESPACE, new Locale("cn")); 
		System.out.println(localizedName);
		
		String localizedHierarchy = facadeInstance.getLocalizedHierarchyForClassificationNode("com.traning.ITrainAtt", CsmConstants.NAMESPACE, new Locale("cn")); 
		System.out.println(localizedHierarchy);
		
		Set<AttributeTypeIdentifier> atis = facadeInstance.getClassificationAttributes("com.traning.ITrainAtt"); 
		for(AttributeTypeIdentifier ati :atis){ 
		    System.out.println(ati.getAttributeName()); 
		}
	}
	
	

}
