package com.watchservice;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.HashMap;
import java.util.Map;

public class WatchServiceExample {
	private final WatchService watcher;
	private final Map<WatchKey, Path> dirWatchers;

	public WatchServiceExample(Path dir) throws IOException {
		watcher = FileSystems.getDefault().newWatchService();
		dirWatchers = new HashMap<>();

		scanAndRegisterDirectories(dir);
	}

	private void scanAndRegisterDirectories(Path start) throws IOException {
		Files.walkFileTree(start, new SimpleFileVisitor<Path>(p -> {
			try {
				registerDirectoryWatchers(p);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}));
	}

	private void registerDirectoryWatchers(Path dir) throws IOException {
		WatchKey key = dir.register(watcher, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE,
				StandardWatchEventKinds.ENTRY_MODIFY);

		dirWatchers.put(key, dir);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	void processEvent() {
		while (true) {
			WatchKey key = null;

			try {
				key = watcher.take();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			Path dir = dirWatchers.get(key);
			if (dir == null)
				continue;
			for (WatchEvent<?> event : key.pollEvents()) {

				WatchEvent.Kind kind = event.kind();

				Path name = ((WatchEvent<Path>) event).context();
				Path child = dir.resolve(name);
				System.out.println(event.kind().name() + " " + child + "\n");

				if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
					if (Files.isDirectory(child)) {
						try {
							scanAndRegisterDirectories(child);
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				} else if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
					if (Files.isDirectory(child)) {
						dirWatchers.remove(key);
					}
				}
			}

			boolean valid = key.reset();

			if (!valid) {
				dirWatchers.remove(key);
				if (dirWatchers.isEmpty())
					break;
			}
		}
	}
}
