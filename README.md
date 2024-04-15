# germinate-brapi-pretzel
## Additional Endpoints Added to Germinate-BrAPI for Pretzel Integration

These endpoints have been added to the Germinate-BrAPI to specifically serve requests from the Pretzel platform (https://github.com/plantinformatics/pretzel), facilitating the retrieval and display of genotype calls.

Work carried out by Agriculture Victoria as part of the Australian Grains Genebank Strategic Partnership.

A step by step guide to using the new feature is available here: https://github.com/plantinformatics/pretzel/files/13699164/PretzelGerminateBrAPIConnection_StepByStepGuide.pdf

### Get a List of Samples in a Specified Dataset
**Endpoint:** `brapi/v2/callsets/dataset/{datasetID}`

### Get Genotype Call Data for a Given Sample Within a Specified Interval
This includes a range of positions and a chromosome.

**Endpoint:** `brapi/v2/callsets/{callSetDbId}/calls/mapid/{mapid}/chromosome/{chromosome}/position/{positionStart}/{positionEnd}`

### Get Genotype Values for a Given SampleID, DatasetID, and Range of MarkerIDs
**Endpoint:** `brapi/v2/callsets/{callSetDbId}/calls/markerid/{markerIDStart}/{markerIDEnd}`

