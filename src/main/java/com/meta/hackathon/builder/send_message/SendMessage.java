package com.meta.hackathon.builder.send_message;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import com.meta.hackathon.config.ReloadableProperties;
import com.meta.hackathon.enums.Channel;
import com.meta.hackathon.enums.PayloadType;
import com.meta.hackathon.enums.RequestMethod;
import com.meta.hackathon.http.HTTPRequest;
import com.meta.hackathon.http.HTTPResponse;
import com.meta.Queue.queue.Writable;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SendMessage extends Writable {

	private String channel;
	private String userid;
	private String password;
	private String method;
	private String auth_scheme = "plain";
	private String v = "1.1";
	private String format = "json";
	private String send_to;
	private String recipients;
	private String msg_type;
	private String header;
	private String dltTemplateId;
	private String template_id;
	private Map<String, String> variables;
	private String msg;
	private String caption;
	private String footer;
	private String subject;
	private String content;
	private String name;
	private String fromEmailId;
	private String replyToEmailId;
	private String content_type;
	private String media_url;
	private String filename;
	private String isTemplate;
	private String isHSM;
	private String data_encoding;
	private String timestamp;
	private String scheduled_at;

	public HTTPResponse send() {
		String url = null;
		JSONObject payloadObject = prepareCommonParamsObject();
		if (this.channel.equalsIgnoreCase(Channel.WHATSAPP.name())) {
			payloadObject = prepareWhatsAppObject(payloadObject);
			url = ReloadableProperties.getSendWhatsAppAPI();
		} else if (this.channel.equalsIgnoreCase(Channel.SMS.name())) {
			payloadObject = prepareSMSObject(payloadObject);
			url = ReloadableProperties.getSendSmsAPI();
		} else if (this.channel.equalsIgnoreCase(Channel.EMAIL.name())) {
			payloadObject = prepareEmailObject(payloadObject);
			url = ReloadableProperties.getSendEmailAPI();
		}
		return new HTTPRequest(url, RequestMethod.POST, null, PayloadType.FORMURLENCODED, payloadObject.toString())
				.execute();
	}

	private JSONObject prepareCommonParamsObject() {
		JSONObject commonParams = new JSONObject();
		commonParams.put("userid", this.userid);
		commonParams.put("password", this.password);
		commonParams.put("auth_scheme", this.auth_scheme);
		commonParams.put("v", this.v);
		commonParams.put("format", this.format);
		commonParams.put("method", this.method);
		if (StringUtils.isNotBlank(this.timestamp)) {
			commonParams.put("timestamp", this.timestamp);
		}
		if (this.variables != null && !this.variables.isEmpty()) {
			for (Map.Entry<String, String> set : this.variables.entrySet()) {
				commonParams.put(set.getKey(), set.getValue());
			}
		}
		return commonParams;

	}

	private JSONObject prepareEmailObject(JSONObject paramsObject) {
		paramsObject.put("subject", this.subject);
		paramsObject.put("content_type", this.content_type);
		paramsObject.put("recipients", this.recipients);
		paramsObject.put("name", this.name);
		paramsObject.put("fromEmailId", this.fromEmailId);
		paramsObject.put("replyToEmailId", this.replyToEmailId);
		paramsObject.put("content", this.content);
		if (StringUtils.isNotBlank(this.scheduled_at)) {
			paramsObject.put("scheduled_at", this.scheduled_at);
		}
		return paramsObject;
	}

	private JSONObject prepareSMSObject(JSONObject paramsObject) {
		paramsObject.put("msg", this.msg);
		paramsObject.put("send_to", this.send_to);
		paramsObject.put("msg_type", this.msg_type);
		if (StringUtils.isNotBlank(this.dltTemplateId)) {
			paramsObject.put("dltTemplateId", this.dltTemplateId);
		}

		return paramsObject;
	}

	private JSONObject prepareWhatsAppObject(JSONObject paramsObject) {
		paramsObject.put("send_to", this.send_to);
		paramsObject.put("msg_type", this.msg_type);
		if (StringUtils.isNotBlank(this.header)) {
			paramsObject.put("header", this.header);
		}

		if (StringUtils.isNotBlank(this.template_id)) {
			paramsObject.put("template_id", this.template_id);
		}

		if (this.msg_type.equalsIgnoreCase("text")) {
			paramsObject.put("msg", this.msg);
		} else {
			paramsObject.put("media_url", this.media_url);
			paramsObject.put("caption", this.caption);
		}

		if (StringUtils.isNotBlank(this.footer)) {
			paramsObject.put("footer", this.footer);
		}

		if (StringUtils.isNotBlank(this.filename)) {
			paramsObject.put("filename", this.filename);
		}
		if (StringUtils.isNotBlank(this.isTemplate)) {
			paramsObject.put("isTemplate", this.isTemplate);
		}

		if (StringUtils.isNotBlank(this.isHSM)) {
			paramsObject.put("isHSM", this.isHSM);
		}

		if (StringUtils.isNotBlank(this.data_encoding)) {
			paramsObject.put("data_encoding", this.data_encoding);
		}

		return paramsObject;
	}

}
