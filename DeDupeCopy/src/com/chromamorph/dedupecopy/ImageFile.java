package com.chromamorph.dedupecopy;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Date;

import org.apache.commons.io.FileUtils;

public class ImageFile implements Comparable<ImageFile>{
	private String path, name, suffix, nameWithoutSuffix;
	private long size, lastModified;

	public ImageFile(String path) {
		this.path = path;
		File file = new File(path);
		this.size = file.length();
		this.lastModified = file.lastModified();
		this.name = path.substring(path.lastIndexOf("/")+1);
		this.suffix = path.substring(path.lastIndexOf(".")+1);
		this.nameWithoutSuffix = path.substring(path.lastIndexOf("/")+1,path.lastIndexOf("."));
	}

	public String getPath() {
		return path;
	}

	public String getName() {
		return name;
	}

	public String getSuffix() {
		return suffix;
	}

	public String getNameWithoutSuffix() {
		return nameWithoutSuffix;
	}

	public long getSize() {
		return size;
	}

	public long getLastModified() {
		return lastModified;
	}

	@SuppressWarnings("deprecation")
	public int getYear() {
		return new Date(lastModified).getYear()+1900;
	}

	@SuppressWarnings("deprecation")
	public int getMonth() {
		return new Date(lastModified).getMonth()+1;
	}

	@SuppressWarnings("deprecation")
	public int getDate() {
		return new Date(lastModified).getDate();
	}

	@SuppressWarnings("deprecation")
	public int getHour() {
		return new Date(lastModified).getHours();
	}

	@SuppressWarnings("deprecation")
	public int getMinute() {
		return new Date(lastModified).getMinutes();
	}

	@SuppressWarnings("deprecation")
	public int getSecond() {
		return new Date(lastModified).getSeconds();
	}

	public String getLastModifiedString() {
		return getYear()+"-"+
				String.format("%02d", getMonth())+"-"+
				String.format("%02d", getDate())+" "+
				String.format("%02d", getHour())+":"+
				String.format("%02d", getMinute())+":"+
				String.format("%02d", getSecond());
	}

	public int compareContent(ImageFile o) {
		long dlong = getSize() - o.getSize();
		if (dlong < 0l) return -1;
		if (dlong > 0l) return 1;
//		Sizes are the same
//		If files have identical content, return 0
		File thisFile = new File(getPath());
		File otherFile = new File(o.getPath());

		try {
			if (FileUtils.contentEquals(thisFile, otherFile))
				return 0;
			//	Files are same length and have different content			
			Path thisFilePath = FileSystems.getDefault().getPath(getPath());
			Path otherFilePath = FileSystems.getDefault().getPath(o.getPath());
			byte[] thisFileArray = Files.readAllBytes(thisFilePath);
			byte[] otherFileArray = Files.readAllBytes(otherFilePath);
			for(int i = 0; i < thisFileArray.length; i++) {
				int d = thisFileArray[i] - otherFileArray[i];
				if (d != 0) return d;
			}
			System.out.println("Shouldn't get here!");
			return 0;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 1;
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
		d = getLastModified() - o.getLastModified();
		if (d != 0 && getYear() < 1980) return 1;
		if (d != 0 && o.getYear() < 1980) return -1;
		if (d > 0l) return 1;
		if (d < 0l) return -1;
//		System.out.println(" Lastmodified is the same");
		//		Compare by pathname
		return getPath().compareTo(o.getPath());				
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof ImageFile)) return false;
		return compareTo((ImageFile)obj)==0;
	}

	public String toString() {
		return String.format("%15d %s %s", getSize(), getLastModifiedString(), getPath());
	}

	public static void main(String[] args) {
		ImageFile otherFile = new ImageFile("/Users/dave/Documents/Work/Research/2015-06-17-workspace/DeDupeCopy/inputTestData/testroot/folder1/aliasveryoldlargefile1a.jpg");
		ImageFile thisFile = new ImageFile("/Users/dave/Documents/Work/Research/2015-06-17-workspace/DeDupeCopy/inputTestData/testroot/folder1/aliasnewlargefile1b.jpg");
		System.out.println(thisFile.compareContent(otherFile));
	}
}


