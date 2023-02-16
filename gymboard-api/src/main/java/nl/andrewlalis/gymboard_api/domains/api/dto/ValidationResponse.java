package nl.andrewlalis.gymboard_api.domains.api.dto;

import java.util.ArrayList;
import java.util.List;

public class ValidationResponse {
	private boolean valid = true;
	private List<String> messages = new ArrayList<>();

	public void addMessage(String message) {
		this.messages.add(message);
		this.valid = false;
	}

	public boolean isValid() {
		return valid;
	}

	public List<String> getMessages() {
		return messages;
	}
}
