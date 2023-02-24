package sk.stuba.fei.uim.testing

public class Student {
    public String aisId = ''
    public String name = ''
    public String email = ''

    @Override
    String toString(String delimiter = ';') {
        return String.join(delimiter, aisId, name, email)
    }

    Node toXml() {
        return new NodeBuilder().student {
            'aisId'(aisId)
            'name'(name)
            'email'(email)
        } as Node
    }
}
