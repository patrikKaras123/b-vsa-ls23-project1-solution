package sk.stuba.fei.uim.testing

import groovy.sql.Sql
import groovy.xml.XmlParser
import groovy.xml.XmlUtil

class Database {

    static String URL = System.getenv("VSA_DB_URL")
    static String USERNAME = System.getenv("VSA_DB_USERNAME")
    static String PASSWORD = System.getenv("VSA_DB_PASSWORD")
    static String DRIVER = System.getenv("VSA_DB_DRIVER")

    Node persistence
    Node persistenceUnit
    File projectDir

    Database(File projectDir) {
        def persistFile = new File(projectDir.absolutePath + File.separator + String.join(File.separator, ['src', 'main', 'resources', 'META-INF', 'persistence.xml']))
        if (!persistFile.exists()) throw new RuntimeException("Cannot find persistence.xml file")
        persistence = new XmlParser().parse(persistFile)
        persistenceUnit = persistence?.'persistence-unit'[0] as Node
        if (!persistenceUnit)
            throw new RuntimeException("persistence.xml in project " + projectDir.getName() + "is setup incorrectly! persistence-unit was not found")
    }

    Node setProperty(String name, String value) {
        def props = persistenceUnit?.'properties'[0]
        if (!props) {
            props = new NodeBuilder().'properties'() {}
            persistenceUnit.append(props)
        }
        def prop = props.property.find { it.'@name' == name } as Node
        if (prop) {
            prop.'@value' = value
        } else {
            props.append(new NodeBuilder().property(name: name, value: value) {})
        }
        return prop
    }

    String toString() {
        return XmlUtil.serialize(persistence)
    }

    File saveToFile() {
        File persistFile = new File(projectDir.absolutePath + File.separator + String.join(File.separator, ['src', 'main', 'resources', 'META-INF', 'persistence.xml']))
        persistFile.text = this.toString()
        return persistFile
    }

    void clearDatabase() {
        def sql = Sql.newInstance(URL, USERNAME, PASSWORD, DRIVER)
        if (!sql)
            throw new RuntimeException("Cannot connect to the MySQL DB")
        sql.execute 'SET FOREIGN_KEY_CHECKS = 0'
        sql.execute 'SET GROUP_CONCAT_MAX_LEN = 32768'
        sql.execute 'SET @tables = NULL'
        sql.execute '''
        SELECT GROUP_CONCAT('`', table_name, '`') INTO @tables
        FROM information_schema.tables
        WHERE table_schema = (SELECT DATABASE())
    '''
        sql.execute 'SELECT IFNULL(@tables,\'dummy\') INTO @tables'
        sql.execute 'SET @tables = CONCAT(\'DROP TABLE IF EXISTS \', @tables)'
        sql.execute 'PREPARE stmt FROM @tables'
        sql.execute 'EXECUTE stmt'
        sql.execute 'DEALLOCATE PREPARE stmt'
        sql.execute 'SET FOREIGN_KEY_CHECKS = 1'
        sql.close()
    }
}
