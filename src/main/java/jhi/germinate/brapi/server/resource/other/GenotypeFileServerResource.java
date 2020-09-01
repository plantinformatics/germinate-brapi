package jhi.germinate.brapi.server.resource.other;

import org.jooq.DSLContext;
import org.restlet.data.Status;
import org.restlet.data.*;
import org.restlet.ext.servlet.ServletUtils;
import org.restlet.representation.FileRepresentation;
import org.restlet.resource.*;

import java.io.*;
import java.sql.*;
import java.util.*;

import jhi.germinate.brapi.server.Brapi;
import jhi.germinate.server.Database;
import jhi.germinate.server.database.tables.records.DatasetsRecord;
import jhi.germinate.server.util.*;

import static jhi.germinate.server.database.tables.Datasets.*;

/**
 * @author Sebastian Raubach
 */
public class GenotypeFileServerResource extends FileServerResource
{
	private String datasetId;

	@Override
	public void doInit()
		throws ResourceException
	{
		super.doInit();

		try
		{
			this.datasetId = getRequestAttributes().get("datasetId").toString();
		}
		catch (Exception e)
		{
		}
	}

	@Get("text/tab-separated-values")
	public FileRepresentation getJson()
	{
		if (StringUtils.isEmpty(datasetId))
			throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND);

		try (Connection conn = Database.getConnection();
			 DSLContext context = Database.getContext(conn))
		{
			DatasetsRecord ds = context.selectFrom(DATASETS)
									   .where(DATASETS.DATASET_STATE_ID.eq(1))
									   .and(DATASETS.IS_EXTERNAL.eq(false))
									   .and(DATASETS.DATASETTYPE_ID.eq(1))
									   .and(DATASETS.ID.cast(String.class).eq(datasetId)).fetchAny();

			if (ds == null || StringUtils.isEmpty(ds.getSourceFile()))
				throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND);

			File resultFile = createTempFile(null, "genotypes-" + ds.getId(), ".txt", true);

			Hdf5ToFJTabbedConverter converter = new Hdf5ToFJTabbedConverter(new File(Brapi.BRAPI.hdf5BaseFolder, ds.getSourceFile()), null, null, resultFile.getAbsolutePath(), false);
			// TODO: Generate header links
			String clientBase = Brapi.getServerBase(ServletUtils.getRequest(getRequest()));

			List<String> result = new ArrayList<>();

			if (!StringUtils.isEmpty(clientBase))
			{
				if (clientBase.endsWith("/"))
					clientBase = clientBase.substring(0, clientBase.length() - 1);
				result.add("# fjDatabaseLineSearch = " + clientBase + "/#/data/germplasm/$LINE");
				result.add("# fjDatabaseGroupPreview = " + clientBase + "/#/groups/upload/$GROUP");
				result.add("# fjDatabaseMarkerSearch = " + clientBase + "/#/data/genotypes/marker/$MARKER");
				result.add("# fjDatabaseGroupUpload = " + clientBase + "/api/group/upload");
			}
			converter.extractData(CollectionUtils.join(result, "\n") + "\n");

			FileRepresentation representation = new FileRepresentation(resultFile, MediaType.TEXT_TSV);
			representation.setSize(resultFile.length());
			representation.setAutoDeleting(true);

			// TODO: Kick of deletion thread from here as well!
			Disposition disp = new Disposition(Disposition.TYPE_ATTACHMENT);
			disp.setFilename(resultFile.getName());
			disp.setSize(resultFile.length());
			representation.setDisposition(disp);
			return representation;
		}
		catch (SQLException | IOException e)
		{
			e.printStackTrace();
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
		}
	}
}
