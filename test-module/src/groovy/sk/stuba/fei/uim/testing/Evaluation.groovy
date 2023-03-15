package sk.stuba.fei.uim.testing

import groovy.xml.XmlUtil

import java.time.Duration

public class Evaluation {
    public String exam = ''
    public Student student = new Student()
    public String github = ''
    public TestRun required = new TestRun()
    public TestRun bonus = new TestRun()
    public Integer totalPoints = 0
    public String notes = ''
    public Duration testDuration

    File projectDir

    Evaluation(File projectDir) {
        this.projectDir = projectDir
    }

    TestRun getTotalTestRun() {
        TestRun total = new TestRun(
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
            'testRuns' {
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
        (xml.testRuns.required.tests[0] as Node).replaceNode(required.toXml())
        (xml.testRuns.bonus.tests[0] as Node).replaceNode(bonus.toXml())
        return xml
    }

    File[] aggregateTestReports() {
        File surefireXml = new File(projectDir.absolutePath + File.separator + Constants.FEEDBACK_DIR + File.separator + Maven.SUREFIRE_REPORTS_DIR + "surefire-report.xml");
        File surefireTxt = new File(projectDir.absolutePath + File.separator + Constants.FEEDBACK_DIR + File.separator + Maven.SUREFIRE_REPORTS_DIR + "surefire-report.txt");
        surefireXml.text = '<?xml version="1.0" encoding="UTF-8"?>'
        surefireTxt.text = ''
        File[] reports = new File(projectDir.absolutePath + File.separator + Maven.REPORT_DIR).listFiles()
        for (File report : reports) {
            if (report.name.endsWith(".xml")) {
                surefireXml.append(report.text.replace('<?xml version="1.0" encoding="UTF-8"?>', ''))
            } else if (report.name.endsWith(".txt")) {
                surefireTxt.append(report.text)
                def delimiter = ''
                69.times { delimiter += '-' }
                surefireTxt.append(delimiter + "\n \n")
            }
        }
        return new File[]{surefireXml, surefireTxt};
    }

    File buildSummaryFile() {
        def summaryFile = new File(projectDir.absolutePath + File.separator + Constants.FEEDBACK_DIR + File.separator + 'summary.xml')
        summaryFile.text = XmlUtil.serialize(this.toXml())
        return summaryFile;
    }

    TestRun runTestProcedure(Maven maven, boolean bonus, Closure copyFunction) {
        println "\t Copying test files"
        copyFunction()
        println "\t Starting maven tests"
        maven.runGoal("clean", "compile", "test")
        println "\t Aggregating Surefire reports into one file"
        aggregateTestReports()
        println "\t Evaluating test results"
        return new TestRun().evaluateTests(new File(projectDir.absolutePath + File.separator + Constants.FEEDBACK_DIR + File.separator + Maven.SUREFIRE_REPORTS_DIR + "surefire-report.txt"), bonus)
    }
}
