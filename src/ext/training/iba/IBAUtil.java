package ext.training.iba;

import java.rmi.RemoteException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Locale;
import java.util.TimeZone;

import wt.iba.definition.DefinitionLoader;
import wt.iba.definition.litedefinition.AbstractAttributeDefinizerView;
import wt.iba.definition.litedefinition.AttributeDefDefaultView;
import wt.iba.definition.litedefinition.AttributeDefNodeView;
import wt.iba.definition.litedefinition.BooleanDefView;
import wt.iba.definition.litedefinition.FloatDefView;
import wt.iba.definition.litedefinition.IntegerDefView;
import wt.iba.definition.litedefinition.StringDefView;
import wt.iba.definition.litedefinition.TimestampDefView;
import wt.iba.definition.litedefinition.URLDefView;
import wt.iba.definition.litedefinition.UnitDefView;
import wt.iba.definition.service.IBADefinitionHelper;
import wt.iba.value.DefaultAttributeContainer;
import wt.iba.value.IBAHolder;
import wt.iba.value.IBAValueUtility;
import wt.iba.value.litevalue.AbstractValueView;
import wt.iba.value.litevalue.BooleanValueDefaultView;
import wt.iba.value.litevalue.FloatValueDefaultView;
import wt.iba.value.litevalue.IntegerValueDefaultView;
import wt.iba.value.litevalue.StringValueDefaultView;
import wt.iba.value.litevalue.TimestampValueDefaultView;
import wt.iba.value.litevalue.URLValueDefaultView;
import wt.iba.value.litevalue.UnitValueDefaultView;
import wt.iba.value.service.IBAValueHelper;
import wt.session.SessionHelper;
import wt.util.WTException;
import wt.util.WTPropertyVetoException;

/**
 * 本类主要用于对物件的软属性的获取和修改<br>
 * <p>
 * 类方法说明：<br>
 * （1）本类方法中没有对参考类型软属性的操作；<br>
 * （2）本类方法中对时间类型的软属性操作时存在8个小时的时区差异（待解决）；<br>
 * （3）在本方法中对URL的标识符没有修改。 <br>
 * （例如URL的地址为：www.baidu.com.cn 标识符为123则在软属性页面中显示的为123，点击后会自动跳向URL地址）<br>
 * 
 * @version 1.1
 */
public class IBAUtil {

	private IBAHolder ibaHolder;
	// 是否显示输出信息开关，True:输出;False:不输出
	private static boolean VERBOSE = true;
	// 定义Key为软属性名称，Value为软属性全部信息的Hashtable
	Hashtable ibaContainer;
	// 定义Key为软属性名称，Value为软属性的值的Hashtable
	Hashtable ibaNameValue;

	/**
	 * 构造方法，指定IBAHolder以便对其软属性进行操作。
	 * 
	 * @param ibaholder
	 *            指定的IBAHolder
	 */
	public IBAUtil(IBAHolder ibaholder) {
		initializeIBAPart(ibaholder);
	}

	/**
	 * 获取软属性s的值
	 * 
	 * @param s
	 *            软属性名称
	 * @return String 软属性的值
	 */
	public String getIBAValue(String s) {
		try {
			return getIBAValue(s, SessionHelper.manager.getLocale());
		} catch (WTException wte) {
			wte.printStackTrace();
		}
		return null;
	}
	

	public String getIBAValue(String s, Locale locale) {
		Object[] obj = (Object[]) ibaContainer.get(s);
		if (obj == null)
			return null;
		AbstractValueView[] abstractValueViews = (AbstractValueView[]) obj[1];
		AbstractValueView avv = (AbstractValueView) abstractValueViews[0];
		if (avv == null)
			return null;
		try {
			return IBAValueUtility.getLocalizedIBAValueDisplayString(avv, locale);
		} catch (WTException wte) {
			wte.printStackTrace();
		}
		return null;
	}
	
	
	public String[] getIBAValues(String s) {
		try {
			return getIBAValues(s, SessionHelper.manager.getLocale());
		} catch (WTException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public String[] getIBAValues(String s, Locale locale) throws WTException {
		Object[] obj = (Object[]) ibaContainer.get(s);
		if (obj == null)
			return null;
		AbstractValueView[] abstractValueViews = (AbstractValueView[]) obj[1];
		String[] values = new String[abstractValueViews.length];
		for(int i=0; i < abstractValueViews.length; i++ ) {
			AbstractValueView abstractvalueview = abstractValueViews[i]; 
			values[i] = IBAValueUtility.getLocalizedIBAValueDisplayString(abstractvalueview, locale);
		}
		return null;
	}
	
	
	public String getIBADisplayName(String s) {
		Object[] obj = (Object[]) ibaContainer.get(s);
		if(obj == null) {
			return null;
		}
		AbstractValueView abstractValueView = ((AbstractValueView[]) obj[1])[0];
		return abstractValueView.getDefinition().getDisplayName();
	}
	

	/**
	 * 获取软属性的名称和值
	 * @return Hashtable Key值为软属性名称，Value为软属性的值
	 */
	public Hashtable getNameAndValue() {
		return ibaNameValue;
	}

	/**
	 * 查找所有的软属性，并把相应的结果结果存放到ibaContainer，ibaNameValue
	 * @param ibaholder
	 *            指定的IBAHolder
	 */
	private void initializeIBAPart(IBAHolder ibaholder) {
		ibaContainer = new Hashtable();
		ibaNameValue = new Hashtable();
		try {// 更新IBAHolder
			this.ibaHolder = IBAValueHelper.service.refreshAttributeContainer(
					ibaholder, null, SessionHelper.manager.getLocale(), null);
			ibaholder = this.ibaHolder;
			// 获取物件所有的软属性的信息
			DefaultAttributeContainer defaultattributecontainer = (DefaultAttributeContainer) ibaholder
					.getAttributeContainer();
			if (defaultattributecontainer != null) {
				// 读取物件所有的软属性的信息
				AttributeDefDefaultView aattributedefdefaultview[] = defaultattributecontainer
						.getAttributeDefinitions();
				// 逐个获取软属性
				for (int i = 0; i < aattributedefdefaultview.length; i++) {
					// 获取详细信息
					AbstractValueView aabstractvalueview[] = defaultattributecontainer
							.getAttributeValues(aattributedefdefaultview[i]);
					if (aabstractvalueview != null) {
						Object aobj[] = new Object[2];
						aobj[0] = aattributedefdefaultview[i];
						aobj[1] = aabstractvalueview[0];
						// 解析出软属性的名称和值，并进行保存
						String name = aattributedefdefaultview[i].getName();
						String value = IBAValueUtility
								.getLocalizedIBAValueDisplayString(
										(AbstractValueView) aobj[1],
										SessionHelper.manager.getLocale());
						ibaContainer.put(name, ((aobj)));
						ibaNameValue.put(name, value);
					}
				}
			}
		} catch (Exception exception) {
			exception.printStackTrace();
		}
	}

	/**
	 * 修改好软属性值后更新容器的方法
	 * 
	 * @param ibaholder
	 *            指定的IBAHolder
	 * @return IBAHolder
	 * @throws java.lang.Exception
	 */
	public IBAHolder updateIBAPart(IBAHolder ibaholder) throws Exception {
		try {
			// 主要是更新ibaContainer中的内容
			initializeIBAPart(ibaholder);
			// 更新IBAHolder
			ibaholder = IBAValueHelper.service.refreshAttributeContainer(
					ibaholder, null, SessionHelper.manager.getLocale(), null);
			// 获取物件所有的软属性的信息
			DefaultAttributeContainer defaultattributecontainer = (DefaultAttributeContainer) ibaholder
					.getAttributeContainer();
			// 逐个查询
			for (Enumeration enumeration = ibaContainer.elements(); enumeration
					.hasMoreElements();) {
				Object aobj[] = (Object[]) enumeration.nextElement();
				AbstractValueView abstractvalueview = (AbstractValueView) aobj[1];
				AttributeDefDefaultView attributedefdefaultview = (AttributeDefDefaultView) aobj[0];
				if (abstractvalueview.getState() == 1) {
					defaultattributecontainer
							.deleteAttributeValues(attributedefdefaultview);
					abstractvalueview.setState(3);
					defaultattributecontainer
							.addAttributeValue(abstractvalueview);
				}
			}
			ibaholder.setAttributeContainer(defaultattributecontainer);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return ibaholder;
	}

	/**
	 * 获得存放软属性的容器
	 * 
	 * @param ibaHolder
	 *            指定的IBAHolder
	 * @return DefaultAttributeContainer
	 * @throws Exception
	 */
	public static DefaultAttributeContainer getContainer(IBAHolder ibaHolder)
			throws WTException, RemoteException {
		ibaHolder = IBAValueHelper.service
				.refreshAttributeContainerWithoutConstraints(ibaHolder);
		DefaultAttributeContainer defaultattributecontainer = (DefaultAttributeContainer) ibaHolder
				.getAttributeContainer();
		return defaultattributecontainer;
	}
	
	
	public void setIBAValue(String s, String s1) throws WTPropertyVetoException {
		AbstractValueView abstractValueView = null;
		AttributeDefDefaultView attributeDefDefaultView = null;
		Object[] aobj = (Object[]) ibaContainer.get(s);
		if(aobj != null) {
			abstractValueView = ((AbstractValueView[]) aobj[1])[0];
			attributeDefDefaultView = (AttributeDefDefaultView) aobj[0];
		}
		if(abstractValueView == null) {
			attributeDefDefaultView = getAttributeDefinition(s);
		}
		if(attributeDefDefaultView == null) {
			return;
		}
		abstractValueView = internalCreateValue(attributeDefDefaultView, s1);
		if(abstractValueView == null) {
			return ;
		}else {
			abstractValueView.setState(1);
			Object[] aobj1 = new Object[2];
			aobj[1] = attributeDefDefaultView;
			aobj[0] = abstractValueView;
			ibaContainer.put(attributeDefDefaultView.getName(), (Object) aobj1);
			return ;
		}
	}
	

	private AbstractValueView internalCreateValue(AttributeDefDefaultView attributeDefDefaultView, String s1) {
		AttributeDefDefaultView attributeDefDefaultView2 = null;
		
		return null;
	}

	/**
	 * 获取软属性值得所有信息
	 * 
	 * @param dac
	 * @param ibaName
	 *            软属性名称
	 * @param ibaClass
	 *            软属性类型
	 * @return AbstractValueView
	 * @throws java.lang.Exception
	 */
	public static AbstractValueView getIBAValueView(
			DefaultAttributeContainer dac, String ibaName, String ibaClass)
			throws Exception {
		AbstractValueView aabstractvalueview[] = null;
		AbstractValueView avv = null;
		aabstractvalueview = dac.getAttributeValues();
		for (int j = 0; j < aabstractvalueview.length; j++) {
			String thisIBAName = aabstractvalueview[j].getDefinition().getName();
			String thisIBAValue = IBAValueUtility
					.getLocalizedIBAValueDisplayString(aabstractvalueview[j],
							Locale.CHINA);
			String thisIBAClass = (aabstractvalueview[j].getDefinition())
					.getAttributeDefinitionClassName();
			if (thisIBAName.equals(ibaName) && thisIBAClass.equals(ibaClass)) {
				avv = aabstractvalueview[j];
				break;
			}
		}
		return avv;
	}

	/**
	 * 获取软属性值得所有信息
	 * 
	 * @param dac
	 * @param ibaName
	 *            软属性名称
	 * @return AbstractValueView
	 * @throws java.lang.Exception
	 */
	public static AbstractValueView getIBAValueViewDefault(
			DefaultAttributeContainer dac, String ibaName) throws Exception {
		AbstractValueView aabstractvalueview[] = null;
		AbstractValueView avv = null;
		aabstractvalueview = dac.getAttributeValues();
		for (int j = 0; j < aabstractvalueview.length; j++) {
			String thisIBAName = aabstractvalueview[j].getDefinition()
					.getName();
			if (thisIBAName.equals(ibaName)) {
				avv = aabstractvalueview[j];
				break;
			}
		}
		return avv;
	}

	/**
	 * 修改整型类型的软属性值
	 * 
	 * @param ibaholder
	 *            物件的IBAHolder
	 * @param ibaName
	 *            软属性的名称
	 * @param newStringValue
	 *            软属性的值
	 * @return IBAHolder 修改后的IBAHolder
	 * @throws wt.util.WTException
	 */
	public IBAHolder setIBAIntegerValue(IBAHolder ibaholder, String ibaName,
			String newStringValue) {
		// 将字符串解析为长整型
		if (VERBOSE) {
			printout("setIBAIntegerValue()");
			printout("IBAName: " + ibaName + " Value: " + newStringValue);
		}
		long newValue = Long.parseLong(newStringValue);
		try {
			DefaultAttributeContainer defaultattributecontainer = getContainer(ibaholder);
			if (defaultattributecontainer == null) {
				defaultattributecontainer = new DefaultAttributeContainer();
				ibaholder.setAttributeContainer(defaultattributecontainer);
			}
			AbstractValueView valueview = getIBAValueViewDefault(
					defaultattributecontainer, ibaName);
			if (valueview != null
					&& !(valueview instanceof IntegerValueDefaultView)) {
				return ibaholder;
			}
			IntegerValueDefaultView valuedefaultview = (IntegerValueDefaultView) getIBAValueView(
					defaultattributecontainer, ibaName,
					"wt.iba.definition.IntegerDefinition");
			if (valuedefaultview != null) {
				valuedefaultview.setValue(newValue);
				defaultattributecontainer
						.updateAttributeValue(valuedefaultview);
			} else {
				AttributeDefDefaultView attributedefdefaultview = getAttributeDefinition(ibaName);
				if (!(attributedefdefaultview instanceof IntegerDefView)) {
					return ibaholder;
				}
				IntegerValueDefaultView abstractvalueview = new IntegerValueDefaultView(
						(IntegerDefView) attributedefdefaultview);
				abstractvalueview.setValue(newValue);
				defaultattributecontainer.addAttributeValue(abstractvalueview);
			}
			ibaholder.setAttributeContainer(defaultattributecontainer);
			wt.iba.value.service.LoadValue.applySoftAttributes(ibaholder);
		} catch (Exception exception) {
			exception.printStackTrace();
		}
		return ibaholder;
	}

	/**
	 * 修改浮点类型的软属性值
	 * 
	 * @param ibaholder
	 *            物件的IBAHolder
	 * @param ibaName
	 *            软属性的名称
	 * @param newStringValue
	 *            软属性的值
	 * @return IBAHolder 修改后的IBAHolder
	 * @throws wt.util.WTException
	 */
	public IBAHolder setIBAFloatValue(IBAHolder ibaholder, String ibaName,
			String newStringValue) throws Exception {
		if (VERBOSE) {
			printout("setIBAFloatValue()");
			printout("IBAName: " + ibaName + " Value: " + newStringValue);
		}
		// 计算数据的精度
		int precision = 0;
		if (newStringValue.indexOf(".") >= 0) {
			precision = newStringValue.length() - newStringValue.indexOf(".")
					- 1;
		}
		// 把字符串数据转换为双精度型数据
		double newValue = Double.parseDouble(newStringValue);
		try {
			DefaultAttributeContainer defaultattributecontainer = getContainer(ibaholder);
			if (defaultattributecontainer == null) {
				defaultattributecontainer = new DefaultAttributeContainer();
				ibaholder.setAttributeContainer(defaultattributecontainer);
			}
			AbstractValueView valueview = getIBAValueViewDefault(
					defaultattributecontainer, ibaName);
			if (valueview != null
					&& !(valueview instanceof FloatValueDefaultView)) {
				return ibaholder;
			}
			FloatValueDefaultView valuedefaultview = (FloatValueDefaultView) getIBAValueView(
					defaultattributecontainer, ibaName,
					"wt.iba.definition.FloatDefinition");
			if (valuedefaultview != null) {
				valuedefaultview.setValue(newValue);
				valuedefaultview.setPrecision(precision);
				defaultattributecontainer
						.updateAttributeValue(valuedefaultview);
			} else {
				AttributeDefDefaultView attributedefdefaultview = getAttributeDefinition(ibaName);
				if (!(attributedefdefaultview instanceof FloatDefView)) {
					return ibaholder;
				}
				FloatValueDefaultView abstractvalueview = new FloatValueDefaultView(
						(FloatDefView) attributedefdefaultview);
				abstractvalueview.setValue(newValue);
				abstractvalueview.setPrecision(precision);
				defaultattributecontainer.addAttributeValue(abstractvalueview);
			}
			ibaholder.setAttributeContainer(defaultattributecontainer);
			wt.iba.value.service.LoadValue.applySoftAttributes(ibaholder);
		} catch (Exception exception) {
			exception.printStackTrace();
		}
		return ibaholder;
	}

	/**
	 * 修改带有单位的实数类型的软属性值
	 * 
	 * @param ibaholder
	 *            物件的IBAHolder
	 * @param ibaName
	 *            软属性的名称
	 * @param newStringValue
	 *            软属性的值
	 * @return IBAHolder 修改后的IBAHolder
	 * @throws wt.util.WTException
	 */
	public IBAHolder setIBAFloatUnitValue(IBAHolder ibaholder, String ibaName,
			String newStringValue) throws Exception {
		if (VERBOSE) {
			printout("setIBAFloatUnitValue()");
			printout("IBAName: " + ibaName + " Value: " + newStringValue);
		}
		// 计算数据的精度
		int precision = 0;
		if (newStringValue.indexOf(".") >= 0) {
			precision = newStringValue.length() - newStringValue.indexOf(".");
		}
		// 字符串转换为实数
		double newValue = Double.parseDouble(newStringValue);
		try {
			DefaultAttributeContainer defaultattributecontainer = getContainer(ibaholder);
			if (defaultattributecontainer == null) {
				defaultattributecontainer = new DefaultAttributeContainer();
				ibaholder.setAttributeContainer(defaultattributecontainer);
			}
			AbstractValueView valueview = getIBAValueViewDefault(
					defaultattributecontainer, ibaName);
			if (valueview != null
					&& !(valueview instanceof UnitValueDefaultView)) {
				return ibaholder;
			}
			UnitValueDefaultView valuedefaultview = (UnitValueDefaultView) getIBAValueView(
					defaultattributecontainer, ibaName,
					"wt.iba.definition.UnitDefinition");
			if (valuedefaultview != null) {
				valuedefaultview.setValue(newValue);
				valuedefaultview.setPrecision(precision);
				defaultattributecontainer
						.updateAttributeValue(valuedefaultview);
			} else {
				AttributeDefDefaultView attributedefdefaultview = getAttributeDefinition(ibaName);
				if (!(attributedefdefaultview instanceof UnitDefView)) {
					return ibaholder;
				}
				UnitValueDefaultView abstractvalueview = new UnitValueDefaultView(
						(UnitDefView) attributedefdefaultview);
				abstractvalueview.setValue(newValue);
				abstractvalueview.setPrecision(precision);
				defaultattributecontainer.addAttributeValue(abstractvalueview);
			}
			ibaholder.setAttributeContainer(defaultattributecontainer);
			wt.iba.value.service.LoadValue.applySoftAttributes(ibaholder);
		} catch (Exception exception) {
			exception.printStackTrace();
		}
		return ibaholder;
	}

	/**
	 * 修改布尔类型的软属性值
	 * 
	 * @param ibaholder
	 *            物件的IBAHolder
	 * @param ibaName
	 *            软属性的名称
	 * @param newStringValue
	 *            软属性的值
	 * @return IBAHolder 修改后的IBAHolder
	 * @throws wt.util.WTException
	 */
	public IBAHolder setIBABooleanValue(IBAHolder ibaholder, String ibaName,
			String newStringValue) {
		if (VERBOSE) {
			printout("tIBABooleanValue()");
			printout("IBAName: " + ibaName + " Value: " + newStringValue);
		}
		// 将字符串解析为布尔型
		boolean newValue = Boolean.parseBoolean(newStringValue);
		String ibaClass = "wt.iba.definition.BooleanDefinition";
		try {
			DefaultAttributeContainer defaultattributecontainer = getContainer(ibaHolder);
			if (defaultattributecontainer == null) {
				defaultattributecontainer = new DefaultAttributeContainer();
				ibaholder.setAttributeContainer(defaultattributecontainer);
			}
			AbstractValueView valueview = getIBAValueViewDefault(
					defaultattributecontainer, ibaName);
			if (valueview != null
					&& !(valueview instanceof BooleanValueDefaultView)) {
				return ibaholder;
			}
			BooleanValueDefaultView valuedefaultview = (BooleanValueDefaultView) getIBAValueView(
					defaultattributecontainer, ibaName, ibaClass);
			if (valuedefaultview != null) {
				valuedefaultview.setValue(newValue);
				defaultattributecontainer
						.updateAttributeValue(valuedefaultview);
			} else {
				AttributeDefDefaultView attributedefdefaultview = getAttributeDefinition(ibaName);
				if (!(attributedefdefaultview instanceof BooleanDefView)) {
					return ibaholder;
				}
				BooleanValueDefaultView abstractvalueview = new BooleanValueDefaultView(
						(BooleanDefView) attributedefdefaultview);
				abstractvalueview.setValue(newValue);
				defaultattributecontainer.addAttributeValue(abstractvalueview);
			}
			ibaholder.setAttributeContainer(defaultattributecontainer);
			wt.iba.value.service.LoadValue.applySoftAttributes(ibaholder);
			return ibaholder;
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return ibaholder;
	}

	/**
	 * 修改字符串类型的软属性值
	 * 
	 * @param ibaholder
	 *            物件的IBAHolder
	 * @param ibaName
	 *            软属性的名称
	 * @param newValue
	 *            软属性的值
	 * @return IBAHolder 修改后的IBAHolder
	 * @throws wt.util.WTException
	 */
	public IBAHolder setIBAStringValue(IBAHolder ibaholder, String ibaName,
			String newValue) throws Exception {
		if (VERBOSE) {
			printout("setIBAStringValue()");
			printout("IBAName: " + ibaName + " Value: " + newValue);
		}
		try {
			DefaultAttributeContainer defaultattributecontainer = getContainer(ibaholder);
			if (defaultattributecontainer == null) {
				defaultattributecontainer = new DefaultAttributeContainer();
				ibaholder.setAttributeContainer(defaultattributecontainer);
			}
			AbstractValueView valueview = getIBAValueViewDefault(
					defaultattributecontainer, ibaName);
			if (valueview != null
					&& !(valueview instanceof StringValueDefaultView)) {
				return ibaholder;
			}
			StringValueDefaultView stringvaluedefaultview = (StringValueDefaultView) getIBAValueView(
					defaultattributecontainer, ibaName,
					"wt.iba.definition.StringDefinition");
			if (stringvaluedefaultview != null) {
				stringvaluedefaultview.setValue(newValue);
				defaultattributecontainer
						.updateAttributeValue(stringvaluedefaultview);
			} else {
				AttributeDefDefaultView attributedefdefaultview = getAttributeDefinition(ibaName);
				if (!(attributedefdefaultview instanceof StringDefView)) {
					return ibaholder;
				}
				StringValueDefaultView abstractvalueview = new StringValueDefaultView(
						(StringDefView) attributedefdefaultview, newValue);
				defaultattributecontainer.addAttributeValue(abstractvalueview);
			}
			ibaholder.setAttributeContainer(defaultattributecontainer);
			wt.iba.value.service.LoadValue.applySoftAttributes(ibaholder);
		} catch (Exception exception) {
			exception.printStackTrace();
		}
		return ibaholder;
	}

	/**
	 * 修改URL类型的软属性值
	 * 
	 * @param ibaholder
	 *            物件的IBAHolder
	 * @param ibaName
	 *            软属性的名称
	 * @param newValue
	 *            软属性的值
	 * @return IBAHolder 修改后的IBAHolder
	 * @throws wt.util.WTException
	 */
	public IBAHolder setIBAURLValue(IBAHolder ibaholder, String ibaName,
			String newValue) throws Exception {
		if (VERBOSE) {
			printout("setIBAURLValue() ");
			printout("IBAName: " + ibaName + " Value: " + newValue);
		}
		try {
			DefaultAttributeContainer defaultattributecontainer = getContainer(ibaholder);
			if (defaultattributecontainer == null) {
				defaultattributecontainer = new DefaultAttributeContainer();
				ibaholder.setAttributeContainer(defaultattributecontainer);
			}
			AbstractValueView valueview = getIBAValueViewDefault(
					defaultattributecontainer, ibaName);
			if (valueview != null
					&& !(valueview instanceof URLValueDefaultView)) {
				return ibaholder;
			}
			URLValueDefaultView stringvaluedefaultview = (URLValueDefaultView) getIBAValueView(
					defaultattributecontainer, ibaName,
					"wt.iba.definition.URLDefinition");
			if (stringvaluedefaultview != null) {
				stringvaluedefaultview.setValue(newValue);
				defaultattributecontainer
						.updateAttributeValue(stringvaluedefaultview);
			} else {
				AttributeDefDefaultView attributedefdefaultview = getAttributeDefinition(ibaName);
				if (!(attributedefdefaultview instanceof URLDefView)) {
					return ibaholder;
				}
				URLValueDefaultView abstractvalueview = new URLValueDefaultView(
						(URLDefView) attributedefdefaultview, newValue,
						newValue);
				abstractvalueview.setValue(newValue);
				defaultattributecontainer.addAttributeValue(abstractvalueview);
			}
			ibaholder.setAttributeContainer(defaultattributecontainer);
			wt.iba.value.service.LoadValue.applySoftAttributes(ibaholder);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		return ibaholder;
	}

	/**
	 * 修改Timestamp类型的软属性值
	 * 
	 * @param ibaholder
	 *            物件的IBAHolder
	 * @param ibaName
	 *            软属性的名称
	 * @param newValue
	 *            软属性的值
	 * @return IBAHolder 修改后的IBAHolder
	 * @throws wt.util.WTException
	 */
	public IBAHolder setIBATimestampValue(IBAHolder ibaholder, String ibaName,
			String newValue) throws Exception {
		if (VERBOSE) {
			printout("setIBATimestampValue()");
			printout("IBAName: " + ibaName + " Value: " + newValue);
		}
		if (newValue.matches("\\d{4}-\\d{1,2}-\\d{1,2}")) {
			newValue = newValue + " 00:00:00.0";
		} else if (newValue
				.matches("\\d{4}-\\d{1,2}-\\d{1,2} \\d{1,2}:\\d{1,2}:\\d{ 1,2}")) {
			newValue = newValue + ".0";
		} else if (newValue
				.matches("\\d{4}-\\d{1,2}-\\d{1,2} \\d{1,2}:\\d{1,2}:\\d{ 1,2}.\\d{1,8}"))
			;
		else {
			printout("Wrong data format");
		}
		try {
			DefaultAttributeContainer defaultattributecontainer = getContainer(ibaholder);
			if (defaultattributecontainer == null) {
				defaultattributecontainer = new DefaultAttributeContainer();
				ibaholder.setAttributeContainer(defaultattributecontainer);
			}
			AbstractValueView valueview = getIBAValueViewDefault(
					defaultattributecontainer, ibaName);
			if (valueview != null
					&& !(valueview instanceof TimestampValueDefaultView)) {
				return ibaholder;
			}
			TimestampValueDefaultView valuedefaultview = (TimestampValueDefaultView) getIBAValueView(
					defaultattributecontainer, ibaName,
					"wt.iba.definition.TimestampDefinition");
			if (valuedefaultview != null) {
				valuedefaultview.setValue(getCurrentTime(newValue));
				defaultattributecontainer
						.updateAttributeValue(valuedefaultview);
			} else {
				AttributeDefDefaultView attributedefdefaultview = getAttributeDefinition(ibaName);
				if (!(attributedefdefaultview instanceof TimestampDefView)) {
					return this.ibaHolder;
				}
				TimestampValueDefaultView abstractvalueview = new TimestampValueDefaultView(
						(TimestampDefView) attributedefdefaultview);
				abstractvalueview.setValue(getCurrentTime(newValue));
				defaultattributecontainer.addAttributeValue(abstractvalueview);
			}
			ibaholder.setAttributeContainer(defaultattributecontainer);
			wt.iba.value.service.LoadValue.applySoftAttributes(ibaholder);
		} catch (Exception exception) {
			exception.printStackTrace();
		}
		return ibaholder;
	}

	/**
	 * 根据软属性的名称获取软属性的默认属性
	 * 
	 * @param s
	 *            软属性名称
	 * @return AttributeDefDefaultView
	 */
	public static AttributeDefDefaultView getAttributeDefinition(String s) {
		AttributeDefDefaultView attributedefdefaultview = null;
		try {
			attributedefdefaultview = IBADefinitionHelper.service
					.getAttributeDefDefaultViewByPath(s);
			if (attributedefdefaultview == null) {
				AbstractAttributeDefinizerView abstractattributedefinizerview = DefinitionLoader
						.getAttributeDefinition(s);
				if (abstractattributedefinizerview != null) {
					attributedefdefaultview = IBADefinitionHelper.service
							.getAttributeDefDefaultView((AttributeDefNodeView) abstractattributedefinizerview);
				}
			}
		} catch (Exception exception) {
			exception.printStackTrace();
		}
		return attributedefdefaultview;
	}

	/**
	 * 设置布尔类型的软属性
	 * 
	 * @param defaultattributecontainer
	 *            存放软属性的默认容器
	 * @param ibaName
	 *            软属性名称
	 * @param newibaValue
	 *            软属性值
	 * @return DefaultAttributeContainer 存放软属性的默认容器
	 * @throws java.lang.Exception
	 */
	private DefaultAttributeContainer setIBABooleanValue(
			DefaultAttributeContainer defaultattributecontainer,
			String ibaName, String newibaValue) throws Exception {
		if (VERBOSE) {
			printout("setIBABooleanValue()");
		}
		boolean newValue = Boolean.parseBoolean(newibaValue);
		BooleanValueDefaultView valuedefaultview = (BooleanValueDefaultView) getIBAValueView(
				defaultattributecontainer, ibaName,
				"wt.iba.definition.BooleanDefinition");
		if (valuedefaultview != null) {
			valuedefaultview.setValue(newValue);
			defaultattributecontainer.updateAttributeValue(valuedefaultview);
		} else {
			AttributeDefDefaultView attributedefdefaultview = getAttributeDefinition(ibaName);
			if (!(attributedefdefaultview instanceof BooleanDefView)) {
				return defaultattributecontainer;
			}
			BooleanValueDefaultView abstractvalueview = new BooleanValueDefaultView(
					(BooleanDefView) attributedefdefaultview);
			abstractvalueview.setValue(newValue);
			defaultattributecontainer.addAttributeValue(abstractvalueview);
		}
		return defaultattributecontainer;
	}

	/**
	 * 设置URL类型的软属性
	 * 
	 * @param defaultattributecontainer
	 *            存放软属性的默认容器
	 * @param ibaName
	 *            软属性名称
	 * @param newibaValue
	 *            软属性值
	 * @return DefaultAttributeContainer 存放软属性的默认容器
	 * @throws java.lang.Exception
	 */
	private DefaultAttributeContainer setIBAURLValue(
			DefaultAttributeContainer defaultattributecontainer,
			String ibaName, String newibaValue) throws Exception {
		if (VERBOSE) {
			printout("setIBAURLValue()");
		}
		URLValueDefaultView stringvaluedefaultview = (URLValueDefaultView) getIBAValueView(
				defaultattributecontainer, ibaName,
				"wt.iba.definition.URLDefinition");
		if (stringvaluedefaultview != null) {
			stringvaluedefaultview.setValue(newibaValue);
			defaultattributecontainer
					.updateAttributeValue(stringvaluedefaultview);
		} else {
			AttributeDefDefaultView attributedefdefaultview = getAttributeDefinition(ibaName);
			if (!(attributedefdefaultview instanceof URLDefView)) {
				return defaultattributecontainer;
			}
			URLValueDefaultView abstractvalueview = new URLValueDefaultView(
					(URLDefView) attributedefdefaultview, newibaValue,
					newibaValue);
			abstractvalueview.setValue(newibaValue);
			defaultattributecontainer.addAttributeValue(abstractvalueview);
		}
		return defaultattributecontainer;
	}

	/**
	 * 设置字符型类型的软属性
	 * 
	 * @param defaultattributecontainer
	 *            存放软属性的默认容器
	 * @param ibaName
	 *            软属性名称
	 * @param newibaValue
	 *            软属性值
	 * @return DefaultAttributeContainer 存放软属性的默认容器
	 * @throws java.lang.Exception
	 */
	// private DefaultAttributeContainer setIBAStringValue(
	// DefaultAttributeContainer defaultattributecontainer,
	// String ibaName, String newibaValue) throws Exception {
	// if (VERBOSE) {
	// printout("setIBAStringValue()");
	// }
	// StringValueDefaultView stringvaluedefaultview = (StringValueDefaultView)
	// getIBAValueView(
	// defaultattributecontainer, ibaName,
	// "wt.iba.definition.StringDefinition");
	// if (stringvaluedefaultview != null) {
	// stringvaluedefaultview.setValue(newibaValue);
	// defaultattributecontainer
	// .updateAttributeValue(stringvaluedefaultview);
	// } else {
	// AttributeDefDefaultView attributedefdefaultview =
	// getAttributeDefinition(ibaName);
	// if (!(attributedefdefaultview instanceof StringDefView)) {
	// return defaultattributecontainer;
	// }
	// StringValueDefaultView abstractvalueview = new StringValueDefaultView(
	// (StringDefView) attributedefdefaultview, newibaValue);
	// defaultattributecontainer.addAttributeValue(abstractvalueview);
	// }
	// return defaultattributecontainer;
	// }
	/**
	 * 设置实数类型的软属性
	 * 
	 * @param defaultattributecontainer
	 *            存放软属性的默认容器
	 * @param ibaName
	 *            软属性名称
	 * @param newibaValue
	 *            软属性值
	 * @return DefaultAttributeContainer 存放软属性的默认容器
	 * @throws java.lang.Exception
	 */
	private DefaultAttributeContainer setIBAFloatValue(
			DefaultAttributeContainer defaultattributecontainer,
			String ibaName, String newibaValue) throws Exception {
		if (VERBOSE) {
			printout("setIBAFloatValue()");
		}
		int precision = 0;
		if (newibaValue.indexOf(".") >= 0) {
			precision = newibaValue.length() - newibaValue.indexOf(".");
		}
		double newValue = Double.parseDouble(newibaValue);
		FloatValueDefaultView valuedefaultview = (FloatValueDefaultView) getIBAValueView(
				defaultattributecontainer, ibaName,
				"wt.iba.definition.FloatDefinition");
		if (valuedefaultview != null) {
			valuedefaultview.setValue(newValue);
			valuedefaultview.setPrecision(precision);
			defaultattributecontainer.updateAttributeValue(valuedefaultview);
		} else {
			AttributeDefDefaultView attributedefdefaultview = getAttributeDefinition(ibaName);
			if (!(attributedefdefaultview instanceof FloatDefView)) {
				return defaultattributecontainer;
			}
			FloatValueDefaultView abstractvalueview = new FloatValueDefaultView(
					(FloatDefView) attributedefdefaultview);
			abstractvalueview.setValue(newValue);
			abstractvalueview.setPrecision(precision);
			defaultattributecontainer.addAttributeValue(abstractvalueview);
		}
		return defaultattributecontainer;
	}

	/**
	 * 设置带单位的实数类型的软属性
	 * 
	 * @param defaultattributecontainer
	 *            存放软属性的默认容器
	 * @param ibaName
	 *            软属性名称
	 * @param newibaValue
	 *            软属性值
	 * @return DefaultAttributeContainer 存放软属性的默认容器
	 * @throws java.lang.Exception
	 */
	private DefaultAttributeContainer setIBAUnitFloatValue(
			DefaultAttributeContainer defaultattributecontainer,
			String ibaName, String newibaValue) throws Exception {
		if (VERBOSE) {
			printout("setIBAUnitFloatValue()");
		}
		int precision = 0;
		if (newibaValue.indexOf(".") >= 0) {
			precision = newibaValue.length() - newibaValue.indexOf(".");
		}
		double newValue = Double.parseDouble(newibaValue);
		UnitValueDefaultView valuedefaultview = (UnitValueDefaultView) getIBAValueView(
				defaultattributecontainer, ibaName,
				"wt.iba.definition.UnitDefinition");
		if (valuedefaultview != null) {
			valuedefaultview.setValue(newValue);
			valuedefaultview.setPrecision(precision);
			defaultattributecontainer.updateAttributeValue(valuedefaultview);
		} else {
			AttributeDefDefaultView attributedefdefaultview = getAttributeDefinition(ibaName);
			if (!(attributedefdefaultview instanceof UnitDefView)) {
				return defaultattributecontainer;
			}
			UnitValueDefaultView abstractvalueview = new UnitValueDefaultView(
					(UnitDefView) attributedefdefaultview);
			abstractvalueview.setValue(newValue);
			abstractvalueview.setPrecision(precision);
			defaultattributecontainer.addAttributeValue(abstractvalueview);
		}
		return defaultattributecontainer;
	}

	/**
	 * 设置整数类型的软属性
	 * 
	 * @param defaultattributecontainer
	 *            存放软属性的默认容器
	 * @param ibaName
	 *            软属性名称
	 * @param newibaValue
	 *            软属性值
	 * @return DefaultAttributeContainer 存放软属性的默认容器
	 * @throws java.lang.Exception
	 */
	private DefaultAttributeContainer setIBAIntegerValue(
			DefaultAttributeContainer defaultattributecontainer,
			String ibaName, String newibaValue) throws Exception {
		if (VERBOSE) {
			printout("setIBAIntegerValue()");
		}
		long newValue = Long.parseLong(newibaValue);
		IntegerValueDefaultView valuedefaultview = (IntegerValueDefaultView) getIBAValueView(
				defaultattributecontainer, ibaName,
				"wt.iba.definition.IntegerDefinition");
		if (valuedefaultview != null) {
			valuedefaultview.setValue(newValue);
			defaultattributecontainer.updateAttributeValue(valuedefaultview);
		} else {
			AttributeDefDefaultView attributedefdefaultview = getAttributeDefinition(ibaName);
			if (!(attributedefdefaultview instanceof IntegerDefView)) {
				return defaultattributecontainer;
			}
			IntegerValueDefaultView abstractvalueview = new IntegerValueDefaultView(
					(IntegerDefView) attributedefdefaultview);
			abstractvalueview.setValue(newValue);
			defaultattributecontainer.addAttributeValue(abstractvalueview);
		}
		return defaultattributecontainer;
	}

	/**
	 * 设置时间类型的软属性
	 * 
	 * @param defaultattributecontainer
	 *            存放软属性的默认容器
	 * @param ibaName
	 *            软属性名称
	 * @param newibaValue
	 *            软属性值
	 * @return DefaultAttributeContainer 存放软属性的默认容器
	 * @throws java.lang.Exception
	 */
	private DefaultAttributeContainer setIBATimestampValue(
			DefaultAttributeContainer defaultattributecontainer,
			String ibaName, String newibaValue) throws Exception {
		if (VERBOSE) {
			printout("setIBATimestampValue()");
		}
		if (newibaValue.matches("\\d{4}-\\d{1,2}-\\d{1,2}")) {
			newibaValue = newibaValue + " 00:00:00.0";
		} else if (newibaValue
				.matches("\\d{4}-\\d{1,2}-\\d{1,2} \\d{1,2}:\\d{1,2}:\\d{ 1,2}")) {
			newibaValue = newibaValue + ".0";
		} else if (newibaValue
				.matches("\\d{4}-\\d{1,2}-\\d{1,2} \\d{1,2}:\\d{1,2}:\\d{ 1,2}.\\d{1,3}"))
			;
		else {
			System.out.println("Wrong data format");
		}
		TimestampValueDefaultView valuedefaultview = (TimestampValueDefaultView) getIBAValueView(
				defaultattributecontainer, ibaName,
				"wt.iba.definition.TimestampDefinition");
		if (valuedefaultview != null) {
			valuedefaultview.setValue(getCurrentTime(newibaValue));
			defaultattributecontainer.updateAttributeValue(valuedefaultview);
		} else {
			AttributeDefDefaultView attributedefdefaultview = getAttributeDefinition(ibaName);
			if (!(attributedefdefaultview instanceof TimestampDefView)) {
				return defaultattributecontainer;
			}
			TimestampValueDefaultView abstractvalueview = new TimestampValueDefaultView(
					(TimestampDefView) attributedefdefaultview);
			abstractvalueview.setValue(getCurrentTime(newibaValue));
			defaultattributecontainer.addAttributeValue(abstractvalueview);
		}
		return defaultattributecontainer;
	}

	/**
	 * 时间字符时区转换
	 * 
	 * @param timeForm
	 *            表示时间的字符串
	 * @return String 时区转换后的字符
	 */
	private static Timestamp getCurrentTime(String timeDate) {
		if (timeDate == null) {
			return null;
		}
		Timestamp temp = Timestamp.valueOf(timeDate);
		// 设置字符格式。
		// 注意其中字母的大小写代表表示的进制不同，具体参考JavaAPI中关于SimpleDateFormat的介绍
		String timeForm = "yyyy-MM-dd HH:mm:ss.sssssssss";
		SimpleDateFormat simpledateformat = new SimpleDateFormat(timeForm);
		TimeZone timezone = TimeZone.getTimeZone("GTM");
		simpledateformat.setTimeZone(timezone);
		temp = Timestamp.valueOf(simpledateformat.format(temp));
		if (VERBOSE) {
			printout("=================>" + temp);
		}
		return temp;
	}

	/**
	 * 设置物件的软属性
	 * 
	 * @param ibaName
	 *            软属性名称
	 * @param newibaValue
	 *            软属性的值
	 * @return IBAHolder 软属性修改后的物件对象
	 * @throws java.lang.Exception
	 */
	// public IBAHolder setIBAValue(String ibaName, String newibaValue)
	// throws Exception {
	// if (VERBOSE) {
	// printout("setIBAValue()");
	// }
	// DefaultAttributeContainer defaultattributecontainer =
	// getContainer(this.ibaHolder);
	// if (defaultattributecontainer == null) {
	// defaultattributecontainer = new DefaultAttributeContainer();
	// this.ibaHolder.setAttributeContainer(defaultattributecontainer);
	// }
	// // 根据软属性的名称获取软属性的默认类型
	// AttributeDefDefaultView attributedefdefaultview =
	// getAttributeDefinition(ibaName);
	// if (attributedefdefaultview instanceof BooleanDefView) {
	// defaultattributecontainer = setIBABooleanValue(
	// defaultattributecontainer, ibaName, newibaValue);
	// } else if (attributedefdefaultview instanceof URLDefView) {
	// defaultattributecontainer = setIBAURLValue(
	// defaultattributecontainer, ibaName, newibaValue);
	// } else if (attributedefdefaultview instanceof StringDefView) {
	// defaultattributecontainer = setIBAStringValue(
	// defaultattributecontainer, ibaName, newibaValue);
	// } else if (attributedefdefaultview instanceof FloatDefView) {
	// defaultattributecontainer = setIBAFloatValue(
	// defaultattributecontainer, ibaName, newibaValue);
	// } else if (attributedefdefaultview instanceof UnitDefView) {
	// defaultattributecontainer = setIBAUnitFloatValue(
	// defaultattributecontainer, ibaName, newibaValue);
	// } else if (attributedefdefaultview instanceof IntegerDefView) {
	// defaultattributecontainer = setIBAIntegerValue(
	// defaultattributecontainer, ibaName, newibaValue);
	// } else if (attributedefdefaultview instanceof TimestampDefView) {
	// defaultattributecontainer = setIBATimestampValue(
	// defaultattributecontainer, ibaName, newibaValue);
	// }
	// this.ibaHolder.setAttributeContainer(defaultattributecontainer);
	// // 保存设置和修改，此行不可缺省，否则软属性操作失败
	// wt.iba.value.service.LoadValue.applySoftAttributes(this.ibaHolder);
	// if (VERBOSE) {
	// printout("IBA Name: " + ibaName + " Vlaue:" + newibaValue);
	// }
	// ibaNameValue.put(ibaName, newibaValue);
	// return this.ibaHolder;
	// }
	/**
	 * 文字输出格式的规范方法
	 * 
	 * @param out
	 *            要输出的文字信息
	 */

	private static void printout(String out) {
		if (out != null && !out.equals("")) {
			if (out.length() > 40) {
				System.out.println(out);
			} else {
				System.out.print("**********\t");
				System.out.print(out);
				System.out.println("\t**********");
			}
		}
	}
}