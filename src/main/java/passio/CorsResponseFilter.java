package passio;

import javax.annotation.Priority;

import javax.ws.rs.Priorities;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;

import javax.ws.rs.core.MultivaluedMap;

@Priority(Priorities.HEADER_DECORATOR)
public class CorsResponseFilter implements ContainerResponseFilter {

	@Override
	public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
		MultivaluedMap<String, Object> headers = responseContext.getHeaders();

		headers.add("Access-Control-Allow-Origin", "*");

		headers.add("Access-Control-Allow-Methods", "GET");
		headers.add("Access-Control-Allow-Methods", "POST");
		headers.add("Access-Control-Allow-Methods", "PUT");

		headers.add("Access-Control-Allow-Headers", "X-Signing-Key");
		headers.add("Access-Control-Allow-Headers", "X-MAC");
		headers.add("Access-Control-Allow-Headers", "Content-Type");
	}

}
