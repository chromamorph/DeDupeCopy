package com.chromamorph.newdedupecopy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.TreeSet;

public class ReversePath implements Comparable<ReversePath> {

	private ArrayList<String> reverseFolderPath = new ArrayList<String>();
	private Path path;

	public ReversePath(String pathString) {
		path = Path.of(pathString);
		int c = path.getNameCount();
		for(int i = c-1; i >= 0; i--) {
			reverseFolderPath.add(path.getName(i).toString());
		}
	}
	
	public boolean exists() {
		return Files.exists(path);
	}
	
	public Long getLastModifiedTimeInMillis() throws IOException {
		if (exists())
			return Files.getLastModifiedTime(path).toMillis();
		return null;
	}
		
	public Long getCreationTimeInMillis() throws IOException {
		if (exists()) {
			BasicFileAttributes bfa = Files.readAttributes(path,BasicFileAttributes.class);
			return bfa.creationTime().toMillis();
		}
		return null;
	}
	
	public Long getContentCreatedTimeInMillis() {
		if (exists()) {
			ImageInputStream iis = ImageIO.
		}
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null) return false;
		if (!(obj instanceof ReversePath)) return false;
		return compareTo((ReversePath)obj)==0;
	}
	
	@Override
	public int compareTo(ReversePath o) {
		if (o == null) return 1;
		int leastLength = Math.min(getLength(), o.getLength());
		for(int i = 0; i < leastLength; i++) {
			int d = get(i).compareTo(o.get(i));
			if (d != 0) return d;
		}
		return getLength() - o.getLength();
	}
	
	public int getLength() {
		return getReverseFolderPath().size();
	}
	
	public ArrayList<String> getReverseFolderPath() {
		return reverseFolderPath;
	}
	
	public String get(int i) {
		return getReverseFolderPath().get(i);
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(get(0));
		for(int i = 1; i < getLength(); i++) {
			sb.append(" > "+get(i));
		}
		return sb.toString();
	}
	
	public static void main(String[] args) {
		ReversePath f = new ReversePath("/Users/susanne/Repos/mixed-reality-technologies/lupins.jpg");
		System.out.println(f);
		
		ReversePath f2 = new ReversePath("/Users/susanne/Archive/lupins.jpg");
		TreeSet<ReversePath> ts = new TreeSet<ReversePath>();
		ts.add(f);
		ts.add(f2);
		for(ReversePath g : ts) System.out.println(g);
		
		String chimpAndBaby = "/Users/susanne/Archive/2020-11-26-Corsair/SEAGATE4TB/Susanne Dell Inspiron RedWhite/Users/Susanne/Pictures/1 Wildlife/Chimp and baby.jpg";
		String stjernestudiet = "/Users/susanne/Archive/2020-11-26-Corsair/SEAGATE4TB/Susanne white iBook G4/Pictures/04.08.2007/stjernestudiet_1/stjernestudiet_1 001.jpg";
		ReversePath cab = new ReversePath(stjernestudiet);
		System.out.println(cab);
		System.out.println(cab.exists());
		try {
			System.out.println(cab.getLastModifiedTimeInMillis());
			System.out.println(cab.getContentCreatedTimeInMillis());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
