package com.TickTracker.config;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum LogFormatStyle {
	SESSION("Session"),
	EACH_TICK("Each tick");

	private final String name;

	@Override
	public String toString()
	{
		return name;
	}
}
