package publon_review;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.apache.commons.codec.binary.Base64;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

public class mint_doi {

	public static void main(String[] args) throws IOException, ParseException {

		String path = "/Users/xiaosizhe/Desktop/publon/";
		File file = new File(path+"sep2-2019.xls"); // change excel file name
																				
		BufferedReader br1 = new BufferedReader(new FileReader(file));
		String line1;

		if (!file.isFile()) {
			throw new RuntimeException(file + "xxx");
		}
		POIFSFileSystem fs = null;
		HSSFWorkbook wb = null;
		HSSFSheet sheet = null;
		try {
			fs = new POIFSFileSystem(new FileInputStream(file));
			wb = new HSSFWorkbook(fs);
			sheet = wb.getSheetAt(0);
		} catch (IOException e) {
			System.out.println(file);
			e.printStackTrace();
		}

		for (Row row : sheet) {

			String pubdoi = "";
			String reviewname = "";
			String puburl = "";
			String pubtitle = "";
			String date = "";

			for (Cell cell : row) {
				int columnIndex = cell.getColumnIndex();

				if (columnIndex == 0) {

					pubdoi = cell.getStringCellValue();
					pubdoi = pubdoi.replace("https://doi.org/", "");

					System.out.println("pubdoi: " + pubdoi);

				}

				if (columnIndex == 1) {
					reviewname = cell.getStringCellValue();
					System.out.println("reviewname: " + reviewname);

				}

				if (columnIndex == 2) {
					puburl = cell.getStringCellValue();
					System.out.println("puburl: " + puburl);
				}
				if (columnIndex == 3) {
					pubtitle = cell.getStringCellValue();
					System.out.println("pubtitle: " + pubtitle);
				}

				if (columnIndex == 4) {
					date = cell.getStringCellValue();
					String[] aa = date.split("/");

					final String OLD_FORMAT = "dd/MM/yy";
					final String NEW_FORMAT = "yyyy-MM-dd";

					SimpleDateFormat sdf = new SimpleDateFormat(OLD_FORMAT);
					Date d = sdf.parse(date);
					sdf.applyPattern(NEW_FORMAT);
					date = sdf.format(d);

					System.out.println("pubdate: " + date);
				}

			}
			if (pubdoi == "") {
				break;
			}

			generatexml(pubdoi, reviewname, puburl, pubtitle, date, path);

		}

	}

	public static void generatexml(String pubdoi, String reviewername, String puburl, String pubtitle, String date, String path)
			throws IOException {
		File file1 = new File(path+"doi");
		BufferedReader br1 = new BufferedReader(new FileReader(file1));
		String sCurrentLine;
		String doi = "";
		int doinumber = 0;

		while ((sCurrentLine = br1.readLine()) != null) {

			doinumber = Integer.valueOf(sCurrentLine) + 1;
			doi = "10.5524/review." + String.valueOf(doinumber);

		}

		File file2 = new File(path + doi.replace("/", "-") + ".xml");
		BufferedWriter bw2 = new BufferedWriter(new FileWriter(file2));

		String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
				+ "<resource xmlns=\"http://datacite.org/schema/kernel-3\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://datacite.org/schema/kernel-3 http://schema.datacite.org/meta/kernel-3/metadata.xsd\">\n";
		xml += ("<identifier identifierType=\"DOI\">@</identifier>\n").replace("@", doi);
		xml += "<creators>\n" + "<creator>\n";
		xml += ("<creatorName>@</creatorName>\n").replace("@", reviewername);
		xml += "</creator>\n" + "</creators>\n" + "<titles>\n";
		xml += ("<title>Peer review of \"@\"</title>\n".replace("@", pubtitle));
		xml += "</titles>\n" + "<publisher>GigaScience</publisher>\n" + "<publicationYear>2019</publicationYear>\n"
				+ "<subjects>\n" + "<subject>Peer review</subject>\n" + "</subjects>\n" + "<language>eng</language>\n"
				+ "<dates>\n";

		xml += ("<date dateType=\"Submitted\">@</date>\n").replace("@", date);
		xml += "</dates>\n" + "<resourceType resourceTypeGeneral=\"Text\">Peer review</resourceType>\n"
				+ "<relatedIdentifiers>\n";
		xml += ("<relatedIdentifier relatedIdentifierType=\"DOI\" relationType=\"Reviews\">@</relatedIdentifier>\n")
				.replace("@", pubdoi);
		xml += "</relatedIdentifiers>\n";
		xml += "<rightsList>\n"
				+ "<rights rightsURI=\"http://creativecommons.org/licenses/by/4.0/\">Creative Commons Attribution-4.0 International (CC-BY 4.0)</rights>\n"
				+ "</rightsList>\n" + "<descriptions>\n"
				+ "<description descriptionType=\"Other\">This is the open peer reviewers comments and recommendations regarding the submitted GigaScience article and/or dataset.</description>\n"
				+ "</descriptions>\n" + "</resource>\n";

		bw2.write(xml);
		bw2.close();

		BufferedWriter bw1 = new BufferedWriter(new FileWriter(file1));
		System.out.println(String.valueOf(doinumber));
		bw1.write(String.valueOf(doinumber));
		bw1.close();
		br1.close();

		int code = uploadmeta(path + doi.replace("/", "-") + ".xml");
		if (code == 201) {
			int code2 = mintdoi(puburl, doi);

			if (code2 == 201) {
				System.out.println(puburl + "    " + doi);

			}

		}

	}

	public static int mintdoi(String url, String doi) {
		URL url1 = null;
		HttpURLConnection httpurlconnection1 = null;
		String content = "doi=" + doi + "\n" + "url=" + url;
		int code = 0;

		try {

			url1 = new URL("https://mds.datacite.org/doi");

			httpurlconnection1 = (HttpURLConnection) url1.openConnection();
			httpurlconnection1.setDoOutput(true);
			httpurlconnection1.setRequestMethod("POST");
			httpurlconnection1.setRequestProperty("Content-type", "text/plain");
			httpurlconnection1.setRequestProperty("Charset", "UTF-8");
			String username = "CNGB.GIGADB:GigaDB2018";

			byte[] encoding = Base64.encodeBase64(username.getBytes());
			httpurlconnection1.setRequestProperty("Authorization", "Basic " + new String(encoding));
			byte[] postDataBytes = content.toString().getBytes("UTF-8");
			httpurlconnection1.getOutputStream().write(postDataBytes);
			;

			// httpurlconnection.getOutputStream().write(username.getBytes());

			code = httpurlconnection1.getResponseCode();

			System.out.println("code   " + code);
			System.out.println(content);

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (httpurlconnection1 != null)
				httpurlconnection1.disconnect();
		}

		return code;

	}

	public static int uploadmeta(String filelocation) throws IOException {
		int code = 0;
		URL url = null;
		HttpURLConnection httpurlconnection = null;
		File msg = new File(filelocation);

		BufferedReader br = new BufferedReader(new FileReader(msg));
		String line;
		StringBuilder sb = new StringBuilder();

		while ((line = br.readLine()) != null) {

			sb.append(line);
		}

		try {
			url = new URL("https://mds.datacite.org/metadata");

			httpurlconnection = (HttpURLConnection) url.openConnection();
			httpurlconnection.setDoOutput(true);
			httpurlconnection.setRequestMethod("POST");
			httpurlconnection.setRequestProperty("Content-type", "application/xml");
			httpurlconnection.setRequestProperty("Charset", "UTF-8");
			String username = "CNGB.GIGADB:GigaDB2018";

			byte[] encoding = Base64.encodeBase64(username.getBytes());
			httpurlconnection.setRequestProperty("Authorization", "Basic " + new String(encoding));
			byte[] postDataBytes = sb.toString().getBytes("UTF-8");
			httpurlconnection.getOutputStream().write(postDataBytes);
			;

			// httpurlconnection.getOutputStream().write(username.getBytes());

			code = httpurlconnection.getResponseCode();

			System.out.println("code   " + code);

		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (httpurlconnection != null)
				httpurlconnection.disconnect();
		}

		return code;
	}
}
