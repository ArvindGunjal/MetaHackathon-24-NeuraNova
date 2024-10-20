package com.meta.hackathon.api.resource;

import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.meta.hackathon.api.resource.config.ConfigResource;
import com.meta.hackathon.api.resource.incoming.IncomingResource;

@Path("/")
@Produces(MediaType.APPLICATION_JSON)
public class RootResource {

	@Path("incoming")
	public IncomingResource getIncomingResource() {
		return new IncomingResource();
	}

	@Path("config")
	public ConfigResource getConfigResource() {
		return new ConfigResource();
	}

}
