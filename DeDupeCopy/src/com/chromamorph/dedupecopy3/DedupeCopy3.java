package com.chromamorph.dedupecopy3;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Set;

import javax.swing.JFileChooser;

import org.apache.commons.io.FileUtils;

public class DedupeCopy3 {

	static Path inputRootDirectoryPath = null;
	static Path outputRootDirectoryPath = null;
	static Path logFilePath = null;
	static PrintWriter logFilePrintWriter = null;
	static HashMap<Long,ArrayList<Path>> copiedFilesHashMap = new HashMap<Long,ArrayList<Path>>();

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


	public static void logFilePrintWriterprintln(String s) {
		logFilePrintWriter.println(s);
		System.out.println(s);
	}
	
	public static void setInputAndOutputDirectories() {
		JFileChooser chooser = new JFileChooser();
		chooser.setDialogTitle("Select input directory");
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		int returnVal = chooser.showOpenDialog(null);
		if (returnVal != JFileChooser.APPROVE_OPTION) return;
		inputRootDirectoryPath = chooser.getSelectedFile().toPath();
		chooser.setDialogTitle("Select output directory");
		returnVal = chooser.showOpenDialog(null);
		if (returnVal != JFileChooser.APPROVE_OPTION) return;
		outputRootDirectoryPath = chooser.getSelectedFile().toPath();
	}

	public static void copyFiles(Path sourceDirectoryPath) {
		String[] fileList = sourceDirectoryPath.toFile().list();
		for(String fileName : fileList) {
			if (fileName.equals(".") || fileName.equals(".."))
				continue;
			Path thisFilePath = sourceDirectoryPath.resolve(fileName);
			if (Files.isDirectory(thisFilePath))
				copyFiles(thisFilePath);
			else if (isImageFile(thisFilePath)) {
				processImageFile(thisFilePath);
			}
		}
	}

	public static void processImageFile(Path filePath) {
		Path alreadyCopiedFile = null;
		/*
		 * If 
		 * 		no file has already been copied that has the same content
		 */ 
		if ((alreadyCopiedFile = fileAlreadyCopiedWithSameContent(filePath)) == null)
			/* then 
			 * 		copy this file to the directory for its last modified date
			 */
		{
			copyFile(filePath);  //Remember to add to hashMap!
		}
		/*
		 * If 
		 * 		a file has already been copied that has the same content
		 * 		and this file's last modified date is legitimate
		 * 		and the copied file has a later last modified date than this file 
		 * 			or the copied file's last modified date may not be legitimate
		 */
		else if (lastModifiedDateIsLegitimate(filePath) &&
				(!lastModifiedDateIsLegitimate(alreadyCopiedFile) ||
						lastModifiedDateLaterThan(alreadyCopiedFile,filePath)))
			/* then
			 * 		copy this file to the directory for its last modified date
			 * 		delete already-copied file with same content
			 */
		{
			deleteFile(alreadyCopiedFile); //Remember to remove this file from the hashMap!
			copyFile(filePath);
		}
	}

	public static boolean lastModifiedDateIsLegitimate(Path filePath) {
		try {
			FileTime lastModifiedTime = Files.getLastModifiedTime(filePath);
			Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(lastModifiedTime.toMillis());
			int dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);
			int month = cal.get(Calendar.MONTH);
			int hour = cal.get(Calendar.HOUR);
			int minute = cal.get(Calendar.MINUTE);
			if (minute == 0 && hour == 0 && month == 0 && dayOfMonth == 1)
				return false;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return true;
	}

	public static void deleteFile(Path filePath) {
		try {
			removeFileFromHashMap(filePath);
			Files.delete(filePath);
			logFilePrintWriterprintln("\nFile deleted: "+filePath.toString());
			Path parentPath = filePath.getParent();
			while(isEmptyDirectory(parentPath)) {
				Files.delete(parentPath);
				logFilePrintWriterprintln("\nDirectory deleted: "+parentPath.toString());
				parentPath = parentPath.getParent();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static boolean isEmptyDirectory(Path filePath) {
		if (!Files.isDirectory(filePath)) return false;
		String[] fileList = filePath.toFile().list();
		return fileList.length == 0;
	}
	
	public static boolean lastModifiedDateLaterThan(Path filePath1, Path filePath2) {
		try {
			FileTime time1 = Files.getLastModifiedTime(filePath1);
			FileTime time2 = Files.getLastModifiedTime(filePath2);
			return (time1.compareTo(time2) > 0);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	public static void copyFile(Path filePath) {
		try {
			Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(Files.getLastModifiedTime(filePath).toMillis());
			String yearString = String.format("%d",cal.get(Calendar.YEAR));
			String monthString = String.format("%02d", cal.get(Calendar.MONTH)+1);
			Path name = filePath.getFileName();
			String nameString = name.toString().substring(0, name.toString().lastIndexOf("."));
			String suffix = name.toString().substring(name.toString().lastIndexOf(".")+1);
			Path outputFilePath = outputRootDirectoryPath.resolve("files").resolve(yearString).resolve(monthString).resolve(name);
			int i = 1;
			while (fileExists(outputFilePath)) {
				outputFilePath = outputFilePath.resolveSibling(nameString+String.format("_%03d",i)+"."+suffix);
				i++;
			}
			File outputFile = outputFilePath.toFile();
			outputFile.getParentFile().mkdirs();

			Files.copy(filePath,outputFilePath,StandardCopyOption.COPY_ATTRIBUTES);
			addFileToHashMap(outputFilePath);
			logFilePrintWriterprintln("\nFile copied: "+filePath.toString()+"\n    > "+outputFilePath);System.out.flush();
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}

	public static void addFileToHashMap(Path filePath) {
		Long key = getHashKey(filePath);
		ArrayList<Path> pathsForKey = copiedFilesHashMap.get(key);
		if (pathsForKey == null) { //Make new key-value pair for this filePath
			ArrayList<Path> filePathArrayList = new ArrayList<Path>();
			filePathArrayList.add(filePath);
			copiedFilesHashMap.put(key, filePathArrayList);
		}  else { // Add this filePath to the value for the key already in the hashMap
			pathsForKey.add(filePath);
		}
		
	}

	public static void removeFileFromHashMap(Path filePath) {
		Long key = getHashKey(filePath);
		ArrayList<Path> pathsForKey = copiedFilesHashMap.get(key);
		if (pathsForKey.size() == 1 && pathsForKey.get(0).equals(filePath))
			copiedFilesHashMap.remove(key);
		else if (pathsForKey.size() > 1)
			pathsForKey.remove(filePath);
		else
			try {
				throw new Exception("removeFileFromHashMap required to remove file, "+filePath.toString()+" contains a key with 1 path that is not equal to it!");
			} catch (Exception e) {
				e.printStackTrace();
			}
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

	public static Long getHashKey(Path filePath) {
		try {
			return Files.size(filePath);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Path fileAlreadyCopiedWithSameContent(Path filePath) {
		try {
			Long key = getHashKey(filePath);
			ArrayList<Path> alreadyCopiedFiles = copiedFilesHashMap.get(key);
			if (alreadyCopiedFiles == null)
				return null;
			else { //Check each file in paths for this key to see if it has the same content
				File file = filePath.toFile();
				for(Path pathOfCopiedFile : alreadyCopiedFiles) {
					if (FileUtils.contentEquals(pathOfCopiedFile.toFile(), file)) {
						logFilePrintWriterprintln("\nFound copied file that is equal to file: "+pathOfCopiedFile+" has same content as "+ filePath);
						return pathOfCopiedFile;
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static boolean isImageFile(Path filePath) {
		for(String imageSuffix : imageFileSuffixes)
			if (filePath.getFileName().toString().toLowerCase().endsWith(imageSuffix))
				return true;
		return false;
	}

	static HashMap<Long,ArrayList<Path>> getHashMapOfFiles(Path rootDirectoryPath) {
		HashMap<Long,ArrayList<Path>> hashMap = new HashMap<Long,ArrayList<Path>>();
		String[] fileList = rootDirectoryPath.toFile().list();
		for(String fileName : fileList) {
			if (fileName.equals(".") || fileName.equals("..")) continue;
			Path filePath = rootDirectoryPath.resolve(fileName);
			if (Files.isDirectory(filePath)) {
				HashMap<Long,ArrayList<Path>> subHashMap = getHashMapOfFiles(filePath);
				Set<Long> subKeySet = subHashMap.keySet();
				for(Long key : subKeySet) {
					ArrayList<Path> pathsForThisKey = hashMap.get(key);
					if (pathsForThisKey == null)
						hashMap.put(key, subHashMap.get(key));
					else
						pathsForThisKey.addAll(subHashMap.get(key));
				}
			} else { //filePath is not a directory - need to add it to the hashMap
				Long key = FileUtils.sizeOf(filePath.toFile());
				ArrayList<Path> pathsForThisKey = hashMap.get(key);
				if (pathsForThisKey == null) {
					ArrayList<Path> filePathArray = new ArrayList<Path>();
					filePathArray.add(filePath);
					hashMap.put(key, filePathArray);
				} else {
					pathsForThisKey.add(filePath);
				}
			}
		}
		return hashMap;
	}

	public static void setupLogFile() {
		Calendar cal = Calendar.getInstance();
		String yearString = String.format("%d",cal.get(Calendar.YEAR));
		String monthString = String.format("%02d",cal.get(Calendar.MONTH)+1);
		String dateString = String.format("%02d",cal.get(Calendar.DAY_OF_MONTH));
		String hourString = String.format("%02d",cal.get(Calendar.HOUR));
		String minuteString = String.format("%02d",cal.get(Calendar.MINUTE));
		String timeString = yearString+"-"+monthString+"-"+dateString+"-"+hourString+"-"+minuteString+"DeDupeCopy3.log";
		String logFileName = timeString+"DeDupeCopy.log";
		logFilePath = outputRootDirectoryPath.resolve(logFileName);
		try {
			logFilePrintWriter = new PrintWriter(logFilePath.toFile());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	public static void closeLogFile() {
		logFilePrintWriter.flush();
		logFilePrintWriter.close();
	}
	
	public static void main(String[] args) {
		//		Input source directory and target directory
		setInputAndOutputDirectories();
		setupLogFile();
		logFilePrintWriterprintln("\nSource directory: "+inputRootDirectoryPath.toString());
		logFilePrintWriterprintln("\nTarget directory: "+outputRootDirectoryPath.toString());
		//		Copy files
		copiedFilesHashMap = getHashMapOfFiles(outputRootDirectoryPath);
		copyFiles(inputRootDirectoryPath);
		closeLogFile();
	}
}
