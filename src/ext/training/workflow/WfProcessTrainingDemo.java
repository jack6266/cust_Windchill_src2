package ext.training.workflow;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import wt.fc.ObjectReference;
import wt.fc.Persistable;
import wt.fc.PersistenceHelper;
import wt.fc.PersistenceServerHelper;
import wt.fc.QueryResult;
import wt.fc.ReferenceFactory;
import wt.fc.WTObject;
import wt.inf.container.WTContained;
import wt.inf.container.WTContainerHelper;
import wt.inf.container.WTContainerRef;
import wt.lifecycle.LifeCycleHelper;
import wt.lifecycle.LifeCycleManaged;
import wt.lifecycle.LifeCycleState;
import wt.lifecycle.LifeCycleTemplate;
import wt.lifecycle.LifeCycleTemplateMaster;
import wt.lifecycle.LifeCycleTemplateReference;
import wt.lifecycle.State;
import wt.method.RemoteAccess;
import wt.org.WTPrincipal;
import wt.pom.PersistentObjectManager;
import wt.pom.Transaction;
import wt.project.Role;
import wt.session.SessionHelper;
import wt.team.Team;
import wt.team.TeamException;
import wt.team.TeamHelper;
import wt.team.TeamManaged;
import wt.team.TeamReference;
import wt.util.WTException;
import wt.util.WTInvalidParameterException;
import wt.util.WTPropertyVetoException;
import wt.util.WTRuntimeException;
import wt.vc.VersionControlHelper;
import wt.workflow.definer.ProcessDataInfo;
import wt.workflow.definer.WfDefinerHelper;
import wt.workflow.definer.WfProcessDefinition;
import wt.workflow.engine.InvalidDataException;
import wt.workflow.engine.ProcessData;
import wt.workflow.engine.WfActivity;
import wt.workflow.engine.WfAdHocActivity;
import wt.workflow.engine.WfContainer;
import wt.workflow.engine.WfEngineHelper;
import wt.workflow.engine.WfExecutionObject;
import wt.workflow.engine.WfProcess;
import wt.workflow.engine.WfRequesterActivity;
import wt.workflow.engine.WfState;
import wt.workflow.engine.WfVariable;
import wt.workflow.work.WfAssignedActivity;
import wt.workflow.work.WorkItem;

public class WfProcessTrainingDemo implements RemoteAccess, Serializable {

	
	
	public static void startProcess(String processName, String workflowTemplateName, Map parameters) throws WTException, WTPropertyVetoException {
		Transaction trx = null;
		try {
			if(!PersistentObjectManager.getPom().isTransactionActive()) {
				trx = new Transaction();
				trx.start();
			}
	
			WTContainerRef wtContainerRef = null;
			TeamReference teamReference = null;
			Object primaryBussinessObject = parameters.get("primaryBusinessObject");
			if(primaryBussinessObject instanceof WTContained) {
				wtContainerRef = ((WTContained) primaryBussinessObject).getContainerReference();
			}
			if(primaryBussinessObject instanceof TeamManaged) {
				teamReference = ((TeamManaged) primaryBussinessObject).getTeamId();
			}
			WfProcessDefinition wfProcessDefinition = WfDefinerHelper.service.getProcessDefinition(workflowTemplateName);
			
			WfProcess wfprocess = null;
			if(wtContainerRef != null) {
				wfprocess = WfEngineHelper.service.createProcess(wfProcessDefinition, teamReference, wtContainerRef);
			} else {
				wfprocess = WfEngineHelper.service.createProcess(wfProcessDefinition, teamReference);
			}
			
			wfprocess.setName(processName);
			wfprocess.setCreator(SessionHelper.manager.getPrincipalReference());
			ProcessData processData = getProcessData(wfprocess, parameters);
			if(processData == null) {
				wfprocess.setContext(ProcessData.newProcessData(ProcessDataInfo.newProcessDataInfo()));
			}
			
			wfprocess = wfprocess.start(processData, true, WTContainerHelper.getExchangeRef());
			
			if(trx != null) {
				trx.commit();
				trx = null;
			}
		} finally {
			if(trx != null) {
				trx.rollback();
				trx = null;
			}
		}
	}
	
	
	
	public static ProcessData getProcessData(WfExecutionObject process, Map parameters) throws InvalidDataException {
		ProcessData processData = process.getContext();
		if(processData == null) {
			return null;
		}
		Iterator iterator = parameters.keySet().iterator();
		while(iterator.hasNext()) {
			String key = (String) iterator.next();
			Object value = parameters.get(key);
			if(value != null) {
				processData.setValue(key, value);
			}
		}
		return processData;
	}
	
	
	
	public static void workItemAssigned(WorkItem workitem, WTPrincipal oldPrincipal, WTPrincipal newPrincipal) throws WTException {
		WfAssignedActivity activity = (WfAssignedActivity) workitem.getSource().getObject();
		workitem.getOwnership().getOwner().setObject(newPrincipal);
		PersistenceServerHelper.manager.update(workitem);
	}
	
	
	public static void replaceTeamPrincipal(WfProcess wfprocess, Role role, WTPrincipal oldPrincipal, WTPrincipal newPrincipal) throws TeamException, WTException {
		Team team = TeamHelper.service.getTeam(wfprocess);
		team.deletePrincipalTarget(role, oldPrincipal);
		team.addPrincipal(role, newPrincipal);
		wfprocess = (WfProcess) PersistenceHelper.manager.save(wfprocess);
		PersistenceHelper.manager.refresh(wfprocess);
	}
	
	
	public static WfProcess getWfProcessByPBO(WTObject pbo, WfState wfstate) throws WTException {
		WfProcess wfProcess = null;
		QueryResult qr = WfEngineHelper.service.getAssociatedProcesses(pbo, wfstate, null);
		while(qr.hasMoreElements()) {
			wfProcess = (WfProcess) qr.nextElement();
		}
		return wfProcess;
	}
	
	public static Object getWfProcessVariableValue(ObjectReference objectReference, String variablename) throws WTRuntimeException, WTException {
		WfProcess wfprocess = null;
		Object obj = getExxecutionObjectVariableValue(wfprocess, variablename);
		return obj;
	}
	
	
	public static Object getExxecutionObjectVariableValue(WfExecutionObject wfprocess, String variablename) throws WTRuntimeException, WTException {
		Map<String, Object> map = new HashMap<>();
		ProcessData processData = wfprocess.getContext();
		if(processData != null) {
			WfVariable wfVariable = processData.getVariable(variablename);
			if(wfVariable != null) {
				Object tmpObject = wfVariable.getValueObject();
				if(tmpObject != null && tmpObject instanceof String && Team.class.isAssignableFrom(wfVariable.getClass())) {
					tmpObject = (new ReferenceFactory()).getReference(tmpObject.toString()).getObject();
				}
				map.put(variablename, tmpObject);
			}
		}
		return map;
	}

	
	public static Persistable setLifeCycle(LifeCycleManaged lifeCycleManaged, String lifecycleTemplate, String state) throws WTInvalidParameterException, WTPropertyVetoException, WTException {
		LifeCycleState lifeCycleState = LifeCycleState.newLifeCycleState();
		lifeCycleState.setState(State.toState(state));
		LifeCycleTemplate lifeCycleTemplate2 = LifeCycleHelper.service.getLifeCycleTemplate(lifecycleTemplate, WTContainerHelper.service.getExchangeRef());
		LifeCycleTemplateReference lifeCycleTemplateReference = null;
		if(lifeCycleTemplate2 != null) {
			lifeCycleTemplateReference = lifeCycleTemplateReference.newLifeCycleTemplateReference(lifeCycleTemplate2);
		}
		if(lifeCycleTemplateReference != null) {
			LifeCycleTemplate lct = (LifeCycleTemplate) lifeCycleTemplateReference.getReadOnlyObject();
			if(!VersionControlHelper.isLatestIteration(lct)) {
				
				LifeCycleTemplate lct1 = LifeCycleHelper.service.getLatestIteration((LifeCycleTemplateMaster) ((LifeCycleTemplate)lifeCycleTemplateReference.getReadOnlyObject()).getMaster());
				lifeCycleTemplateReference = LifeCycleTemplateReference.newLifeCycleTemplateReference(lct1);
			}
			lifeCycleState.setLifeCycleId(lifeCycleTemplateReference);
		}
		return lifeCycleManaged;
	}
	
	

	public static List getAllActivities(WfContainer wtContainer, WfState[] wfstate) throws WTException {
		Enumeration actEnume = WfEngineHelper.service.getProcessSteps(wtContainer, null);
		List activities = new ArrayList();
		while(actEnume.hasMoreElements()) {
			WfActivity wfActivity = (WfActivity) actEnume.nextElement();
			boolean isSatisfy = isSatisfy(wfActivity, wfstate);
			if(wfActivity instanceof WfAdHocActivity) {
				WfAdHocActivity wfAdHocActivity = (WfAdHocActivity) wfActivity;
				if(!isIgnoreParentAdHoc(wfAdHocActivity) && isSatisfy) {
					activities.add(wfAdHocActivity);
				}
				if(wfAdHocActivity.getPerformer() != null) {
					activities.addAll();
				}
			} else if(wfActivity instanceof WfRequesterActivity) {
				WfRequesterActivity wfRequesterActivity = (WfRequesterActivity) wfActivity;
				WfContainer wfContainer = wfRequesterActivity.getPerformer();
				activities.addAll(getAllActivities(wfContainer, wfstate));
			} else if(isSatisfy) {
				
			}
		}
		
	}
	
	
	private static boolean isSatisfy(WfActivity wfActivity, WfState[] wfstate) {
		WfState wfState = wfActivity.getState();
		if(wfstate == null || wfstate.length == 0) {
			return true;
		}
		for(WfState state : wfstate) {
			if(state.equals(wfState)) {
				return true;
			}
		}
		return false;
	}
	
	
	private static boolean isSatisfy(WfActivity wfActivity, WfState wfstate) {
		WfState state = wfActivity.getState();
		if(wfstate == null) {
			return true;
		}
		if(state.equals(wfstate)) {
			return true;
		}
		return false;
	}
	
}
