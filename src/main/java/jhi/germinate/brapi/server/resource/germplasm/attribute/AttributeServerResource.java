package jhi.germinate.brapi.server.resource.germplasm.attribute;

import org.jooq.*;
import org.jooq.impl.DSL;
import org.restlet.data.Status;
import org.restlet.resource.*;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

import jhi.germinate.server.Database;
import jhi.germinate.server.auth.*;
import jhi.germinate.server.database.codegen.enums.AttributesDatatype;
import jhi.germinate.server.database.codegen.tables.records.AttributesRecord;
import jhi.germinate.server.util.StringUtils;
import uk.ac.hutton.ics.brapi.resource.base.*;
import uk.ac.hutton.ics.brapi.resource.germplasm.attribute.Attribute;
import uk.ac.hutton.ics.brapi.server.germplasm.attribute.BrapiAttributeServerResource;

import static jhi.germinate.server.database.codegen.tables.Attributedata.*;
import static jhi.germinate.server.database.codegen.tables.Attributes.*;
import static jhi.germinate.server.database.codegen.tables.Germinatebase.*;

/**
 * @author Sebastian Raubach
 */
public class AttributeServerResource extends AttributeBaseServerResource implements BrapiAttributeServerResource
{
	private static final String PARAM_ATTRIBUTE_CATEGORY        = "attributeCategory";
	private static final String PARAM_ATTRIBUTE_DB_ID           = "attributeDbId";
	private static final String PARAM_ATTRIBUTE_NAME            = "attributeName";
	private static final String PARAM_GERMPLASM_DB_ID           = "germplasmDbId";
	private static final String PARAM_EXTERNAL_REFERENCE_ID     = "externalReferenceID";
	private static final String PARAM_EXTERNAL_REFERENCE_SOURCE = "externalReferenceSource";

	private String attributeCategory;
	private String attributeDbId;
	private String attributeName;
	private String germplasmDbId;
	private String externalReferenceID;
	private String externalReferenceSource;

	@Override
	public void doInit()
	{
		super.doInit();

		this.attributeCategory = getQueryValue(PARAM_ATTRIBUTE_CATEGORY);
		this.attributeDbId = getQueryValue(PARAM_ATTRIBUTE_DB_ID);
		this.attributeName = getQueryValue(PARAM_ATTRIBUTE_NAME);
		this.germplasmDbId = getQueryValue(PARAM_GERMPLASM_DB_ID);
		this.externalReferenceID = getQueryValue(PARAM_EXTERNAL_REFERENCE_ID);
		this.externalReferenceSource = getQueryValue(PARAM_EXTERNAL_REFERENCE_SOURCE);
	}

	@Post
	@MinUserType(UserType.DATA_CURATOR)
	public BaseResult<ArrayResult<Attribute>> postAttributes(Attribute[] newAttributes)
	{
		try (Connection conn = Database.getConnection();
			 DSLContext context = Database.getContext(conn))
		{
			List<Integer> newIds = Arrays.stream(newAttributes)
										 .map(a -> {
											 if (StringUtils.isEmpty(a.getAttributeName()))
												 return null;

											 AttributesRecord attribute = context.newRecord(ATTRIBUTES);
											 attribute.setName(a.getAttributeName());
											 attribute.setDescription(a.getAttributeDescription());
											 attribute.setDatatype(AttributesDatatype.char_);
											 attribute.setTargetTable("germinatebase");
											 attribute.setCreatedOn(new Timestamp(System.currentTimeMillis()));
											 attribute.store();

											 return attribute.getId();
										 })
										 .filter(Objects::nonNull)
										 .collect(Collectors.toList());

			List<Attribute> av = getAttributes(context, Collections.singletonList(ATTRIBUTES.ID.in(newIds)));

			long totalCount = context.fetchOne("SELECT FOUND_ROWS()").into(Long.class);
			return new BaseResult<>(new ArrayResult<Attribute>()
				.setData(av), currentPage, pageSize, totalCount);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
		}
	}

	@Get
	public BaseResult<ArrayResult<Attribute>> getAttributes()
	{
		try (Connection conn = Database.getConnection();
			 DSLContext context = Database.getContext(conn))
		{
			List<Condition> conditions = new ArrayList<>();

			if (!StringUtils.isEmpty(attributeDbId))
				conditions.add(ATTRIBUTES.ID.cast(String.class).eq(attributeDbId));
			if (!StringUtils.isEmpty(attributeName))
				conditions.add(ATTRIBUTES.NAME.eq(attributeName));
			if (!StringUtils.isEmpty(germplasmDbId))
				conditions.add(DSL.exists(DSL.selectOne().from(ATTRIBUTEDATA).leftJoin(GERMINATEBASE).on(ATTRIBUTEDATA.FOREIGN_ID.eq(GERMINATEBASE.ID)).where(ATTRIBUTEDATA.ATTRIBUTE_ID.eq(ATTRIBUTES.ID)).and(GERMINATEBASE.ID.cast(String.class).eq(germplasmDbId))));

			List<Attribute> av = getAttributes(context, conditions);

			long totalCount = context.fetchOne("SELECT FOUND_ROWS()").into(Long.class);
			return new BaseResult<>(new ArrayResult<Attribute>()
				.setData(av), currentPage, pageSize, totalCount);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
		}
	}
}
