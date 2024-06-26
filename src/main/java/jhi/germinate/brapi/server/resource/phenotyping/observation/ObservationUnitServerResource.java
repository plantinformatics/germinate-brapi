package jhi.germinate.brapi.server.resource.phenotyping.observation;

import jakarta.annotation.security.PermitAll;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.*;
import jhi.germinate.resource.enums.UserType;
import jhi.germinate.server.*;
import jhi.germinate.server.database.codegen.tables.pojos.Datasets;
import jhi.germinate.server.database.codegen.tables.records.PhenotypedataRecord;
import jhi.germinate.server.resource.datasets.DatasetTableResource;
import jhi.germinate.server.util.*;
import org.jooq.DSLContext;
import uk.ac.hutton.ics.brapi.resource.base.*;
import uk.ac.hutton.ics.brapi.resource.phenotyping.observation.*;
import uk.ac.hutton.ics.brapi.server.phenotyping.observation.BrapiObservationUnitServerResource;

import java.io.IOException;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;

import static jhi.germinate.server.database.codegen.tables.Datasets.*;
import static jhi.germinate.server.database.codegen.tables.Phenotypedata.*;

@Path("brapi/v2/observationunits")
public class ObservationUnitServerResource extends ObservationUnitBaseServerResource implements BrapiObservationUnitServerResource
{
	@GET
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Secured
	@PermitAll
	public BaseResult<ArrayResult<ObservationUnit>> getObservationUnits(@QueryParam("observationUnitDbId") String observationUnitDbId,
																		@QueryParam("observationUnitName") String observationUnitName,
																		@QueryParam("locationDbId") String locationDbId,
																		@QueryParam("seasonDbId") String seasonDbId,
																		@QueryParam("includeObservations") String includeObservations,
																		@QueryParam("observationUnitLevelName") String observationUnitLevelName,
																		@QueryParam("observationUnitLevelOrder") String observationUnitLevelOrder,
																		@QueryParam("observationUnitLevelCode") String observationUnitLevelCode,
																		@QueryParam("observationUnitLevelRelationshipName") String observationUnitLevelRelationshipName,
																		@QueryParam("observationUnitLevelRelationshipOrder") String observationUnitLevelRelationshipOrder,
																		@QueryParam("observationUnitLevelRelationshipCode") String observationUnitLevelRelationshipCode,
																		@QueryParam("observationUnitLevelRelationshipDbId") String observationUnitLevelRelationshipDbId,
																		@QueryParam("commonCropName") String commonCropName,
																		@QueryParam("programDbId") String programDbId,
																		@QueryParam("trialDbId") String trialDbId,
																		@QueryParam("studyDbId") String studyDbId,
																		@QueryParam("germplasmDbId") String germplasmDbId,
																		@QueryParam("externalReferenceId") String externalReferenceId,
																		@QueryParam("externalReferenceSource") String externalReferenceSource)
		throws IOException, SQLException
	{
		resp.sendError(Response.Status.NOT_IMPLEMENTED.getStatusCode());
		return null;
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Secured(UserType.DATA_CURATOR)
	public BaseResult<ArrayResult<ObservationUnit>> postObservationUnits(List<ObservationUnit> newObservationUnits)
		throws IOException, SQLException
	{
		if (CollectionUtils.isEmpty(newObservationUnits))
		{
			return new BaseResult<ArrayResult<ObservationUnit>>()
				.setResult(new ArrayResult<ObservationUnit>()
					.setData(new ArrayList<>()));
		}

		// Check that all requested study ids are valid and the user has permissions
		AuthenticationFilter.UserDetails userDetails = (AuthenticationFilter.UserDetails) securityContext.getUserPrincipal();
		List<Integer> datasetIds = DatasetTableResource.getDatasetIdsForUser(req, userDetails, "trials");
		for (ObservationUnit ou : newObservationUnits)
		{
			if (!CollectionUtils.isEmpty(ou.getObservations()))
			{
				for (Observation o : ou.getObservations())
				{
					try
					{
						Integer id = Integer.parseInt(o.getStudyDbId());

						if (!datasetIds.contains(id))
						{
							resp.sendError(Response.Status.FORBIDDEN.getStatusCode());
							return null;
						}
					}
					catch (NullPointerException | NumberFormatException e)
					{
						resp.sendError(Response.Status.BAD_REQUEST.getStatusCode());
						return null;
					}
				}
			}
		}

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");

		List<Integer> newIds = new ArrayList<>();

		try (Connection conn = Database.getConnection())
		{
			DSLContext context = Database.getContext(conn);
			for (ObservationUnit n : newObservationUnits)
			{
				Short row = null;
				Short col = null;
				String rep = null;

				ObservationUnitPosition pos = n.getObservationUnitPosition();

				if (pos != null)
				{
					if (pos.getObservationLevel() != null)
					{
						if (Objects.equals(pos.getObservationLevel().getLevelName(), "rep"))
						{
							rep = pos.getObservationLevel().getLevelCode();
						}
					}

					if (Objects.equals(pos.getPositionCoordinateXType(), "GRID_ROW"))
					{
						try
						{
							row = Short.parseShort(pos.getPositionCoordinateX());
						}
						catch (Exception e)
						{
							// Ignore
						}
					}
					if (Objects.equals(pos.getPositionCoordinateXType(), "GRID_COL"))
					{
						try
						{
							col = Short.parseShort(pos.getPositionCoordinateX());
						}
						catch (Exception e)
						{
							// Ignore
						}
					}
					if (Objects.equals(pos.getPositionCoordinateYType(), "GRID_ROW"))
					{
						try
						{
							row = Short.parseShort(pos.getPositionCoordinateY());
						}
						catch (Exception e)
						{
							// Ignore
						}
					}
					if (Objects.equals(pos.getPositionCoordinateYType(), "GRID_COL"))
					{
						try
						{
							col = Short.parseShort(pos.getPositionCoordinateY());
						}
						catch (Exception e)
						{
							// Ignore
						}
					}
				}

				if (!CollectionUtils.isEmpty(n.getObservations()))
				{
					for (Observation o : n.getObservations())
					{
						Datasets dataset = context.selectFrom(DATASETS).where(DATASETS.ID.cast(String.class).eq(o.getStudyDbId())).fetchAnyInto(Datasets.class);

						if (dataset == null)
							continue;

						PhenotypedataRecord pd = context.newRecord(PHENOTYPEDATA);
						pd.setDatasetId(dataset.getId());
						pd.setGerminatebaseId(Integer.parseInt(o.getGermplasmDbId()));
						pd.setPhenotypeId(Integer.parseInt(o.getObservationVariableDbId()));
						pd.setPhenotypeValue(o.getValue());
						pd.setRep(rep);
						pd.setBlock("1");
						pd.setTrialRow(row);
						pd.setTrialColumn(col);
						if (o.getGeoCoordinates() != null)
						{
							if (o.getGeoCoordinates().getGeometry() != null)
							{
								if (o.getGeoCoordinates().getGeometry().getCoordinates() != null)
								{
									double[] coords = o.getGeoCoordinates().getGeometry().getCoordinates();

									if (coords.length == 3)
									{
										pd.setLatitude(toBigDecimal(coords[0]));
										pd.setLongitude(toBigDecimal(coords[1]));
										pd.setElevation(toBigDecimal(coords[2]));
									}
								}
							}
						}
						try
						{
							pd.setRecordingDate(new Timestamp(sdf.parse(o.getObservationTimeStamp()).getTime()));
						}
						catch (Exception e)
						{
							// Ignore
						}

						// Let's see if this comes from GridScore and we can get information about the trait type
						boolean isMultiTrait = false;
						if (o.getAdditionalInfo() != null)
						{
							try
							{
								isMultiTrait = Objects.equals(o.getAdditionalInfo().get("traitMType"), "multi");
							}
							catch (Exception e)
							{
								// Ignore...
							}
						}

						if (isMultiTrait)
						{
							// Check if there's already an entry for the same plot, trait and timepoint and value
							PhenotypedataRecord pdOld = context.selectFrom(PHENOTYPEDATA)
															   .where(PHENOTYPEDATA.PHENOTYPE_ID.isNotDistinctFrom(pd.getPhenotypeId()))
															   .and(PHENOTYPEDATA.REP.isNotDistinctFrom(pd.getRep()))
															   .and(PHENOTYPEDATA.BLOCK.isNotDistinctFrom(pd.getBlock()))
															   .and(PHENOTYPEDATA.TRIAL_ROW.isNotDistinctFrom(pd.getTrialRow()))
															   .and(PHENOTYPEDATA.TRIAL_COLUMN.isNotDistinctFrom(pd.getTrialColumn()))
															   .and(PHENOTYPEDATA.TREATMENT_ID.isNotDistinctFrom(pd.getTreatmentId()))
															   .and(PHENOTYPEDATA.GERMINATEBASE_ID.isNotDistinctFrom(pd.getGerminatebaseId()))
															   .and(PHENOTYPEDATA.DATASET_ID.isNotDistinctFrom(pd.getDatasetId()))
															   .and(PHENOTYPEDATA.RECORDING_DATE.isNotDistinctFrom(pd.getRecordingDate()))
															   .and(PHENOTYPEDATA.LOCATION_ID.isNotDistinctFrom(pd.getLocationId()))
															   .and(PHENOTYPEDATA.TRIALSERIES_ID.isNotDistinctFrom(pd.getTrialseriesId()))
															   .and(PHENOTYPEDATA.PHENOTYPE_VALUE.eq(pd.getPhenotypeValue()))
															   .fetchAny();

							if (pdOld == null)
							{
								pd.store();
								newIds.add(pd.getId());
							}
							else
							{
								newIds.add(pdOld.getId());
							}
						}
						else
						{
							// Otherwise, check if there's a match when ignoring the value. Single traits get their values updated if different
							List<PhenotypedataRecord> matches = context.selectFrom(PHENOTYPEDATA)
																	   .where(PHENOTYPEDATA.PHENOTYPE_ID.isNotDistinctFrom(pd.getPhenotypeId()))
																	   .and(PHENOTYPEDATA.REP.isNotDistinctFrom(pd.getRep()))
																	   .and(PHENOTYPEDATA.BLOCK.isNotDistinctFrom(pd.getBlock()))
																	   .and(PHENOTYPEDATA.TRIAL_ROW.isNotDistinctFrom(pd.getTrialRow()))
																	   .and(PHENOTYPEDATA.TRIAL_COLUMN.isNotDistinctFrom(pd.getTrialColumn()))
																	   .and(PHENOTYPEDATA.TREATMENT_ID.isNotDistinctFrom(pd.getTreatmentId()))
																	   .and(PHENOTYPEDATA.GERMINATEBASE_ID.isNotDistinctFrom(pd.getGerminatebaseId()))
																	   .and(PHENOTYPEDATA.DATASET_ID.isNotDistinctFrom(pd.getDatasetId()))
//																	   .and(PHENOTYPEDATA.RECORDING_DATE.isNotDistinctFrom(pd.getRecordingDate()))
																	   .and(PHENOTYPEDATA.LOCATION_ID.isNotDistinctFrom(pd.getLocationId()))
																	   .and(PHENOTYPEDATA.TRIALSERIES_ID.isNotDistinctFrom(pd.getTrialseriesId()))
																	   .fetch();

							if (!CollectionUtils.isEmpty(matches))
							{
								// At this point, there should only be one match (because it's a single trait), but just in case, check them all
								for (PhenotypedataRecord match : matches)
								{
									if (!Objects.equals(match.getPhenotypeValue(), pd.getPhenotypeValue()))
									{
										// If it doesn't match, the value has been updated on the client, do so here as well
										match.setPhenotypeValue(pd.getPhenotypeValue());
										match.setRecordingDate(pd.getRecordingDate());
										match.store();
									}

									newIds.add(match.getId());
								}
							}
							else
							{
								// Then store the new one
								pd.store();
								newIds.add(pd.getId());
							}
						}
					}
				}
			}

			page = 0;
			pageSize = Integer.MAX_VALUE;
			return getObservationUnitsBase(context, Collections.singletonList(PHENOTYPEDATA.ID.in(newIds)));
		}
	}

	@PUT
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Secured(UserType.DATA_CURATOR)
	public BaseResult<ArrayResult<ObservationUnit>> putObservationUnits(Map<String, ObservationUnit> observationUnits)
		throws IOException, SQLException
	{
		resp.sendError(Response.Status.NOT_IMPLEMENTED.getStatusCode());
		return null;
	}

	@GET
	@Path("/table")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Secured
	@PermitAll
	public Response getObservationUnitTable(@QueryParam("observationUnitDbId") String observationUnitDbId,
											@QueryParam("observationVariableDbId") String observationVariableDbId,
											@QueryParam("locationDbId") String locationDbId,
											@QueryParam("seasonDbId") String seasonDbId,
											@QueryParam("programDbId") String programDbId,
											@QueryParam("trialDbId") String trialDbId,
											@QueryParam("studyDbId") String studyDbId,
											@QueryParam("germplasmDbId") String germplasmDbId,
											@QueryParam("observationUnitLevelName") String observationUnitLevelName,
											@QueryParam("observationUnitLevelOrder") String observationUnitLevelOrder,
											@QueryParam("observationUnitLevelCode") String observationUnitLevelCode,
											@QueryParam("observationUnitLevelRelationshipName") String observationUnitLevelRelationshipName,
											@QueryParam("observationUnitLevelRelationshipOrder") String observationUnitLevelRelationshipOrder,
											@QueryParam("observationUnitLevelRelationshipCode") String observationUnitLevelRelationshipCode,
											@QueryParam("observationUnitLevelRelationshipDbId") String observationUnitLevelRelationshipDbId)
		throws IOException, SQLException
	{
		resp.sendError(Response.Status.NOT_IMPLEMENTED.getStatusCode());
		return null;
	}

	@GET
	@Path("/{observationUnitDbId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Secured
	@PermitAll
	public BaseResult<ObservationUnit> getObservationUnitById(@PathParam("observationUnitDbId") String observationUnitDbId)
		throws IOException, SQLException
	{
		resp.sendError(Response.Status.NOT_IMPLEMENTED.getStatusCode());
		return null;
	}

	@PUT
	@Path("/{observationUnitDbId}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	@Secured(UserType.DATA_CURATOR)
	public BaseResult<ObservationUnit> putObservationUnitById(@PathParam("observationUnitDbId") String observationUnitDbId, ObservationUnit observationUnit)
		throws IOException, SQLException
	{
		resp.sendError(Response.Status.NOT_IMPLEMENTED.getStatusCode());
		return null;
	}
}
