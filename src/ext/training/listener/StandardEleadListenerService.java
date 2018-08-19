package ext.training.listener;

import java.io.Serializable;

import wt.events.KeyedEvent;
import wt.events.KeyedEventListener;
import wt.fc.PersistenceManagerEvent;
import wt.services.ManagerException;
import wt.services.ServiceEventListenerAdapter;
import wt.services.StandardManager;
import wt.util.WTException;
import wt.vc.wip.WorkInProgressServiceEvent;

public class StandardEleadListenerService extends StandardManager implements ListenerService, Serializable {
	
	public static final String CLASSNAME = StandardEleadListenerService.class.getName();
	
	
	private KeyedEventListener listener ;
	
	public String getConceptualClassname() {
		return CLASSNAME;
	}
	
	public static StandardEleadListenerService newStandardEleadListenerService() throws WTException {
		StandardEleadListenerService service = new StandardEleadListenerService();
		service.initialize();
		return service;
	}

	@Override
	protected synchronized void performStartupProcess() throws ManagerException {
		listener = new EleadWCListenerEvenService(this.getConceptualClassname());
		getManagerService().addEventListener(listener, WorkInProgressServiceEvent.generateEventKey(WorkInProgressServiceEvent.POST_CHECKOUT));
		
	}
	
	
	class EleadWCListenerEvenService extends ServiceEventListenerAdapter {

		private final String post_checkout = WorkInProgressServiceEvent.POST_CHECKOUT;
		
		
		public EleadWCListenerEvenService(String var1) {
			super(var1);
		}
		
		public void notifyVetoableEvent(Object event) {
			if(!(event instanceof KeyedEvent)) {
				return ;
			}
			KeyedEvent keyEvent = (KeyedEvent) event;
			Object eventTarget = keyEvent.getEventTarget();
			String enentType = keyEvent.getEventType();
			if(enentType.equalsIgnoreCase(post_checkout)) {
				System.out.println("我被检出了...");
			}
			
			
		}
	}
	
	
}
