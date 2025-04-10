package com.companyname.util.pdf;

import com.testautomationguru.utility.CompareMode;
import com.testautomationguru.utility.PDFUtil;
import de.redsix.pdfcompare.CompareResult;
import de.redsix.pdfcompare.PdfComparator;
import org.apache.commons.io.output.NullOutputStream;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.PDFTextStripperByArea;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.*;

public class PdfCompare {

    private static final Logger log = LogManager.getLogger(PdfCompare.class);

    public static void trimPdf(File pdfFile, int numberOfPages) throws IOException {
        for (int i=0; i<numberOfPages; i++) {
            PDDocument document = Loader.loadPDF(pdfFile);
            document.removePage(0);
            document.save(pdfFile);
            document.close();
        }
    }

    public static boolean comparePdfTextByWordToken(String scFile, String exsFile) throws IOException {
        Boolean scFlag = false;
        File scPdf = new File(scFile);
        PDDocument scDocument = Loader.loadPDF(scPdf);
        int noOfPagesInSc = scDocument.getNumberOfPages();

        File exsPdf = new File(exsFile);
        PDDocument exsDocument = Loader.loadPDF(exsPdf);
        int noOfPagesInExs = exsDocument.getNumberOfPages();

        if (scFile.contains("_SC") || exsFile.contains("_SC")) {
            scFlag = true;
        }

        if (noOfPagesInSc != noOfPagesInExs) {
            log.info("Page count is not the same");
        } else {
            PDFUtil pdfUtil = new PDFUtil();
            String scString = pdfUtil.getText(scFile);
            String exsString = pdfUtil.getText(exsFile);
            exsString = replaceBetweenWithoutRegex(exsString, "-*-*-", "-*-*-", true, true, "");
            exsString = replaceBetweenWithoutRegex(exsString, "-*-", "-*-", true, true, "");
            java.util.List<String> scList = new ArrayList<>();
            java.util.List<String> exsList = new ArrayList<> ();

            String scStr[] = scString.split(" ");
            scList = new ArrayList<String>(Arrays.asList(scStr));


            String exsStr[] = exsString.split(" ");
            exsList = new ArrayList<String>(Arrays.asList(exsStr));

            ArrayList<String> uniquesInScFile = new ArrayList<String>(scList);
            ArrayList<String> uniquesInExsFile = new ArrayList<String>(exsList);

            if (scList.equals(exsList)) {

            } else {
                uniquesInScFile.removeAll(exsList);
                uniquesInExsFile.removeAll(scList);
            }

            if (uniquesInScFile.size() == 0) {
                if (scFlag) {
                    log.info("SmartComm PDF " + StringUtils.substringAfterLast(scFile, File.separator) + " has no mismatch\n");
                } else {
                    log.info("First PDF " + StringUtils.substringAfterLast(scFile, File.separator) + " has no mismatch\n");
                }
                return true;
            } else {
                if (scFlag) {
                    log.info("Unique strings in SmartComm PDF:" + StringUtils.substringAfterLast(scFile, File.separator));
                } else {
                    log.info("Unique strings in First PDF:" + StringUtils.substringAfterLast(scFile, File.separator));
                }
                log.info("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
                for (String item : uniquesInScFile) {
                    log.info(item);
                }
            }
            System.out.println("\n");
            if (uniquesInExsFile.size() == 0) {
                if (scFlag) {
                    log.info("Exstream PDF " + StringUtils.substringAfterLast(exsFile, File.separator) + " has no mismatch\n");
                } else {
                    log.info("Second PDF " + StringUtils.substringAfterLast(exsFile, File.separator) + " has no mismatch\n");
                }
            } else {
                if (scFlag) {
                    log.info("Unique strings in Exstream PDF:" + StringUtils.substringAfterLast(exsFile, File.separator));
                } else {
                    log.info("Unique strings in Second PDF:" + StringUtils.substringAfterLast(exsFile, File.separator));
                }
                log.info("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
                for (String item : uniquesInExsFile) {
                    log.info(item);
                }
            }
        }

        return false;
    }

    public static void compareAndImageDiff(String scFile, String exsFile, String imgPath) throws IOException {
        File scPdf = new File(scFile);
        PDDocument scDocument = Loader.loadPDF(scPdf);
        int noOfPagesInSc = scDocument.getNumberOfPages();

        File exsPdf = new File(exsFile);
        PDDocument exsDocument = Loader.loadPDF(exsPdf);
        int noOfPagesInExs = exsDocument.getNumberOfPages();

        if (noOfPagesInSc != noOfPagesInExs) {
            System.out.println("Page count is not the same");
        } else {
            PDFUtil pdfUtil = new PDFUtil();
            pdfUtil.setCompareMode(CompareMode.VISUAL_MODE);
            pdfUtil.highlightPdfDifference(true);
            pdfUtil.setImageDestinationPath(imgPath);

            for (int i=0; i<noOfPagesInExs; i++) {
                pdfUtil.compare(scFile, exsFile,i+1,i+1);
            }
        }
    }

    public static void pdfCompare(String scFile, String exsFile) throws IOException {
        new PdfComparator(scFile, exsFile).compare().writeTo("diffOutput");

        final CompareResult result = new PdfComparator("expected.pdf", "actual.pdf").compare();
        if (result.isNotEqual()) {
            System.out.println("Differences found!");
        }
        if (result.isEqual()) {
            System.out.println("No Differences found!");
        }
        if (result.hasDifferenceInExclusion()) {
            System.out.println("Differences in excluded areas found!");
        }
        result.getDifferences();
        System.out.println("");
    }

    public static void comparePdfTextBySentenceToken(String exsPDF, String scPDF) throws IOException {

        String scPdfData = "";
        String exsPdfData = "";

        List<String> exsListData = new ArrayList<String>();
        List<String> scListData = new ArrayList<String>();

        java.lang.System.setErr(new PrintStream(new NullOutputStream()));

        if ((exsPDF  == null || scPDF ==null)) {
            System.out.println("Please provide the file names");
            return;
        }

        //Creating the object for the files
        PDDocument exsPdfDocument = Loader.loadPDF(new File(exsPDF));
        PDDocument scPdfDocument = Loader.loadPDF(new File(scPDF));

        if (!(exsPdfDocument.getNumberOfPages() == scPdfDocument.getNumberOfPages())) {
            System.out.println("Both the files pages are not equal, first file page count is "+exsPdfDocument.getNumberOfPages()+" and second file is "+scPdfDocument.getNumberOfPages());
            return;
        }

        //Gets the PDF document data and stores them into strings
        if (!exsPdfDocument.isEncrypted() && !scPdfDocument.isEncrypted()) {
            PDFTextStripperByArea stripper = new PDFTextStripperByArea();
            stripper.setSortByPosition(true);
            PDFTextStripper Tstripper = new PDFTextStripper();
            exsPdfData = Tstripper.getText(exsPdfDocument);
            scPdfData = Tstripper.getText(scPdfDocument);
        }

        if (exsPdfData.equals(scPdfData)) {
            System.out.println("Both the files are having same data");
        }

        else {
            //System.out.println("Data in the files are not same");
            StringTokenizer stExsPdf1 = new StringTokenizer(exsPdfData,"\n");
            StringTokenizer stScPdf1 = new StringTokenizer(scPdfData,"\n");
            while (stExsPdf1.hasMoreTokens()) {
                String nextStr = stExsPdf1.nextToken();
                if (!nextStr.startsWith("-*-")) {
                    exsListData.add(nextStr);
                }
            }

            while (stScPdf1.hasMoreTokens()) {
                String nextStr = stScPdf1.nextToken();
                if (!nextStr.startsWith("-*-")) {
                    scListData.add(nextStr);
                }
            }

            System.out.println("\nDifference between files by sentences:");
            System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
            for (int i=0;i<scListData.size();i++) {
                if (!scListData.contains(exsListData.get(i))) {
                    System.out.println("SmartComm Pdf does not contain: "+exsListData.get(i));
                }
            }
        }
    }

    public static String replaceBetweenWithoutRegex(String str,
                                                    String start, String end,
                                                    boolean startInclusive,
                                                    boolean endInclusive,
                                                    String replaceWith) {
        int i = str.indexOf(start);
        while (i != -1) {
            int j = str.indexOf(end, i + 1);
            if (j != -1) {
                String data = (startInclusive ? str.substring(0, i) : str.substring(0, i + start.length())) +
                        replaceWith;
                String temp = (endInclusive ? str.substring(j + end.length()) : str.substring(j));
                data += temp;
                str = data;
                i = str.indexOf(start, i + replaceWith.length() + end.length() + 1);
            } else {
                break;
            }
        }
        return str;
    }

    public static void main(String[] args) throws IOException {
        trimPdf(new File(args[0]), Integer.valueOf(args[1]));
    }
}
