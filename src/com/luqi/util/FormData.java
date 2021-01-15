package com.luqi.util;
/**
* @author coco
* @date 2020年7月14日 上午9:26:15
* 
package com.silot.test;

*/
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.tomcat.util.http.parser.MediaType;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
public class FormData {
	 
	
	
	 
	 
	    public static void main(String args[]) throws Exception
	    {
	 
	        MultipartEntity multipartEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE, "------------------------------0ea3fcae38ff", Charset.defaultCharset());
	    	 multipartEntity.addPart("O_DESCRIBE", new StringBody("哈哈哈哈哈,待=带附件", Charset.forName("UTF-8")));
	    	 multipartEntity.addPart("O_CUSTOMER", new StringBody("PQV8oo3jeeiDkLbY", Charset.forName("UTF-8")));
	    	 multipartEntity.addPart("O_CUSTOMER_NAME", new StringBody("方升群", Charset.forName("UTF-8")));
	    	 multipartEntity.addPart("O_START_TIME", new StringBody("", Charset.forName("UTF-8")));
	    	 multipartEntity.addPart("O_FINISH_TIME", new StringBody("", Charset.forName("UTF-8")));
	    	 multipartEntity.addPart("O_EXECUTOR", new StringBody("3djXOEk7D1u0Jdnv", Charset.forName("UTF-8")));
	    	 multipartEntity.addPart("O_EXECUTOR_NAME", new StringBody("洪剑锋", Charset.forName("UTF-8")));
	    	 multipartEntity.addPart("SOURCE", new StringBody("1", Charset.forName("UTF-8")));
	    	 multipartEntity.addPart("loginName", new StringBody("PQV8oo3jeeiDkLbY", Charset.forName("UTF-8")));
	    	 multipartEntity.addPart("File", new FileBody(new File("E:\\build(31)\\build\\favicon.ico")));
	    	 multipartEntity.addPart("File", new FileBody(new File("E:\\build(31)\\build\\logo.png")));
	    	 multipartEntity.addPart("File", new FileBody(new File("E:\\build(31)\\build\\manifest.json")));
		/*multipartEntity.addPart("FILE","/C:/Users/39805/Pictures/20141223223757_SRi3i.jpeg",
			    new StringBody(create(MediaType.parse("application/octet-stream"),
			    new File("/C:/Users/39805/Pictures/20141223223757_SRi3i.jpeg")));*/
	    	String url="http://59.202.68.43:8080/1do/do/saveIdo";
	        HttpPost request = new HttpPost(url);
	        request.setEntity(multipartEntity);
	        request.addHeader("Content-Type", "multipart/form-data; boundary=------------------------------0ea3fcae38ff");
	        DefaultHttpClient httpClient = new DefaultHttpClient();
	        HttpResponse response = httpClient.execute(request);
	 
	        InputStream is = response.getEntity().getContent();
	        BufferedReader in = new BufferedReader(new InputStreamReader(is));
	        StringBuffer buffer = new StringBuffer();
	        String line = "";
	        while ((line = in.readLine()) != null)
	        {
	            buffer.append(line);
	        }
	 
	        System.out.println("发送消息收到的返回：" + buffer.toString());
	    }
	 
	
}
