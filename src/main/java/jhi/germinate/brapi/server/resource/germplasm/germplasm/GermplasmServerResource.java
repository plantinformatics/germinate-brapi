package jhi.germinate.brapi.server.resource.germplasm.germplasm;

import org.jooq.*;
import org.jooq.impl.DSL;
import org.restlet.data.Status;
import org.restlet.resource.*;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

import jhi.germinate.brapi.resource.ArrayResult;
import jhi.germinate.brapi.resource.base.BaseResult;
import jhi.germinate.brapi.resource.germplasm.Germplasm;
import jhi.germinate.server.Database;
import jhi.germinate.server.auth.*;
import jhi.germinate.server.database.tables.Germinatebase;
import jhi.germinate.server.util.StringUtils;

import static jhi.germinate.server.database.tables.Germinatebase.*;
import static jhi.germinate.server.database.tables.Synonyms.*;
import static jhi.germinate.server.database.tables.Taxonomies.*;

/**
 * @author Sebastian Raubach
 */
public class GermplasmServerResource extends GermplasmBaseServerResource<ArrayResult<Germplasm>>
{
	public static final String PARAM_GERMPLASM_PUI             = "germplasmPUI";
	public static final String PARAM_GERMPLASM_DB_ID           = "germplasmDbId";
	public static final String PARAM_GERMPLASM_NAME            = "germplasmName";
	public static final String PARAM_CROP_COMMON_NAME          = "commonCropName";
	public static final String PARAM_ACCESSION_NUMBER          = "accessionNumber";
	public static final String PARAM_COLLETION                 = "collection";
	public static final String PARAM_GENUS                     = "genus";
	public static final String PARAM_SPEcIES                   = "species";
	public static final String PARAM_STUDY_DB_ID               = "studyDbId";
	public static final String PARAM_SYNONYM                   = "synonym";
	public static final String PARAM_PARENT_DB_ID              = "parentDbId";
	public static final String PARAM_PROGENY_DB_ID             = "progenyDbId";
	public static final String PARAM_EXTERNAL_REFERENCE_ID     = "externalReferenceID";
	public static final String PARAM_EXTERNAL_REFERENCE_SOURCE = "externalReferenceSource";

	private String germplasmPUI;
	private String germplasmDbId;
	private String germplasmName;
	private String commonCropName;
	private String accessionNumber;
	private String collection;
	private String genus;
	private String species;
	private String studyDbId;
	private String synonym;
	private String parentDbId;
	private String progenyDbId;
	private String externalReferenceID;
	private String externalReferenceSource;

	@Override
	public void doInit()
	{
		super.doInit();

		this.germplasmPUI = getQueryValue(PARAM_GERMPLASM_PUI);
		this.germplasmDbId = getQueryValue(PARAM_GERMPLASM_DB_ID);
		this.germplasmName = getQueryValue(PARAM_GERMPLASM_NAME);
		this.commonCropName = getQueryValue(PARAM_CROP_COMMON_NAME);
		this.accessionNumber = getQueryValue(PARAM_ACCESSION_NUMBER);
		this.collection = getQueryValue(PARAM_COLLETION);
		this.genus = getQueryValue(PARAM_GENUS);
		this.species = getQueryValue(PARAM_SPEcIES);
		this.studyDbId = getQueryValue(PARAM_STUDY_DB_ID);
		this.synonym = getQueryValue(PARAM_SYNONYM);
		this.parentDbId = getQueryValue(PARAM_PARENT_DB_ID);
		this.progenyDbId = getQueryValue(PARAM_PROGENY_DB_ID);
		this.externalReferenceID = getQueryValue(PARAM_EXTERNAL_REFERENCE_ID);
		this.externalReferenceSource = getQueryValue(PARAM_EXTERNAL_REFERENCE_SOURCE);
	}

	@Post
	@MinUserType(UserType.DATA_CURATOR)
	public BaseResult<ArrayResult<Germplasm>> postJson(Germplasm[] newGermplasm)
	{
		try (Connection conn = Database.getConnection();
			 DSLContext context = Database.getContext(conn))
		{
			List<Integer> newIds = Arrays.stream(newGermplasm)
										 .map(g -> addGermplasm(context, g, false))
										 .collect(Collectors.toList());

			List<Germplasm> list = getGermplasm(context, Collections.singletonList(GERMINATEBASE.ID.in(newIds)));

			return new BaseResult<>(new ArrayResult<Germplasm>()
				.setData(list), currentPage, pageSize, list.size());
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
		}
	}

	@Override
	public BaseResult<ArrayResult<Germplasm>> getJson()
	{
		try (Connection conn = Database.getConnection();
			 DSLContext context = Database.getContext(conn))
		{
			List<Condition> conditions = new ArrayList<>();

			if (!StringUtils.isEmpty(germplasmPUI))
				conditions.add(GERMINATEBASE.PUID.eq(germplasmPUI));
			if (!StringUtils.isEmpty(germplasmDbId))
				conditions.add(GERMINATEBASE.ID.cast(String.class).eq(germplasmDbId));
			if (!StringUtils.isEmpty(germplasmName))
				conditions.add(GERMINATEBASE.NAME.eq(germplasmName));
			if (!StringUtils.isEmpty(commonCropName))
				conditions.add(TAXONOMIES.CROPNAME.eq(commonCropName));
			if (!StringUtils.isEmpty(accessionNumber))
				conditions.add(GERMINATEBASE.NAME.eq(accessionNumber));
			if (!StringUtils.isEmpty(genus))
				conditions.add(TAXONOMIES.GENUS.eq(genus));
			if (!StringUtils.isEmpty(species))
				conditions.add(TAXONOMIES.SPECIES.eq(species));
			if (!StringUtils.isEmpty(synonym))
			{
				String cleaned = synonym.replaceAll("[^a-zA-Z0-9_-]", "");
				conditions.add(DSL.condition("JSON_CONTAINS(" + SYNONYMS.SYNONYMS_.getName() + ", '\"" + cleaned + "\"')"));
			}
			if (!StringUtils.isEmpty(parentDbId))
				conditions.add(GERMINATEBASE.ENTITYPARENT_ID.cast(String.class).eq(parentDbId));
			if (!StringUtils.isEmpty(progenyDbId))
			{
				Germinatebase g = GERMINATEBASE.as("g");
				conditions.add(DSL.exists(DSL.selectOne().from(g).where(g.ENTITYPARENT_ID.eq(GERMINATEBASE.ID).and(g.ID.cast(String.class).eq(progenyDbId)))));
			}

			List<Germplasm> lists = getGermplasm(context, conditions);

			long totalCount = context.fetchOne("SELECT FOUND_ROWS()").into(Long.class);
			return new BaseResult<>(new ArrayResult<Germplasm>()
				.setData(lists), currentPage, pageSize, totalCount);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
		}
	}
}
