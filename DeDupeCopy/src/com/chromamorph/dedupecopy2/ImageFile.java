package com.chromamorph.dedupecopy2;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.Calendar;

import org.apache.commons.io.FileUtils;

class ImageFile implements Comparable<ImageFile>{

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
	
	private Path path;

	ImageFile(Path path) {
		this.path = path;
	}

	public Path getPath() {
		return path;
	}

	public Path getFileName() {
		return getPath().getFileName();
	}

	public long getSize() {
		try {
			return Files.size(getPath());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return 0l;
	}

	public FileTime getLastModifiedTime() {
		try {
			return Files.getLastModifiedTime(getPath());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public long getLastModifiedInMillis() {
		return getLastModifiedTime().toMillis();
	}

	public File getFile() {
		return getPath().toFile();
	}

	public int compareContent(ImageFile o) {
		long dlong = getSize() - o.getSize();
		if (dlong < 0l) return -1;
		if (dlong > 0l) return 1;
		//		Sizes are the same
		//		If files have identical content, return 0
		File thisFile = getFile();
		File otherFile = o.getFile();

		try {
			if (FileUtils.contentEquals(thisFile, otherFile))
				return 0;
			//	Files are same length and have different content			
			Path thisFilePath = getPath();
			Path otherFilePath = o.getPath();
			byte[] thisFileArray = Files.readAllBytes(thisFilePath);
			byte[] otherFileArray = Files.readAllBytes(otherFilePath);
			for(int i = 0; i < thisFileArray.length; i++) {
				int d = thisFileArray[i] - otherFileArray[i];
				if (d != 0) return d;
			}
			System.out.println("Shouldn't get here!");
			return 0;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return 1;
	}

	public int getYear() {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(getLastModifiedInMillis());
		return cal.get(Calendar.YEAR);
	}
	
	public int getMonth() {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(getLastModifiedInMillis());
		return cal.get(Calendar.MONTH)+1;
	}
	
	public int getDayOfMonth() {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(getLastModifiedInMillis());
		return cal.get(Calendar.DAY_OF_MONTH);
	}
	
	public String getFileNameWithoutSuffix() {
		String fileName = getFileName().toString();
		return fileName.substring(0, fileName.lastIndexOf("."));
	}
	
	public String getSuffixString() {
		String fileName = getFileName().toString();
		return fileName.substring(fileName.lastIndexOf(".")+1);
		
	}
	
	@Override
	public int compareTo(ImageFile o) {
		//		Compare by size
		//		System.out.print("Sizes: "+getSize()+":"+o.getSize());
		long d = getSize() - o.getSize();
		if (d > 0l) return 1;
		if (d < 0l) return -1;
		//		System.out.println(" Sizes are the same");
		//		Compare by content
		int dInt = compareContent(o);
		if (dInt != 0) return dInt;
		//		System.out.println("Content is the same");
		//		Compare by lastModified
		//		If lastModified date is too early, then place at end of this content segment
		//		System.out.println("LastModified: "+getLastModifiedString()+":"+o.getLastModifiedString());
		d = getLastModifiedInMillis() - o.getLastModifiedInMillis();
		if (d != 0 && getYear() < 1980) return 1;
		if (d != 0 && o.getYear() < 1980) return -1;
		if (d > 0l) return 1;
		if (d < 0l) return -1;
		//		System.out.println(" Lastmodified is the same");
		//		Compare by pathname
		return getPath().toString().compareTo(o.getPath().toString());				
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof ImageFile)) return false;
		return compareTo((ImageFile)obj)==0;
	}

	public String toString() {
		return String.format("%15d %s %s", getSize(), getLastModifiedTime(), getPath());
	}

	public static boolean isImageFile(Path filePath) {
		String suffix = filePath.toString().substring(filePath.toString().lastIndexOf(".")+1).toLowerCase();
		for(String imgsuf : imageFileSuffixes)
			if (imgsuf.equals(suffix))
				return true;
		return false;
	}


	
	public static void main(String[] args) {
		String root = "/Users/dave/Documents/Work/Research/2015-06-17-workspace/DeDupeCopy/inputTestData/formats";
		String[] fileList = new File("/Users/dave/Documents/Work/Research/2015-06-17-workspace/DeDupeCopy/inputTestData/formats").list();
		for(String name : fileList) {
			Path namePath = new File(root+"/"+name).toPath();
			System.out.println(name+": "+isImageFile(namePath));
		}
		
//		ImageFile otherFile = new ImageFile("/Users/dave/Documents/Work/Research/2015-06-17-workspace/DeDupeCopy/inputTestData/testroot/folder1/aliasveryoldlargefile1a.jpg");
//		ImageFile thisFile = new ImageFile("/Users/dave/Documents/Work/Research/2015-06-17-workspace/DeDupeCopy/inputTestData/testroot/folder1/aliasnewlargefile1b.jpg");
//		System.out.println(thisFile.compareContent(otherFile));
	}
}


