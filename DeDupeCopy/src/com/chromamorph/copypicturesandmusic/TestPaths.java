package com.chromamorph.copypicturesandmusic;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import com.chromamorph.dedupecopy.ImageFile;

import org.apache.commons.io.FileUtils;

public class TestPaths {
	public static void main(String[] args) {
//		Path path1 = Paths.get("/Volumes/SEAGATE4TB/Susanne white iBook G4/Music/iTunes/iTunes Music/Music/Bruce Willis/The Universal Masters Collection_ Classic Bruce Willis/01 Under the Boardwalk.m4p");
//		Path path2 = Paths.get("/Volumes/SEAGATE4TB/Susanne white iBook G4/Music/iTunes/iTunes Music/Music/Compilations/Greatest/02 The Reflex.m4a");
//		Path ROOT_OUTPUT_FOLDER = Paths.get("/Volumes/SEAGATE4TB");
//		Path[] paths = {path1, path2};
//		Path MUSIC_FOLDER = ROOT_OUTPUT_FOLDER.resolve("Music");
//
//		for(Path path : paths) {
//			Path parentAndFileName = path.subpath(path.getNameCount() - 3, path.getNameCount());
//				System.out.println(MUSIC_FOLDER.resolve(parentAndFileName));
//		}
//
//		Path filePath = Paths.get("A/A/B/C/D");
//		Path outputFilePath = null;
//		if (filePath.getNameCount() >= 3) {
//			Path pathFromGrandParent = filePath.subpath(filePath.getNameCount() - 3, filePath.getNameCount());
//			outputFilePath = MUSIC_FOLDER.resolve(pathFromGrandParent);
//		} else if (filePath.getNameCount() == 2) {
//			Path pathFromGrandParent = filePath.subpath(filePath.getNameCount() - 2, filePath.getNameCount());
//			outputFilePath = MUSIC_FOLDER.resolve("other").resolve(pathFromGrandParent);
//		} else if (filePath.getNameCount() == 1) {
//			Path pathFromGrandParent = filePath.subpath(filePath.getNameCount() - 1, filePath.getNameCount());
//			outputFilePath = MUSIC_FOLDER.resolve("other").resolve("other").resolve(pathFromGrandParent);
//		}
//		
//		System.out.println(outputFilePath);
//
//		
		Path file1 = Paths.get("/Volumes/SEAGATE4TB/Music/Eurythmics/Revenge/05 The Miracle Of Love_001.m4a");
		Path file2 = Paths.get("/Volumes/SEAGATE4TB/Music/Eurythmics/Revenge/05 The Miracle Of Love_002.m4a");
		Path file3 = Paths.get("/Volumes/SEAGATE4TB/Music/Eurythmics/Revenge/05 The Miracle Of Love.m4a");
		
		try {
			System.out.println(FileUtils.contentEquals(file1.toFile(), file2.toFile()));
			System.out.println(FileUtils.contentEquals(file1.toFile(), file3.toFile()));
			System.out.println(FileUtils.contentEquals(file2.toFile(), file3.toFile()));
			ImageFile if1 = new ImageFile(file1.toString());
			ImageFile if2 = new ImageFile(file2.toString());
			ImageFile if3 = new ImageFile(file3.toString());
			System.out.println(if1.compareContent(if2));
			System.out.println(if1.compareContent(if3));
			System.out.println(if2.compareContent(if3));

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
}
