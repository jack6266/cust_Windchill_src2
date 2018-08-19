package ext.training.mvc.builder;

import com.ptc.jca.mvc.builders.DefaultInfoComponentBuilder;
import com.ptc.mvc.components.ComponentBuilder;
import com.ptc.mvc.components.ComponentId;
import com.ptc.mvc.components.ComponentParams;
import com.ptc.mvc.components.InfoConfig;
import com.ptc.mvc.components.TypeBased;

import wt.util.WTException;

@ComponentBuilder(ComponentId.INFOPAGE_ID)
@TypeBased("com.acme.Pet")
public class PetInfoBuilder extends DefaultInfoComponentBuilder {
	
	@Override
	protected InfoConfig buildInfoConfig(ComponentParams componentparams) throws WTException {
		InfoConfig info = getComponentConfigFactory().newInfoConfig();
		info.setTabSet("petDetails");
		return info;
	}
}
