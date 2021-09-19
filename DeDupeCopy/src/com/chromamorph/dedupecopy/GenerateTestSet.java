package com.chromamorph.dedupecopy;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.io.IOUtils;

public class GenerateTestSet {

	static String rootFolderPath = "inputTestData/testroot";
	static long veryoldDate = 0l;
	static long oldDate = 0l;
	static long newDate = 0l;

	public static void main(String[] args) {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR,1980); cal.set(Calendar.MONTH,2); cal.set(Calendar.DAY_OF_MONTH,5);
		veryoldDate = cal.getTimeInMillis();
		cal.set(Calendar.YEAR,1998); cal.set(Calendar.MONTH,3); cal.set(Calendar.DAY_OF_MONTH,6);
		oldDate = cal.getTimeInMillis();
		cal.set(Calendar.YEAR,2015); cal.set(Calendar.MONTH,4); cal.set(Calendar.DAY_OF_MONTH,7);
		newDate = cal.getTimeInMillis();
		try {
			File smallFile1 = new File("smallFile1");
			PrintWriter pw = new PrintWriter(smallFile1);
			for(int i = 1; i < 100; i++) pw.println("a");
			pw.close();

			File smallFile2 = new File("smallFile2");
			pw = new PrintWriter(smallFile2);
			for(int i = 1; i < 100; i++) pw.println("b");
			pw.close();

			File largeFile1 = new File("largeFile1");
			pw = new PrintWriter(largeFile1);
			for(int i = 1; i < 10000; i++) pw.println("a");
			pw.close();

			File largeFile2 = new File("largeFile2");
			pw = new PrintWriter(largeFile2);
			for(int i = 1; i < 10000; i++) pw.println("b");
			pw.close();

			for(String age : new String[]{"veryold", "old", "new"})  {
				for(String size : new String[]{"small","large"}) {
					for(String content : new String[]{"file1", "file2"}) {
						for(String targetFolder : new String[]{"a","b"}) {
							for(String inputFileName : new String[]{"alias",""}) {
								for(String inputFolderPath : new String[]{"folder1","folder1/folder11","folder2"}) {
									String filePath = rootFolderPath+"/"+inputFolderPath+"/"+inputFileName+age+size+content+targetFolder+".jpg";
									File file = new File(filePath);
									file.getParentFile().mkdirs();
									String dataFilePath = (size.equals("small")?(content.equals("file1")?"smallFile1":"smallFile2"):(content.equals("file1")?"largeFile1":"largeFile2"));
									try {
										InputStream input = new FileInputStream(dataFilePath);
										OutputStream output = new FileOutputStream(filePath);
										IOUtils.copy(input, output);
										input.close();
										output.close();
									} catch (IOException e) {
										e.printStackTrace();
									}
									
									if (age.equals("veryold"))
										file.setLastModified(veryoldDate + (targetFolder.equals("a")?1000:0));
									else if (age.equals("old"))
										file.setLastModified(oldDate + (targetFolder.equals("a")?1000:0));
									else if (age.equals("new"))
										file.setLastModified(newDate + (targetFolder.equals("a")?1000:0));
								}
							}
						}
					}
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
}
