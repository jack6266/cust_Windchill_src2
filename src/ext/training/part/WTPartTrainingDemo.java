package ext.training.part;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;

import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.fc.WTReference;
import wt.folder.Cabinet;
import wt.folder.Folder;
import wt.folder.FolderEntry;
import wt.folder.FolderHelper;
import wt.folder.SubFolder;
import wt.folder.SubFolderLink;
import wt.inf.container.WTContainer;
import wt.inf.container.WTContainerRef;
import wt.method.RemoteAccess;
import wt.method.RemoteMethodServer;
import wt.part.QuantityUnit;
import wt.part.Source;
import wt.part.WTPart;
import wt.pdmlink.PDMLinkProduct;
import wt.pds.StatementSpec;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.series.MultilevelSeries;
import wt.series.Series;
import wt.type.TypeDefinitionReference;
import wt.type.Typed;
import wt.type.TypedUtility;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;
import wt.vc.IterationIdentifier;
import wt.vc.VersionControlHelper;
import wt.vc.VersionIdentifier;
import wt.vc.Versioned;

public class WTPartTrainingDemo implements RemoteAccess, Serializable {
	
	
	
	public static void main(String[] args) throws RemoteException, InvocationTargetException {
		
		RemoteMethodServer server = RemoteMethodServer.getDefault();
		
		server.invoke("createNewPart", WTPartTrainingDemo.class.getName(), null, new Class[]{}, new Object[]{});
	}

	
	public static void createNewPart() throws WTException, WTPropertyVetoException, ClassNotFoundException {
		WTPart wtpart = WTPart.newWTPart();
		wtpart.setName("car-lamp2");
		wtpart.setNumber(PersistenceHelper.manager.getNextSequence("ID_Sequence"));
		
		TypedUtility.initTypeDefinitions();
		TypeDefinitionReference typeReference = TypedUtility.getTypeDefinitionReference("wt.part.WTPart");
		wtpart.setTypeDefinitionReference(typeReference);
		
		WTContainer container = getContainerByName("Computer", PDMLinkProduct.class.getName());
		wtpart.setContainerReference(WTContainerRef.newWTContainerRef(container));
		
		wtpart.setSource(Source.MAKE);
		wtpart.setDefaultUnit(QuantityUnit.EA);
		
		MultilevelSeries multilevelSeries = MultilevelSeries.newMultilevelSeries("wt.vc.VersionIdentifier", "A");
		VersionIdentifier versionIdentifier = VersionIdentifier.newVersionIdentifier(multilevelSeries);
		VersionControlHelper.setVersionIdentifier((Versioned) wtpart, versionIdentifier, false);
		Series series = Series.newSeries("wt.vc.IterationIdentifier", "2");
		IterationIdentifier iterationIdentifier = IterationIdentifier.newIterationIdentifier(series);
		VersionControlHelper.setIterationIdentifier(wtpart, iterationIdentifier);
		
		Folder folder = FolderHelper.service.getFolder("/Default/", WTContainerRef.newWTContainerRef(container));
		FolderHelper.assignLocation((FolderEntry) wtpart, folder);
		
		wtpart = (WTPart) PersistenceHelper.manager.save(wtpart);
		System.out.println("部件已完成创建");
	}
	
	
	private static void assignFolder(WTPart part, String folder, WTContainer wtcontainer) throws WTException {
		SubFolder subFolder = getSubFolderByName(wtcontainer, folder);
		if (subFolder == null) {
			subFolder = FolderHelper.service.createSubFolder("/Default/" + folder,
					WTContainerRef.newWTContainerRef(wtcontainer));
		}
		FolderHelper.assignLocation(part, subFolder);
	}
	
	
	private static SubFolder getSubFolderByName(WTContainer container, String subfolder) throws WTException {
		WTReference reference = WTContainerRef.newWTContainerRef(container);
		Object refObject = reference.getObject();
		SubFolder folder = null;
		if (refObject instanceof WTContainer) {
			container = (WTContainer) refObject;
			Cabinet cabinet = container.getDefaultCabinet();
			QueryResult qResult = PersistenceHelper.manager.navigate(cabinet, "member", SubFolderLink.class);
			while (qResult.hasMoreElements()) {
				folder = (SubFolder) qResult.nextElement();
				if (folder.getName().equalsIgnoreCase(subfolder)) {
					return folder;
				}
			}
		}
		return folder;
	}

	public static WTContainer getContainerByName(String name,
			String type) throws ClassNotFoundException, WTException {
		WTContainer container = null;
		QueryResult qResult = null;
		if (type != null) {
			QuerySpec qsQuerySpec = new QuerySpec(Class.forName(type));
			if (name != null && name.length() > 1) {
				qsQuerySpec.appendWhere(new SearchCondition(Class
						.forName(type), "containerInfo.name",
						SearchCondition.EQUAL, name));
			}
			System.out.println(qsQuerySpec);
			qResult = PersistenceHelper.manager.find(qsQuerySpec);
			if (qResult != null && qResult.hasMoreElements()) {
				container = (WTContainer) qResult.nextElement();
			}
		}
		return container;
	}
	
	
	public static void setType(Typed type, String softType) throws WTException, WTPropertyVetoException {
		TypedUtility.initTypeDefinitions();
		TypeDefinitionReference reference = TypedUtility.getTypeDefinitionReference(softType);
		if (reference == null) {
			reference = TypeDefinitionReference.newTypeDefinitionReference();
		}
		type.setTypeDefinitionReference(reference);
	}
}
