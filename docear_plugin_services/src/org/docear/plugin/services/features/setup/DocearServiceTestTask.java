package org.docear.plugin.services.features.setup;

import org.docear.plugin.services.DocearServiceException;
import org.docear.plugin.services.features.user.DocearUser;

public interface DocearServiceTestTask {

	public boolean isSuccessful();

	public void run(DocearUser settings) throws DocearServiceException;

}
