package sample;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.decimal4j.util.DoubleRounder;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;

public class mergeThread extends Thread{

    private String file;

    private String postPath;

    public mergeThread(String file, String post) {
        this.file = file;
        postPath = post;
    }

    public void run() {
        try {
            int index2 = 0;
            File folders = new File("C:\\" + file);
            File[] files = folders.listFiles();
            int size = files.length;
            if (size == 1) {
                FileUtils.copyFile(files[0], new File(postPath + "\\" + file + ".txt"));
                Files.deleteIfExists(files[0].toPath());
            }
            while (size > 1) {
                int i;
                for (i = 0; i < size - 1; i = i + 2) {
                    index2++;
                    File f1 = files[i];
                    File f2 = files[i + 1];
                    if (size == 2) {
                        if (file.equals("city"))
                            mergeCities(f1, f2);
                        else
                            mergeFiles(f1, f2, index2, true, file);
                        break;
                    } else
                        mergeFiles(f1, f2, index2, false, file);

                }
                files = folders.listFiles();
                size = files.length;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void mergeFiles(File left, File right, int TmpIndex, boolean flag, String dir) {
        try {
            FileWriter fw;
            if (flag)
                fw = new FileWriter(postPath + "\\" + dir + ".txt");
            else
                fw = new FileWriter("C:\\" + dir + "\\tmp" + TmpIndex + ".txt");
            BufferedWriter bw = new BufferedWriter(fw);
            FileReader frLeft = new FileReader(left.getPath());
            BufferedReader brLeft = new BufferedReader(frLeft);
            FileReader frRight = new FileReader(right.getPath());
            BufferedReader brRight = new BufferedReader(frRight);
            int leftIdx = 0;
            int rightIdx = 0;
            String newLine = "";
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

    private void mergeCities(File left, File right) {
        try {
            FileWriter fw = new FileWriter(postPath + "\\cities.txt");
            BufferedWriter bw = new BufferedWriter(fw);
            FileReader frLeft = new FileReader(left.getPath());
            BufferedReader brLeft = new BufferedReader(frLeft);
            FileReader frRight = new FileReader(right.getPath());
            BufferedReader brRight = new BufferedReader(frRight);
            int leftIdx = 0;
            int rightIdx = 0;
            String newLine = "";
            String leftLine = brLeft.readLine();
            String rightLine = brRight.readLine();
            while (leftLine != null && rightLine != null) {
                if (leftLine.equals("") || rightLine.equals(""))
                    continue;
                if (leftLine.equalsIgnoreCase(rightLine)) {
                    newLine = getDetails(leftLine);
                    bw.write(newLine);
                    bw.newLine();
                    leftLine = brLeft.readLine();
                    rightLine = brRight.readLine();
                } else if (leftLine.compareToIgnoreCase(rightLine) < 0) {
                    newLine = getDetails(leftLine);
                    bw.write(newLine);
                    bw.newLine();
                    leftLine = brLeft.readLine();
                } else {
                    newLine = getDetails(rightLine);
                    bw.write(newLine);
                    bw.newLine();
                    rightLine = brRight.readLine();
                }
            }
            if (leftLine != null) {
                newLine = getDetails(leftLine);
                bw.write(newLine);
                bw.newLine();
                while ((leftLine = brLeft.readLine()) != null) {
                    newLine = getDetails(leftLine);
                    bw.write(newLine);
                    bw.newLine();
                }
            }
            if (rightLine != null) {
                newLine = getDetails(rightLine);
                bw.write(newLine);
                bw.newLine();
                while ((rightLine = brRight.readLine()) != null) {
                    newLine = getDetails(rightLine);
                    bw.write(newLine);
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

    private String getDetails(String city) {
        try {
            URL url = new URL("http://getcitydetails.geobytes.com/GetCityDetails?fqcn=" + city);
            if (url != null) {

                String page = IOUtils.toString(url.openConnection().getInputStream());

                String currency = StringUtils.substringBetween(page, '"' + "geobytescurrencycode" + '"' + ":" + '"', '"' + ",");
                if (currency == null || currency.equals(""))
                    currency = "X";
                String pop = StringUtils.substringBetween(page, '"' + "geobytespopulation" + '"' + ":" + '"', '"' + ",");
                String population;
                if (pop == null || pop.equals(""))
                    population = "X";
                else
                    population = getNumber(pop);
                String country = StringUtils.substringBetween(page, '"' + "geobytescountry" + '"' + ":" + '"', '"' + ",");
                if (country == null || country.equals(""))
                    country = "X";
                return (city + "," + country + "," + population + "," + currency);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    private String getNumber(String s) {
        double num = Double.parseDouble(s);
        String Snum = "";
        if (num >= 1000000000) {
            num = num / 1000000000;
            num = DoubleRounder.round(num, 2);
            if (num == (int) num)
                Snum = (int) num + "B";
            else
                Snum = num + "B";
        } else if (num >= 1000000) {
            num = num / 1000000;
            num = DoubleRounder.round(num, 2);
            if (num == (int) num)
                Snum = (int) num + "M";
            else
                Snum = num + "M";
        } else if (num >= 1000) {
            num = num / 1000;
            num = DoubleRounder.round(num, 2);
            if (num == (int) num)
                Snum = (int) num + "K";
            else
                Snum = num + "K";
        } else
            Snum = s;
        return Snum;
    }


}
