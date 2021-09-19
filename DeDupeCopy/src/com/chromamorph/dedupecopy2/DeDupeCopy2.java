package com.chromamorph.dedupecopy2;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.TreeSet;

import javax.swing.JFileChooser;


public class DeDupeCopy2 {

	
	static Path lastFileCopied = null;


	static Path inputRootDirectoryPath = null;
	static Path outputRootDirectoryPath = null;
	static TreeSet<ImageFile> allInputImages = null;

	public static TreeSet<ImageFile> findAllInputImages(Path rootDirectoryPath) {
		TreeSet<ImageFile> inputImages = new TreeSet<ImageFile>();
		String[] fileNames = rootDirectoryPath.toFile().list();
		for(String fileName : fileNames) {
			Path filePath = rootDirectoryPath.resolve(fileName);
			if (ImageFile.isImageFile(filePath)) {
				inputImages.add(new ImageFile(filePath));
			} else if (!fileName.equals(".") && !fileName.equals("..") && Files.isDirectory(filePath))
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
		String yearString = String.format("%d",file.getYear());
		String monthString = String.format("%02d", file.getMonth());
		Path name = file.getFileName();
		String nameString = file.getFileNameWithoutSuffix();
		String suffix = file.getSuffixString();
		Path outputFilePath = outputRootDirectoryPath.resolve("files").resolve(yearString).resolve(monthString).resolve(name);
		int i = 1;
		while (fileExists(outputFilePath)) {
			outputFilePath = outputFilePath.resolveSibling(nameString+String.format("%03d",i)+"."+suffix);
			i++;
		}
		File outputFile = outputFilePath.toFile();
		outputFile.getParentFile().mkdirs();

		try {
			Files.copy(file.getPath(),outputFilePath,StandardCopyOption.COPY_ATTRIBUTES);
			lastFileCopied = outputFilePath;
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("\nFile copied: "+file.getPath().toString()+"\n    > "+outputFilePath);System.out.flush();
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

	/**
	 * Create link to lastFileCopied in link tree at path of file.
	 * @param args
	 */
	public static void copyLink(ImageFile file) {
		Path filePathFromInputDirectoryPath = inputRootDirectoryPath.relativize(file.getPath());
		Path linkTreePath = outputRootDirectoryPath.resolve("links").resolve(filePathFromInputDirectoryPath);
		try {
			linkTreePath.toFile().getParentFile().mkdirs();
			Files.createSymbolicLink(linkTreePath,lastFileCopied);
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("\nLink created: "+linkTreePath+"\n    > " + lastFileCopied.toString());System.out.flush();
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
	
	public static void main(String[] args) {
		
		if  (args.length == 2) {
			inputRootDirectoryPath = Paths.get(args[0]);
			outputRootDirectoryPath = Paths.get(args[1]);
		} else 
			setInputAndOutputDirectories();
		
		/* Get list of all image files in inputRootDirectoryPath
		 * This list is sorted by
		 * 		size
		 * 		content
		 * 		lastmodified
		 * 		original pathname
		 */
		System.out.println("\nSearching for files in "+inputRootDirectoryPath);
		allInputImages = findAllInputImages(inputRootDirectoryPath);
//		for(ImageFile image : allInputImages)
//			System.out.println(image);

		System.out.println("\nAll files found. Now copying files and links...");
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
