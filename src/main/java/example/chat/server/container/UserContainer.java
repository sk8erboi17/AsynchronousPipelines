package example.chat.server.container;

import example.chat.common.object.User;

import java.util.HashSet;
import java.util.Set;

public class UserContainer {
    private static final Set<User> userContainer = new HashSet<>();

    public static void addUser(String name, String sex, int age) {
        userContainer.add(new User(name, sex, age));
    }

    public static void addUser(User user) {
        userContainer.add(user);
    }

    public static User getUser(String name) {
        return userContainer.stream().filter(user -> user.getName().equals(name)).findFirst().orElse(null);
    }

    public static Set<User> getAllUsers() {
        return userContainer;
    }

}
