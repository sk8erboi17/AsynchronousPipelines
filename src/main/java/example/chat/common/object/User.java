package example.chat.common.object;

import net.techtrends.general.listeners.output.OutputListener;

import java.io.Serializable;

public class User implements Serializable {

    private final String name;
    private final String sex;
    private final int age;
    private OutputListener outputListener;

    public User(String name, String sex, int age) {
        this.name = name;
        this.sex = sex;
        this.age = age;
    }

    public int getAge() {
        return age;
    }

    public String getName() {
        return name;
    }

    public String getSex() {
        return sex;
    }

    public OutputListener getOutputListener() {
        return outputListener;
    }

    public void setOutputListener(OutputListener outputListener) {
        this.outputListener = outputListener;
    }
}
