package com.meta.hackathon.config;

import org.json.JSONObject;

public class ReloadableProperties {

	private ReloadableProperties() {

	}

	public static String getGroqLLMAPI() {
		return ReloadableCache.instance.get("groq.llm.api").trim();
	}

	public static String getGroqLLMTranscriptionAPI() {
		return ReloadableCache.instance.get("groq.llm.transcription.api").trim();
	}

	public static String getSarvamLLMTranscriptionAPI() {
		return ReloadableCache.instance.get("sarvam.llm.transcription.api").trim();
	}

	public static String getSarvamLLMTTSAPI() {
		return ReloadableCache.instance.get("sarvam.llm.tts.api").trim();
	}

	public static String getSarvamLLMTranslateAPI() {
		return ReloadableCache.instance.get("sarvam.llm.translate.api").trim();
	}

	public static String getChatGPTAPI() {
		return ReloadableCache.instance.get("chat.gpt.api").trim();
	}

	public static String getGroqLLMAPIKey() {
		return ReloadableCache.instance.get("groq.llm.api.key").trim();
	}

	public static String getSarvamLLMAPIKey() {
		return ReloadableCache.instance.get("sarvam.llm.api.key").trim();
	}

	public static String getChatGPTLLMAPIKey() {
		return ReloadableCache.instance.get("chatgpt.llm.api.key").trim();
	}

	public static int getOkHttpConnectTimeoutInSeconds() {
		return Integer.parseInt(ReloadableCache.instance.get("okhttp.connect.timeout.in.seconds").trim());
	}

	public static int getOkHttpReadTimeoutInSeconds() {
		return Integer.parseInt(ReloadableCache.instance.get("okhttp.read.timeout.in.seconds").trim());
	}

	public static int getOkHttpWriteTimeoutInSeconds() {
		return Integer.parseInt(ReloadableCache.instance.get("okhttp.write.timeout.in.seconds").trim());
	}

	public static int getOkHttpMaxRequestsPerHost() {
		return Integer.parseInt(ReloadableCache.instance.get("okhttp.max.requests.per.host").trim());
	}

	public static int getInterceptorQueueBatchSize() {
		return Integer.parseInt(ReloadableCache.instance.get("interceptor.queue.batch.size").trim());
	}

	public static long getInterceptorQueueSleepTimeInMillis() {
		return Long.parseLong(ReloadableCache.instance.get("interceptor.queue.sleep.time.in.millis").trim());
	}

	public static int getInterceptorQueueNoOfThreads() {
		return Integer.parseInt(ReloadableCache.instance.get("interceptor.queue.no.of.threads").trim());
	}

	public static String getDefaultLLMProvider() {
		return ReloadableCache.instance.get("default.llm.provider").trim();
	}

	public static String getGroqLLMModel() {
		return ReloadableCache.instance.get("groq.llm.model").trim();
	}

	public static String getGroqWhisperModel() {
		return ReloadableCache.instance.get("groq.whisper.model").trim();
	}

	public static String getInferVerticalPrompt() {
		return ReloadableCache.instance.get("infer.vertical.prompt").trim();
	}

	public static String getVerticalPrompts() {
		return ReloadableCache.instance.get("vertical.prompts").trim();
	}

	public static JSONObject getAckMessages() {
		return new JSONObject(ReloadableCache.instance.get("ack.messages").trim());
	}

	public static long getIncomingProcessorSleepTimeInMillis() {
		return Long.parseLong(ReloadableCache.instance.get("incoming.processor.sleep.time.in.millis").trim());
	}

	public static int getIncomingProcessorBatchSize() {
		return Integer.parseInt(ReloadableCache.instance.get("incoming.processor.batch.size").trim());
	}

	public static int getIncomingProcessorNoOfThreads() {
		return Integer.parseInt(ReloadableCache.instance.get("incoming.processor.no.of.threads").trim());
	}

	public static long getSendMessageProcessorSleepTimeInMillis() {
		return Long.parseLong(ReloadableCache.instance.get("send.message.processor.sleep.time.in.millis").trim());
	}

	public static int getSendMessageProcessorBatchSize() {
		return Integer.parseInt(ReloadableCache.instance.get("send.message.processor.batch.size").trim());
	}

	public static int getSendMessageProcessorNoOfThreads() {
		return Integer.parseInt(ReloadableCache.instance.get("send.message.processor.no.of.threads").trim());
	}

	public static String getSendWhatsAppAPI() {
		return ReloadableCache.instance.get("send.whatsapp.api").trim();
	}

	public static String getSendSmsAPI() {
		return ReloadableCache.instance.get("send.sms.api").trim();
	}

	public static String getSendEmailAPI() {
		return ReloadableCache.instance.get("send.email.api").trim();
	}

	public static String getWhatsAppOneWayAccountUserId() {
		return ReloadableCache.instance.get("whatsapp.one.way.account.userid").trim();
	}

	public static String getWhatsAppOneWayAccountPassword() {
		return ReloadableCache.instance.get("whatsapp.one.way.account.password").trim();
	}

	public static String getWhatsAppTwoWayAccountUserId() {
		return ReloadableCache.instance.get("whatsapp.two.way.account.userid").trim();
	}

	public static String getWhatsAppTwoWayAccountPassword() {
		return ReloadableCache.instance.get("whatsapp.two.way.account.password").trim();
	}

	public static String getSMSAccountUserId() {
		return ReloadableCache.instance.get("sms.account.userid").trim();
	}

	public static String getSMSAccountPassword() {
		return ReloadableCache.instance.get("sms.account.password").trim();
	}

	public static String getEmailAccountUserId() {
		return ReloadableCache.instance.get("email.account.userid").trim();
	}

	public static String getEmailAccountPassword() {
		return ReloadableCache.instance.get("email.account.password").trim();
	}

	public static String getWhatsAppWABANumber() {
		return ReloadableCache.instance.get("whatsapp.waba.number").trim();
	}

	public static String getAudioFileDownloadLocation() {
		return ReloadableCache.instance.get("audio.file.download.location").trim();
	}

	public static String getGoogleTTSAPI() {
		return ReloadableCache.instance.get("google.tts.api").trim();
	}

	public static String getGoogleTTSAPIKey() {
		return ReloadableCache.instance.get("google.tts.api.key").trim();
	}

	public static String getTTSMp3FileUploadAPI() {
		return ReloadableCache.instance.get("tts.mp3.file.upload.api").trim();
	}

	public static String getReminderWhatsAppMessageTemplateID() {
		return ReloadableCache.instance.get("reminder.whatsapp.message.template.id").trim();
	}

	public static String getReminderSMSMessageTemplateID() {
		return ReloadableCache.instance.get("reminder.sms.message.template.id").trim();
	}

	public static String getReminderSMSMessageTemplate() {
		return ReloadableCache.instance.get("reminder.sms.message.template").trim();
	}

	public static String getReminderEmailMessage() {
		return ReloadableCache.instance.get("reminder.email.message").trim();
	}

	public static String getSportRapidApiHost() {
		return ReloadableCache.instance.get("sport.rapid.api.host").trim();
	}

	public static String getSportRapidApiKey() {
		return ReloadableCache.instance.get("sport.rapid.api.key").trim();
	}

	public static String getSportRapidApiMatchListUrl() {
		return ReloadableCache.instance.get("rapid.api.match.list.url").trim();
	}

	public static String getSportRapidApiScheduleUrl() {
		return ReloadableCache.instance.get("rapid.api.team.schedule.url").trim();
	}

	public static String getSportRapidRootUrl() {
		return ReloadableCache.instance.get("sport.rapid.api.root.url").trim();
	}

	public static JSONObject getSportCategorywisePromt() {
		return new JSONObject(ReloadableCache.instance.get("sport.categorywise.promt").trim());

	}

	public static JSONObject getSportCategorywiseMessageResponse() {
		return new JSONObject(ReloadableCache.instance.get("sport.categorywise.message.response").trim());
	}

	public static JSONObject getSportsPromtJson() {
		return new JSONObject(ReloadableCache.instance.get("sports.promt.json").trim());
	}

	public static String getSportPlayerSearchUrl() {
		return ReloadableCache.instance.get("sport.player.search.api").trim();
	}

	public static String getSportPlayerStatsUrl() {
		return ReloadableCache.instance.get("sport.player.stats.api").trim();
	}

	public static String getSportcricScoreApi() {
		return ReloadableCache.instance.get("sport.api.cric.score").trim();
	}

	public static JSONObject getteamNameAndIdMapping() {
		return new JSONObject(ReloadableCache.instance.get("team.name.id.mapping").trim());
	}

	public static String getTravelPrompts() {
		return ReloadableCache.instance.get("travel.prompt.json").trim();
	}

	public static String getRapidApiRootUrl() {
		return ReloadableCache.instance.get("rapid.api.root.url").trim();
	}

	public static String getRapidApiLiveTrainStatusUrl() {
		return ReloadableCache.instance.get("rapid.api.train.live.status.url").trim();
	}

	public static String getRapidApiSearchByTrainNoUrl() {
		return ReloadableCache.instance.get("rapid.api.search.train.by.no.url").trim();
	}

	public static String getRapidApiSearchTrainBetweenStationUrl() {
		return ReloadableCache.instance.get("rapid.api.search.between.station.url").trim();
	}

	public static String getRapidApiPnrDetailsUrl() {
		return ReloadableCache.instance.get("rapid.api.pnr.details.url").trim();
	}

	public static String getRapidApiSeatAvailabilityUrl() {
		return ReloadableCache.instance.get("rapid.api.seat.availability.url").trim();
	}

	public static String getRapidApiTrainFareUrl() {
		return ReloadableCache.instance.get("rapid.api.train.fare.url").trim();
	}

	public static String getRapidApiTrainScheduleUrl() {
		return ReloadableCache.instance.get("rapid.api.train.schedule.url").trim();
	}

	public static String getRapidApiHost() {
		return ReloadableCache.instance.get("rapid.api.host").trim();
	}

	public static String getRapidApiKey() {
		return ReloadableCache.instance.get("rapid.api.key").trim();
	}

	public static JSONObject getTravelSingleResponseJson() {
		return new JSONObject(ReloadableCache.instance.get("travel.category.single.message.response").trim());
	}

	public static JSONObject getStickerCategories() {
		return new JSONObject(ReloadableCache.instance.get("sticker.categories").trim());
	}

	public static String getMemesPrompt() {
		return ReloadableCache.instance.get("memes.description.compare.prompt").trim();
	}

	public static String getMemesDescription() {
		return ReloadableCache.instance.get("memes.description").trim();
	}

	public static String getMemesUrlHost() {
		return ReloadableCache.instance.get("memes.url.host").trim();
	}
}
