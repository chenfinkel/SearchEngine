package sample;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.decimal4j.util.DoubleRounder;
import sun.awt.Mutex;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.concurrent.ConcurrentHashMap;

/**
 * a thread for merging temporary index files
 */
public class mergeThread extends Thread {

    /**
     * the type of file to merge
     */
    private String file;

    /**
     * the location of the merged files
     */
    private String postPath;

    /**
     * constructor
     */
    public mergeThread(String file, String post, Indexer index) {
        this.file = file;
        postPath = post;
    }

    /**
     * override
     * this method merges the temporary files of the indexer
     */
    public void run() {
        try {
            int index2 = 0;
            File folders = new File("C:\\TempFiles\\" + file);
            File[] files = folders.listFiles();
            int size = files.length;
            if (size == 1) {
                try {
                    FileUtils.copyFile(files[0], new File(postPath + "\\" + file + ".txt"));
                    Files.deleteIfExists(files[0].toPath());
                }catch (Exception e) { e.printStackTrace(); }
            }
            while (size > 1) {
                int i;
                for (i = 0; i < size - 1; i = i + 2) {
                    index2++;
                    File f1 = files[i];
                    File f2 = files[i + 1];
                    if (size == 2) {
                            mergeFiles(f1, f2, index2, true, file);
                        break;
                    } else
                        mergeFiles(f1, f2, index2, false, file);
                }
                files = folders.listFiles();
                size = files.length;
            }
            if(file.equals("languages"))
                saveLanguages();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }
    }

    //saves languages to main memory
    private void saveLanguages() throws Exception {
        LinkedHashMap<String, String> list = new LinkedHashMap<>();
        FileReader fr = new FileReader(postPath + "\\languages.txt");
        BufferedReader br = new BufferedReader(fr);
        String line = br.readLine();
        while (line != null) {
            list.put(line, line);
            line = br.readLine();
        }
        SearchEngine.languages.putAll(list);
        fr.close();
    }

    //merge sort for merging files
    private void mergeFiles(File left, File right, int TmpIndex, boolean flag, String dir) {
        try {
            FileWriter fw;
            if (flag)
                fw = new FileWriter(postPath + "\\" + dir + ".txt");
            else
                fw = new FileWriter("C:\\TempFiles\\" + dir + "\\tmp" + TmpIndex + ".txt");
            BufferedWriter bw = new BufferedWriter(fw);
            FileReader frLeft = new FileReader(left.getPath());
            BufferedReader brLeft = new BufferedReader(frLeft);
            FileReader frRight = new FileReader(right.getPath());
            BufferedReader brRight = new BufferedReader(frRight);
            String leftLine = brLeft.readLine();
            String rightLine = brRight.readLine();
            while (leftLine != null && rightLine != null) {
                if (leftLine.equals("") || rightLine.equals(""))
                    continue;
                String leftToken = leftLine;
                String rightToken = rightLine;
                if (dir.equals("docs")) {
                    String[] split1 = leftLine.split("~");
                    String[] split2 = rightLine.split("~");
                    leftToken = split1[0];
                    rightToken = split2[0];
                }
                if (leftToken.equalsIgnoreCase(rightToken)) {
                    bw.write(leftLine);
                    bw.newLine();
                    leftLine = brLeft.readLine();
                    rightLine = brRight.readLine();
                } else if (leftToken.compareToIgnoreCase(rightToken) < 0) {
                    bw.write(leftLine);
                    bw.newLine();
                    leftLine = brLeft.readLine();
                } else {
                    bw.write(rightLine);
                    bw.newLine();
                    rightLine = brRight.readLine();
                }
            }
            if (leftLine != null) {
                bw.write(leftLine);
                bw.newLine();
                while ((leftLine = brLeft.readLine()) != null) {
                    bw.write(leftLine);
                    bw.newLine();
                }
            }
            if (rightLine != null) {
                bw.write(rightLine);
                bw.newLine();
                while ((rightLine = brRight.readLine()) != null) {
                    bw.write(rightLine);
                    bw.newLine();
                }
            }
            bw.flush();
            fw.close();
            frLeft.close();
            frRight.close();
            Files.deleteIfExists(left.toPath());
            Files.deleteIfExists(right.toPath());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
