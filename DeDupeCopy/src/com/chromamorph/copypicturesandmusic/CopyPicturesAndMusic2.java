package com.chromamorph.copypicturesandmusic;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Calendar;

import org.apache.commons.io.FileUtils;

public class CopyPicturesAndMusic2 {

	static PrintWriter logFilePrintWriter = null;

	public static String[] INPUT_FOLDERS = {
		//		"/Volumes/SEAGATE4TB/Billeder til CD",
		//		"/Volumes/SEAGATE4TB/Dave Dropbox",
		//				"/Volumes/SEAGATE4TB/Dave MacBookPro 2014",
		//				"/Volumes/SEAGATE4TB/Formac 120GB  211205512",
		//				"/Volumes/SEAGATE4TB/Seagate 500GB 2GHWDHAG",
		//		"/Volumes/SEAGATE4TB/Seagate Dashboard",
		//				"/Volumes/SEAGATE4TB/Susanne Dell Desktop",
		//				"/Volumes/SEAGATE4TB/Susanne Dell Inspiron Laptop",
//		"/Volumes/SEAGATE4TB/Susanne Dell Inspiron Laptop/Users/Susanne",
//		"/Volumes/SEAGATE4TB/Susanne Dell Inspiron Laptop/Users/Susanne_2",
//		"/Volumes/SEAGATE4TB/Susanne MacBookPro Other",
//		"/Volumes/SEAGATE4TB/Susanne MacBookPro Pictures"
//		"/Volumes/SEAGATE4TB/Susanne white iBook G4",
//		"/Volumes/SEAGATE4TB/Susannes iPhone 4S",
//		"/Volumes/SEAGATE4TB/trashbox",
		"/Volumes/SEAGATE4TB/Unused"
	};

	public static Path ROOT_OUTPUT_FOLDER = Paths.get("/Volumes/SEAGATE4TB");
	public static Path PICTURES_FOLDER = ROOT_OUTPUT_FOLDER.resolve("Pictures");
	public static Path MUSIC_FOLDER = ROOT_OUTPUT_FOLDER.resolve("Music");

	static String[] imageFileSuffixes = {
		"gif",
		"jp2",
		"jpg",
		"mov",
		"avi",
		//			"pct",
		//			"pdf",
		"png",
		"psd",
		"tif",
		"cr2",
		"bmp",
	"raw"};

	static String[] musicFileSuffixes = {
		"wav",
		"mp3",
		"m4a",
		"m4p",
		"m4r"
	};

	public static boolean isImageFile(Path filePath) {
		for(String imageSuffix : imageFileSuffixes)
			if (filePath.getFileName().toString().toLowerCase().endsWith("."+imageSuffix))
				return true;
		return false;
	}

	public static boolean isMusicFile(Path filePath) {
		for(String musicSuffix : musicFileSuffixes)
			if (filePath.getFileName().toString().toLowerCase().endsWith("."+musicSuffix))
				return true;
		return false;
	}

	/**
	 * Returns true if file at filePath with any capitalization exists
	 * @param filePath
	 * @return
	 */
	public static boolean fileExists(Path filePath) {
		if (Files.exists(filePath)) {
			//			System.out.println("basic file exists");
			return true;
		}
		String suffix = filePath.toString().substring(filePath.toString().lastIndexOf(".")+1);
		String pathWithoutSuffix = filePath.toString().substring(0, filePath.toString().lastIndexOf(".")+1);
		if (Files.exists(Paths.get(pathWithoutSuffix+suffix.toLowerCase()))) {
			//			System.out.println("tolowercase");
			return true;
		}
		if (Files.exists(Paths.get(pathWithoutSuffix+suffix.toUpperCase()))) {
			//			System.out.println("touppercase");
			return true;
		}
		return false;
	}

	public static void copyImageFile(Path filePath) {
		try {
			Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(Files.getLastModifiedTime(filePath).toMillis());
			String yearString = String.format("%d",cal.get(Calendar.YEAR));
			String monthString = String.format("%02d", cal.get(Calendar.MONTH)+1);
			Path name = filePath.getFileName();
			String nameString = name.toString().substring(0, name.toString().lastIndexOf("."));
			String suffix = name.toString().substring(name.toString().lastIndexOf(".")+1);
			Path outputFilePath = PICTURES_FOLDER.resolve(yearString).resolve(monthString).resolve(name);
			int i = 1;
			while (fileExists(outputFilePath)) {
				if (FileUtils.contentEquals(outputFilePath.toFile(), filePath.toFile())) {
					logFilePrintWriterprintln("File NOT copied: "+filePath.toString()+"\n    same as "+outputFilePath);
					return;
				}
				outputFilePath = outputFilePath.resolveSibling(nameString+String.format("_%03d",i)+"."+suffix);
				i++;
			}
			File outputFile = outputFilePath.toFile();
			outputFile.getParentFile().mkdirs();

			Files.copy(filePath,outputFilePath,StandardCopyOption.COPY_ATTRIBUTES);
			logFilePrintWriterprintln("\nFile copied: "+filePath.toString()+"\n    > "+outputFilePath);System.out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}


	public static void copyMusicFile(Path filePath) {
		try {

			/*
			 * if filePath's grandparent folder is "Compilations", then output path is MUSIC_FOLDER/Compilations/parentfolder/filename
			 * otherwise, output path is MUSIC_FOLDER/parentfolder/fileName
			 */ 

			Path outputFilePath = null;

			if (filePath.getNameCount() >= 3) {
				Path pathFromGrandParent = filePath.subpath(filePath.getNameCount() - 3, filePath.getNameCount());
				outputFilePath = MUSIC_FOLDER.resolve(pathFromGrandParent);
			} else if (filePath.getNameCount() == 2) {
				Path pathFromGrandParent = filePath.subpath(filePath.getNameCount() - 2, filePath.getNameCount());
				outputFilePath = MUSIC_FOLDER.resolve("other").resolve(pathFromGrandParent);
			} else if (filePath.getNameCount() == 1) {
				Path pathFromGrandParent = filePath.subpath(filePath.getNameCount() - 1, filePath.getNameCount());
				outputFilePath = MUSIC_FOLDER.resolve("other").resolve("other").resolve(pathFromGrandParent);
			}

			Path name = filePath.getFileName();
			String nameString = null;
			String suffix = null;
			try {
				nameString = name.toString().substring(0, name.toString().lastIndexOf("."));
				suffix = name.toString().substring(name.toString().lastIndexOf(".")+1);
			}catch (StringIndexOutOfBoundsException e) {
				logFilePrintWriterprintln(e.getMessage());
				logFilePrintWriterprintln("copyMusicFile threw exception on file: "+filePath);
				closeLogFile();
				throw e;
			}

			/* while there is already a file at the outputpath, then
			 * 		if this existing file has the same content as this file
			 * 			continue to next file
			 * 		else
			 * 			modify outputpath
			 */

			int i = 1;
			while (fileExists(outputFilePath)) {
				if (FileUtils.contentEquals(outputFilePath.toFile(), filePath.toFile())) {
					logFilePrintWriterprintln("File NOT copied: "+filePath.toString()+"\n    same as "+outputFilePath);
					return;
				}
				outputFilePath = outputFilePath.resolveSibling(nameString+String.format("_%03d",i)+"."+suffix);
				i++;
			}
			File outputFile = outputFilePath.toFile();
			outputFile.getParentFile().mkdirs();

			Files.copy(filePath,outputFilePath,StandardCopyOption.COPY_ATTRIBUTES);
			logFilePrintWriterprintln("\nFile copied: "+filePath.toString()+"\n    > "+outputFilePath);System.out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}

	public static void logFilePrintWriterprintln(String s) {
		logFilePrintWriter.println(s);
		System.out.println(s);
	}

	public static void setupLogFile() {
		Calendar cal = Calendar.getInstance();
		String yearString = String.format("%d",cal.get(Calendar.YEAR));
		String monthString = String.format("%02d",cal.get(Calendar.MONTH)+1);
		String dateString = String.format("%02d",cal.get(Calendar.DAY_OF_MONTH));
		String hourString = String.format("%02d",cal.get(Calendar.HOUR));
		String minuteString = String.format("%02d",cal.get(Calendar.MINUTE));
		String timeString = yearString+"-"+monthString+"-"+dateString+"-"+hourString+"-"+minuteString;
		String logFileName = timeString+"-copy.log";
		try {
			logFilePrintWriter = new PrintWriter(ROOT_OUTPUT_FOLDER.resolve(logFileName).toFile());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public static void closeLogFile() {
		logFilePrintWriter.flush();
		logFilePrintWriter.close();
	}

	public static void copyFiles(Path startPath) {
		String[] fileList = startPath.toFile().list();
		for(String fileName : fileList) {
			Path filePath = startPath.resolve(fileName);
			if (Files.isDirectory(filePath))
				copyFiles(filePath);
			else if (isImageFile(filePath))
				copyImageFile(filePath);
			else if (isMusicFile(filePath))
				copyMusicFile(filePath);
		}
	}
	
	public static void main(String[] args) {
			setupLogFile();
			for(String startPathString : INPUT_FOLDERS) {
				Path startPath = Paths.get(startPathString);
				copyFiles(startPath);
			}
			closeLogFile();
	}
}
