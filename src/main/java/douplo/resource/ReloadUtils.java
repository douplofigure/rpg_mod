package douplo.resource;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.stream.JsonReader;
import net.minecraft.util.Identifier;

import java.io.InputStream;
import java.io.InputStreamReader;

public class ReloadUtils {

    public static Identifier filenameToObjectId(Identifier filename) {
        int startIndex = filename.getPath().indexOf("/");
        int endIndex = filename.getPath().lastIndexOf(".");
        String simplePath = filename.getPath().substring(startIndex+1, endIndex);
        return new Identifier(filename.getNamespace(), simplePath);
    }

    public static JsonObject jsonFromStream(InputStream stream) {
        Gson gson = new GsonBuilder().create();
        JsonReader reader = gson.newJsonReader(new InputStreamReader(stream));
        return gson.fromJson(reader, JsonObject.class);
    }

}
