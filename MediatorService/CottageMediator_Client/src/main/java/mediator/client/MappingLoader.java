package mediator.client;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class MappingLoader {

    private final ObjectMapper mapper;

    public MappingLoader() {
        this.mapper = new ObjectMapper();
    }

    /**
     * Loads the list of MappingEntry objects from a JSON file.
     * @param jsonPath The path to the JSON configuration file (e.g. absolute file path).
     * @return A List of MappingEntry objects.
     * @throws IOException if file reading fails.
     */
    public List<MappingEntry> loadJsonData(String jsonPath) throws IOException {

        File file = new File(jsonPath);

        if (!file.exists()) {
        	return new ArrayList<>();
        }
        
        return mapper.readValue(file, new TypeReference<List<MappingEntry>>() {});
    }

    /**
     * Saves the list of MappingEntry objects to a JSON file.
     * @param jsonPath The path where the JSON file should be saved.
     * @param modelInstance The list of MappingEntry objects to save.
     * @throws IOException if file writing fails.
     */
    public void saveJsonData(String jsonPath, List<MappingEntry> modelInstance) throws IOException {

        File file = new File(jsonPath);
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs(); // make sure directory exists
        }

        mapper.writerWithDefaultPrettyPrinter().writeValue(file, modelInstance);
    }
}
