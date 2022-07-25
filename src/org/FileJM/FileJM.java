package org.FileJM;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import java.util.zip.ZipInputStream;
import java.util.Scanner;
import java.nio.file.StandardCopyOption;
import java.nio.file.Files;

public class FileJM extends File {
    private String path;
    public int bufferSize = 2048;
    public String stackTrace = "";

    // main
    public FileJM(String path){
        super(path);
        this.path = path;
    }

    // path
    public String[] toPathArray(){
        return getAbsolutePath().split("/");
    }

    // list - find
    public String[] fileList(int recursion){
        File[] files = listFiles();
        ArrayList<String> pathList = new ArrayList<String>();

        assert files != null;
        for (File file : files) {
            if (isFile()) {
                pathList.add(file.getAbsolutePath());

            } else if (recursion > 0){
                String[] subDirectory = new FileJM(file.getAbsolutePath()).fileList(recursion - 1);
                pathList.addAll(Arrays.asList(subDirectory));
            }
        }
        return pathList.toArray(new String[0]);
    }
    private String[] find(Pattern pattern, int recursion){
        String[] files = fileList(recursion);
        ArrayList<String> pathList = new ArrayList<String>();

        assert files != null;
        FileJM file;
        for (String path : files) {
            file = new FileJM(path);
            if (!file.isFile()){
                continue;
            }
            Matcher matcher = pattern.matcher(file.getName());
            if (matcher.find()){
                pathList.add(path);
            }
        }
        return pathList.toArray(new String[0]);
    }

    // make - delete
    public boolean makeFile(){
        try{
            if (!exists()){
                return createNewFile();
            }
            return true;
        }catch(Exception e){
            stackTrace = Arrays.toString(e.getStackTrace());
            return false;
        }
    }
    public boolean makeFolder(){
        try{
            if(!exists()) {
                mkdir();
            }
            return true;
        }catch(Exception e){
            stackTrace = Arrays.toString(e.getStackTrace());
            return false;
        }

    }
    public boolean remove(){
        if (exists()){
            return delete();
        } else {
            return false;
        }
    }

    // move
    public boolean copy(String newDest){
        try{
            File dest = new File(newDest);
            Files.copy(toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return true;
        }catch(Exception e){
            stackTrace = Arrays.toString(e.getStackTrace());
            return false;
        }
    }
    public boolean rename(String newName){
        try{
            return renameTo(new File(newName));
        }catch(Exception e){
            stackTrace = Arrays.toString(e.getStackTrace());
            return false;
        }
    }
    public boolean move(String newDir){
        try{
            File dest = new File(newDir);
            Files.copy(toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
            remove();
            return true;
        }catch(Exception e){
            stackTrace = Arrays.toString(e.getStackTrace());
            return false;
        }
    }

    // read - write
    public boolean write(String data){
        try{
            FileWriter writer = new FileWriter(path);
            writer.write(data);
            writer.close();
            return true;
        }catch(Exception e){
            stackTrace = Arrays.toString(e.getStackTrace());
            return false;
        }
    }
    public void append(String data){
        try{
            String previous = readAll();
            FileWriter writer = new FileWriter(path);
            writer.write(previous + data);
            writer.close();
        }catch(Exception e){
            stackTrace = Arrays.toString(e.getStackTrace());
        }
    }
    public String[] readLine(){
        try{
            String text = readAll();
            return text.split("\n");
        }catch(Exception e){
            return null;
        }
    }
    public String readAll(){
        try{
            Scanner reader = new Scanner(this);
            String text = "";
            while(reader.hasNextLine()){
                text += "\n" + reader.nextLine();
            }
            reader.close();
            return text;
        }catch(Exception e){
            stackTrace = Arrays.toString(e.getStackTrace());
            return null;
        }
    }

    // zip
    private boolean zip(String dest, int recursion){
        String[] files = fileList(recursion);
        String folder = getName();

        File newFile;
        byte[] buffer = new byte[bufferSize];
        try{
            ZipOutputStream zos = new ZipOutputStream(new FileOutputStream((new FileJM(dest)).getAbsolutePath()));

            for(String f : files){ // zip each file
                newFile = new File(f);
                ZipEntry ze = new ZipEntry(newFile.getAbsolutePath().substring(getAbsolutePath().indexOf(folder) + folder.length() + 1));
                zos.putNextEntry(ze);
                FileInputStream fis = new FileInputStream(newFile);
                int len;
                while((len = fis.read(buffer)) > 0){
                    zos.write(buffer,0,len);
                }

                // close all stuffs
                fis.close();
                zos.closeEntry();
            }
            zos.close();
            return true;
        }catch(Exception e){
            stackTrace = Arrays.toString(e.getStackTrace());
            return false;
        }
    }
    private boolean unZip(String dest) {
        FileJM destDir = new FileJM(dest);
        if (!destDir.exists()) destDir.mkdirs(); // make output folder

        FileInputStream fis;
        byte[] buffer = new byte[bufferSize];
        try {
            fis = new FileInputStream(getAbsolutePath());
            ZipInputStream zis = new ZipInputStream(fis);
            ZipEntry ze = zis.getNextEntry();

            while (ze != null) { // for each entry in zip file
                String fileName = ze.getName();
                FileJM newFile = new FileJM(destDir.getAbsolutePath() + FileJM.separator + fileName);

                if (ze.isDirectory()){ // make directories
                    newFile.mkdirs();
                } else {
                    FileOutputStream fos = new FileOutputStream(newFile); // write file (extract)
                    int len;
                    while ((len = zis.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }
                    fos.close();
                }

                // close all stuffs
                zis.closeEntry();
                ze = zis.getNextEntry();
            }

            zis.closeEntry();
            zis.close();
            fis.close();
            return true;
        } catch (IOException e) {
            stackTrace = Arrays.toString(e.getStackTrace());
            return false;
        }
    }
}