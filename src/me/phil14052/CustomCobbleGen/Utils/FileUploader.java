/**
 * CustomCobbleGen By @author Philip Flyvholm
 * FileUploader.java
 */
package me.phil14052.CustomCobbleGen.Utils;

import me.phil14052.CustomCobbleGen.CustomCobbleGen;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * @author Philip
 *
 */
public class FileUploader {
	private CustomCobbleGen plugin = CustomCobbleGen.getInstance();
	private String APIKey = "a9ddc4cbb455f561b1653754bfb9b273";
	private boolean showLineNumber = true;
	
	public Response<String> pastebinUpload(String... fileNames){
		StringBuilder sb = new StringBuilder();
		for(String fileName : fileNames) {
            sb.append(System.lineSeparator());
			sb.append("----------------------------");
			sb.append("Content of " + fileName + ":");
			sb.append("----------------------------");
            sb.append(System.lineSeparator());
            int i = 1;
	        try(BufferedReader br = new BufferedReader(new FileReader(plugin.getDataFolder()+"//" + fileName))) {
	            String line = br.readLine();
	            while (line != null) {
	                sb.append(this.showLineNumber ? i +  ": " + line : line);
	                sb.append(System.lineSeparator());
	                line = br.readLine();
		            i++;
	            }
	        } catch (IOException e) {
	        	plugin.error("Failed to get file content of filename: " + fileName);
	        	plugin.error(e.getMessage());
	        	plugin.error(Arrays.toString(e.getStackTrace()));
	        	return new Response<>("Failed to get file content of filename: " + fileName + " - See console for more info", true);
			}
		}

        String raw = sb.toString();
		URL url = null;
		try {
			url = new URL("https://pastebin.com/api/api_post.php");
		} catch (MalformedURLException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
   	 	HttpURLConnection con = null;
        try {
        	con = (HttpURLConnection) url.openConnection();
             con.setRequestMethod("POST");
             con.setDoOutput(true);
         	HttpParametersUtils parameters = new HttpParametersUtils();
         	parameters.put("api_option", "paste");
         	parameters.put("api_user_key", "");
         	parameters.put("api_paste_private", "1");
         	parameters.put("api_paste_name", Arrays.toString(fileNames) + " for " + plugin.getDescription().getName() + " v" + plugin.getDescription().getVersion());
         	parameters.put("api_dev_key", APIKey);
         	parameters.put("api_paste_code", raw);
         	parameters.put("api_paste_expire_date", "1M");
         	sendParameters(con.getOutputStream(), parameters);
         	final String response = buildResponse(con.getInputStream());
         	plugin.log(response);
         	return new Response<>(response, false);
        }catch(IOException e) {
         	return new Response<>("Unable to connect to pastebin", true);
        }finally {
        	if(con != null) {
        		con.disconnect();
        	}
        }
		
	}
	private void sendParameters(final OutputStream destination, final HttpParametersUtils parametersUtils) throws IOException {
	    final byte[] parameters = parametersUtils.toUrlFormat().getBytes(StandardCharsets.UTF_8);
	    DataOutputStream dataOutputStream = null;

	    try {
	      dataOutputStream = new DataOutputStream(destination);
	      dataOutputStream.write(parameters);
	    } finally {
	      if (dataOutputStream != null) {
	        dataOutputStream.close();
	      }
	    }
	}
	private String buildResponse(@NotNull final InputStream source) throws IOException {
	    final BufferedReader inputReader = new BufferedReader(new InputStreamReader(source));
	    final StringBuilder responseBuilder = new StringBuilder();

	    for(String line; (line = inputReader.readLine()) != null;) {
	      responseBuilder.append(line);
	      responseBuilder.append('\n');
	    }

	    return responseBuilder.toString();
	  }
}
