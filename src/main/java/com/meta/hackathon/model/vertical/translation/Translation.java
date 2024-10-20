package com.meta.hackathon.model.vertical.translation;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
public class Translation {
	@JsonProperty("text_to_be_translated")
	private String textToBeTranslated;
	@JsonProperty("translated_text")
	private String translatedText;
	@JsonProperty("source_language")
	private String sourceLanguageCode;
	@JsonProperty("output_language")
	private String targetLanguageCode;
	@JsonProperty("output_format")
	private String outputFormat;
}
