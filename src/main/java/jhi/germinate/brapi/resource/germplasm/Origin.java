package jhi.germinate.brapi.resource.germplasm;

import jhi.germinate.brapi.resource.location.CoordinatesPoint;

/**
 * @author Sebastian Raubach
 */
public class Origin
{
	private String           coordinateUncertainty;
	private CoordinatesPoint coordinates;

	public String getCoordinateUncertainty()
	{
		return coordinateUncertainty;
	}

	public Origin setCoordinateUncertainty(String coordinateUncertainty)
	{
		this.coordinateUncertainty = coordinateUncertainty;
		return this;
	}

	public CoordinatesPoint getCoordinates()
	{
		return coordinates;
	}

	public Origin setCoordinates(CoordinatesPoint coordinates)
	{
		this.coordinates = coordinates;
		return this;
	}
}
