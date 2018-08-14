package ext.training.mvc.builder;

import java.util.List;

import com.ptc.core.htmlcomp.components.ConfigurableTableBuilder;
import com.ptc.core.htmlcomp.tableview.ConfigurableTable;
import com.ptc.core.ui.resources.ComponentMode;
import com.ptc.jca.mvc.components.JcaColumnConfig;
import com.ptc.jca.mvc.components.JcaTableConfig;
import com.ptc.mvc.components.AbstractComponentConfigBuilder;
import com.ptc.mvc.components.ColumnConfig;
import com.ptc.mvc.components.ComponentConfig;
import com.ptc.mvc.components.ComponentConfigFactory;
import com.ptc.mvc.components.ComponentParams;
import com.ptc.mvc.components.TableConfig;

import wt.util.WTException;



public abstract class AbstractCscTableConfigBuilder extends AbstractComponentConfigBuilder implements ConfigurableTableBuilder {
	
	
	
	@Override
	public ConfigurableTable buildConfigurableTable(String s) throws WTException {
		
		return null;
	}
	
	
	@Override
	public ComponentConfig buildComponentConfig(ComponentParams componentparams) {
		
		ComponentConfigFactory componentconfigfactory = getComponentConfigFactory();
		JcaTableConfig tableconfig = (JcaTableConfig) componentconfigfactory.newTableConfig();
		tableconfig.setComponentMode(ComponentMode.VIEW);
		tableconfig.setLabel("PART_TABLE_LABEL");
		tableconfig.setSelectable(true);
		tableconfig.setType("com.acme.Pet");
		tableconfig.setConfigurable(true);
		
		tableconfig.setActionModel("pets list");
		tableconfig.setShowCount(true);
		tableconfig.setShowCustomViewLink(true);
		
		
		ColumnConfig columnconfig = componentconfigfactory.newColumnConfig("name", true);
		tableconfig.addComponent(columnconfig);
		tableconfig.addComponent(componentconfigfactory.newColumnConfig("kind", false));
		tableconfig.addComponent(componentconfigfactory.newColumnConfig("dateOfBirth", false));
		tableconfig.addComponent(componentconfigfactory.newColumnConfig("fixed", false));
		tableconfig.addComponent(componentconfigfactory.newColumnConfig("infoPageAction", false));
		
		tableconfig.addComponent(componentconfigfactory.newColumnConfig("thePersistInfo.modifyStamp", true));
		tableconfig.addComponent(componentconfigfactory.newColumnConfig("containerName", false));
		
		tableconfig.setView("/csc/cscMvcExampleTable.jsp");
		
		return tableconfig;
	}


	

}
