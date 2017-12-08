package danbroid.mopidy;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import org.junit.Before;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import java.io.ByteArrayOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.StringWriter;

import danbroid.mopidy.model.Base;

/**
 * Created by dan on 9/12/17.
 */
public class JsonTest {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(JsonTest.class);

	Gson gson;

	@Before
	public void setup() {
		log.info("setup()");
		gson = new CallContext().getGsonBuilder().setPrettyPrinting().create();
	}


	@Test
	public void test() throws Exception {
		parseTest("/tracks/track1.json");
	}

	public void parseTest(String jsonFile) throws Exception {
		log.info("parseTest(): {}", jsonFile);

		StringWriter sw = new StringWriter();
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		byte buf[] = new byte[10240];
		int c = 0;
		InputStream input = getClass().getResourceAsStream(jsonFile);
		while ((c = input.read(buf)) != -1) {
			bos.write(buf, 0, c);
		}
		input.close();

		String in_json = bos.toString("UTF-8");

		FileWriter fw = new FileWriter("/tmp/input.json");
		fw.write(in_json);
		fw.close();
		Base result[] = gson.fromJson(in_json,Base[].class);
		String out_json = '[' + gson.toJson(result[0]) + ']';

		fw = new FileWriter("/tmp/output.json");
		fw.write(out_json);
		fw.close();


		JSONAssert.assertEquals(in_json,out_json,true);



	}


}

