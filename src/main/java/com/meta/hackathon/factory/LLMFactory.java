package com.meta.hackathon.factory;

import com.meta.hackathon.ai.LLM;
import com.meta.hackathon.ai.impl.GoogleLLM;
import com.meta.hackathon.ai.impl.GroqLLM;
import com.meta.hackathon.ai.impl.SarvamLLM;

public class LLMFactory {

	public static final LLMFactory instance = new LLMFactory();

	public LLM getLLM(String llmProvider, String llmModel, String prompt) {
		if (llmProvider.trim().equalsIgnoreCase("groq")) {
			LLM llm = new GroqLLM();
			llm.setModel(llmModel);
			llm.setPrompt(prompt);
			return llm;
		}
		if (llmProvider.trim().equalsIgnoreCase("sarvam")) {
			LLM llm = new SarvamLLM();
			llm.setModel(llmModel);
			llm.setPrompt(prompt);
			return llm;
		}
		if (llmProvider.trim().equalsIgnoreCase("google")) {
			LLM llm = new GoogleLLM();
			llm.setModel(llmModel);
			llm.setPrompt(prompt);
			return llm;
		}
		return null;
	}
}
