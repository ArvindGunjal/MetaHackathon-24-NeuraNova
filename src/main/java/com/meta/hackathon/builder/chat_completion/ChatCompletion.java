package com.meta.hackathon.builder.chat_completion;

import java.util.List;

import org.json.JSONObject;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ChatCompletion {

	private String model; // mandatory
	private List<Messages> messages; // mandatory
	@JsonInclude(Include.NON_NULL)
	private List<Function> functions; // optional
	@JsonInclude(Include.NON_NULL)
	private String function_call; // optional
	private double temperature; // optional
	private double top_p; // optional
	private long n; // optional
	private boolean stream; // optional
	@JsonInclude(Include.NON_NULL)
	private Object stop; // optional
	private long max_tokens;
	private double presence_penalty; // optional
	private double frequency_penalty; // optional
	@JsonInclude(Include.NON_NULL)
	private JSONObject logit_bias; // optional
	@JsonInclude(Include.NON_NULL)
	private String user; // optional
	@JsonProperty("response_format")
	private ResponseFormat response_format; // optional

	public String getModel() {
		return model;
	}

	public List<Messages> getMessages() {
		return messages;
	}

	public List<Function> getFunctions() {
		return functions;
	}

	public String getFunction_call() {
		return function_call;
	}

	public double getTemperature() {
		return temperature;
	}

	public double getTop_p() {
		return top_p;
	}

	public long getN() {
		return n;
	}

	public boolean isStream() {
		return stream;
	}

	public Object getStop() {
		return stop;
	}

	public long getMax_tokens() {
		return max_tokens;
	}

	public double getPresence_penalty() {
		return presence_penalty;
	}

	public double getFrequency_penalty() {
		return frequency_penalty;
	}

	public JSONObject getLogit_bias() {
		return logit_bias;
	}

	public String getUser() {
		return user;
	}

	@JsonProperty("response_format")
	public ResponseFormat getResponseFormat() {
		return response_format;
	}

	private ChatCompletion(Builder ChatCompletionBuilder) {
		this.model = ChatCompletionBuilder.model;
		this.messages = ChatCompletionBuilder.messages;
		this.functions = ChatCompletionBuilder.functions;
		this.function_call = ChatCompletionBuilder.function_call;
		this.temperature = ChatCompletionBuilder.temperature;
		this.top_p = ChatCompletionBuilder.top_p;
		this.n = ChatCompletionBuilder.n;
		this.stream = ChatCompletionBuilder.stream;
		this.stop = ChatCompletionBuilder.stop;
		this.max_tokens = ChatCompletionBuilder.max_tokens;
		this.presence_penalty = ChatCompletionBuilder.presence_penalty;
		this.frequency_penalty = ChatCompletionBuilder.frequency_penalty;
		this.logit_bias = ChatCompletionBuilder.logit_bias;
		this.user = ChatCompletionBuilder.user;
		this.response_format = ChatCompletionBuilder.response_format;
	}

	public static class Builder {

		private String model; // mandatory
		private List<Messages> messages; // mandatory
		private List<Function> functions; // optional
		private String function_call; // optional
		private double temperature; // optional
		private double top_p; // optional
		private long n; // optional
		private boolean stream; // optional
		private Object stop; // optional
		private long max_tokens; // optional
		private double presence_penalty; // optional
		private double frequency_penalty; // optional
		private JSONObject logit_bias; // optional
		private String user; // optional
		private ResponseFormat response_format; // optional

		public Builder(String model, List<Messages> messages) {
			this.model = model;
			this.messages = messages;
		}

		public String getModel() {
			return model;
		}

		public List<Messages> getMessages() {
			return messages;
		}

		public List<Function> getFunctions() {
			return functions;
		}

		public Builder setFunctions(List<Function> functions) {
			this.functions = functions;
			return this;
		}

		public String getFunction_call() {
			return function_call;
		}

		public Builder setFunction_call(String function_call) {
			this.function_call = function_call;
			return this;
		}

		public double getTemperature() {
			return temperature;
		}

		public Builder temperature(double temperature) {
			this.temperature = temperature;
			return this;
		}

		public double getTop_p() {
			return top_p;
		}

		public Builder topP(double top_p) {
			this.top_p = top_p;
			return this;
		}

		public long getN() {
			return n;
		}

		public Builder setN(long n) {
			this.n = n;
			return this;
		}

		public boolean isStream() {
			return stream;
		}

		public Builder setStream(boolean stream) {
			this.stream = stream;
			return this;
		}

		public Object getStop() {
			return stop;
		}

		public Builder setStop(Object stop) {
			this.stop = stop;
			return this;
		}

		public long getMax_tokens() {
			return max_tokens;
		}

		public Builder maxTokens(long max_tokens) {
			this.max_tokens = max_tokens;
			return this;
		}

		public double getPresence_penalty() {
			return presence_penalty;
		}

		public Builder presencePenalty(double presence_penalty) {
			this.presence_penalty = presence_penalty;
			return this;
		}

		public double getFrequency_penalty() {
			return frequency_penalty;
		}

		public Builder frequencyPenalty(double frequency_penalty) {
			this.frequency_penalty = frequency_penalty;
			return this;
		}

		public JSONObject getLogit_bias() {
			return logit_bias;
		}

		public Builder setLogit_bias(JSONObject logit_bias) {
			this.logit_bias = logit_bias;
			return this;
		}

		public String getUser() {
			return user;
		}

		public Builder setUser(String user) {
			this.user = user;
			return this;
		}

		public Builder response_format(ResponseFormat response_format) {
			this.response_format = response_format;
			return this;
		}

		public ChatCompletion build() {
			return new ChatCompletion(this);
		}

	}

}
