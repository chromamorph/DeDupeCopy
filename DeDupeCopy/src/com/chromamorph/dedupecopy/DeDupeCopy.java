package com.chromamorph.dedupecopy;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Date;
import java.util.TreeSet;

import javax.activation.MimetypesFileTypeMap;


/**
 * Takes as input a file system (i.e., a directory tree)
 * that may contain more than one copy of each file.
 * 
 * Generates an output file system in which there is only 
 * one copy of each different file.
 * @author dave
 *
 */
public class DeDupeCopy {

	static String[] imageFileSuffixes = {"jpg","png","tif","gif","raw"};
	static ArrayList<String> imageFileSuffixesArrayList = null;

	static String inputRootDirectoryPath = "/Volumes/susanne-archive";
	static String outputRootDirectoryPath = "/Volumes/Seagate Backup Plus Drive/Pictures";
	static TreeSet<ImageFile> allInputImages = null;
	static int indent = 0;

	public static TreeSet<ImageFile> findAllInputImages(String rootDirectoryPath) {
		indent++;
		System.out.println();
		for(int i = 0; i < indent; i++) System.out.print("  ");
		System.out.print(rootDirectoryPath);
		TreeSet<ImageFile> inputImages = new TreeSet<ImageFile>();
		String[] fileNames = new File(rootDirectoryPath).list();
		for(String fileName : fileNames) {
			System.out.print(".");
			String filePath = rootDirectoryPath + "/"+fileName;
			if (isImageFile(filePath)) {
				inputImages.add(new ImageFile(filePath));
			} else if (!fileName.equals(".") && !fileName.equals("..") && new File(filePath).isDirectory())
				inputImages.addAll(findAllInputImages(filePath));
		}
		indent--;
		return inputImages;
	}

	public static boolean isImageFile(String filePath) {
		String fileSuffix = filePath.toLowerCase().substring(filePath.lastIndexOf(".")+1);
		if (imageFileSuffixesArrayList.contains(fileSuffix)) return true;
		File f = new File(filePath);
		String mimetype= new MimetypesFileTypeMap().getContentType(f);
		String type = mimetype.split("/")[0];
		if (type.equals("image")) return true;
		return false;
	}

	@SuppressWarnings("deprecation")
	public static void copyFilesToOutputDirectory() {
		try {
			for (ImageFile imageFile : allInputImages) {
				Date date = new Date(imageFile.lastModified);
				int year = date.getYear()+1900;
				int month = date.getMonth();
				String monthString = String.format("%02d",month+1);

				int nameEnd = imageFile.name.lastIndexOf(".");
				if (nameEnd < 0) nameEnd = imageFile.name.length();
				String name = imageFile.name.substring(0,nameEnd);
				String suffix = "";
				if (nameEnd < imageFile.name.length())
					suffix = imageFile.name.substring(nameEnd).toLowerCase();

				String outputFilePath = outputRootDirectoryPath + "/" + year+"/"+monthString+"/"+name+suffix;
				int i = 1;
				while (new File(outputFilePath).exists()) {
					outputFilePath = outputRootDirectoryPath + "/" + year+"/"+monthString+"/"+name+String.format("_%03d",i)+suffix;
					i++;
				}
				File outputFile = new File(outputFilePath);
				outputFile.getParentFile().mkdirs();

				Path inputPath = FileSystems.getDefault().getPath(imageFile.path);
				Path outputPath = FileSystems.getDefault().getPath(outputFilePath);
				Files.copy(inputPath,outputPath,StandardCopyOption.COPY_ATTRIBUTES);

				//				FileInputStream input = new FileInputStream(imageFile.filePath);
//				FileOutputStream output = new FileOutputStream(outputFilePath);
//				IOUtils.copy(input, output);
//				input.close();
//				output.close();

			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void copyFile(ImageFile imageFile) {

	}

	public static void main(String[] args) {
		imageFileSuffixesArrayList = new ArrayList<String>();
		for(String imageFileSuffix : imageFileSuffixes) 
			imageFileSuffixesArrayList.add(imageFileSuffix);

		allInputImages = findAllInputImages(inputRootDirectoryPath);
		//		for(ImageFile image : allInputImages)
		//			System.out.println(image);

		//		Copy files without duplicates to output directory
		copyFilesToOutputDirectory();
	}
}
