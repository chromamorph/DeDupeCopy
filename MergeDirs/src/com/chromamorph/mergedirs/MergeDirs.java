package com.chromamorph.mergedirs;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.TreeSet;

public class MergeDirs {

	public static Path sourceDirPath = Path.of("/Users/susanne/Susannes Pictures");
	public static Path destDirPath = Path.of("/Volumes/192.168.1.10/Pictures");
	public static TreeSet<Path> sourceFilePaths = new TreeSet<Path>();

	public static int numDestFilesDeleted = 0;
	public static int numSourceFilesMoved = 0;
	public static int numSourceFilesDeleted = 0;
	public static int numSourceFilesMovedAndRenamed = 0;
	public static int numEmptyFoldersDeleted = 0;

	public static void mergeDirs(boolean move) throws IOException {
		for(Path sourceFilePath : sourceFilePaths) {
			try {
				if (!sourceFilePath.toFile().isDirectory()) {
					Path sourceFileSubPath = sourceDirPath.relativize(sourceFilePath);
					Path destinationFilePath = destDirPath.resolve(sourceFileSubPath);
					if (!Files.exists(destinationFilePath)) {
						if (!Files.exists(destinationFilePath.getParent()))
							Files.createDirectories(destinationFilePath.getParent());
						if (move)
							Files.move(sourceFilePath, destinationFilePath);
						else
							Files.copy(sourceFilePath, destinationFilePath, StandardCopyOption.COPY_ATTRIBUTES);
						numSourceFilesMoved++;
					} else if (Files.mismatch(sourceFilePath,destinationFilePath) == -1L){ 
						//						Destination file exists and has identical content to source file
						FileTime destFileTime = Files.getLastModifiedTime(destinationFilePath);
						FileTime sourceFileTime = Files.getLastModifiedTime(sourceFilePath);
						if (sourceFileTime.compareTo(destFileTime) < 0) {
							//							Source file time is earlier than destination file time
							Files.delete(destinationFilePath);
							numDestFilesDeleted++;
							if (move)
								Files.move(sourceFilePath, destinationFilePath);
							else
								Files.copy(sourceFilePath, destinationFilePath, StandardCopyOption.COPY_ATTRIBUTES);
							numSourceFilesMoved++;
						} else if (move) {
							Files.delete(sourceFilePath);
							numSourceFilesDeleted++;
						}
					} else {
						//						Destination file exists with same name but not identical to source
						//						Move source file but rename it
						boolean moved = false;							
						for(int i = 1; !moved; i++) {
							String destFilePathString = destinationFilePath.toString();
							int suffixStart = destFilePathString.lastIndexOf('.');
							String suffix = "";
							if (suffixStart != -1)
								suffix = destFilePathString.substring(suffixStart);
							else
								suffixStart = destFilePathString.length();
							String newDestFilePathString = destFilePathString.substring(0,suffixStart) + String.format("-%03d", i) + suffix;
							Path newDestFilePath = Path.of(newDestFilePathString);
							//							System.out.println(newDestFilePath);
							if (!Files.exists(newDestFilePath)) {
								if (move)
									Files.move(sourceFilePath,newDestFilePath);
								else
									Files.copy(sourceFilePath, newDestFilePath, StandardCopyOption.COPY_ATTRIBUTES);
								numSourceFilesMovedAndRenamed++;
								moved = true;
							} else if (move && Files.mismatch(sourceFilePath, newDestFilePath) == -1L) {
								//								Renamed file already exists and is identical to this one
								Files.delete(sourceFilePath);
								numSourceFilesDeleted++;
								moved = true;
							}
						}
					}
				}
			} catch (IOException e) {
				System.out.println("ERROR on file: "+ sourceFilePath);
			}
		}
	}

	public static boolean isEmptyDir(Path dirPath) {
		if (!Files.isDirectory(dirPath)) return false;
		String[] fileList = dirPath.toFile().list();
		return (fileList.length == 0);
	}

	public static void removeFolderIfEmpty(Path dirPath) throws IOException {
		if (Files.isDirectory(dirPath) && isEmptyDir(dirPath)) {
			Files.delete(dirPath);
			numEmptyFoldersDeleted++;
		}
	}

	public static void getSourceFilePaths(Path rootPath) {
		String[] fileList = rootPath.toFile().list();
		ArrayList<Path> dirList = new ArrayList<Path>();
		for(String f : fileList) {
			Path p = rootPath.resolve(f);
			if (p.toFile().isDirectory()) {
				dirList.add(p);				
			}
			sourceFilePaths.add(p);
		}
		for(Path dirPath : dirList) {
			getSourceFilePaths(dirPath);
		}
	}

	public static void removeEmptyFolders() throws IOException {
		ArrayList<Path> emptyDirs = new ArrayList<Path>();

		for(Path p : sourceFilePaths) {
			if (p.toFile().isDirectory() && isEmptyDir(p)) {
				emptyDirs.add(p);
			}
		}
		Collections.sort(emptyDirs, new Comparator<Path>() {

			@Override
			public int compare(Path o1, Path o2) {
				return -1 * o1.compareTo(o2);
			}

		});
		for(Path p : emptyDirs) {
			Files.delete(p);
			numEmptyFoldersDeleted++;
		}
	}

	public static void outputReport() {
		System.out.println(numSourceFilesDeleted + " source files deleted");
		System.out.println(numDestFilesDeleted + " destination files deleted");
		System.out.println(numSourceFilesMoved + " source files moved");
		System.out.println(numSourceFilesMovedAndRenamed + " source files moved and renamed");
		System.out.println(numEmptyFoldersDeleted + " empty folders deleted");
	}

	public static void main(String[] args) {

		if (args.length > 0)
			sourceDirPath = Path.of(args[0]);
		if (args.length > 1)
			destDirPath = Path.of(args[1]);
		try {
			getSourceFilePaths(sourceDirPath);
			System.out.println(sourceFilePaths.size() + " source files found");
			mergeDirs(false); // false indicates copy instead of move
			removeEmptyFolders();
			outputReport();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
