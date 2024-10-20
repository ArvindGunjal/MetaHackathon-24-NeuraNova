package com.meta.hackathon.api.resource.config;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.meta.hackathon.api.service.config.impl.ConfigServiceImpl;

public class ConfigResource {
	@POST
	@Path("reload")
	public Response reload(@Context ContainerRequestContext requestContext, String cacheName) {
		try {
			ConfigServiceImpl.instance.reload(cacheName);
			return Response.status(Status.OK).entity("").build();
		} catch (Exception ex) {
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("").build();
		}
	}
}
