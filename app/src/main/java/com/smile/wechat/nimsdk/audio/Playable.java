package com.smile.wechat.nimsdk.audio;

public interface Playable {
	long getDuration();
	String getPath();
	boolean isAudioEqual(Playable audio);
}