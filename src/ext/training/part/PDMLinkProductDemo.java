package ext.training.part;

import java.rmi.RemoteException;

import com.ptc.core.foundation.type.server.impl.TypeHelper;
import com.ptc.core.meta.common.TypeIdentifier;
import com.ptc.windchill.uwgm.common.container.OrganizationHelper;

import wt.fc.Persistable;
import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.folder.Cabinet;
import wt.folder.FolderHelper;
import wt.folder.SubFolder;
import wt.folder.SubFolderLink;
import wt.inf.container.WTContainer;
import wt.inf.container.WTContainerHelper;
import wt.inf.container.WTContainerRef;
import wt.inf.container.WTContainerServerHelper;
import wt.inf.container.WTContainerTemplateRef;
import wt.inf.team.ContainerTeamHelper;
import wt.inf.team.ContainerTeamServerHelper;
import wt.inf.template.ContainerTemplateHelper;
import wt.inf.template.WTContainerTemplateMaster;
import wt.org.OrganizationServicesHelper;
import wt.org.WTOrganization;
import wt.pdmlink.PDMLinkProduct;
import wt.pds.StatementSpec;
import wt.query.QueryException;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.session.SessionHelper;
import wt.type.TypeDefinitionReference;
import wt.type.TypedUtility;
import wt.type.TypedUtilityServiceHelper;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;

public class PDMLinkProductDemo {

	
	public static void main(String[] args) {
		
	}
	
	
	public static PDMLinkProduct createProduct() throws WTPropertyVetoException, WTException, RemoteException {
		PDMLinkProduct product = PDMLinkProduct.newPDMLinkProduct();
		product.setName("");
		product.setCreator(SessionHelper.manager.getPrincipal());
		
		WTOrganization wtOrganization = OrganizationServicesHelper.manager.getOrganization(SessionHelper.manager.getPrincipal());
		WTContainerRef wtContainerRef = WTContainerHelper.service.getOrgContainerRef(wtOrganization);
		product.setContainerReference(wtContainerRef);
		
		TypeDefinitionReference type = TypedUtilityServiceHelper.service.getTypeDefinitionReference("wt.pdmlink.PDMLinkProduct");
		product.setTypeDefinitionReference(type);
		
		WTContainerTemplateRef wtContainerTemplateRef = null;
		WTContainerTemplateMaster wtContainerTemplateMaster = getWTContainerTemplateMaster("General Product");
		wtContainerTemplateRef = (WTContainerTemplateRef) ContainerTemplateHelper.service.getContainerTemplateRef(wtContainerTemplateMaster).getTemplate();
		product.setContainerTemplateReference(wtContainerTemplateRef);
		
		product = (PDMLinkProduct) WTContainerHelper.service.create(product);
		product = (PDMLinkProduct) PersistenceHelper.manager.save(product);
		
		return product;
		
	}


	private static WTContainerTemplateMaster getWTContainerTemplateMaster(String name) throws WTException {
		WTContainerTemplateMaster wtContainerTemplateMaster = null;
		QuerySpec qs = new QuerySpec(WTContainerTemplateMaster.class);
		qs.setAdvancedQueryEnabled(true);
		qs.appendWhere(new SearchCondition(WTContainerTemplateMaster.class, WTContainerTemplateMaster.NAME, SearchCondition.EQUAL, name), new int[] {});
		QueryResult qr = PersistenceHelper.manager.find((StatementSpec) qs);
		while(qr.hasMoreElements()) {
			wtContainerTemplateMaster = (WTContainerTemplateMaster) qr.nextElement();
		}
		return wtContainerTemplateMaster;
	}
	
	public static SubFolder getSubFolder(String subfoldername, WTContainer wtContainer) throws WTException {
		SubFolder subfolder = null;
		WTContainerRef wtContainerRef = WTContainerRef.newWTContainerRef(wtContainer);
		Persistable persistable = wtContainerRef.getObject();
		if(persistable instanceof WTContainer) {
			wtContainer = (WTContainer) persistable;
		}
		Cabinet cabinet = wtContainer.getDefaultCabinet();
		QueryResult qr = PersistenceHelper.manager.navigate(cabinet, "member", SubFolderLink.class);
		while(qr.hasMoreElements()) {
			SubFolder _subfolder = (SubFolder) qr.nextElement();
			if(subfolder.getName().equalsIgnoreCase(subfoldername)) {
				subfolder = _subfolder;
			}
		}
		return subfolder;
	}
	
	public static SubFolder createSubFolder(String foldername, WTContainer wtContainer) throws WTException {
		SubFolder subfolder = null;
		if(wtContainer != null) {
			subfolder = FolderHelper.service.createSubFolder("/Default/" + foldername, WTContainerRef.newWTContainerRef(wtContainer));
		}
		return subfolder;
	}
	
}
	