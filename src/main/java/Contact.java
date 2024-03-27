import java.util.*;

record Contact(String name, LinkedList<String> phones) implements Comparable<Contact> {

    Contact(String phone) {
        this(null, new LinkedList<>(List.of(phone)));
    }


    @Override
    public int compareTo(Contact o) {
        return name().compareTo(o.name());
    }
}