package com.meta.hackathon.vertical.reminder;

import java.io.UnsupportedEncodingException;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
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
import com.meta.hackathon.queue.SendMessageProcessor;
import com.meta.hackathon.vertical.Vertical;

public class TravelServiceImpl implements Vertical {

	private static final Logger LOG = LogManager.getLogger(TravelServiceImpl.class.getSimpleName());

	@Override
	public void process(IncomingMessage incomingMessage) throws Exception {
		handleTravelPrompt(incomingMessage);
	}

	public void handleTravelPrompt(IncomingMessage incomingMessage) {
		try {
			LOG.info("{} - inside travel prompt processing ", incomingMessage.getMobile());

			JSONObject verticalWisePrompts = new JSONObject(ReloadableProperties.getVerticalPrompts());
			String prompt = verticalWisePrompts.getString(incomingMessage.getVertical());
			prompt = prompt.replace("user_prompt", incomingMessage.getUserMessage());

			JSONObject travelCategoryPromptResponse = callLlmModel(incomingMessage, prompt);

			String userResponse = "Sorry i am not able to interpret your query please reframe request and retry";

			if (travelCategoryPromptResponse.getJSONObject("meta").getBoolean("prompt_identified_success")) {

				JSONObject travelPrompt = new JSONObject(ReloadableProperties.getTravelPrompts());

				String categoryId = travelCategoryPromptResponse.getJSONObject("data").getString("category_id");

				if (travelPrompt.has(categoryId)) {
					String travelCategoryQueryPrompt = travelPrompt.getString(categoryId);
					travelCategoryQueryPrompt = travelCategoryQueryPrompt.replace("user_prompt",
							incomingMessage.getUserMessage());

					travelCategoryPromptResponse = callLlmModel(incomingMessage, travelCategoryQueryPrompt);

					userResponse = performCategoryWiseRapidApiCall(String.valueOf(incomingMessage.getMobile()),
							categoryId, travelCategoryPromptResponse);
				}

			} else {
				if (travelCategoryPromptResponse.getJSONObject("meta").has("missing_parameter")) {
					userResponse = travelCategoryPromptResponse.getJSONObject("meta").getString("missing_parameter");
				}
			}

			// call enterprise api

			executeMediaApi(String.valueOf(incomingMessage.getMobile()), userResponse);

		} catch (Exception e) {
			LOG.error("{} - exception while processing travel prompt ", incomingMessage.getMobile(), e);
		}
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

	public String performCategoryWiseRapidApiCall(String mobileNo, String categoryId, JSONObject llmResponse) {

		String promptForErr = "Something went wrong please try again later";

		if (categoryId.equalsIgnoreCase("1")) { // live train status

			String trainNo = llmResponse.getJSONObject("data").getString("train_no");

			HTTPResponse httpResponse = execute(
					ReloadableProperties.getRapidApiLiveTrainStatusUrl().replace("{{train_no}}", trainNo));

			if (httpResponse == null) {
				return promptForErr;
			}

			return generateSingleResponsePayload(mobileNo, categoryId, httpResponse);

		} else if (categoryId.equalsIgnoreCase("2")) { // search train by number

			String trainNo = llmResponse.getJSONObject("data").getString("train_no");

			HTTPResponse httpResponse = execute(
					ReloadableProperties.getRapidApiSearchByTrainNoUrl().replace("{{train_no}}", trainNo));

			if (httpResponse == null) {
				return promptForErr;
			}

			return generateSingleResponsePayload(mobileNo, categoryId, httpResponse);

		} else if (categoryId.equalsIgnoreCase("3")) { // train between 2 station

			String srcStationCode = llmResponse.getJSONObject("data").getString("source_station_code");
			String destStationCode = llmResponse.getJSONObject("data").getString("destination_station_code");
			String date = llmResponse.getJSONObject("data").getString("date");

			HTTPResponse httpResponse = execute(ReloadableProperties.getRapidApiSearchTrainBetweenStationUrl()
					.replace("{{srcStationCode}}", srcStationCode).replace("{{destStationCode}}", destStationCode)
					.replace("{{date}}", date));

			if (httpResponse == null) {
				return promptForErr;
			}
			return generateSingleResponsePayload(mobileNo, categoryId, httpResponse);

		} else if (categoryId.equalsIgnoreCase("4")) { // pnr status

			String pnrDetails = llmResponse.getJSONObject("data").getString("pnr_no");

			HTTPResponse httpResponse = execute(
					ReloadableProperties.getRapidApiPnrDetailsUrl().replace("{{pnr_no}}", pnrDetails));

			if (httpResponse == null) {
				return promptForErr;
			}

			return generateSingleResponsePayload(mobileNo, categoryId, httpResponse);

		} else if (categoryId.equalsIgnoreCase("5")) { // seat availability
			String classType = llmResponse.getJSONObject("data").getString("class_type");
			String srcStationCode = llmResponse.getJSONObject("data").getString("source_station_code");
			String destStationCode = llmResponse.getJSONObject("data").getString("destination_station_code");
			String trainNo = llmResponse.getJSONObject("data").getString("train_no");
			String date = llmResponse.getJSONObject("data").getString("date");

			HTTPResponse httpResponse = execute(ReloadableProperties.getRapidApiSeatAvailabilityUrl()
					.replace("{{class_type}}", classType).replace("{{quota}}", "GN")
					.replace("{{srcStationCode}}", srcStationCode).replace("{{destStationCode}}", destStationCode)
					.replace("{{train_no}}", trainNo).replace("{{date}}", date));

			if (httpResponse == null) {
				return promptForErr;
			}

			return generateSingleResponsePayload(mobileNo, categoryId, httpResponse);

		} else if (categoryId.equalsIgnoreCase("6")) { // get train fair
			String srcStationCode = llmResponse.getJSONObject("data").getString("source_station_code");
			String destStationCode = llmResponse.getJSONObject("data").getString("destination_station_code");
			String trainNo = llmResponse.getJSONObject("data").getString("train_no");

			HTTPResponse httpResponse = execute(
					ReloadableProperties.getRapidApiTrainFareUrl().replace("{{srcStationCode}}", srcStationCode)
							.replace("{{destStationCode}}", destStationCode).replace("{{train_no}}", trainNo));

			if (httpResponse == null) {
				return promptForErr;
			}

			return generateSingleResponsePayload(mobileNo, categoryId, httpResponse);

		} else if (categoryId.equalsIgnoreCase("7")) {
			String srcStationCode = llmResponse.getJSONObject("data").getString("source_station_code");
			String destStationCode = llmResponse.getJSONObject("data").getString("destination_station_code");
			String date = llmResponse.getJSONObject("data").getString("date");

			return handleDependentCategories(mobileNo, srcStationCode, destStationCode, date);
		}

		else {
			return "Requested query currently not supported";
		}

	}

	public HTTPResponse execute(String queryParam) {

		JSONObject headers = new JSONObject();
		headers.put("x-rapidapi-host", ReloadableProperties.getRapidApiHost());
		headers.put("x-rapidapi-key", ReloadableProperties.getRapidApiKey());

		String url = ReloadableProperties.getRapidApiRootUrl() + queryParam;

		HTTPRequest httpRequest = new HTTPRequest(url, RequestMethod.GET, headers, PayloadType.QUERYPARAMS, null);
		HTTPResponse response = httpRequest.execute();
		LOG.info("Retry count {} Http response {} ",response);
		return response;

	}

	public void executeMediaApi(String mobile, String msg) throws UnsupportedEncodingException {

		SendMessage sendMessage = new SendMessage();
		sendMessage.setChannel(Channel.WHATSAPP.name());
		sendMessage.setUserid(ReloadableProperties.getWhatsAppTwoWayAccountUserId());
		sendMessage.setPassword(ReloadableProperties.getWhatsAppTwoWayAccountPassword());
		sendMessage.setMethod("sendmessage");
		sendMessage.setSend_to(String.valueOf(mobile));
		sendMessage.setMsg(msg);
		sendMessage.setData_encoding("unicode_text");
		sendMessage.setMsg_type("text");
		SendMessageProcessor.instance.addEntry(sendMessage);
	}

	private String handleDependentCategories(String mobileNo, String srcStationCode, String destStationCode,
			String date) {
		String genericExceptionMsg = "Something went wrong while processing your request. Please try again later";
		String invalidParamMsg = "Please check your query and try again";
		String noDataAvailable = "Sorry no train details available. Please try for other dates";

		try {

			HTTPResponse httpResponse = execute(ReloadableProperties.getRapidApiSearchTrainBetweenStationUrl()
					.replace("{{srcStationCode}}", srcStationCode).replace("{{destStationCode}}", destStationCode)
					.replace("{{date}}", date));

			if (!httpResponse.isSuccessful()) {
				return genericExceptionMsg;
			}

			JSONObject rapidApiResponseObject = new JSONObject(httpResponse.getBodyAsString());

			if (rapidApiResponseObject.has("errors")) {
				return invalidParamMsg;
			}

			StringBuilder allTrainResponse = new StringBuilder();

			boolean isNotDataAvailable = rapidApiResponseObject.getJSONArray("data").isEmpty();

			if (isNotDataAvailable) {
				executeMediaApi(mobileNo, noDataAvailable);
			}

			for (int i = 0; i < rapidApiResponseObject.getJSONArray("data").length(); i++) {

				try {

					if (i > 0) {
						break;
					}

					JSONObject trainDetailsObject = rapidApiResponseObject.getJSONArray("data").getJSONObject(i);

					String trainNo = trainDetailsObject.getString("train_number");
					String srcStation = trainDetailsObject.getString("train_src");
					String destStation = trainDetailsObject.getString("train_dstn");

					HTTPResponse seatAvailabilityResponse = execute(ReloadableProperties
							.getRapidApiSeatAvailabilityUrl().replace("{{class_type}}", "2A").replace("{{quota}}", "GN")
							.replace("{{srcStationCode}}", srcStation).replace("{{destStationCode}}", destStation)
							.replace("{{train_no}}", trainNo).replace("{{date}}", date));

					JSONObject seatAvailabilityResponseObject = new JSONObject(
							seatAvailabilityResponse.getBodyAsString());

					if (rapidApiResponseObject.has("errors")) {
						return invalidParamMsg;
					}

					String category5Response = generateCategory5Response("5", seatAvailabilityResponseObject,
							ReloadableProperties.getTravelSingleResponseJson());

					allTrainResponse.append("🚂 *Train no - " + trainNo + "*").append("\n \n").append(category5Response)
							.append("\n\n");
				} catch (Exception e) {
					LOG.error("Exception while getting train details {} ", mobileNo);
				}

			}

			return allTrainResponse.toString();

		} catch (Exception e) {
			LOG.error("{} - exception while processing ", mobileNo, e);
			return genericExceptionMsg;
		}

	}

	public String generateSingleResponsePayload(String mobileNo, String category, HTTPResponse response) {

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

			JSONObject travelSingleResponseJson = ReloadableProperties.getTravelSingleResponseJson();

			if (category.equals("1")) {
				String msg = travelSingleResponseJson.getString(category);

				if (!rapidApiResponseObject.has("data")) {

				}

				JSONObject dataObject = rapidApiResponseObject.getJSONObject("data");

				String nextStation = "";

				if (dataObject.has("upcoming_stations")) {
					JSONArray upStationArr = dataObject.getJSONArray("upcoming_stations");

					for (int i = 0; i < upStationArr.length(); i++) {
						nextStation += upStationArr.getJSONObject(i).getString("station_name") + "\n";
					}

				}

				msg = msg.replace("{{train_name}}", dataObject.optString("train_name"))
						.replace("{{start_date}}", dataObject.optString("train_start_date"))
						.replace("{{src_station_name}}", dataObject.optString("source_stn_name"))
						.replace("{{dest_station_name}}", dataObject.optString("dest_stn_name"))
						.replace("{{available_on}}", dataObject.optString("run_days"))
						.replace("{{current_station}}", dataObject.optString("current_station_name"))
						.replace("{{status}}", dataObject.optString("ahead_distance_text"))
						.replace("{{next_station}}", nextStation)
						.replace("{{total_distance}}", String.valueOf(dataObject.optLong("total_distance")));

				return msg;
			} else if (category.equals("2")) {
				String msg = travelSingleResponseJson.getString(category);

				JSONObject dataObject = rapidApiResponseObject.getJSONArray("data").getJSONObject(0);
				return msg.replace("{{train_name}}", dataObject.optString("train_name"));

			} else if (category.equals("3")) {

				String trainDetails = "";

				for (int i = 0; i < rapidApiResponseObject.getJSONArray("data").length(); i++) {
					JSONObject trainDetailsObject = rapidApiResponseObject.getJSONArray("data").getJSONObject(i);

					String msg = travelSingleResponseJson.optString(category);
					msg = msg.replace("{{train_name}}", trainDetailsObject.optString("train_name"))
							.replace("{{train_number}}", trainDetailsObject.optString("train_number"))
							.replace("{{run_on}}", trainDetailsObject.optString("run_days"))
							.replace("{{class_type}}", trainDetailsObject.optString("class_type"))
							.replace("{{src_station_name}}", trainDetailsObject.optString("from_station_name"))
							.replace("{{dest_station_name}}", trainDetailsObject.optString("to_station_name"))
							.replace("{{date}}", trainDetailsObject.optString("train_date"));

					trainDetails = trainDetails + "\n\n" + msg;
				}

				if (StringUtils.isBlank(trainDetails)) {
					return "Not able to get details";
				} else {
					return "Train Details are as below : \n \n" + trainDetails;
				}
			} else if (category.equals("4")) {
				String msg = travelSingleResponseJson.getString(category);
				JSONObject dataObject = rapidApiResponseObject.getJSONObject("data");

				msg = msg.replace("{{train_name}}", dataObject.optString("TrainName"))
						.replace("{{train_no}}", dataObject.optString("TrainNo"))
						.replace("{{doj}}", dataObject.optString("Doj"))
						.replace("{{booking_date}}", dataObject.optString("BookingDate"))
						.replace("{{frm_actual_station}}", dataObject.optString("FromStnActual"))
						.replace("{{to_station}}", dataObject.optString("To"))
						.replace("{{boarding_station_name}}", dataObject.optString("BoardingStationName"))
						.replace("{{passenge_count}}", String.valueOf(dataObject.optInt("PassengerCount")))
						.replace("{{class}}", dataObject.optString("Class"))
						.replace("{{quota}}", dataObject.optString("Quota"))
						.replace("{{ticket_fare}}", dataObject.optString("TicketFare"))
						.replace("{{departure_time}}", dataObject.optString("DepartureTime"))
						.replace("{{arrival_time}}", dataObject.optString("ArrivalTime"))
						.replace("{{frm_station_name}}", dataObject.optString("SourceName"))
						.replace("{{boarding_station_name}}", dataObject.optString("BoardingStationName"));

				StringBuilder passengerDetailsBuilder = new StringBuilder();

				for (int i = 0; i < dataObject.getJSONArray("PassengerStatus").length(); i++) {

					StringBuilder passenger = new StringBuilder("*Passenger no " + (i + 1) + " details*");
					passenger.append("\n🛤️ *Coach* - "
							+ dataObject.getJSONArray("PassengerStatus").getJSONObject(i).optString("Coach") + "\n");
					passenger.append("🛏️ *Berth* - "
							+ dataObject.getJSONArray("PassengerStatus").getJSONObject(i).optInt("Berth") + "\n");
					passenger.append("📋*Booking Status* - "
							+ dataObject.getJSONArray("PassengerStatus").getJSONObject(i).optString("BookingStatus")
							+ "\n");
					passenger.append("🔄*Current Status* - "
							+ dataObject.getJSONArray("PassengerStatus").getJSONObject(i).optString("CurrentStatus")
							+ "\n");

					passengerDetailsBuilder.append(passenger.toString());
					passengerDetailsBuilder.append("\n");
				}

				msg = msg.replace("{{passender_details}}", passengerDetailsBuilder.toString());

				return msg;

			} else if (category.equals("5")) {

				return generateCategory5Response(category, rapidApiResponseObject, travelSingleResponseJson);

			} else if (category.equals("6")) {

				JSONArray generalCategory = rapidApiResponseObject.getJSONObject("data").getJSONArray("general");
				JSONArray tatkalCategory = rapidApiResponseObject.getJSONObject("data").getJSONArray("tatkal");

				StringBuilder gnCategoryBuilder = new StringBuilder("Category: 🛤️ General");
				StringBuilder tatkalCategoryBuilder = new StringBuilder("Category: 🚄 - Tatkal");

				StringBuilder trainTicketFareBuilder = new StringBuilder();

				for (int i = 0; i < generalCategory.length(); i++) {
					String msg = travelSingleResponseJson.getString(category);

					JSONArray breakUpObjectArry = generalCategory.getJSONObject(i).getJSONArray("breakup");

					String baseCharge = "0";
					String resrCharge = "0";
					String gstTax = "0";

					for (int j = 0; j < breakUpObjectArry.length(); j++) {

						if (breakUpObjectArry.getJSONObject(j).getString("key").equalsIgnoreCase("baseFare")) {
							baseCharge = String.valueOf(breakUpObjectArry.getJSONObject(j).optInt("cost"));
						}

						if (breakUpObjectArry.getJSONObject(j).getString("key")
								.equalsIgnoreCase("reservationCharges")) {
							resrCharge = String.valueOf(breakUpObjectArry.getJSONObject(j).getInt("cost"));
						}

						if (breakUpObjectArry.getJSONObject(j).getString("key").equalsIgnoreCase("serviceTax")) {
							gstTax = String.valueOf(breakUpObjectArry.getJSONObject(j).optInt("cost"));
						}
					}

					msg = msg.replace("{{class_type}}", generalCategory.getJSONObject(i).optString("classType"))
							.replace("{{base_charges}}", baseCharge).replace("{{reservation_charges}}", resrCharge)
							.replace("{{gst}}", gstTax)
							.replace("{{total}}", generalCategory.getJSONObject(i).optString("fare"));

					gnCategoryBuilder.append("\n\n").append(msg);

				}

				for (int i = 0; i < tatkalCategory.length(); i++) {

					String msg = travelSingleResponseJson.getString(category);

					JSONArray breakUpObjectArry = tatkalCategory.getJSONObject(i).getJSONArray("breakup");

					String baseCharge = "0";
					String resrCharge = "0";
					String gstTax = "0";

					for (int j = 0; j < breakUpObjectArry.length(); j++) {

						if (breakUpObjectArry.getJSONObject(j).getString("key").equalsIgnoreCase("baseFare")) {
							baseCharge = String.valueOf(breakUpObjectArry.getJSONObject(j).optInt("cost"));
						}

						if (breakUpObjectArry.getJSONObject(j).getString("key")
								.equalsIgnoreCase("reservationCharges")) {
							resrCharge = String.valueOf(breakUpObjectArry.getJSONObject(j).optInt("cost"));
						}

						if (breakUpObjectArry.getJSONObject(j).getString("key").equalsIgnoreCase("serviceTax")) {
							gstTax = String.valueOf(breakUpObjectArry.getJSONObject(j).optInt("cost"));
						}
					}

					msg = msg.replace("{{class_type}}", tatkalCategory.getJSONObject(i).optString("classType"))
							.replace("{{base_charges}}", baseCharge).replace("{{reservation_charges}}", resrCharge)
							.replace("{{gst}}", gstTax)
							.replace("{{total}}", tatkalCategory.getJSONObject(i).optString("fare"));

					tatkalCategoryBuilder.append("\n\n").append(msg);

				}

				return "Here are your details \n\n" + trainTicketFareBuilder.append(gnCategoryBuilder.toString())
						.append("\n\n").append(tatkalCategoryBuilder.toString()).toString();

			} else {
				return invalidParamMsg;
			}

		} catch (Exception e) {
			LOG.error("{} - exception while processing ", mobileNo, e);
			return genericExceptionMsg;
		}
	}

	private String generateCategory5Response(String category, JSONObject rapidApiResponseObject,
			JSONObject travelSingleResponseJson) {
		StringBuilder seatDetailsBuilder = new StringBuilder();

		for (int i = 0; i < rapidApiResponseObject.getJSONArray("data").length(); i++) {
			JSONObject seatDetailsObject = rapidApiResponseObject.getJSONArray("data").getJSONObject(i);

			String msg = travelSingleResponseJson.getString(category);

			msg = msg.replace("{{seat_no}}", String.valueOf(i + 1))
					.replace("{{ticket_fair}}", String.valueOf(seatDetailsObject.optInt("ticket_fare")))
					.replace("{{catering_charge}}", String.valueOf(seatDetailsObject.optInt("catering_charge")))
					.replace("{{total_fare}}", String.valueOf(seatDetailsObject.optInt("total_fare")))
					.replace("{{date}}", seatDetailsObject.optString("date"))
					.replace("{{confirm_probability}}",
							seatDetailsObject.has("confirm_probability")
									? seatDetailsObject.optString("confirm_probability")
									: "NA")
					.replace("{{current_status}}",
							seatDetailsObject.has("current_status") ? seatDetailsObject.optString("current_status")
									: "NA");

			seatDetailsBuilder.append(msg);
			seatDetailsBuilder.append("\n\n");

		}

		return seatDetailsBuilder.toString();

	}

}
