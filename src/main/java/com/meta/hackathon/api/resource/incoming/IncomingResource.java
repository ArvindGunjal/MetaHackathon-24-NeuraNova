package com.meta.hackathon.api.resource.incoming;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.meta.hackathon.api.service.incoming.impl.IncomingServiceImpl;

public class IncomingResource {

	private static final Logger LOG = LogManager.getLogger(IncomingResource.class.getSimpleName());

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response process(@Context ContainerRequestContext requestContext, String incomingPayload) {
		try {
			IncomingServiceImpl.instance.processIncoming(incomingPayload);
			return Response.status(Status.OK).entity("").build();
		} catch (Exception ex) {
			LOG.error("Exception ", ex);
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity("").build();
		}
	}

}
