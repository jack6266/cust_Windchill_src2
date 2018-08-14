package ext.training.iba;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.ptc.core.lwc.common.view.AttributeDefinitionReadView;
import com.ptc.core.lwc.common.view.ConstraintDefinitionReadView;
import com.ptc.core.lwc.common.view.TypeDefinitionReadView;
import com.ptc.core.lwc.server.LWCNormalizedObject;
import com.ptc.core.lwc.server.TypeDefinitionServiceHelper;
import com.ptc.core.meta.common.DisplayOperationIdentifier;
import com.ptc.core.meta.common.TypeIdentifier;
import com.ptc.core.meta.common.UpdateOperationIdentifier;

import wt.fc.Persistable;
import wt.fc.PersistenceHelper;
import wt.session.SessionHelper;
import wt.type.TypedUtility;
import wt.util.WTException;

public class IBATrainingDemo {
	
	
	
	public static void main(String[] args) {
		
		
	}
	
	
	
	public static List getConstaintRule(String typeName, String ibaName) throws WTException {
		List ruleList = new ArrayList();
		TypeIdentifier typeIdentifier = TypedUtility.getTypeIdentifier(typeName);
		TypeDefinitionReadView view = TypeDefinitionServiceHelper.service.getTypeDefView(typeIdentifier);
		AttributeDefinitionReadView attributeReadView = view.getAttributeByName(ibaName);
		for(ConstraintDefinitionReadView constaint : attributeReadView.getAllConditions()) {
			ruleList.add(constaint);
		}
		return ruleList;
	}
	
	
	public static String getIBADisplayName(String typeName, String ibaName, Locale locale) throws WTException {
		TypeIdentifier ti = TypedUtility.getTypeIdentifier(typeName);
		TypeDefinitionReadView readView = TypeDefinitionServiceHelper.service.getTypeDefView(ti);
		AttributeDefinitionReadView attributeReadView = readView.getAttributeByName(ibaName);
		if(attributeReadView == null) {
			return "";
		}
		String ibaDispalyName = attributeReadView.getPropertyValueByName("displayName").getValueAsString(locale, true);
		return ibaDispalyName;
	}
	
	
	public static Object getValue(Persistable persistable, String ibaName) throws WTException {
		Locale locale = SessionHelper.getLocale();
		Object value = null;
		LWCNormalizedObject lwcNormalizedObject = new LWCNormalizedObject(persistable, null, locale, new DisplayOperationIdentifier());
		lwcNormalizedObject.load(ibaName);
		value = lwcNormalizedObject.get(ibaName);
		return value;
	}
	
	
	public static void updateValue(Persistable persistable, String ibaName, Object ibaValue) throws WTException {
		Locale locale = SessionHelper.getLocale();
		LWCNormalizedObject obj = new LWCNormalizedObject(persistable, null, locale, new UpdateOperationIdentifier());
		obj.load(ibaName);
		obj.set(ibaName, ibaValue);
		Persistable object = obj.apply();
		object  = (Persistable) PersistenceHelper.manager.modify(object);
	}
	
	
	
	
	
}
