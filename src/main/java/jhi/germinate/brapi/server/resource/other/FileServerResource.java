package jhi.germinate.brapi.server.resource.other;

import jhi.germinate.resource.enums.ServerProperty;
import jhi.germinate.server.util.*;
import uk.ac.hutton.ics.brapi.server.base.ContextResource;

import java.io.*;
import java.util.UUID;

public abstract class FileServerResource extends ContextResource
{
	protected File createTempFile(String parentFolder, String filename, String extension, boolean create)
		throws IOException
	{
		extension = extension.replace(".", "");

		String path = PropertyWatcher.get(ServerProperty.DATABASE_NAME);
		File folder = new File(System.getProperty("java.io.tmpdir"), path);
		folder.mkdirs();

		if (!StringUtils.isEmpty(parentFolder))
		{
			folder = new File(folder, parentFolder);
			folder.mkdirs();
		}

		File file;
		do
		{
			file = new File(folder, filename + "-" + UUID.randomUUID() + "." + extension);
		} while (file.exists());

		if (create)
			file.createNewFile();

		return file;
	}
}
