import groovy.sql.Sql
@GrabConfig(systemClassLoader = true)
@Grab(group = 'mysql', module = 'mysql-connector-java', version = '5.1.49')

import groovy.xml.XmlUtil

import javax.swing.*
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.time.Duration
import java.time.LocalDateTime

// CONSTANTS
Boolean ALL = false
String CWD = new File("..").absolutePath
String TEST_PROJECT = new File(CWD + File.separator + "b-vsa-ls22-project1").absolutePath
String STUDENT_GROUP = "a"
String FEEDBACK_DIR = "feedback"
String SOURCE_TEST_DIR = File.separator + String.join(File.separator, ['src', 'test', 'java', 'sk', 'stuba', 'fei', 'uim', 'vsa', 'pr1'])
String TARGET_TEST_DIR = File.separator + String.join(File.separator, ['src', 'test', 'java', 'sk', 'stuba', 'fei', 'uim', 'vsa', 'pr1' + STUDENT_GROUP])
String RESPORTS_DIR = File.separator + String.join(File.separator, ['target', 'surefire-reports'])
def DB = [url : "jdbc:mysql://localhost:3306/VSA_PR1?useUnicode=true&characterEncoding=UTF-8",
          user: 'vsa', password: 'vsa', driver: 'com.mysql.jdbc.Driver']
def CSV_HEADER = ['AISID', 'Name', 'Email', 'GitHub', 'Tests Run', 'Succeeded', 'Failures', 'Errors', 'Skipped', 'Points',
                  'Bonus Tests Run', 'Bonus Succeeded', 'Bonus Failures', 'Bonus Errors', 'Bonus Skipped', 'Bonus Points', 'Total Points', 'Notes']
String CSV_DELIMITER = ';'
String MAVEN_OUTPUT = 'test-output.txt'
String MAVEN_ERRORS = 'test-error-output.txt'
String SUREFIRE_REPORTS = 'surefire-test-reports'
Integer MAX_POINTS = 20
Integer MAX_BONUS_POINTS = 5

Map<String, String> ENV = System.getenv()
def input = JOptionPane.&showInputDialog

// CLASSES
class Student {
    String aisId = ''
    String name = ''
    String email = ''

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

class TestsRun {
    Integer totalRun = 0
    Integer success = 0
    Integer failure = 0
    Integer error = 0
    Integer skip = 0
    Integer points = 0

    void calcPoints(Double maxPoints = 20.0) {
        if (success == 0)
            calcSuccess()
        points = Math.ceil((maxPoints / (totalRun.doubleValue() - skip.doubleValue())) * success.doubleValue()).intValue()
    }

    void calcSuccess() {
        success = totalRun - (failure + error + skip)
    }

    @Override
    String toString(String delimiter = ';') {
        return String.join(delimiter, [
                totalRun as String,
                success as String,
                failure as String,
                error as String,
                skip as String,
                points as String
        ])
    }

    Node toXml() {
        return new NodeBuilder().testsRun {
            'totalRun'(totalRun)
            'success'(success)
            'failure'(failure)
            'error'(error)
            'skip'(skip)
            'points'(points)
        } as Node
    }
}

class Evaluation {
    String exam = 'B-VSA 21/22 Semestrálny projekt 1'
    Student student = new Student()
    String github = ''
    TestsRun required = new TestsRun()
    TestsRun bonus = new TestsRun()
    Integer totalPoints = 0
    String notes = ''
    Duration testDuration

    TestsRun getTotalTestsRun() {
        TestsRun total = new TestsRun(
                totalRun: required.totalRun,
                success: required.success,
                failure: required.failure,
                error: required.error,
                skip: required.skip,
                points: required.points
        )
        total.totalRun += bonus.totalRun
        total.success += bonus.success
        total.failure += bonus.failure
        total.error += bonus.error
        total.skip += bonus.skip
        total.points += bonus.points
        return total
    }

    Integer calcTotalPoints(int max, int bon) {
        required.calcPoints(max.doubleValue())
        bonus.calcPoints(bon.doubleValue())
        totalPoints = required.points + bonus.points
        return totalPoints
    }

    @Override
    String toString(String delimiter = ';') {
        return String.join(delimiter, [
                student.toString(delimiter),
                github,
                required.toString(delimiter),
                bonus.toString(delimiter),
                totalPoints as String,
                notes])
    }

    Node toXml() {
        Node xml = new NodeBuilder().evaluation {
            'exam'(exam)
            'student'('Dummy student')
            'github'(github)
            'testsRuns' {
                'required' {
                    'tests'('Tests placeholder')
                }
                'bonus' {
                    'tests'('Tests placeholder')
                }
            }
            'totalPoints'(totalPoints)
            'notes'(notes)
            'testDuration'(testDuration.toString())
        }
        (xml.student[0] as Node).replaceNode(student.toXml())
        (xml.testsRuns.required.tests[0] as Node).replaceNode(required.toXml())
        (xml.testsRuns.bonus.tests[0] as Node).replaceNode(bonus.toXml())
        return xml
    }
}

List<Evaluation> results = []

// Test steps:
// cd to student project
// copy necessery files
// edit some file
// run maven test
// aggregate test reports to one file
// evaluate results and assign points to a student
// git push student's repo changes

void removeFrom(File file) {
    if (file.isFile()) {
        file.delete()
        return
    }
    File[] files = file.listFiles()
    for (File f : files) {
        if (f.isDirectory()) {
            removeFrom(f)
            f.deleteDir()
            continue
        }
        f.delete()
    }
}

def copyDir = { File from, File to ->
    to.mkdirs()
    File[] files = from.listFiles()
    for (File file : files) {
        if (file.isDirectory()) continue
        File target = new File(to.absolutePath + File.separator + file.getName())
        Files.copy(
                file.toPath(),
                target.toPath(),
                StandardCopyOption.COPY_ATTRIBUTES,
                StandardCopyOption.REPLACE_EXISTING
        )
        target.text = target.text.replace('sk.stuba.fei.uim.vsa.pr1', 'sk.stuba.fei.uim.vsa.pr1' + STUDENT_GROUP)
    }
}

def createDeps = { Node pom, String group, String artifact, String versionValue, Boolean testScope ->
    Node dep = pom.dependencies.dependency.find { it.groupId[0].text() == group && it.artifactId[0].text() == artifact } as Node
    Node newDep = new NodeBuilder().dependency() {
        groupId(group)
        artifactId(artifact)
        version(versionValue)
        if (testScope) {
            scope('test')
        }
    }
    if (dep) {
        dep.replaceNode(newDep)
    } else {
        pom.dependencies[0].append(newDep)
    }
}

def editPom = { File project ->
    def pomFile = new File(project.absolutePath + File.separator + "pom.xml")
    if (!pomFile.exists()) throw new RuntimeException("Cannot find pom.xml file")
    def pom = new XmlParser().parse(pomFile)

    if (pom.'properties'.'junit.version'.size() > 0) {
        pom.'properties'.'junit.version'[0].setValue('5.8.2')
    } else {
        def junitProperty = new NodeBuilder().'junit.version'('5.8.2')
        pom.'properties'[0].append(junitProperty)
    }

    createDeps(pom, 'mysql', 'mysql-connector-java', '5.1.49', false)
    createDeps(pom, 'org.junit.jupiter', 'junit-jupiter-engine', '\${junit.version}', true)
    createDeps(pom, 'org.junit.jupiter', 'junit-jupiter-api', '\${junit.version}', true)
    createDeps(pom, 'org.reflections', 'reflections', '0.10.2', false)

    def build = new NodeBuilder().build() {
        plugins() {
            plugin() {
                groupId('org.apache.maven.plugins')
                artifactId('maven-surefire-plugin')
                version('2.22.2')
            }
        }
    }
    if (pom.build.size() > 0) {
        pom.build[0].replaceNode(build)
    } else {
        pom.append(build)
    }
    def xmlString = XmlUtil.serialize(pom)
    pomFile.text = xmlString

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

def editProperty = { Node properties, String name, String value ->
    def prop = properties.property.find { it.'@name' == name }
    if (prop) {
        prop.'@value' = value
    } else {
        properties.append(new NodeBuilder().property(name: name, value: value) {})
    }
}

def editPersistence = { File project ->
    def persistFile = new File(project.absolutePath + File.separator + String.join(File.separator, ['src', 'main', 'resources', 'META-INF', 'persistence.xml']))
    if (!persistFile.exists()) throw new RuntimeException("Cannot find persistence.xml file")
    def persist = new XmlParser().parse(persistFile)
    def punit = persist?.'persistence-unit'[0] as Node
    if (!punit) throw new RuntimeException("persistence.xml file is setup incorrectly! persistence-unit was not found.")
    def props = punit?.'properties'[0]
    if (!props) {
        println "\tProperties of persistence.xml was not found!"
        props = new NodeBuilder().'properties'() {}
        punit.append(props)
    }
    editProperty(props as Node, 'javax.persistence.jdbc.driver', 'com.mysql.jdbc.Driver')
    editProperty(props as Node, 'javax.persistence.jdbc.url', 'jdbc:mysql://localhost:3306/VSA_PR1?serverTimezone=UTC')
    editProperty(props as Node, 'javax.persistence.jdbc.user', 'vsa')
    editProperty(props as Node, 'javax.persistence.jdbc.password', 'vsa')
    editProperty(props as Node, 'javax.persistence.schema-generation.database.action', 'drop-and-create')
    editProperty(props as Node, 'eclipselink.target-database', 'MySQL')
    editProperty(props as Node, 'eclipselink.logging.level.sql', 'FINE')
    editProperty(props as Node, 'eclipselink.logging.parameters', 'true')
    def xmlString = XmlUtil.serialize(persist)
    persistFile.text = xmlString
}

def clearDatabase = {
    def sql = Sql.newInstance(DB.url, DB.user, DB.password, DB.driver)
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

def runMvnTest = { File project, File output, File errors ->
    def args = ['cmd', '/c', 'mvn', 'clean', 'compile', 'test']
    def builder = new ProcessBuilder(args)
    builder.directory(project)
    builder.redirectOutput(output)
    builder.redirectError(errors)
    def process = builder.start()
    process.waitFor()
}

//TODO dať do Evaluation triedy
def aggregateTestReports = { File project, File surefireXml, File surefireTxt ->
    surefireXml.text = '<?xml version="1.0" encoding="UTF-8"?>'
    surefireTxt.text = ''
    File[] reports = new File(project.absolutePath + RESPORTS_DIR).listFiles()
    for (File report : reports) {
        if (report.name.endsWith(".xml")) {
            surefireXml.append(report.text.replace('<?xml version="1.0" encoding="UTF-8"?>', ''))
        } else if (report.name.endsWith(".txt")) {
            surefireTxt.append(report.text)
            def delimiter = ''
            79.times { delimiter += '-' }
            surefireTxt.append(delimiter + "\n \n")
        }
    }
}

//TODO dať do Evaluation triedy
def evaluateTests = { File surefireReport, boolean bonusTests ->
    TestsRun run = new TestsRun()
    surefireReport.eachLine { line ->
        if (!line.startsWith('Tests run:')) return
        String[] parts = line.split(',')
        run.totalRun += Integer.parseInt(parts[0].substring(parts[0].lastIndexOf(' ')).trim())
        run.failure += Integer.parseInt(parts[1].substring(parts[1].lastIndexOf(' ')).trim())
        run.error += Integer.parseInt(parts[2].substring(parts[2].lastIndexOf(' ')).trim())
        run.skip += Integer.parseInt(parts[3].substring(parts[3].lastIndexOf(' ')).trim())

    }
    int maxPoints = bonusTests ? MAX_BONUS_POINTS : MAX_POINTS
    run.calcSuccess()
    run.calcPoints(maxPoints.doubleValue())
    return run
}

//TODO dať do Evaluation triedy
def buildSummaryFile = { Evaluation eval, File project ->
    def summaryFile = new File(project.absolutePath + File.separator + FEEDBACK_DIR + File.separator + 'summary.xml')
    summaryFile.text = XmlUtil.serialize(eval.toXml())
}

def runTestProcedure = { File project, File outputs, File errors, File surefireXml, File surefireTxt, Closure copyFunction ->
    println "\t Copying test files"
    copyFunction()
    println "\t Starting maven tests"
    runMvnTest(project, outputs, errors)
    println "\t Aggregating Surefire reports into one file"
    aggregateTestReports(project, surefireXml, surefireTxt)
    println "\t Evaluating test results"
    return evaluateTests(surefireTxt, outputs.getName().contains('bonus'))
}

def evaluateStudent = { File project ->
    if (!project.isDirectory()) return null
    LocalDateTime startOfTest = LocalDateTime.now()
    println "Starting evaluation of student ${project.getName()}"
    Evaluation student = new Evaluation()
    student.github = project.getName()
    println "Creating feedback folder"
    new File(project.absolutePath + File.separator + FEEDBACK_DIR).mkdirs()
    File outFile = new File(project.absolutePath + File.separator + FEEDBACK_DIR + File.separator + MAVEN_OUTPUT)
    File bonusOutFile = new File(project.absolutePath + File.separator + FEEDBACK_DIR + File.separator + 'bonus-' + MAVEN_OUTPUT)
    File errFile = new File(project.absolutePath + File.separator + FEEDBACK_DIR + File.separator + MAVEN_ERRORS)
    File bonusErrFile = new File(project.absolutePath + File.separator + FEEDBACK_DIR + File.separator + 'bonus-' + MAVEN_ERRORS)
    File surefireXml = new File(project.absolutePath + File.separator + FEEDBACK_DIR + File.separator + SUREFIRE_REPORTS + '.xml')
    File bonusSurefireXml = new File(project.absolutePath + File.separator + FEEDBACK_DIR + File.separator + 'bonus-' + SUREFIRE_REPORTS + '.xml')
    File surefireTxt = new File(project.absolutePath + File.separator + FEEDBACK_DIR + File.separator + SUREFIRE_REPORTS + '.txt')
    File bonusSurefireTxt = new File(project.absolutePath + File.separator + FEEDBACK_DIR + File.separator + 'bonus-' + SUREFIRE_REPORTS + '.txt')
    outFile.text = ''
    errFile.text = ''
    bonusOutFile.text = ''
    bonusErrFile.text = ''

    try {
        println "Editing pom.xml"
        Student studentId = editPom(project)
        if (!studentId) {
            println "\t Cannot parse student from pom.xml"
            studentId = new Student('name': project.getName())
        }
        student.student = studentId
        println "\t Detected student credentials: ${student.student}"

        println "Editing persistence.xml"
        editPersistence(project)

        println "Clearing database before tests"
        clearDatabase()

        println "Required Tests Run"
        student.required = runTestProcedure(project, outFile, errFile, surefireXml, surefireTxt, {
            def target = new File(project.absolutePath + TARGET_TEST_DIR)
            removeFrom(target)
            copyDir(new File(TEST_PROJECT + SOURCE_TEST_DIR), target)
            copyDir(new File(TEST_PROJECT + SOURCE_TEST_DIR + File.separator + "tests"),
                    new File(project.absolutePath + TARGET_TEST_DIR + File.separator + "tests"))
            copyDir(new File(TEST_PROJECT + SOURCE_TEST_DIR + File.separator + "group" + STUDENT_GROUP),
                    new File(project.absolutePath + TARGET_TEST_DIR + File.separator + "group" + STUDENT_GROUP))
        })
        println "Bonus Tests Run"
        student.bonus = runTestProcedure(project, bonusOutFile, bonusErrFile, bonusSurefireXml, bonusSurefireTxt, {
            def target = new File(project.absolutePath + TARGET_TEST_DIR)
            removeFrom(target)
            copyDir(new File(TEST_PROJECT + SOURCE_TEST_DIR), target)
            copyDir(new File(TEST_PROJECT + SOURCE_TEST_DIR + File.separator + "bonus"),
                    new File(project.absolutePath + TARGET_TEST_DIR + File.separator + "bonus"))
        })
        println "Finalizing results"
        student.calcTotalPoints(MAX_POINTS, MAX_BONUS_POINTS)
        TestsRun totalTests = student.getTotalTestsRun()
        println "Results: Run: ${totalTests.totalRun}, Success: ${totalTests.success}, Failure: ${totalTests.failure}, Error: ${totalTests.error}, Skip: ${totalTests.skip}, Points: ${totalTests.points}"
    } catch (Exception ex) {
        ex.printStackTrace()
        errFile << "\n"
        errFile << ex.message
        student.notes += ex.message
    }
    if (outFile.text.contains('BUILD FAILURE')) {
        if (!student.notes.isEmpty()) student.notes += ';'
        if (outFile.text.contains('COMPILATION ERROR'))
            student.notes += 'Maven Compilation Error'
        else if (outFile.text.contains('T E S T S'))
            student.notes += 'Maven Tests failed'
    }
    student.testDuration = Duration.between(startOfTest, LocalDateTime.now())
    buildSummaryFile(student, project)
    println "Test took ${student.testDuration.toString()}"
    println "-----------------------------\n"
    return student
}

LocalDateTime startOfScript = LocalDateTime.now()

if (ALL) {
    println "Evaluating students projects in Group " + STUDENT_GROUP.toUpperCase()
    println "\n"
    File[] files = new File("$CWD${File.separator}skupina${STUDENT_GROUP.toUpperCase()}").listFiles()
    for (File project : files) {
        Evaluation e = evaluateStudent(project)
        results << e
    }

    def resultFile = new File(CWD + File.separator + "results-${STUDENT_GROUP}.csv")
    resultFile.text = String.join(CSV_DELIMITER, CSV_HEADER) + '\n'
    results.each {
        resultFile.append(it.toString(CSV_DELIMITER))
        resultFile.append("\n")
    }
} else {
    def projectDir = input "Enter students folder to test"
    if (!projectDir)
        throw new RuntimeException("No student's folder were entered!")
    Evaluation e = evaluateStudent(new File("$CWD${File.separator}skupina${STUDENT_GROUP.toUpperCase()}${File.separator}$projectDir"))
    println e
}

println "Tests run for all tests took ${Duration.between(startOfScript, LocalDateTime.now()).toString()}"



