package com.eurotech.edcdroid.gateway;

import java.util.regex.Pattern;

import com.eurotech.cloud.message.EdcInvalidTopicException;

public class EdcTopicPattern {

	static Pattern s_escaper = Pattern.compile("([^a-zA-z0-9/+#]|\\[|\\]|\\^|\\\\)");

	private String  m_topic;
	private String  m_regex;
	private Pattern m_pattern;

	private EdcTopicPattern(String topic, String regex) {
		m_topic   = topic;
		m_regex   = regex;
		m_pattern = Pattern.compile(regex);

	}

	public String getTopic() {
		return m_topic;
	}

	public String getRegex() {
		return m_regex;
	}

	public boolean matches(String topic) {
		boolean matches = m_pattern.matcher(topic).matches();
		return matches;
	}

	public static EdcTopicPattern compile(String topic) throws EdcInvalidTopicException {

		String processedTopic = topic.trim();

		processedTopic = escapeRegEx(processedTopic);

		String[] topicParts = processedTopic.split("/");
		String account = topicParts.length > 0 ? topicParts[0] : "unknown";

		int hashPosition = processedTopic.indexOf("#");
		if (hashPosition != -1) {

			if (hashPosition == 0) {
				if (processedTopic.length() != 1) {
					throw new EdcInvalidTopicException(account, processedTopic);
				}
			}
			else if (!(hashPosition == (processedTopic.length()-1) &&
					processedTopic.charAt(hashPosition-1) == '/')) {
				throw new EdcInvalidTopicException(account, processedTopic);
			}
		}

		int plusPosition = processedTopic.indexOf("+");
		if (plusPosition != -1) {

			if (plusPosition == 0) {
				if (processedTopic.length() > 1 &&
						processedTopic.charAt(plusPosition+1) != '/') {
					throw new EdcInvalidTopicException(account, processedTopic);
				}
			}
			else if (plusPosition == (processedTopic.length()-1)) {
				if (processedTopic.charAt(plusPosition-1) != '/') {
					throw new EdcInvalidTopicException(account, processedTopic);
				}
			}
			else if (processedTopic.charAt(plusPosition-1) != '/' ||
					processedTopic.charAt(plusPosition+1) != '/') {
				throw new EdcInvalidTopicException(account, processedTopic);
			}
		}

		String regex = "^" + processedTopic;
		regex = regex.replace("+", "[^/]+");
		regex = regex.replace("/+", "/[^/]+");
		regex = regex.replace("/#", "(/.+)?$");

		return new EdcTopicPattern(topic, regex);
	}

	private static String escapeRegEx(String str) {
		return s_escaper.matcher(str).replaceAll("\\\\$1");
	}
}
