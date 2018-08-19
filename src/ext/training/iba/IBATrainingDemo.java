package ext.training.iba;

import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.ObjectUtils;

import com.ptc.core.lwc.common.view.AttributeDefinitionReadView;
import com.ptc.core.lwc.common.view.ConstraintDefinitionReadView;
import com.ptc.core.lwc.common.view.DatatypeReadView;
import com.ptc.core.lwc.common.view.TypeDefinitionReadView;
import com.ptc.core.lwc.server.LWCNormalizedObject;
import com.ptc.core.lwc.server.PersistableAdapter;
import com.ptc.core.lwc.server.TypeDefinitionServiceHelper;
import com.ptc.core.meta.common.DisplayOperationIdentifier;
import com.ptc.core.meta.common.FloatingPoint;
import com.ptc.core.meta.common.TypeIdentifier;
import com.ptc.core.meta.common.UpdateOperationIdentifier;
import com.ptc.core.meta.common.impl.TypeIdentifierUtilityHelper;
import com.ptc.core.meta.server.TypeIdentifierUtility;

import wt.fc.Persistable;
import wt.fc.PersistenceHelper;
import wt.fc.PersistenceServerHelper;
import wt.fc.QueryResult;
import wt.folder.Folder;
import wt.iba.definition.FloatDefinition;
import wt.iba.definition.litedefinition.AttributeDefDefaultView;
import wt.iba.definition.litedefinition.FloatDefView;
import wt.iba.value.IBAHolder;
import wt.method.MethodContext;
import wt.pom.PersistenceException;
import wt.pom.WTConnection;
import wt.query.QueryException;
import wt.query.QuerySpec;
import wt.query.SearchCondition;
import wt.session.SessionHelper;
import wt.type.TypedUtility;
import wt.units.FloatingPointWithUnits;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;
import wt.vc.wip.CheckoutLink;
import wt.vc.wip.NonLatestCheckoutException;
import wt.vc.wip.WorkInProgressException;
import wt.vc.wip.WorkInProgressHelper;
import wt.vc.wip.Workable;

public class IBATrainingDemo {
	
	
	
	public static void main(String[] args) {
		
		
	}
	
	
	public static Workable doCheckout(Persistable persistable) throws NonLatestCheckoutException, WorkInProgressException, WTPropertyVetoException, PersistenceException, WTException {
		Workable _workable = null;
        if (!wt.vc.wip.WorkInProgressHelper.isCheckedOut((Workable) persistable))
        {
                if(WorkInProgressHelper.isWorkingCopy((Workable) persistable))
                {
                        _workable= (Workable) persistable;
                }else{
                        Folder chkfolder = WorkInProgressHelper.service.getCheckoutFolder();
                        CheckoutLink checkout_link= WorkInProgressHelper.service.checkout((Workable) persistable, chkfolder, "");
                        _workable = checkout_link.getWorkingCopy();
                }
        }
		return _workable;
	}
	
	
	public static Workable doCheckin(Workable workable) throws WTException, WTPropertyVetoException {
		Workable _workable = null;
		if (wt.vc.wip.WorkInProgressHelper.isCheckedOut((Workable) workable))
        {
			_workable = WorkInProgressHelper.service.checkin(workable, "check in");
        } else {
        	_workable = workable;
        }
		return _workable;
		
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
	
	
	
	public static void updateStandardAttributeIBAValue(IBAHolder ibaHolder, String attrName, Object value) throws WTException {
		Persistable persistable = (Persistable) ibaHolder;
		PersistableAdapter persistableAdapter = new PersistableAdapter(persistable, null, Locale.getDefault(), new UpdateOperationIdentifier());
		TypeIdentifier typeIdentifier = TypeIdentifierUtility.getTypeIdentifier(persistable);
		TypeDefinitionReadView view = TypeDefinitionServiceHelper.service.getTypeDefView(typeIdentifier);
		if(view != null) {
			AttributeDefinitionReadView attributeDefinitionReadView = view.getAttributeByName(attrName);
			if(attributeDefinitionReadView != null) {
				persistableAdapter.load(new String[] { attrName });
				persistableAdapter.set(attrName, value);
				persistableAdapter.apply();
				PersistenceServerHelper.manager.update(persistable);
			}
		}
	}
	
	
	public static void updateIBAValue(IBAHolder ibaHolder, String attrName, Object value) throws WTException {
		Persistable persistable = (Persistable) ibaHolder;
		LWCNormalizedObject obj = new LWCNormalizedObject(persistable, null,Locale.getDefault(), new UpdateOperationIdentifier());
		obj.load(attrName);
		obj.set(attrName,value);
		persistable = obj.apply();
		persistable  = (Persistable)PersistenceHelper.manager.modify(persistable);
	}
	
	
	public static void setIBAValue(Class clazz, IBAHolder ibaholder, HashMap<String, Object> ibamap) throws Exception {
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



	private static void setIBAValue2(Class clazz, IBAHolder ibaholder, HashMap<String, Object> softAttrs) throws WTException {
		List floatValueList = new ArrayList();
		List stringValueList = new ArrayList();
		List stringValuesList = new ArrayList();
		List integerValueList = new ArrayList();
		List ratioValueList = new ArrayList();
		List timestampValueList = new ArrayList();
		List booleanValueList = new ArrayList();
		List urlValueList = new ArrayList();
		List unitValueList = new ArrayList();
		
		for(Entry entry: softAttrs.entrySet()) {
			String ibaname = (String) entry.getKey();
			AttributeDefDefaultView view = getAttributeDefinition(ibaname);
			if(view instanceof FloatDefView) {
				FloatDefinition definition = (FloatDefinition) getDefinition(FloatDefinition.class, ibaname);
				if(definition == null) {
					continue;
				}
				Map map = new HashMap<>();
				map.put(definition, String.valueOf(entry.getValue()));
				floatValueList.add(map);
			}
		}
		
		if(floatValueList.size() > 0) {
			
		}
		
	}


	private static Object getDefinition(Class class1, String ibaname) throws WTException {
		Object object = null;
		QuerySpec qs = new QuerySpec(class1);
		qs.appendWhere(new SearchCondition(class1, "name", SearchCondition.EQUAL, ibaname), new int[] {});
		QueryResult qr = PersistenceServerHelper.manager.query(qs);
		while(qr.hasMoreElements()) {
			object = qr.nextElement();
		}
		
		return object;
	}


	private static AttributeDefDefaultView getAttributeDefinition(String ibaname) {
		
		return null;
	}


	private static void setStandardAttributeIBAValue(IBAHolder ibaholder, String ibaname, Object ibavalue, String dbColumnLabel) throws Exception {
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
	

	private static Object getTimestampDate(String _s) {
		String timeString = _s;
		Timestamp timestamp = null;
		if(timeString != null && !"".equals(timeString)) {
			timeString += "00:00:00";
		}
		java.text.SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date = new Date(timeString);
		simpleDateFormat.format(date);
		timestamp = new Timestamp(date.getTime());
		return timestamp;
	}


	private static void updateDB(String value, String dbColumnLabel, Persistable ibaholder) throws Exception {
		MethodContext methodContext = MethodContext.getContext();
		
		WTConnection wtConnection = null;
		PreparedStatement statement = null;
		
		try {
			String objectName = ibaholder.getClassInfo().getClassname();
			String name = objectName.substring(objectName.lastIndexOf(".")+1, objectName.length());
			String[] cols = dbColumnLabel.split(",");
			
			String sql = "update " + name + " set " + cols[0] + " = ? where ida2a2 = ?";
			
			wtConnection = (WTConnection) methodContext.getConnection();
			statement = wtConnection.prepareStatement(sql.toString());
			statement.setString(1, value);
			statement.setString(2, String.valueOf(PersistenceHelper.getObjectIdentifier(ibaholder).getId()));
			statement.execute();
			if(statement != null) {
				statement.close();
				statement = null;
			}
		}finally {
			if(statement != null) {
				statement.close();
				statement = null;
			}
		}
	}



	private static String getDatabaseColumnsLabel(AttributeDefinitionReadView attributeDefinitionReadView, String ibaname) throws WTException {
		String dbColumnLabel = null;
		if(attributeDefinitionReadView != null) {
			dbColumnLabel = attributeDefinitionReadView.getDatabaseColumnsLabel();
		}
		return dbColumnLabel;
	}
	
	
	
	public static List getConstraintRule(String type, String ibaName) throws WTException {
		List rules = new ArrayList();
		TypeIdentifier ti = TypedUtility.getTypeIdentifier(type);
		TypeDefinitionReadView view =TypeDefinitionServiceHelper.service.getTypeDefView(ti);
		AttributeDefinitionReadView attributeDefinitionReadView =  view.getAttributeByName(ibaName);
		for(ConstraintDefinitionReadView constraint : attributeDefinitionReadView.getAllConstraints()) {
			rules.add(constraint);
		}
		return rules;
	}
	
	
	
	
}
