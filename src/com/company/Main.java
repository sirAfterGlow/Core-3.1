package com.company;

import java.io.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class Main {

    public static StringBuilder log = new StringBuilder();

    public static void install() {
        List<File> dirs = new ArrayList<>();
        dirs.add(new File("Games//src"));
        dirs.add(new File("Games//res"));
        dirs.add(new File("Games//savegames"));
        dirs.add(new File("Games//temp"));
        dirs.add(new File("Games//src//main"));
        dirs.add(new File("Games//src//test"));
        dirs.add(new File("Games//res//drawables"));
        dirs.add(new File("Games//res//vectors"));
        dirs.add(new File("Games//res//icons"));
        createDirs(dirs);

        List<File> files = new ArrayList<>();
        files.add(new File("Games//src//main", "Main.java"));
        files.add(new File("Games//src//main", "Utils.java"));
        files.add(new File("Games//temp", "temp.txt"));
        createFiles(files);
    }

    public static void createDirs(List<File> dirs) {
        for (File dir : dirs) {
            if (dir.mkdir()) {
                log.append("[SUCCESS] ["+ new Date()  + "] Создана папка " + dir.getName() + "\n");
            } else {
                log.append("[FAIL] [" + new Date()  + "] Неудачная попытка создать папку " + dir.getName() + "\n");
            }
        }
    }
    public static void createFiles(List<File> files) {
        for (File file : files) {
            try {
                if (file.createNewFile()) {
                    log.append("[SUCCESS] [" + new Date() + "] Создан файл " + file.getName() + "\n");
                } else {
                    log.append("[FAIL] [" + new Date() + "] Неудачная попытка создать файл " + file.getName() + "\n");
                }
            } catch (IOException exception) {
                System.out.println(exception.getMessage());
            }
        }
    }

    public static void writeLog() {
        try (FileWriter fw = new FileWriter("Games//temp//temp.txt", true)) {
            fw.write(log.toString());
            fw.flush();
        } catch (IOException exception) {
            System.out.println(exception.getMessage());
        }
    }

    public static void saveGame(GameProgress save, String path) {
        try (FileOutputStream fos = new FileOutputStream(path);
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(save);

            log.append("[SUCCESS] [" + new Date() + "] Прогресс сохранен в файл " + path.substring(18) + "\n");
        } catch (IOException exception) {
            System.out.println(exception.getMessage());
            log.append("[FAIL] [" + new Date() + "] Неудачная попытка сохранить прогресс " + path.substring(18) + "\n");
        }
    }

    public static void zipFiles(String file, List<String> saves) {
        try (ZipOutputStream zout = new ZipOutputStream(new FileOutputStream(file))) {
            for (String save : saves) {
                try (FileInputStream fis = new FileInputStream(save)) {
                    ZipEntry entry = new ZipEntry(save.substring(18));
                    zout.putNextEntry(entry);
                    byte[] buffer = new byte[fis.available()];
                    fis.read(buffer);
                    zout.write(buffer);
                    zout.closeEntry();
                } catch (IOException exception) {
                    exception.getMessage();
                }
                File savedFile = new File(save);
                if (savedFile.delete()) {
                    log.append("[SUCCESS] [" + new Date() + "] Файл сериализации удален " + save.substring(18) + "\n");
                } else {
                    log.append("[FAIL] [" + new Date() + "] Неудачная пыпытка удалить файл сериализации " + save.substring(18) + "\n");
                }
            }
            log.append("[SUCCESS] [" + new Date() + "] Сохранения заархивированы успешно\n");
        }catch (Exception exception) {
            log.append("[FAIL] [" + new Date() + "] Неудачная попытка заархивировать сохранения\n");
            System.out.println(exception.getMessage());
        }
    }

    public static void openZip(String inputPath, String outputPath) {
        try (ZipInputStream zin = new ZipInputStream(new FileInputStream(inputPath))){
            ZipEntry entry;
            String name;
            while ((entry = zin.getNextEntry()) != null) {
                name = entry.getName();
                FileOutputStream fout = new FileOutputStream(outputPath + name);
                for (int c = zin.read(); c != -1; c = zin.read()) {
                    fout.write(c);
                }
                fout.flush();
                zin.closeEntry();
                fout.close();
            }
            log.append("[SUCCESS] [" + new Date() + "] Сохранения разархивированы успешно\n");
        } catch (Exception exception) {
            log.append("[FAIL] [" + new Date() + "] Неудачная попытка разархивировать сохранения\n");
            System.out.println(exception.getMessage());
        }
    }

    public static GameProgress openProgress(String path){
        GameProgress gameProgressResult = null;
        try (FileInputStream fis = new FileInputStream(path);
             ObjectInputStream ois = new ObjectInputStream(fis)){
            gameProgressResult = (GameProgress) ois.readObject();
            log.append("[SUCCESS] [" + new Date() + "] Сохранени десериализованно успешно\n");
        } catch (Exception exception) {
            log.append("[FAIL] [" + new Date() + "] Неудачная попытка десериализовать сохранение\n");
            System.out.println(exception.getMessage());
        }
        return gameProgressResult;
    }

    public static void main(String[] args) {

        install();

        GameProgress gp1 = new GameProgress(100, 2,1,13);
        GameProgress gp2 = new GameProgress(79, 4,2,1245);
        GameProgress gp3 = new GameProgress(45, 7,5,8124);

        List<String> savesPaths = new ArrayList<>();
        savesPaths.add("Games//savegames//save1.dat");
        savesPaths.add("Games//savegames//save2.dat");
        savesPaths.add("Games//savegames//save3.dat");

        saveGame(gp1, savesPaths.get(0));
        saveGame(gp2, savesPaths.get(1));
        saveGame(gp3, savesPaths.get(2));

        zipFiles("Games//savegames//saves.zip", savesPaths);

        openZip("Games//savegames//saves.zip", "Games//savegames//");

        GameProgress loadedSave = openProgress("Games//savegames//save3.dat");
        System.out.println(loadedSave.toString());

        writeLog();

    }
}
