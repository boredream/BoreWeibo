package com.boredream.boreweibo.entity.response;

import java.util.ArrayList;

import com.boredream.boreweibo.entity.Status;

public class StatusTimeLineResponse {

	private ArrayList<Status> statuses;
	private int total_number;

	public ArrayList<Status> getStatuses() {
		return statuses;
	}

	public void setStatuses(ArrayList<Status> statuses) {
		this.statuses = statuses;
	}

	public int getTotal_number() {
		return total_number;
	}

	public void setTotal_number(int total_number) {
		this.total_number = total_number;
	}

}
