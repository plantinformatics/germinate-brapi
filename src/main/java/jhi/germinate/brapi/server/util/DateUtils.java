package jhi.germinate.brapi.server.util;

import java.sql.*;
import java.time.format.DateTimeFormatter;

/**
 * @author Sebastian Raubach
 */
public class DateUtils
{
	public static Timestamp getTimestamp(Date date)
	{
		if (date != null)
			return new Timestamp(date.getTime());
		else
			return null;
	}

	public static synchronized String getSimpleDate(Date date)
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
}
