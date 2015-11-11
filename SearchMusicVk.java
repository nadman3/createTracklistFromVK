import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.*;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.ParseException;
import java.util.Scanner;

public class SearchMusicVk{

	public static String[] artists, titles;

	public static void main(String[] args) throws URISyntaxException,
			IOException, ParseException{

//		commented for reading args from command line or from console
//		String pathJSON = args[0];
//		String pathTracklist = args[1];
		Scanner scanner = new Scanner(System.in);
		System.out.print("Input path and name of JSONFile : ");
		String pathJSON = scanner.nextLine();
		System.out.print("Input name and path where you want to save tracklist : ");
		String pathTracklist = scanner.nextLine();

		String ACCESS_TOKEN = "123";
		String YOUR_ID_VK = "456";
		parseLogJSON(pathJSON);
		String urlForVk = "https://api.vk.com/method/audio.search?&" +
				"oid=" + YOUR_ID_VK +
				"need_user=0&" +
				"q=" + artists[0] + "&" +
				"auto_complete=0&" +
				"lyrics=0&" +
				"performer_only=1&" +
				"sort=2&" +
				"search_own=0&" +
				"offset=1&" +
				"count=300&" +
				"access_token=" + ACCESS_TOKEN;
		parseAndSaveTracklist(urlForVk, pathTracklist);
	}

	private static void parseLogJSON(String pathJSON){
		JSONParser parser = new JSONParser();
		try{
			Object obj = parser.parse(new FileReader(pathJSON));
			JSONObject jsonObject = (JSONObject) obj;
			JSONObject insideJsonObject;
			artists = new String[((JSONObject) obj).size()];
			titles = new String[((JSONObject) obj).size()];
			for(int i = 0; i < ((JSONObject) obj).size(); i++){
				insideJsonObject = (JSONObject) jsonObject.get("" + (i + 1) + "");
				artists[i] = ((String) insideJsonObject.get("artist")).trim();
				titles[i] = ((String) insideJsonObject.get("title")).trim();
			}
		}catch (Exception e){
			e.printStackTrace();
		}
	}

	private static String getJSON(String urle){
		try{
			URL url = new URL(urle);
			java.net.HttpURLConnection con = (java.net.HttpURLConnection) url.openConnection();
			con.setConnectTimeout(3000);
			con.connect();
			int resp = con.getResponseCode();
			if(resp == 200 || resp == 6){
				BufferedReader br = new BufferedReader(new InputStreamReader(con.getInputStream()));
				String line;
				StringBuilder sb = new StringBuilder();
				while((line = br.readLine()) != null){
					sb.append(line);
					sb.append("\n");
				}
				br.close();
				return sb.toString();
			} else
				System.out.println("Error " + resp);
		} catch(Exception e){
			e.printStackTrace();
		}
		return null;
	}

	private static void parseAndSaveTracklist(String url, String pathTracklist){
		JSONParser pars = new JSONParser();
		String jsonFromVk = getJSON(url);
		try {
			Object objectFinal = pars.parse(jsonFromVk);
			JSONObject objJsonFromVk = (JSONObject) objectFinal;
			JSONArray objectInJson = (JSONArray) objJsonFromVk.get("response");
			PrintWriter writer = new PrintWriter(pathTracklist);
			writer.println("#EXTM3U");
			for(int i = 0; i < objectInJson.size() - 5; i++){
				JSONObject arrayInNumber = (JSONObject) objectInJson.get(i + 1);
				String titleSong = (String) arrayInNumber.get("title");
				if(!(arrayInNumber.get("url")).equals("") && checkTitle(titleSong.trim())){
						writer.println("#EXTINF:" + (arrayInNumber.get("duration")) + "," +
								((String)arrayInNumber.get("artist")).trim() + " - " +
								((String)arrayInNumber.get("title")).trim());
						String URL = (String) arrayInNumber.get("url");
						URL = URL.replace("https", "http");
						String[] cache = URL.split("\\?");
						URL = cache[0];
						writer.println(URL);
					}
			}
			writer.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	private static boolean checkTitle(String t){
		for(int i = 0; i < titles.length; i++)
			if(titles[i].equalsIgnoreCase(t)){
				titles[i] = "!@#$%^";   //unreal name to search again
				return true;
			}
		return false;
	}
}