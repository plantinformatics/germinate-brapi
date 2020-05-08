package jhi.germinate.brapi.server.resource.other;

import org.restlet.resource.ServerResource;

import java.io.*;
import java.util.*;

import jhi.germinate.server.util.*;

/**
 * @author Sebastian Raubach
 */
public abstract class FileServerResource extends ServerResource
{
	protected File createTempFile(String parentFolder, String filename, String extension, boolean create)
		throws IOException
	{
		extension = extension.replace(".", "");

		List<String> segments = getReference().getSegments(true);

		String path;
		if (CollectionUtils.isEmpty(segments))
			path = "germinate-brapi";
		else
			path = segments.get(0);
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
