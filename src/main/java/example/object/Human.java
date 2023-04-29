package example.object;

import java.io.Serializable;
import java.util.UUID;

public class Human implements Serializable {
    private final UUID uuid;

    private final int age;

    public Human(UUID uuid, int age) {
        this.uuid = uuid;
        this.age = age;
    }

    public int getAge() {
        return age;
    }

    public UUID getUuid() {
        return uuid;
    }
}
