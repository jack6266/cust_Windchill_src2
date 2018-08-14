package ext.training.mvc.builder;

import com.acme.Pet;
import com.ptc.core.components.descriptor.DescriptorConstants.ColumnIdentifiers;
import com.ptc.mvc.components.AbstractComponentBuilder;
import com.ptc.mvc.components.ColumnConfig;
import com.ptc.mvc.components.ComponentBuilder;
import com.ptc.mvc.components.ComponentConfig;
import com.ptc.mvc.components.ComponentConfigFactory;
import com.ptc.mvc.components.ComponentParams;
import com.ptc.mvc.components.TableConfig;

import ext.training.jca.acmeManagerResource;
import wt.fc.PersistenceHelper;
import wt.query.QuerySpec;
import wt.util.WTException;
import wt.util.WTMessage;


@ComponentBuilder("acme.pet.table")
public class PetTable extends AbstractComponentBuilder {

	static final String RESOURCE = acmeManagerResource.class.getName();

	@Override
	public Object buildComponentData(ComponentConfig arg0, ComponentParams arg1) throws Exception {

		return PersistenceHelper.manager.find(new QuerySpec(Pet.class));
	}

	@Override
	public ComponentConfig buildComponentConfig(ComponentParams arg0) throws WTException {
		final ComponentConfigFactory factory = getComponentConfigFactory();

		TableConfig table = null;
		table = factory.newTableConfig();
		table.setType(Pet.class.getName());
		table.setLabel(WTMessage.getLocalizedMessage(RESOURCE, acmeManagerResource.PET_TABLE_LABEL, null));
		table.setSelectable(true);
		table.setActionModel("pets list");
		table.setShowCount(true);
		table.setShowCustomViewLink(false);

		ColumnConfig name = null;
		name = factory.newColumnConfig(Pet.NAME, true);
		name.setInfoPageLink(true);
		name.setSortable(true);

		table.addComponent(name);
		table.addComponent(getColumn(ColumnIdentifiers.INFO_ACTION, factory));
		table.addComponent(getColumn(ColumnIdentifiers.NM_ACTIONS, factory));
		table.addComponent(getColumn(ColumnIdentifiers.LAST_MODIFIED, factory));
		table.addComponent(getColumn(Pet.KIND, factory));
		table.addComponent(getColumn(Pet.DATE_OF_BIRTH, factory));
		table.addComponent(getColumn(Pet.FIXED, factory));
		return table;
	}

	private ColumnConfig getColumn(final String id, final ComponentConfigFactory factory) {
		final ColumnConfig column = factory.newColumnConfig(id, true);
		column.setSortable(false);
		return column;
	}

}
