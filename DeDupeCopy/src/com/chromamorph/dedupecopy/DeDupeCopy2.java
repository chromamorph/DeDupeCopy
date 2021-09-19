package com.chromamorph.dedupecopy;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.TreeSet;

import javax.activation.MimetypesFileTypeMap;


public class DeDupeCopy2 {

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
	static ArrayList<String> imageFileSuffixesArrayList = null;
	
	static String lastFileCopied = null;


	static String inputRootDirectoryPath = "/Volumes/susanne-archive/TEST";
	static String outputRootDirectoryPath = "/Volumes/Seagate Backup Plus Drive/TEST";
	static TreeSet<ImageFile> allInputImages = null;

	public static boolean isImageFile(String filePath) {
		String suffix = filePath.substring(filePath.lastIndexOf(".")+1).toLowerCase();
		if (imageFileSuffixesArrayList.contains(suffix)) return true;
		File f = new File(filePath);
		String mimetype= new MimetypesFileTypeMap().getContentType(f);
//		System.out.println(filePath+": "+mimetype);
		String type = mimetype.split("/")[0];
		if (type.equals("image")) return true;
		return false;
	}

	public static TreeSet<ImageFile> findAllInputImages(String rootDirectoryPath) {
		TreeSet<ImageFile> inputImages = new TreeSet<ImageFile>();
		String[] fileNames = new File(rootDirectoryPath).list();
		for(String fileName : fileNames) {
			String filePath = rootDirectoryPath + "/"+fileName;
			if (isImageFile(filePath)) {
				inputImages.add(new ImageFile(filePath));
			} else if (!fileName.equals(".") && !fileName.equals("..") && new File(filePath).isDirectory())
				inputImages.addAll(findAllInputImages(filePath));
		}
		System.out.println(rootDirectoryPath+": "+inputImages.size()+" image files found"); System.out.flush();
		return inputImages;
	}

	/**
	 * Run through allInputImages and
	 * 1. copy each file at the beginning of a content segment
	 *    to the target folder for its lastmodified date.
	 *    If there is already a file with its name in the target folder,
	 *    then append a suffix "00k" to this file's name
	 * 2. create symbolic links in the link tree to the file copied in 1
	 *    for every file in the content segment (including the file copied
	 *    in 1).
	 */
	public static void copyFilesAndLinks() {
		ImageFile prev = null;
		for(ImageFile file : allInputImages) {
			//			If this is the first file, then copy it and its link, then continue
			if (prev == null) {
				copyFileAndLink(file);
				prev = file;
				continue;
			}
			//			prev is not null
			//			If this file is a different size from the previous file,
			//			then copy it and its link, then continue
			if (file.getSize() != prev.getSize()) {
				copyFileAndLink(file);
				prev = file;
				continue;
			}
			//			prev is not null and this file is the same size as the previous file
			//			If this file has different content from the previous file,
			//			then copy the file and its link
			if (file.compareContent(prev) != 0) {
				copyFileAndLink(file);
				prev = file;
				continue;
			}
			//			prev is not null, prev and file have the same content				
			//			Just copy link
			copyLink(file);
			prev = file;
		}
	}

	/**
	 * Copy file to the target folder for its lastmodified date.
	 * If there is already a file with its name in the target folder,
	 * then append a suffix "00k" to this file's name 
	 * Create a symbolic link in the link tree for this file.
	 * @param file
	 */
	public static void copyFileAndLink(ImageFile file) {
		copyFile(file);
		copyLink(file);
	}
	
	/**
	 * Copy file to the target folder for its lastmodified date.
	 * If there is already a file with its name in the target folder,
	 * then append a suffix "00k" to this file's name 
	 * Create a symbolic link in the link tree for this file.
	 * @param file
	 */
	public static void copyFile(ImageFile file) {
		int year = file.getYear();
		String monthString = String.format("%02d", file.getMonth());
		String name = file.getNameWithoutSuffix();
		String suffix = file.getSuffix();
		String outputFilePath = outputRootDirectoryPath + "/files/" + year+"/"+monthString+"/"+name+"."+suffix;
		int i = 1;
		while (new File(outputFilePath).exists()) {
			outputFilePath = outputRootDirectoryPath + "/files/" + year+"/"+monthString+"/"+name+String.format("_%03d",i)+"."+suffix;
			i++;
		}
		File outputFile = new File(outputFilePath);
		outputFile.getParentFile().mkdirs();

		Path inputPath = FileSystems.getDefault().getPath(file.getPath());
		Path outputPath = FileSystems.getDefault().getPath(outputFilePath);
		try {
			Files.copy(inputPath,outputPath,StandardCopyOption.COPY_ATTRIBUTES);
			lastFileCopied = outputFilePath;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create link to lastFileCopied in link tree at path of file.
	 * @param args
	 */
	public static void copyLink(ImageFile file) {
		String filePathFromInputDirectoryPath = file.getPath().substring(inputRootDirectoryPath.length());
		String linkTreePathString = outputRootDirectoryPath+"/links"+filePathFromInputDirectoryPath;
		Path linkTreePath = FileSystems.getDefault().getPath(linkTreePathString);
		Path lastFileCopiedPath = FileSystems.getDefault().getPath(lastFileCopied);
		try {
			new File(linkTreePathString).getParentFile().mkdirs();
			Files.createSymbolicLink(linkTreePath,lastFileCopiedPath);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		//		Make ArrayList of image file suffixes 
		imageFileSuffixesArrayList = new ArrayList<String>();
		for(String imageFileSuffix : imageFileSuffixes) 
			imageFileSuffixesArrayList.add(imageFileSuffix);

		/* Get list of all image files in inputRootDirectoryPath
		 * This list is sorted by
		 * 		size
		 * 		content
		 * 		lastmodified
		 * 		original pathname
		 */
		allInputImages = findAllInputImages(inputRootDirectoryPath);
//		for(ImageFile image : allInputImages)
//			System.out.println(image);

		/*
		 * Run through allInputImages and
		 * 1. copy each file at the beginning of a content segment
		 *    to the target folder for its lastmodified date.
		 *    If there is already a file with its name in the target folder,
		 *    then append a suffix "00k" to this file's name
		 * 2. create symbolic links in the link tree to the file copied in 1
		 *    for every file in the content segment (including the file copied
		 *    in 1).
		 */
		copyFilesAndLinks();
	}
}
