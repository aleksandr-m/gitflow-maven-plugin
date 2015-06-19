import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Locale;

import org.junit.Test;

import ch.qos.cal10n.verifier.Cal10nError;
import ch.qos.cal10n.verifier.IMessageKeyVerifier;
import ch.qos.cal10n.verifier.MessageKeyVerifier;

import com.amashchenko.maven.plugin.gitflow.i18n.LogMessages;


public class LogMessagesVerificationTest {

	@Test
	public void en() {
		IMessageKeyVerifier mkv = new MessageKeyVerifier(LogMessages.class);
		List<Cal10nError> errorList = mkv.verify(Locale.ENGLISH);
		for (Cal10nError error : errorList) {
			System.out.println(error);
		}
		assertEquals(0, errorList.size());
	}
}
