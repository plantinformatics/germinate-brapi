package jhi.germinate.brapi.server.resource.germplasm.attribute;

import org.jooq.*;
import org.restlet.data.Status;
import org.restlet.resource.*;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

import jhi.germinate.server.Database;
import jhi.germinate.server.auth.*;
import jhi.germinate.server.database.enums.AttributesDatatype;
import jhi.germinate.server.database.tables.records.*;
import jhi.germinate.server.util.*;
import uk.ac.hutton.ics.brapi.resource.base.*;
import uk.ac.hutton.ics.brapi.resource.germplasm.attribute.AttributeValue;
import uk.ac.hutton.ics.brapi.server.germplasm.attribute.BrapiAttributeValueServerResource;

import static jhi.germinate.server.database.tables.Attributedata.*;
import static jhi.germinate.server.database.tables.Attributes.*;
import static jhi.germinate.server.database.tables.Germinatebase.*;
import static jhi.germinate.server.database.tables.ViewTableGermplasmAttributes.*;

/**
 * @author Sebastian Raubach
 */
public class AttributeValueServerResource extends AttributeValueBaseServerResource implements BrapiAttributeValueServerResource
{
	private static final String PARAM_ATTRIBUTE_VALUE_DB_ID     = "attributeValueDbId";
	private static final String PARAM_ATTRIBUTE_DB_ID           = "attributeDbId";
	private static final String PARAM_ATTRIBUTE_NAME            = "attributeName";
	private static final String PARAM_GERMPLASM_DB_ID           = "germplasmDbId";
	private static final String PARAM_EXTERNAL_REFERENCE_ID     = "externalReferenceID";
	private static final String PARAM_EXTERNAL_REFERENCE_SOURCE = "externalReferenceSource";

	private String attributeValueDbId;
	private String attributeDbId;
	private String attributeName;
	private String germplasmDbId;
	private String externalReferenceID;
	private String externalReferenceSource;

	@Override
	public void doInit()
	{
		super.doInit();

		this.attributeValueDbId = getQueryValue(PARAM_ATTRIBUTE_VALUE_DB_ID);
		this.attributeDbId = getQueryValue(PARAM_ATTRIBUTE_DB_ID);
		this.attributeName = getQueryValue(PARAM_ATTRIBUTE_NAME);
		this.germplasmDbId = getQueryValue(PARAM_GERMPLASM_DB_ID);
		this.externalReferenceID = getQueryValue(PARAM_EXTERNAL_REFERENCE_ID);
		this.externalReferenceSource = getQueryValue(PARAM_EXTERNAL_REFERENCE_SOURCE);
	}

	@Post
	@MinUserType(UserType.DATA_CURATOR)
	public BaseResult<ArrayResult<AttributeValue>> postAttributeValues(AttributeValue[] newValues)
	{
		if (CollectionUtils.isEmpty(newValues))
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);

		try (Connection conn = Database.getConnection();
			 DSLContext context = Database.getContext(conn))
		{
			List<Integer> newIds = Arrays.stream(newValues)
										 .map(v -> {
											 // Get germplasm based on id
											 GerminatebaseRecord germplasm = context.selectFrom(GERMINATEBASE)
																					.where(GERMINATEBASE.ID.cast(String.class).eq(v.getGermplasmDbId()))
																					.fetchAny();

											 // If it doesn't exist fail
											 if (germplasm == null)
												 throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Invalid germplasmDbId");

											 AttributesRecord attribute;
											 // Get attribute for id
											 if (!StringUtils.isEmpty(v.getAttributeDbId()))
											 {
												 attribute = context.selectFrom(ATTRIBUTES).where(ATTRIBUTES.ID.cast(String.class).eq(v.getAttributeDbId())).fetchAny();

												 // If it doesn't exist, fail
												 if (attribute == null)
													 throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Invalid attribute id");
											 }
											 // Get attribute for name
											 else if (!StringUtils.isEmpty(v.getAttributeName()))
											 {
												 attribute = context.selectFrom(ATTRIBUTES).where(ATTRIBUTES.NAME.eq(v.getAttributeName())).fetchAny();

												 // If it doesn't exist, create
												 if (attribute == null)
												 {
													 attribute = context.newRecord(ATTRIBUTES);
													 attribute.setName(v.getAttributeName());
													 attribute.setDescription(v.getAttributeName());
													 attribute.setTargetTable("germinatebase");
													 attribute.setDatatype(AttributesDatatype.char_);
													 attribute.store();
												 }
											 }
											 else
											 {
												 throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "No attribute id or name specified");
											 }

											 // Create a new attribute data record
											 AttributedataRecord record = context.newRecord(ATTRIBUTEDATA);
											 record.setForeignId(germplasm.getId());
											 record.setAttributeId(attribute.getId());
											 record.setCreatedOn(v.getDeterminedDate());
											 record.setValue(v.getValue());
											 record.store();

											 // Return the id
											 return record.getId();
										 })
										 .filter(Objects::nonNull)
										 .collect(Collectors.toList());

			List<AttributeValue> av = getAttributeValues(context, Collections.singletonList(VIEW_TABLE_GERMPLASM_ATTRIBUTES.ATTRIBUTE_VALUE_ID.in(newIds)));

			long totalCount = context.fetchOne("SELECT FOUND_ROWS()").into(Long.class);
			return new BaseResult<>(new ArrayResult<AttributeValue>()
				.setData(av), currentPage, pageSize, totalCount);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
		}
	}

	@Get
	public BaseResult<ArrayResult<AttributeValue>> getAttributeValues()
	{
		try (Connection conn = Database.getConnection();
			 DSLContext context = Database.getContext(conn))
		{
			List<Condition> conditions = new ArrayList<>();

			if (!StringUtils.isEmpty(attributeValueDbId))
				conditions.add(VIEW_TABLE_GERMPLASM_ATTRIBUTES.ATTRIBUTE_VALUE_ID.cast(String.class).eq(attributeValueDbId));
			if (!StringUtils.isEmpty(attributeDbId))
				conditions.add(VIEW_TABLE_GERMPLASM_ATTRIBUTES.ATTRIBUTE_ID.cast(String.class).eq(attributeDbId));
			if (!StringUtils.isEmpty(attributeName))
				conditions.add(VIEW_TABLE_GERMPLASM_ATTRIBUTES.ATTRIBUTE_NAME.eq(attributeName));
			if (!StringUtils.isEmpty(germplasmDbId))
				conditions.add(VIEW_TABLE_GERMPLASM_ATTRIBUTES.GERMPLASM_ID.cast(String.class).eq(germplasmDbId));

			List<AttributeValue> av = getAttributeValues(context, conditions);

			long totalCount = context.fetchOne("SELECT FOUND_ROWS()").into(Long.class);
			return new BaseResult<>(new ArrayResult<AttributeValue>()
				.setData(av), currentPage, pageSize, totalCount);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
			throw new ResourceException(Status.SERVER_ERROR_INTERNAL);
		}
	}
}