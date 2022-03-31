package com.watchservice;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class WatcherTest {
	public static String HOME = "E:\\BridgeLabz";
	public static String PLAY_WITH_NIO = "watch_dir";

	public static void main(String[] args) throws IOException {
		Path playPath = Paths.get(HOME + "/" + PLAY_WITH_NIO);

		if (Files.notExists(playPath))
			Files.createDirectory(playPath);
		System.out.println(playPath);
		new WatchServiceExample(playPath).processEvent();
	}
}
