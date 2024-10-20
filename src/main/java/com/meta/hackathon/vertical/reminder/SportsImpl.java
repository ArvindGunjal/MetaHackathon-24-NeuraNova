package com.meta.hackathon.vertical.reminder;

import java.io.UnsupportedEncodingException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import com.meta.hackathon.ai.LLM;
import com.meta.hackathon.builder.send_message.SendMessage;
import com.meta.hackathon.config.ReloadableProperties;
import com.meta.hackathon.enums.Channel;
import com.meta.hackathon.enums.PayloadType;
import com.meta.hackathon.enums.RequestMethod;
import com.meta.hackathon.factory.LLMFactory;
import com.meta.hackathon.http.HTTPRequest;
import com.meta.hackathon.http.HTTPResponse;
import com.meta.hackathon.model.IncomingMessage;
import com.meta.hackathon.vertical.Vertical;

public class SportsImpl implements Vertical {
	private static final Logger LOG = LogManager.getLogger(SportsImpl.class.getSimpleName());

	@Override
	public void process(IncomingMessage incomingMessage) throws Exception {
		try {
			LOG.info("{} - inside sport prompt processing ", incomingMessage.getMobile());

			JSONObject verticalWisePrompts = new JSONObject(ReloadableProperties.getVerticalPrompts());
			String prompt = verticalWisePrompts.getString(incomingMessage.getVertical());
			prompt = prompt.replace("user_prompt", incomingMessage.getUserMessage());

			JSONObject sportsCategoryPromptResponse = callLlmModel(incomingMessage, prompt);
			String userResponse = "Sorry but I didn't understood your request 😔. Please try again.s";

			if (sportsCategoryPromptResponse.getJSONObject("meta").getBoolean("prompt_identified_success")) {
				JSONObject sprotsPrompt = ReloadableProperties.getSportsPromtJson();
				String categoryId = sportsCategoryPromptResponse.getJSONObject("data").getString("category_id");
				if (sprotsPrompt.has(categoryId)) {
					String travelCategoryQueryPrompt = sprotsPrompt.getString(categoryId);
					travelCategoryQueryPrompt = travelCategoryQueryPrompt.replace("user_prompt",
							incomingMessage.getUserMessage());

					sportsCategoryPromptResponse = callLlmModel(incomingMessage, travelCategoryQueryPrompt);

					LOG.info("sportsCategoryPromptResponse:{}", sportsCategoryPromptResponse);
					userResponse = performCategoryWiseRapidApiCall(String.valueOf(incomingMessage.getMobile()),
							categoryId, sportsCategoryPromptResponse);
				}

			} else {
				if (sportsCategoryPromptResponse.getJSONObject("meta").has("missing_parameter")) {
					userResponse = sportsCategoryPromptResponse.getJSONObject("meta").getString("missing_parameter");
				}
			}

			// call enterprise api

			executeMediaApi(String.valueOf(incomingMessage.getMobile()), userResponse);
		} catch (Exception e) {
			LOG.error("{} - exception while processing travel prompt ", incomingMessage.getMobile(), e);
		}
	}

	private String performCategoryWiseRapidApiCall(String mobileNo, String categoryId, JSONObject llmResponse) {
		if (categoryId.equalsIgnoreCase("1")) {
			HTTPResponse httpResponse = execute(ReloadableProperties.getSportRapidApiMatchListUrl());
			return generateSingleResponsePayload(mobileNo, categoryId, httpResponse, llmResponse);

		} else if (categoryId.equalsIgnoreCase("2")) {
			HTTPResponse httpResponse = executeCricData(ReloadableProperties.getSportPlayerSearchUrl()
					.replace("{{NAME}}", llmResponse.getJSONObject("data").getString("playerName")));
			JSONObject json = new JSONObject(httpResponse.getBodyAsString());
			JSONObject player = json.getJSONArray("data").getJSONObject(0);
			String playerId = player.getString("id");

			HTTPResponse httpResponsefromStats = executeCricData(
					ReloadableProperties.getSportPlayerStatsUrl().replace("{{ID}}", playerId));

			return generateSingleResponsePayload(mobileNo, categoryId, httpResponsefromStats, llmResponse);

		} else if (categoryId.equalsIgnoreCase("3")) {
			int teamId = ReloadableProperties.getteamNameAndIdMapping()
					.getInt(llmResponse.getJSONObject("data").getString("teamName"));

			HTTPResponse httpResponsefromStats = execute(
					ReloadableProperties.getSportRapidApiScheduleUrl().replace("{{TeamId}}", String.valueOf(teamId)));
			return generateSingleResponsePayload(mobileNo, categoryId, httpResponsefromStats, llmResponse);
		}
		return null;
	}

	public String generateSingleResponsePayload(String mobileNo, String category, HTTPResponse response,
			JSONObject llmResponse) {

		String genericExceptionMsg = "Something went wrong while processing your request. Please try again later";
		String invalidParamMsg = "Please check your query and try again";

		try {

			if (!response.isSuccessful()) {
				return genericExceptionMsg;
			}

			JSONObject rapidApiResponseObject = new JSONObject(response.getBodyAsString());

			if (rapidApiResponseObject.has("errors")) {
				return invalidParamMsg;
			}

			String sportCategoryWisePromt = ReloadableProperties.getSportCategorywisePromt().getString(category);

			if (category.equals("1")) {
				String msg = sportCategoryWisePromt.replace("RESPONSE", rapidApiResponseObject.toString())
						.replace("{{TEAM1}}", llmResponse.getJSONObject("data").getString("team1"))
						.replace("{{TEAM2}}", llmResponse.getJSONObject("data").getString("team2"));

				JSONObject jsonObject = callLlmModel(mobileNo, msg);

				StringBuilder message = new StringBuilder();

				String team1Name = jsonObject.getString("team1Name");
				String team2Name = jsonObject.getString("team2Name");

				JSONObject team1Score = jsonObject.getJSONObject("team1Score");
				JSONObject team2Score = jsonObject.getJSONObject("team2Score");

				message.append(String.format("Cricket Update \uD83C\uDFCF\n\n%s vs %s\n\n", team1Name, team2Name));

				int inningsCounter = 1;

				while (true) {
					boolean hasTeam1Inning = team1Score.has("inngs" + inningsCounter);
					boolean hasTeam2Inning = team2Score.has("inngs" + inningsCounter);

					// If neither team has this inning, break the loop
					if (!hasTeam1Inning && !hasTeam2Inning) {
						break;
					}

					// Append team 1 innings if it exists
					if (hasTeam1Inning) {
						JSONObject team1InningScore = team1Score.getJSONObject("inngs" + inningsCounter);

						String team1Runs = team1InningScore.optString("runs", "0");
						String team1Wickets = team1InningScore.optString("wickets", "0");
						String team1Overs = team1InningScore.optString("overs", "0");

						message.append(String.format("**%d%s Innings (Team 1)**\n%s: %s/%s (%s overs)\n\n",
								inningsCounter, getOrdinalSuffix(inningsCounter), team1Name, team1Runs, team1Wickets,
								team1Overs));
					}

					// Append team 2 innings if it exists
					if (hasTeam2Inning) {
						JSONObject team2InningScore = team2Score.getJSONObject("inngs" + inningsCounter);

						String team2Runs = team2InningScore.optString("runs", "0");
						String team2Wickets = team2InningScore.optString("wickets", "0");
						String team2Overs = team2InningScore.optString("overs", "0");

						message.append(String.format("**%d%s Innings (Team 2)**\n%s: %s/%s (%s overs)\n\n",
								inningsCounter, getOrdinalSuffix(inningsCounter), team2Name, team2Runs, team2Wickets,
								team2Overs));
					}

					inningsCounter++;
				}

				return message.toString();

			} else if (category.equals("2")) {

				String msg = sportCategoryWisePromt.replace("RESPONSE", rapidApiResponseObject.toString());

				JSONObject jsonObject = callLlmModel(mobileNo, msg);

				return ReloadableProperties.getSportCategorywiseMessageResponse().getString("2")
						.replace("PLAYER_NAME", llmResponse.getJSONObject("data").getString("playerName"))
						.replace("{T_TestMatches}", jsonObject.getJSONObject("Test Cricket").getInt("Matches") + "")
						.replace("{T_TestInnings}", jsonObject.getJSONObject("Test Cricket").getInt("Innings") + "")
						.replace("{T_TestRuns}", jsonObject.getJSONObject("Test Cricket").getInt("Runs") + "")
						.replace("{T_TestHighestScore}",
								jsonObject.getJSONObject("Test Cricket").getInt("Highest Score") + "")
						.replace("{T_TestAvg}", jsonObject.getJSONObject("Test Cricket").getDouble("Average") + "")
						.replace("{T_TestCenturies}", jsonObject.getJSONObject("Test Cricket").getInt("Centuries") + "")
						.replace("{T_TestHalfCenturies}",
								jsonObject.getJSONObject("Test Cricket").getInt("Half-Centuries") + "")
						.replace("{T_ODIMatches}", jsonObject.getJSONObject("ODI Cricket").getInt("Matches") + "")
						.replace("{T_ODIInnings}", jsonObject.getJSONObject("ODI Cricket").getInt("Innings") + "")
						.replace("{T_ODIRuns}", jsonObject.getJSONObject("ODI Cricket").getInt("Runs") + "")
						.replace("{T_ODIHighestScore}",
								jsonObject.getJSONObject("ODI Cricket").getInt("Highest Score") + "")
						.replace("{T_ODIAvg}", jsonObject.getJSONObject("ODI Cricket").getDouble("Average") + "")
						.replace("{T_ODICenturies}", jsonObject.getJSONObject("ODI Cricket").getInt("Centuries") + "")
						.replace("{T_ODIHalfCenturies}",
								jsonObject.getJSONObject("ODI Cricket").getInt("Half-Centuries") + "")
						.replace("{T20IMatches}", jsonObject.getJSONObject("T20I Cricket").getInt("Matches") + "")
						.replace("{T20IInnings}", jsonObject.getJSONObject("T20I Cricket").getInt("Innings") + "")
						.replace("{T20IRuns}", jsonObject.getJSONObject("T20I Cricket").getInt("Runs") + "")
						.replace("{T20IHighestScore}",
								jsonObject.getJSONObject("T20I Cricket").getInt("Highest Score") + "")
						.replace("{T20IAvg}", jsonObject.getJSONObject("T20I Cricket").getDouble("Average") + "")
						.replace("{T20ICenturies}", jsonObject.getJSONObject("T20I Cricket").getInt("Centuries") + "")
						.replace("{T20IHalfCenturies}",
								jsonObject.getJSONObject("T20I Cricket").getInt("Half-Centuries") + "")
						.replace("{BowlingWickets}",
								jsonObject.getJSONObject("Bowling (Test & ODI)").getInt("Wickets") + "")
						.replace("{BestBowling}",
								jsonObject.getJSONObject("Bowling (Test & ODI)").getString("Best Bowling"));

			} else if (category.equals("3")) {
				String msg = sportCategoryWisePromt.replace("RESPONSE", rapidApiResponseObject.toString());

				JSONObject jsonObject = callLlmModel(mobileNo, msg);

				LOG.info(jsonObject.toString());

				StringBuilder sb = new StringBuilder();
				sb.append("Upcoming Matches\n\n");

				// Variables to keep track of series
				String currentSeries = "";
				int matchCounter = 1;

				// Process match information
				for (String key : jsonObject.keySet()) {
					JSONObject matchInfo = jsonObject.getJSONObject(key);
					String seriesName = matchInfo.getString("seriesName");

					// Add series name if it changes
					if (!currentSeries.equals(seriesName)) {
						currentSeries = seriesName;
						sb.append(currentSeries).append("\n");
					}

					// Append match details with sequence
					appendMatchInfo(sb, matchInfo, matchCounter++);
				}

				return sb.toString();
			} else {
				return invalidParamMsg;
			}

		} catch (Exception e) {
			LOG.error("{} - exception while processing ", mobileNo, e);
			return genericExceptionMsg;
		}

	}

	private static void appendMatchInfo(StringBuilder sb, JSONObject matchInfo, int matchNumber) {
		try {
			String matchDesc = matchInfo.getString("matchDesc");
			String startDate = matchInfo.getString("startDate").split(" ")[0];
			String endDate = matchInfo.getString("endDate").split(" ")[0];
			String city = matchInfo.getString("city");
			String ground = matchInfo.getString("ground");
			String matchFormat = matchInfo.getString("matchFormat");
			String state = matchInfo.getString("state");

			String ordinalSuffix = getOrdinalSuffix(matchNumber);
			sb.append(matchDesc).append(" (").append(startDate).append(" - ").append(endDate).append(", ").append(city)
					.append(", ").append(ground).append(", ").append(matchFormat).append(") - ").append(state)
					.append(" - ").append(matchNumber).append(ordinalSuffix).append(" match\n");

		} catch (JSONException e) {
			LOG.error("exception while processing ", e);
		}
	}

	private static String getOrdinalSuffix(int matchNumber) {
		if (matchNumber % 10 == 1 && matchNumber % 100 != 11) {
			return "st";
		} else if (matchNumber % 10 == 2 && matchNumber % 100 != 12) {
			return "nd";
		} else if (matchNumber % 10 == 3 && matchNumber % 100 != 13) {
			return "rd";
		} else {
			return "th";
		}
	}

	public HTTPResponse execute(String queryParam) {

		JSONObject headers = new JSONObject();
		headers.put("x-rapidapi-host", ReloadableProperties.getSportRapidApiHost());
		headers.put("x-rapidapi-key", ReloadableProperties.getSportRapidApiKey());

		String url = ReloadableProperties.getSportRapidRootUrl() + queryParam;

		HTTPRequest httpRequest = new HTTPRequest(url, RequestMethod.GET, headers, PayloadType.QUERYPARAMS, null);
		HTTPResponse response = httpRequest.execute();
		LOG.info("Http response {} ", response);
		return response;
	}

	public HTTPResponse executeCricData(String url) {
		HTTPRequest httpRequest = new HTTPRequest(url, RequestMethod.GET, PayloadType.QUERYPARAMS, null);
		HTTPResponse response = httpRequest.execute();
		LOG.info("Http response {} ", response);
		return response;
	}

	public void executeMediaApi(String mobile, String msg) throws UnsupportedEncodingException {

		SendMessage sendMessage = new SendMessage();
		sendMessage.setChannel(Channel.WHATSAPP.name());
		sendMessage.setUserid(ReloadableProperties.getWhatsAppTwoWayAccountUserId());
		sendMessage.setPassword(ReloadableProperties.getWhatsAppTwoWayAccountPassword());
		sendMessage.setSend_to(mobile);
		sendMessage.setMsg(msg);
		sendMessage.setMsg_type("TEXT");
		sendMessage.setMethod("SENDMESSAGE");
		sendMessage.send();
	}

	private JSONObject callLlmModel(IncomingMessage incomingMessage, String prompt) throws Exception {
		LLM llm = LLMFactory.instance.getLLM(ReloadableProperties.getDefaultLLMProvider(),
				ReloadableProperties.getGroqLLMModel(), prompt);
		HTTPResponse completionResponse = llm.performChatCompletion();
		JSONObject dataExtractionResponse = new JSONObject(new JSONObject(completionResponse.getBodyAsString())
				.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content"));
		LOG.info("{} - response from llm {}", incomingMessage.getMobile(), dataExtractionResponse);
		return dataExtractionResponse;
	}

	private JSONObject callLlmModel(String mobileNo, String prompt) throws Exception {
		LLM llm = LLMFactory.instance.getLLM(ReloadableProperties.getDefaultLLMProvider(),
				ReloadableProperties.getGroqLLMModel(), prompt);
		HTTPResponse completionResponse = llm.performChatCompletion();
		JSONObject dataExtractionResponse = new JSONObject(new JSONObject(completionResponse.getBodyAsString())
				.getJSONArray("choices").getJSONObject(0).getJSONObject("message").getString("content"));
		LOG.info("{} - response from llm {}", mobileNo, dataExtractionResponse);
		return dataExtractionResponse;
	}

}
