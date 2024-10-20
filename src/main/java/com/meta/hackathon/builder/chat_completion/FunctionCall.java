package com.meta.hackathon.builder.chat_completion;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

public class FunctionCall {

	@JsonInclude(Include. NON_NULL)
	private String name;
	@JsonInclude(Include. NON_NULL)
	private String arguments;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getArguments() {
		return arguments;
	}

	public void setArguments(String arguments) {
		this.arguments = arguments;
	}

}
