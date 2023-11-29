# germinate-brapi
## Additional Endpoints Added to Germinate-BrAPI for Pretzel Integration

These endpoints have been added to the Germinate-BrAPI to specifically serve requests from the Pretzel platform, facilitating the retrieval of genotype calls.

### Get a List of Samples in a Specified Dataset
**Endpoint:** `brapi/v2/callsets/dataset/{datasetID}`

### Get Genotype Call Data for a Given Sample Within a Specified Interval
This includes a range of positions and a chromosome.
**Endpoint:** `brapi/v2/callsets/{callSetDbId}/calls/mapid/{mapid}/chromosome/{chromosome}/position/{positionStart}/{positionEnd}`

### Get Genotype Values for a Given SampleID, DatasetID, and Range of MarkerIDs
**Endpoint:** `brapi/v2/callsets/{callSetDbId}/calls/markerid/{markerIDStart}/{markerIDEnd}`

For the source code of Pretzel, visit: (https://github.com/plantinformatics/pretzel).
