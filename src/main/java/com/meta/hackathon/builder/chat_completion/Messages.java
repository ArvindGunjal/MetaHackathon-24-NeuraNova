package com.meta.hackathon.builder.chat_completion;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

public class Messages {

	private String role;
	private String content;
	@JsonInclude(Include. NON_NULL)
	private String name;
	@JsonInclude(Include. NON_NULL)
	private FunctionCall function_call;

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public FunctionCall getFunction_call() {
		return function_call;
	}

	public void setFunction_call(FunctionCall function_call) {
		this.function_call = function_call;
	}

}
