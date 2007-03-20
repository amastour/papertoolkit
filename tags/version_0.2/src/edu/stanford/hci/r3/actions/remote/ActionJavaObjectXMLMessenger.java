package edu.stanford.hci.r3.actions.remote;

import java.io.IOException;
import java.net.Socket;

import edu.stanford.hci.r3.PaperToolkit;
import edu.stanford.hci.r3.actions.R3Action;

/**
 * <p>
 * Sends an R3 Action as a Java object serialized to xml over a socket.
 * </p>
 * <p>
 * TODO: Seems like we could integrate this with PenServer's equivalent classes. How do we make it
 * generic so that we can send an object over and either consider it a pen sample, or an action?
 * Might be simpler to keep them separate for now. There are some issues with the way we serialize
 * (removing spaces, et cetera).
 * </p>
 * <p>
 * <span class="BSDLicense"> This software is distributed under the <a
 * href="http://hci.stanford.edu/research/copyright.txt">BSD License</a>. </span>
 * </p>
 * 
 * @author <a href="http://graphics.stanford.edu/~ronyeh">Ron B Yeh</a> (ronyeh(AT)cs.stanford.edu)
 */
public class ActionJavaObjectXMLMessenger extends ActionMessenger {

	/**
	 * @param s
	 * @throws IOException
	 */
	public ActionJavaObjectXMLMessenger(Socket s) {
		super(s);
	}

	/**
	 * Turns an R3Action into an xml string and then into the bytes we need to send.
	 * 
	 * @see edu.stanford.hci.r3.actions.remote.ActionMessenger#getMessage(edu.stanford.hci.r3.actions.R3Action)
	 */
	public byte[] getMessage(R3Action action) {
		String xmlString = PaperToolkit.toXML(action);
		// put it all on one line
		if (xmlString.contains("\n")) {
			xmlString = xmlString.replace("\n", "");
		}
		// do not remove spaces!!!! as actions may have paths, which depend on spaces...
		return (xmlString + LINE_SEPARATOR).getBytes();
	}
}