package ext.training.doc;

import java.beans.PropertyVetoException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;

import wt.content.ApplicationData;
import wt.content.ContentHelper;
import wt.content.ContentHolder;
import wt.content.ContentRoleType;
import wt.content.ContentServerHelper;
import wt.content.URLData;
import wt.doc.DocumentType;
import wt.doc.WTDocument;
import wt.fc.Persistable;
import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.folder.Folder;
import wt.folder.FolderEntry;
import wt.folder.FolderHelper;
import wt.inf.container.WTContainer;
import wt.inf.container.WTContainerRef;
import wt.method.RemoteAccess;
import wt.method.RemoteMethodServer;
import wt.pdmlink.PDMLinkProduct;
import wt.pom.Transaction;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.util.WTException;
import wt.util.WTInvalidParameterException;
import wt.util.WTPropertyVetoException;
import wt.vc.IterationInfo;

public class WTDocTrainingDemo implements RemoteAccess, Serializable {
	
	
	public static void main(String[] args) throws RemoteException, InvocationTargetException {
		RemoteMethodServer server = RemoteMethodServer.getDefault();
		
		server.invoke("createNewDoc", WTDocTrainingDemo.class.getName(), null, null, null);
		
	}
	
	public static WTDocument createNewDoc() throws WTException, WTPropertyVetoException {
		WTDocument wtDocument = null;
		Transaction trx = new Transaction();
		try {
			trx.start();
			
			wtDocument = WTDocument.newWTDocument();
			
			wtDocument.setIterationInfo(IterationInfo.newIterationInfo());
			wtDocument.setName("TestDocument3");
			wtDocument.setNumber("TD0000003");
			
			setDocumentType(wtDocument, "Document");
			
			WTContainer wtcontainer = getContainerByName("Computer", PDMLinkProduct.class);
			WTContainerRef wtContainerRef = WTContainerRef.newWTContainerRef(wtcontainer);
			wtDocument.setContainerReference(wtContainerRef);
			
			Folder folder = FolderHelper.service.getFolder("/Default/", wtContainerRef);
			FolderHelper.assignLocation((FolderEntry) wtDocument, folder);
			wtDocument = (WTDocument) PersistenceHelper.manager.save(wtDocument);
			wtDocument = (WTDocument) PersistenceHelper.manager.refresh(wtDocument);
			
			try {
				createContentFile(wtDocument, new File("C:\\Users\\pc\\Desktop\\TCTDEMO_v11_DEMO.txt"));
			} catch (PropertyVetoException e) {
				e.printStackTrace();
			}
			
			if(trx != null) {
				trx.commit();
				trx = null;
			}
		}finally {
			if(trx != null) {
				trx.rollback();
			}
		}
		
		return wtDocument;
	}
	
	
	private static void setDocumentType(WTDocument wtDocument, String type) throws WTInvalidParameterException, WTPropertyVetoException {
		
		try {
			wtDocument.setDocType(DocumentType.toDocumentType(type));
		} catch (WTInvalidParameterException | WTPropertyVetoException e) {
			wtDocument.setDocType(DocumentType.toDocumentType("$$" + type));
		}
		
	}
	
	
	private static void createContentFile(WTDocument wtDocument, File localFile) throws WTException, PropertyVetoException {

		ContentHolder contentHolder = ContentHelper.service.getContents(wtDocument);
		ApplicationData applicationData = ApplicationData.newApplicationData(contentHolder);
		
		applicationData.setRole(ContentRoleType.PRIMARY);
		applicationData.setFileName(localFile.getName());
		applicationData.setUploadedFromPath(localFile.getAbsolutePath());
		applicationData.setFileSize(localFile.length());
		
		applicationData.setCreatedBy(wtDocument.getCreator());
		applicationData.setComments("test document");
		applicationData.setModifiedBy(wtDocument.getModifier());
		
		FileInputStream in = null;
		try {
			in = new FileInputStream(localFile);
			applicationData = ContentServerHelper.service.updateContent(wtDocument, applicationData, in);
			
			ContentServerHelper.service.updateHolderFormat(wtDocument);
			wtDocument = (WTDocument) PersistenceHelper.manager.refresh((Persistable)wtDocument, true, true);
			if(in != null) {
				in.close();
				in = null;
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if(in != null) {
				try {
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				in = null;
			}
		}
	}
	
	
	private static void CreateUrlFile(WTDocument wtDocument, String url) throws WTException, PropertyVetoException {
		ContentHolder contentHolder = ContentHelper.service.getContents(wtDocument);
		URLData urldata = URLData.newURLData(contentHolder);
		urldata.setDisplayName("DisplayName");
		urldata.setUrlLocation(url); //"http://www.xxx.com"
		urldata.setDescription("TestPrimary");
		urldata.setRole(ContentRoleType.PRIMARY);
		
		ContentServerHelper.service.updateContent(wtDocument, urldata);
	}
	
	
	public static WTContainer getContainerByName(String name,
			Class type) throws WTException {
		WTContainer container = null;
		QueryResult qResult = null;
		if (type != null) {
			QuerySpec qsQuerySpec = new QuerySpec(type);
			if (name != null && name.length() > 1) {
				qsQuerySpec.appendWhere(new SearchCondition(type, "containerInfo.name",
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
	
}
