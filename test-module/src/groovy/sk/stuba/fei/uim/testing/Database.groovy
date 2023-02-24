package sk.stuba.fei.uim.testing

import groovy.xml.XmlParser

class Database {

    Node persistence

    Database(File projectDir) {
        def persistFile = new File(projectDir.absolutePath + File.separator + String.join(File.separator, ['src', 'main', 'resources', 'META-INF', 'persistence.xml']))
        if (!persistFile.exists()) throw new RuntimeException("Cannot find persistence.xml file")
        persistence = new XmlParser().parse(persistFile)
    }
}
