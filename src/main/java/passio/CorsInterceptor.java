package passio;

import java.io.IOException;

import javax.ws.rs.core.MultivaluedMap;

import org.apache.cxf.jaxrs.impl.MetadataMap;

import org.apache.cxf.message.Message;

import org.apache.cxf.phase.AbstractPhaseInterceptor;
import org.apache.cxf.phase.Phase;

public class CorsInterceptor extends AbstractPhaseInterceptor<Message> {
	public CorsInterceptor() {
		super(Phase.PREPARE_SEND);
	}

	public void handleMessage(Message message) {
		MultivaluedMap<String, Object> headers =
			(MetadataMap<String, Object>) message.get(Message.PROTOCOL_HEADERS);

		if (headers == null) {
			headers = new MetadataMap<String, Object>();
		}

		headers.add("Access-Control-Allow-Origin", "*");
		headers.add("Access-Control-Allow-Headers", "Authorization");
		headers.add("Access-Control-Allow-Headers", "Content-Type");
		message.put(Message.PROTOCOL_HEADERS, headers);
	}
}
