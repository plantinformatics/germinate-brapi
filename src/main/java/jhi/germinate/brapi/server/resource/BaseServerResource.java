package jhi.germinate.brapi.server.resource;

import org.restlet.resource.*;

import java.sql.*;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

import jhi.germinate.brapi.resource.base.BaseResult;

/**
 * @author Sebastian Raubach
 */
public abstract class BaseServerResource<T> extends ServerResource
{
	protected static final String PARAM_PAGE_SIZE    = "pageSize";
	protected static final String PARAM_CURRENT_PAGE = "page";

	// TODO: do we want to configure this value in the environment somehow (e.g. properties file etc...)
	protected int pageSize    = Integer.MAX_VALUE;
	protected int currentPage = 0;

	protected static String toString(Object value)
	{
		return value == null ? null : Objects.toString(value);
	}

	@Override
	public void doInit()
	{
		super.doInit();

		String pageSize = getQueryValue(PARAM_PAGE_SIZE);
		if (pageSize != null)
		{
			try
			{
				this.pageSize = Integer.parseInt(getQueryValue(PARAM_PAGE_SIZE));
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}

		String page = getQueryValue(PARAM_CURRENT_PAGE);
		if (page != null)
		{
			try
			{
				this.currentPage = Integer.parseInt(getQueryValue(PARAM_CURRENT_PAGE));
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
	}

	@Get("json")
	public abstract BaseResult<T> getJson();

	protected synchronized String getSimpleDate(Date date)
	{
		try
		{
			return date.toLocalDate().format(DateTimeFormatter.ISO_LOCAL_DATE);
		}
		catch (Exception e)
		{
			return null;
		}
	}

	protected Timestamp getTimestamp(Date date)
	{
		if (date != null)
			return new Timestamp(date.getTime());
		else
			return null;
	}
}
