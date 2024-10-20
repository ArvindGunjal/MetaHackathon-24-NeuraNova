package com.meta.hackathon.model.vertical.reminder;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Getter
@Setter
public class Reminder {

	private String vertical;
	private String text;
	private String reminder_timestamp;
	private String channel;
	private String email_address;
	private String reminder_type;
	private String reminder_interval;
	private StopCondition stop_condition;
	private boolean channel_mismatch;

}
