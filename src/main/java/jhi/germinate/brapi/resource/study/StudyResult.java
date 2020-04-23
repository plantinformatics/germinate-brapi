package jhi.germinate.brapi.resource.study;

import java.net.URI;
import java.sql.Timestamp;
import java.util.*;

import jhi.germinate.brapi.resource.*;
import jhi.germinate.brapi.resource.base.Reference;

/**
 * @author Sebastian Raubach
 */
public class StudyResult
{
	private boolean                            active = false;
	private Map<String, String>                additionalInfo;
	private String                             commonCropName;
	private List<ContactResult>                contacts;
	private String                             culturalPractices;
	private List<DataLinkResult>               dataLinks;
	private URI                                documentationURL;
	private Timestamp                          endDate;
	private List<EnvironmentalParameterResult> environmentParameters;
	private PuiDescription                     experimentalDesign;
	private List<Reference>                    externalReference;
	private PuiDescription                     growthFacility;
	private LastUpdateResult                   lastUpdate;
	private String                             license;
	private String                             locationDbId;
	private String                             locationName;
	private List<ObservationLevelResult>       observationLevels;
	private String                             observationUnitsDescription;
	private List<String>                       seasons;
	private Timestamp                          startDate;
	private String                             studyCode;
	private String                             studyDbId;
	private String                             studyDescription;
	private String                             studyName;
	private String                             studyPUI;
	private String                             studyType;
	private String                             trialDbId;
	private String                             trialName;

	public boolean isActive()
	{
		return active;
	}

	public StudyResult setActive(boolean active)
	{
		this.active = active;
		return this;
	}

	public Map<String, String> getAdditionalInfo()
	{
		return additionalInfo;
	}

	public StudyResult setAdditionalInfo(Map<String, String> additionalInfo)
	{
		this.additionalInfo = additionalInfo;
		return this;
	}

	public String getCommonCropName()
	{
		return commonCropName;
	}

	public StudyResult setCommonCropName(String commonCropName)
	{
		this.commonCropName = commonCropName;
		return this;
	}

	public List<ContactResult> getContacts()
	{
		return contacts;
	}

	public StudyResult setContacts(List<ContactResult> contacts)
	{
		this.contacts = contacts;
		return this;
	}

	public String getCulturalPractices()
	{
		return culturalPractices;
	}

	public StudyResult setCulturalPractices(String culturalPractices)
	{
		this.culturalPractices = culturalPractices;
		return this;
	}

	public List<DataLinkResult> getDataLinks()
	{
		return dataLinks;
	}

	public StudyResult setDataLinks(List<DataLinkResult> dataLinks)
	{
		this.dataLinks = dataLinks;
		return this;
	}

	public URI getDocumentationURL()
	{
		return documentationURL;
	}

	public StudyResult setDocumentationURL(URI documentationURL)
	{
		this.documentationURL = documentationURL;
		return this;
	}

	public Timestamp getEndDate()
	{
		return endDate;
	}

	public StudyResult setEndDate(Timestamp endDate)
	{
		this.endDate = endDate;
		return this;
	}

	public List<EnvironmentalParameterResult> getEnvironmentParameters()
	{
		return environmentParameters;
	}

	public StudyResult setEnvironmentParameters(List<EnvironmentalParameterResult> environmentParameters)
	{
		this.environmentParameters = environmentParameters;
		return this;
	}

	public PuiDescription getExperimentalDesign()
	{
		return experimentalDesign;
	}

	public StudyResult setExperimentalDesign(PuiDescription experimentalDesign)
	{
		this.experimentalDesign = experimentalDesign;
		return this;
	}

	public List<Reference> getExternalReference()
	{
		return externalReference;
	}

	public StudyResult setExternalReference(List<Reference> externalReference)
	{
		this.externalReference = externalReference;
		return this;
	}

	public PuiDescription getGrowthFacility()
	{
		return growthFacility;
	}

	public StudyResult setGrowthFacility(PuiDescription growthFacility)
	{
		this.growthFacility = growthFacility;
		return this;
	}

	public LastUpdateResult getLastUpdate()
	{
		return lastUpdate;
	}

	public StudyResult setLastUpdate(LastUpdateResult lastUpdate)
	{
		this.lastUpdate = lastUpdate;
		return this;
	}

	public String getLicense()
	{
		return license;
	}

	public StudyResult setLicense(String license)
	{
		this.license = license;
		return this;
	}

	public String getLocationDbId()
	{
		return locationDbId;
	}

	public StudyResult setLocationDbId(String locationDbId)
	{
		this.locationDbId = locationDbId;
		return this;
	}

	public String getLocationName()
	{
		return locationName;
	}

	public StudyResult setLocationName(String locationName)
	{
		this.locationName = locationName;
		return this;
	}

	public List<ObservationLevelResult> getObservationLevels()
	{
		return observationLevels;
	}

	public StudyResult setObservationLevels(List<ObservationLevelResult> observationLevels)
	{
		this.observationLevels = observationLevels;
		return this;
	}

	public String getObservationUnitsDescription()
	{
		return observationUnitsDescription;
	}

	public StudyResult setObservationUnitsDescription(String observationUnitsDescription)
	{
		this.observationUnitsDescription = observationUnitsDescription;
		return this;
	}

	public List<String> getSeasons()
	{
		return seasons;
	}

	public StudyResult setSeasons(List<String> seasons)
	{
		this.seasons = seasons;
		return this;
	}

	public Timestamp getStartDate()
	{
		return startDate;
	}

	public StudyResult setStartDate(Timestamp startDate)
	{
		this.startDate = startDate;
		return this;
	}

	public String getStudyCode()
	{
		return studyCode;
	}

	public StudyResult setStudyCode(String studyCode)
	{
		this.studyCode = studyCode;
		return this;
	}

	public String getStudyDbId()
	{
		return studyDbId;
	}

	public StudyResult setStudyDbId(String studyDbId)
	{
		this.studyDbId = studyDbId;
		return this;
	}

	public String getStudyDescription()
	{
		return studyDescription;
	}

	public StudyResult setStudyDescription(String studyDescription)
	{
		this.studyDescription = studyDescription;
		return this;
	}

	public String getStudyName()
	{
		return studyName;
	}

	public StudyResult setStudyName(String studyName)
	{
		this.studyName = studyName;
		return this;
	}

	public String getStudyPUI()
	{
		return studyPUI;
	}

	public StudyResult setStudyPUI(String studyPUI)
	{
		this.studyPUI = studyPUI;
		return this;
	}

	public String getStudyType()
	{
		return studyType;
	}

	public StudyResult setStudyType(String studyType)
	{
		this.studyType = studyType;
		return this;
	}

	public String getTrialDbId()
	{
		return trialDbId;
	}

	public StudyResult setTrialDbId(String trialDbId)
	{
		this.trialDbId = trialDbId;
		return this;
	}

	public String getTrialName()
	{
		return trialName;
	}

	public StudyResult setTrialName(String trialName)
	{
		this.trialName = trialName;
		return this;
	}
}
