package org.orienteer.camel.widget;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.inject.Inject;
import com.orientechnologies.orient.core.metadata.schema.OClass;
import com.orientechnologies.orient.core.record.impl.ODocument;

import org.apache.camel.CamelContext;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.model.RouteDefinition;
import org.apache.wicket.Component;
import org.apache.wicket.MetaDataKey;
import org.apache.wicket.authorization.UnauthorizedActionException;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.ResourceModel;
import org.orienteer.camel.component.OrientDBComponent;
import org.orienteer.camel.tasks.CamelEventHandler;
import org.orienteer.camel.tasks.OCamelTaskSession;
import org.orienteer.camel.tasks.OCamelTaskSessionCallback;
import org.orienteer.core.behavior.UpdateOnActionPerformedEventBehavior;
import org.orienteer.core.component.FAIcon;
import org.orienteer.core.component.FAIconType;
import org.orienteer.core.component.property.DisplayMode;
import org.orienteer.core.component.table.OEntityColumn;
import org.orienteer.core.component.table.OPropertyValueColumn;
import org.orienteer.core.component.table.OrienteerDataTable;
import org.orienteer.core.component.table.component.GenericTablePanel;
import org.orienteer.core.service.IOClassIntrospector;
import org.orienteer.core.tasks.ITaskSession;
import org.orienteer.core.widget.AbstractWidget;
import org.orienteer.core.widget.Widget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.ydn.wicket.wicketorientdb.IOrientDbSettings;
import ru.ydn.wicket.wicketorientdb.OrientDbWebApplication;
import ru.ydn.wicket.wicketorientdb.OrientDbWebSession;
import ru.ydn.wicket.wicketorientdb.model.OQueryDataProvider;

import java.io.ByteArrayInputStream;
/**
 * Widget for Orienteer Camel integration, linked to OIntegrationConfig
 *
 */

@Widget(domain="document",selector="OIntegrationConfig", id=CamelWidget.WIDGET_TYPE_ID, order=20, autoEnable=true)
public class CamelWidget extends AbstractWidget<ODocument>{

	public static final String WIDGET_TYPE_ID = "camelIntegration";
	private static final long serialVersionUID = 1L;
	private static final Logger LOG = LoggerFactory.getLogger(CamelWidget.class);
	
	public static final MetaDataKey<Map<String,CamelContext>> INTEGRATION_SESSIONS_KEY = new MetaDataKey<Map<String,CamelContext>>()
	{
		private static final long serialVersionUID = 1L;
	};
	
	private final GenericTablePanel<ODocument> tablePanel;

	private static final List<String> COLUMNS_NAMES = new ArrayList<String>();
	static{
		COLUMNS_NAMES.add(ITaskSession.Field.THREAD_NAME.fieldName());
		COLUMNS_NAMES.add(ITaskSession.Field.STATUS.fieldName());
		COLUMNS_NAMES.add(ITaskSession.Field.START_TIMESTAMP.fieldName());
		COLUMNS_NAMES.add(ITaskSession.Field.FINISH_TIMESTAMP.fieldName());
		COLUMNS_NAMES.add(ITaskSession.Field.PROGRESS_CURRENT.fieldName());
		COLUMNS_NAMES.add(ITaskSession.Field.PROGRESS_FINAL.fieldName());
	}
	
	@Inject
	private IOClassIntrospector oClassIntrospector;
	
	
	public CamelWidget(String id, IModel<ODocument> model, final IModel<ODocument> widgetDocumentModel) {
		super(id, model, widgetDocumentModel);


		IModel<DisplayMode> modeModel = DisplayMode.VIEW.asModel();
		
		OClass taskSessionClass = getModelObject().getDatabase().getMetadata().getSchema().getClass(OCamelTaskSession.TASK_SESSION_CLASS);
		
		List<IColumn<ODocument, String>> columns = makeColumns(taskSessionClass);
		
		OQueryDataProvider<ODocument> provider = new OQueryDataProvider<ODocument>(
				"select from "+OCamelTaskSession.TASK_SESSION_CLASS+" where "+
				OCamelTaskSession.Field.CONFIG.fieldName()+"="+CamelWidget.this.getModelObject().getIdentity());
		oClassIntrospector.defineDefaultSorting(provider, taskSessionClass);

		tablePanel = new GenericTablePanel<ODocument>("tablePanel", columns, provider, 20);
		OrienteerDataTable<ODocument, String> table = tablePanel.getDataTable();

		add(tablePanel);
		add(UpdateOnActionPerformedEventBehavior.INSTANCE_ALL_CONTINUE);

	}
	
	private List<IColumn<ODocument, String>> makeColumns(OClass taskSessionClass) {
		ArrayList<IColumn<ODocument, String>> columns = new ArrayList<IColumn<ODocument,String>>();
		boolean makeLink = true;
		for (String string : COLUMNS_NAMES) {
			if (makeLink){
				columns.add(new OEntityColumn(taskSessionClass.getProperty(string),true, DisplayMode.VIEW.asModel()));
				makeLink = false;
			}else{
				columns.add(new OPropertyValueColumn(string,taskSessionClass.getProperty(string), DisplayMode.VIEW.asModel()));		
			}
		}
		return columns;
	}	
	
	@Override
	protected FAIcon newIcon(String id) {
        return new FAIcon(id, FAIconType.bars);
	}

	@Override
	protected IModel<String> getDefaultTitleModel() {
		return new ResourceModel("integration.camel");
	}
	
	@Override
	protected String getWidgetStyleClass() {
		return "strict";
	}

}
