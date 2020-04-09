import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Clasa care implementeaza canalul folosit de headquarters si space explorers
 * pentru a comunica.
 */
public class CommunicationChannel {
	static LinkedBlockingQueue<Message> spaceExplorerChannel;
	static LinkedBlockingQueue<Message> hqChannel;
	static Map<Long, Integer> parents =  new ConcurrentHashMap<>();

	/**
	 *  In Set-ul visitedSolarSystems tin evidenta sistemelor solare descoperite,
		astfel incat sa nu se viziteze de 2 ori acelasi sistem.
	*/
	static Set<Integer> visitedSolarSystems = new HashSet<>();
	private static String NO_PARENT = "NO_PARENT";

	public CommunicationChannel() {
		if (null == spaceExplorerChannel) {
			spaceExplorerChannel = new LinkedBlockingQueue<Message>();
		}

		if (null == hqChannel) {
			hqChannel = new LinkedBlockingQueue<Message>();
		}
	}

	public void putMessageSpaceExplorerChannel(Message message) {
		try {
			spaceExplorerChannel.put(message);
		} catch (InterruptedException e) { }
	}

	public Message getMessageSpaceExplorerChannel() {
		Message message = null;

		try {
			message = spaceExplorerChannel.take();
		} catch (InterruptedException e){ }

		return message;
	}

	public void putMessageHeadQuarterChannel(Message message) {
		/**
		 * Voi pune un singur mesaj pe canalul HeadQuarter.
		 * Daca primesc END, il ignor. Daca primesc EXIT, adaug mesajul EXIT pe
		   canal.
		 * Un mesaj complet, valid, este format din informatiile a 2 mesaje
		   consecutive primite de la HQ. Acesta trebuie sa contina: parintele,
		   sistemul solar curent si stringul care trebuie hash-uit.
		 * Cand un mesaj este gata pentru a fi pus pe canal, setez variabila
		   isMessageComplete true.
		 */
		if (message.getData().equals(HeadQuarter.END)) {
			return;
		}

		int currentSolarSystem = message.getCurrentSolarSystem();
		long threadId = Thread.currentThread().getId();
		boolean isMessageComplete = false;
		Message finalMessage = null;

		// mesajele de EXIT sunt si ele trimise
		if (message.getData().equals(HeadQuarter.EXIT)) {
			finalMessage = message;
			isMessageComplete = true;
		}

		if (!isMessageComplete) {
			/**
			 * Pentru a mentine cele 2 mesaje in ordine, folosesc map-ul parents
			  	in care cheia este id-ul thread-ului si retin parintele atunci cand
			 	ajunge un mesaj cu informatii despre acesta (map nu contine
			 	threadId).
			 */
			if (!parents.containsKey(threadId)) {
				synchronized (parents) {
					parents.put(threadId, currentSolarSystem);
				}
			/**
			 * La primirea celui de-al doilea mesaj pentru acelasi thread (map
			 contine threadId) extrag parintele si creez mesajul cu el si
			 campurile currentSolarSystem si data, luate din mesajul primit.
			 */
			} else {
				int parent;
				synchronized (parents) {
					parent = parents.remove(threadId);
				}

				/**
				 * Daca currentSolarSystem nu a mai fost descoperit, se adauga
				   nodul in set. Pentru ca, acum, mesajul contine informatiile
				   dorite, se seteaza variabila isMessageComplete true, pentru
				   a-l pune pe canal.
				 */
				if (!visitedSolarSystems.contains(currentSolarSystem)) {
					visitedSolarSystems.add(currentSolarSystem);
					finalMessage = new Message(parent, currentSolarSystem, message.getData());

					isMessageComplete = true;
				}
			}
		}

		if (isMessageComplete) {
			try {
				hqChannel.put(finalMessage);
			} catch (InterruptedException e) {
			}
		}
	}

	public Message getMessageHeadQuarterChannel() {
		Message message = null;

		try {
			message = hqChannel.take();
		} catch (InterruptedException e) { }

		return message;
	}
}
