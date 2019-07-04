package com.ibm.isl.ta;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Files;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;

import com.ibm.isl.ta.util.HttpsSSLClient;

public class UploadTest {

	public static void main( String[] args )
    {
        System.out.println( "Hello World! - START" );
        Uploader up = new Uploader();

        String workspace = "NILADRIWORKSPACE";
        String profileName = "Dmgr01.zip";
        String url = "https://9.202.181.183:443/tatest-server/lands_advisor/advisor/landsw/upload?collection=NILADRICOL1&uploadKey=920c6cubg9dk9ptrhhvbjks3u13encnc";
        File file = new File("/Users/niladri/docspace/TransformationAdvisor/Dmgr01.zip");
        System.out.println(file.getAbsolutePath());
        System.out.println(file.exists());

        try {
            up.post(workspace, profileName, url, file);
        } catch(Exception e) {
            System.out.println("Error" + e.getStackTrace());
        }


        System.out.println( "Hello World! - END" );
    }
	
	
	static void upload() {
		try {
			CloseableHttpClient httpclient =  HttpsSSLClient.createSSLInsecureClient();//new DefaultHttpClient(); 

			String collection = "APICOLL11";
			String uploadKey = "920c6cubg9dk9ptrhhvbjks3u13encnc";
			String profileName = "Dmgr01";
			String workspace = "APIWORKSPACE";
			String authorization = "SWetG7p3YO0fXqHHJtFaNmAgemoJvRM2B9cJfwQ9fhya";

			HttpPost post = new HttpPost(
					"https://9.202.181.183:443/tatest-server/lands_advisor/advisor/landsw/upload?collection="
							+ collection + "&uploadKey=" + uploadKey);
			post.addHeader("authorization", authorization);
			post.addHeader("workspace", workspace);
			post.addHeader("profileName", profileName);
			post.addHeader("Content-Type","application/octet-stream");
			
			File file = new File("/Users/niladri/docspace/TransformationAdvisor/Dmgr01.zip");
			byte[] fileContent = Files.readAllBytes(file.toPath());
			
			
			org.apache.http.HttpEntity httpEntity = MultipartEntityBuilder.create()
					.setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
					.setContentType(ContentType.APPLICATION_OCTET_STREAM)
					.addBinaryBody("file", fileContent, ContentType.APPLICATION_OCTET_STREAM, "Dmgr01.zip").build();
			post.setEntity(httpEntity);

			System.out.println("Content-Length------------->"+post.getHeaders("Content-Length"));
			
			HttpResponse response = httpclient.execute(post);

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	static void downloadPdf()
	{
		
	}
}
