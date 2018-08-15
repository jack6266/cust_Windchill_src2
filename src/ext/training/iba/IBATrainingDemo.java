package ext.training.iba;

import java.rmi.RemoteException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;

import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;

import com.ptc.core.lwc.common.view.AttributeDefinitionReadView;
import com.ptc.core.lwc.common.view.ConstraintDefinitionReadView;
import com.ptc.core.lwc.common.view.DatatypeReadView;
import com.ptc.core.lwc.common.view.TypeDefinitionReadView;
import com.ptc.core.lwc.server.LWCNormalizedObject;
import com.ptc.core.lwc.server.TypeDefinitionServiceHelper;
import com.ptc.core.meta.common.DisplayOperationIdentifier;
import com.ptc.core.meta.common.FloatingPoint;
import com.ptc.core.meta.common.TypeIdentifier;
import com.ptc.core.meta.common.UpdateOperationIdentifier;
import com.ptc.core.meta.common.impl.TypeIdentifierUtilityHelper;
import com.ptc.tml.log.format.StringHelper;

import wt.fc.Persistable;
import wt.fc.PersistenceHelper;
import wt.iba.definition.litedefinition.AttributeDefDefaultView;
import wt.iba.value.IBAHolder;
import wt.session.SessionHelper;
import wt.type.TypedUtility;
import wt.units.FloatingPointWithUnits;
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
	
	
	public static void setIBAValue(Class clazz, IBAHolder ibaholder, HashMap<String, Object> ibamap) throws RemoteException, WTException {
		if(ibamap == null || ibamap.size() == 0 || ibaholder == null) {
			return;
		}
		
		String typename = TypeIdentifierUtilityHelper.service.getTypeIdentifier(ibaholder).toString();
		TypeIdentifier ti = TypedUtility.getTypeIdentifier(typename);
		TypeDefinitionReadView readview = TypeDefinitionServiceHelper.service.getTypeDefView(ti);
		HashMap<String, Object> softAttrs = new HashMap<String, Object>();
		for(Entry<String, Object> entry : ibamap.entrySet()) {
			String ibaname = entry.getKey();
			Object ibavalue = entry.getValue();
			AttributeDefinitionReadView attributeDefinitionReadView = readview.getAttributeByName(ibaname);
			if(attributeDefinitionReadView == null) {
				continue;
			}
			
			String dbColumnLabel = getDatabaseColumnsLabel(attributeDefinitionReadView, ibaname);
			if(dbColumnLabel != null && !"".equals(dbColumnLabel)) {
				setStandardAttributeIBAValue(ibaholder, ibaname, ibavalue, dbColumnLabel);
			} else {
				AttributeDefDefaultView defaultView = attributeDefinitionReadView.getIBARefView();
				if(defaultView != null) {
					softAttrs.put(defaultView.getName(), ibavalue);
				}else {
					softAttrs.put(ibaname, ibavalue);
				}
			}
		}
		if(softAttrs.size() > 0) {
			setIBAValue2(clazz, ibaholder, softAttrs);
		}
	}



	private static void setStandardAttributeIBAValue(IBAHolder ibaholder, String ibaname, Object ibavalue, String dbColumnLabel) throws RemoteException, WTException {
		Object value = null;
		TypeIdentifier ti = TypeIdentifierUtilityHelper.service.getTypeIdentifier(ibaholder);
		String softtype = ti.toString();
		TypeIdentifier typeIdentifier = TypedUtility.getTypeIdentifier(softtype);
		TypeDefinitionReadView typeDefinitionReadView = TypeDefinitionServiceHelper.service.getTypeDefView(typeIdentifier);
		AttributeDefinitionReadView attributeDefinitionReadView = typeDefinitionReadView.getAttributeByName(ibaname);
		DatatypeReadView datatypeReadView = attributeDefinitionReadView.getDatatype();
		String typename = datatypeReadView.getName();
		
		String _s = ObjectUtils.toString(ibavalue, "");
		value = _s;
		
		if(FloatingPoint.class.getName().equalsIgnoreCase(typename)
				|| FloatingPointWithUnits.class.getName().equals(typename)
				|| Double.class.getName().equalsIgnoreCase(typename)) {
			value = Double.valueOf(_s);
		} else if(Integer.class.getName().equalsIgnoreCase(typename)) {
			value = Integer.valueOf(_s);
		} else if(Long.class.getName().equalsIgnoreCase(typename)) {
			value = Long.valueOf(_s);
		} else if(Timestamp.class.getName().equalsIgnoreCase(typename)) {
			value = getTimestampDate(_s);
		} else if(Boolean.class.getName().equalsIgnoreCase(typename)) {
			value = Boolean.valueOf(_s);
		} else if(String.class.getName().equalsIgnoreCase(typename)) {
			value = String.valueOf(_s);
		}
		updateDB(value.toString(), dbColumnLabel, (Persistable) ibaholder);	
		
	}





	private static String getDatabaseColumnsLabel(AttributeDefinitionReadView attributeDefinitionReadView, String ibaname) throws WTException {
		String dbColumnLabel = null;
		if(attributeDefinitionReadView != null) {
			dbColumnLabel = attributeDefinitionReadView.getDatabaseColumnsLabel();
		}
		return dbColumnLabel;
	}
	
	
}
