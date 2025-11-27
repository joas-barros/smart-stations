package edge.src.model;

import device.src.model.ClimateRecord;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class LocalDatabase {

    private final Map<Long, ClimateRecord> storage;

    public LocalDatabase() {
        this.storage = new ConcurrentHashMap<>();
        System.out.println("[DB LOCAL] Armazenamento em memória (Hash Table) inicializado.");
    }

    public void insert(ClimateRecord record) {
        storage.put(record.getId(), record);
        System.out.println("[DB LOCAL] Registro climático inserido. ID: " + record.getId());

    }

    public ClimateRecord findById(long id) {
        return storage.get(id);
    }

    public Map<Long, ClimateRecord> getAll() {
        return storage;
    }
}
