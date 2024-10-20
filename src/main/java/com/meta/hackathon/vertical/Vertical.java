package com.meta.hackathon.vertical;

import com.meta.hackathon.model.IncomingMessage;

public interface Vertical {

	public void process(IncomingMessage incomingMessage) throws Exception;

}
