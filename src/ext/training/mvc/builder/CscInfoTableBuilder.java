package ext.training.mvc.builder;

import org.apache.log4j.Logger;

import com.acme.Pet;
import com.ptc.jca.mvc.components.JcaTableConfig;
import com.ptc.mvc.components.ComponentBuilder;
import com.ptc.mvc.components.ComponentConfig;
import com.ptc.mvc.components.ComponentDataBuilder;
import com.ptc.mvc.components.ComponentParams;
import com.ptc.mvc.components.ds.DataSourceMode;
import com.ptc.mvc.ds.server.jmx.PerformanceConfig;

import wt.fc.PersistenceHelper;
import wt.fc.QueryResult;
import wt.log4j.LogR;
import wt.pds.PartialResultException;
import wt.query.QuerySpec;


@ComponentBuilder({"csc.custom.mvc.table"})
public class CscInfoTableBuilder extends AbstractCscTableConfigBuilder implements ComponentDataBuilder {

	
	private static final Logger log = LogR.getLogger(CscInfoTableBuilder.class.getName());
	
	@Override
	public Object buildComponentData(ComponentConfig componentconfig, ComponentParams componentparams) throws Exception {
		QuerySpec var3 = new QuerySpec(Pet.class);
		PerformanceConfig var4 = PerformanceConfig.getPerformanceConfig();
		int var5 = var4.getQueryLimit();
		var3.setQueryLimit(var5 > 3000 ? 3000 : var5);
		QueryResult var6 = null;

		try {
			var6 = PersistenceHelper.manager.find(var3);
		} catch (PartialResultException var8) {
			var6 = var8.getQueryResult();
			if (log.isDebugEnabled()) {
				log.debug("Performance config query  Limit reached " + var8.getMessage(), var8);
			}
		}

		return var6;
	}
	
	
	@Override
	public ComponentConfig buildComponentConfig(ComponentParams componentparams) {
		JcaTableConfig var2 = (JcaTableConfig) super.buildComponentConfig(componentparams);
		var2.setLabel(var2.getLabel() + " (Datasource disabled)");
		var2.setDataSourceMode(DataSourceMode.DISABLED);
		return var2;
	}



}
