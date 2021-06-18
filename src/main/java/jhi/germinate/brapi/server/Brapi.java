package jhi.germinate.brapi.server;

import org.jooq.tools.StringUtils;

import javax.servlet.http.HttpServletRequest;

/**
 * @author Sebastian Raubach
 */
public class Brapi
{
	public static Brapi BRAPI;

	public final String baseUrl;
	public final String urlPrefix;
	public final String hdf5BaseFolder;

	public Brapi(String baseUrl, String urlPrefix, String hdf5BaseFolder)
	{
		if (!StringUtils.isEmpty(baseUrl))
		{
			if (baseUrl.endsWith("/"))
				baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
			this.baseUrl = baseUrl;
		}
		else
		{
			this.baseUrl = null;
		}
		this.urlPrefix = urlPrefix;
		this.hdf5BaseFolder = hdf5BaseFolder;

		BRAPI = this;
	}

	public static String getServerBase(HttpServletRequest req)
	{
		if (StringUtils.isEmpty(BRAPI.baseUrl))
		{
			String scheme = req.getScheme(); // http or https
			String serverName = req.getServerName(); // ics.hutton.ac.uk
			int serverPort = req.getServerPort(); // 80 or 8080 or 443
			String contextPath = req.getContextPath(); // /germinate-baz

			if (serverPort == 80 || serverPort == 443)
				return scheme + "://" + serverName + contextPath; // http://ics.hutton.ac.uk/germinate-baz
			else
				return scheme + "://" + serverName + ":" + serverPort + contextPath; // http://ics.hutton.ac.uk:8080/germinate-baz
		}
		else
		{
			return BRAPI.baseUrl;
		}
	}
}
