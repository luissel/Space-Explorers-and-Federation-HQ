import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Set;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Clasa pentru space explorer.
 */
public class SpaceExplorer extends Thread {
	// de câte ori un space explorer repetă operația de hash la decodare
	public  Integer hashCount;

	// canalul de comunicare dintre space explorers si headquarters
	public CommunicationChannel channel;

	// set care contine id-urile sistemelor solare descoperite
	public Set<Integer> discovered;

	public SpaceExplorer(Integer hashCount, Set<Integer> discovered, CommunicationChannel channel) {
		this.hashCount = hashCount;
		this.channel = channel;
		this.discovered = discovered;
	}

	@Override
	public void run() {
		while (true) {
			/**
			 * Daca mesajul este de tip EXIT, SpaceExplorer isi termina executia.

			 * In caz contrar, se trimie, pe canalul SpaceExplorer, un nou mesaj
			 avand campurile parentSolarSystem si currentSolarSystem, de la
			 mesajul initial si campul data pe care am aplicat functia
			 encryptMultipleTimes.
			*/
			Message message = channel.getMessageHeadQuarterChannel();

			if (message.getData().equals(HeadQuarter.EXIT)) {
				break;
			}

			int parentSolarSystem = message.getParentSolarSystem();
			int currentSolarSystem = message.getCurrentSolarSystem();

			channel.putMessageSpaceExplorerChannel(new Message(parentSolarSystem,
					currentSolarSystem,
					encryptMultipleTimes(message.getData(), hashCount)));
		}
	}

	/**
	 * Aplica funcia de hash pe un string de count ori
	 */
	private String encryptMultipleTimes(String input, Integer count) {
		String hashed = input;

		for (int i = 0; i < count; ++i) {
			hashed = encryptThisString(hashed);
		}

		return hashed;
	}

	/**
	 * Functia de hash folosita de mai multe ori pentru a decodifica frecventa
	 */
	private static String encryptThisString(String input) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			byte[] messageDigest = md.digest(input.getBytes(StandardCharsets.UTF_8));

			StringBuffer hexString = new StringBuffer();
			for (int i = 0; i < messageDigest.length; i++) {
				String hex = Integer.toHexString(0xff & messageDigest[i]);
				if (hex.length() == 1)
					hexString.append('0');
				hexString.append(hex);
			}
			return hexString.toString();

		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}
}
