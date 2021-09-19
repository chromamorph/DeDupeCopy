package com.chromamorph.newdedupecopy;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.TreeSet;

public class DeDupeCopyFolders {
	public static void main(String[] args) {
		/*
		 * Make sorted list of all folders in root input folder.
		 * Sort folders by reverse order, so that /a/b/c, /a/c, /a/b would be in the order
		 * /a/b (b,a)
		 * /a/c (c,a)
		 * /a/b/c (c,b,a)
		 */
		
		 Path rootFolder = Paths.get("/Users/susanne/Archive");
		 TreeSet<Folder> folders = new TreeSet<Folder>();

		 try {
			Files.walkFileTree(rootFolder, new SimpleFileVisitor<Path>() {
					@Override
					public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
							throws IOException
					{
					}
				});
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		/*
		 * We want to remove from the list all duplicate folders.
		 * A duplicate folder is one that contains exactly the same files
		 * with exactly the same Modified dates and content created dates.
		 */
	}

}
