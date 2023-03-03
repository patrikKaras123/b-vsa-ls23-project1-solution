package sk.stuba.fei.uim.testing

import groovy.xml.XmlParser
import groovy.xml.XmlUtil

class Maven {

    static String OUTPUT = "test-output.txt"
    static String ERRORS = "test-error-output.txt"
    static String SUREFIRE = "surefire-test-reports"

    Node pom
    File projectDir

    Maven(File projectDir) {
        this.projectDir = projectDir
        File pomFile = new File(projectDir.absolutePath + File.separator + 'pom.xml')
        if (!pomFile.exists()) throw new RuntimeException("Cannot find pom.xml in project " + projectDir.getName())
        pom = new XmlParser().parse(pomFile)
    }

    void setProperty(String key, String value) {
        if (pom.'properties'.get(key).size() > 0) {
            pom.'properties'.get(key)[0].setValue(value)
        } else {
            def prop = new NodeBuilder().key(value)
            pom.'properties'[0].append(prop)
        }
    }

    Node setDependency(String group, String artifact, String versionValue, String scopeValue = null) {
        Node dep = pom.dependencies.dependency.find { it.groupId[0].text() == group && it.artifactId[0].text() == artifact } as Node
        Node newDep = new NodeBuilder().dependency() {
            groupId(group)
            artifactId(artifact)
            version(versionValue)
            if (scope) {
                scope(scopeValue)
            }
        }
        if (dep) {
            dep.replaceNode(newDep)
        } else {
            pom.dependencies[0].append(newDep)
        }
        return newDep
    }

    Node setPlugin(String group, String artifact, String versionValue) {
        Node plugin = pom?.build?.plugins?.plugin?.find { it.groupId[0].text() == group && it.artifactId[0].text() == artifact } as Node
        Node newPlugin = new NodeBuilder().plugin() {
            groupId(group)
            artifactId(artifact)
            version(versionValue)
        }
        if (!pom?.build) {
            pom.append(new NodeBuilder().build() as Node)
        }
        if (!pom.build?.plugins) {
            pom.append(new NodeBuilder().plugins() as Node)
        }
        if (plugin) {
            plugin.replaceNode(newPlugin)
        } else {
            pom.plugins[0].append(newPlugin)
        }
        return newPlugin
    }

    String toString() {
        return XmlUtil.serialize(pom)
    }

    File saveToFile() {
        File pomFile = new File(projectDir.absolutePath + File.separator + 'pom.xml')
        pomFile.text = this.toString()
        return pomFile
    }

    Student getDeveloper() {
        if (pom.developers.developer.size() > 0) {
            Node dev = pom.developers.developer[0]
            return new Student(
                    'aisId': dev.id?.text()?.trim()?.replace('\n', '')?.replace('\t', ''),
                    'name': dev.name?.text()?.trim()?.replace('\n', '')?.replace('\t', ''),
                    'email': dev.email?.text()?.trim()?.replace('\n', '')?.replace('\t', '')
            )
        }
        return null
    }

    Process runGoal(String... goal) {
        List<String> args = ['cmd', '/c', 'mvn']
        args += goal as List<String>
        def builder = new ProcessBuilder(args)
        builder.directory(projectDir)
        builder.redirectOutput(new File(projectDir.absolutePath
                + File.separator + Constants.FEEDBACK_DIR
                + File.separator + 'maven'
                + File.separator + OUTPUT))
        builder.redirectError(new File(projectDir.absolutePath
                + File.separator + Constants.FEEDBACK_DIR
                + File.separator + 'maven'
                + File.separator + ERRORS))
        def process = builder.start()
        process.waitFor()
        return process
    }

}
