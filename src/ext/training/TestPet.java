package ext.training;

import com.ptc.core.meta.common.TypeIdentifier;
import com.ptc.core.meta.common.TypeIdentifierHelper;

import wt.method.RemoteMethodServer;
import wt.type.TypeDefinitionReference;
import wt.type.TypedUtility;

public class TestPet {
	
	public static void main(String[] args) throws Exception {
		RemoteMethodServer server = RemoteMethodServer.getDefault();
		server.setUserName("wcadmin");
		server.setPassword("wcadmin");
		
//		Pet dog = Pet.newPet();
//		dog.setName("Fergus");
//		dog.setKind(PetKind.toPetKind("dog"));
//		dog.setDateOfBirth(new Timestamp(new SimpleDateFormat("yyyy-MM-dd").parse("1999-02-11").getTime()));
//		dog.setFixed(false);
//		dog = (Pet) PersistenceHelper.manager.store(dog);
//
//		Pet cat = Pet.newPet();
//		cat.setName("Stimpy");
//		cat.setKind(PetKind.toPetKind("cat"));
//		cat.setDateOfBirth(new Timestamp(new SimpleDateFormat("yyyy-MM-dd").parse("1996-08-24").getTime()));
//		cat.setFixed(false);
//		cat = (Pet) PersistenceHelper.manager.store(cat);	
		
//		TypeIdentifier ti = TypeIdentifierHelper.getTypeIdentifier("com.training.TrainPart");
//		System.out.println(ti.getTypename());
//		TypeDefinitionReference reference = TypedUtility.getTypeDefinitionReference(ti.getTypename());
//		if (reference == null) {
//			reference = TypeDefinitionReference.newTypeDefinitionReference();
//		}
//		System.out.println(reference);
		
	

	}


}
